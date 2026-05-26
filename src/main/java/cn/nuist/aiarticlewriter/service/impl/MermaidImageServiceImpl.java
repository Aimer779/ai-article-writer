package cn.nuist.aiarticlewriter.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.nuist.aiarticlewriter.config.MermaidProperties;
import cn.nuist.aiarticlewriter.model.enums.ImageMediaTypeEnum;
import cn.nuist.aiarticlewriter.model.enums.ImageMethodEnum;
import cn.nuist.aiarticlewriter.model.image.ImageAsset;
import cn.nuist.aiarticlewriter.model.image.ImageRequest;
import cn.nuist.aiarticlewriter.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Mermaid CLI image service implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MermaidImageServiceImpl implements ImageService {

    private final MermaidProperties mermaidProperties;

    @Override
    public ImageMethodEnum getMethod() {
        return ImageMethodEnum.MERMAID;
    }

    @Override
    public boolean supports(ImageRequest request) {
        return request != null && StrUtil.isNotBlank(getMermaidCode(request));
    }

    @Override
    public ImageAsset acquire(ImageRequest request) {
        String mermaidCode = normalizeMermaidCode(getMermaidCode(request));
        if (StrUtil.isBlank(mermaidCode)) {
            return null;
        }
        try {
            RenderedMermaid renderedMermaid = render(mermaidCode);
            return ImageAsset.builder()
                    .method(getMethod())
                    .mediaType(resolveMediaType(renderedMermaid.contentType()))
                    .bytes(renderedMermaid.bytes())
                    .contentType(renderedMermaid.contentType())
                    .fileName("mermaid-" + request.getPosition() + "." + normalizeOutputFormat())
                    .altText(request.getSectionTitle())
                    .description(request.getType())
                    .build();
        } catch (Exception e) {
            log.warn("Failed to render Mermaid diagram, position={}, sectionTitle={}",
                    request.getPosition(), request.getSectionTitle(), e);
            return null;
        }
    }

    private RenderedMermaid render(String mermaidCode) throws IOException, InterruptedException {
        Path inputFile = Files.createTempFile("mermaid-input-", ".mmd");
        Path outputFile = Files.createTempFile("mermaid-output-", "." + normalizeOutputFormat());
        Path logFile = Files.createTempFile("mermaid-cli-", ".log");
        try {
            Files.writeString(inputFile, mermaidCode, StandardCharsets.UTF_8);
            Files.deleteIfExists(outputFile);
            ProcessBuilder processBuilder = new ProcessBuilder(buildCommand(inputFile, outputFile));
            processBuilder.redirectErrorStream(true);
            processBuilder.redirectOutput(logFile.toFile());
            Process process = processBuilder.start();
            boolean finished = process.waitFor(mermaidProperties.getTimeout(), TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new IllegalStateException("Mermaid CLI timed out");
            }
            String cliOutput = Files.exists(logFile) ? Files.readString(logFile, StandardCharsets.UTF_8) : "";
            if (process.exitValue() != 0) {
                throw new IllegalStateException("Mermaid CLI failed: " + cliOutput);
            }
            if (!Files.exists(outputFile) || Files.size(outputFile) == 0) {
                throw new IllegalStateException("Mermaid CLI output file is empty: " + cliOutput);
            }
            byte[] bytes = Files.readAllBytes(outputFile);
            log.info("Mermaid diagram rendered successfully, size={} bytes", bytes.length);
            return new RenderedMermaid(bytes, resolveContentType());
        } finally {
            deleteQuietly(inputFile);
            deleteQuietly(outputFile);
            deleteQuietly(logFile);
        }
    }

    private List<String> buildCommand(Path inputFile, Path outputFile) {
        List<String> command = new ArrayList<>();
        command.add(resolveCliCommand());
        command.add("-i");
        command.add(inputFile.toAbsolutePath().toString());
        command.add("-o");
        command.add(outputFile.toAbsolutePath().toString());
        command.add("-b");
        command.add(mermaidProperties.getBackgroundColor());
        if (mermaidProperties.getWidth() != null && mermaidProperties.getWidth() > 0) {
            command.add("-w");
            command.add(String.valueOf(mermaidProperties.getWidth()));
        }
        log.info("Executing Mermaid CLI command: {}", String.join(" ", command));
        return command;
    }

    private String resolveCliCommand() {
        String command = mermaidProperties.getCliCommand();
        if (StrUtil.isBlank(command)) {
            command = "mmdc";
        }
        if (isWindows() && "mmdc".equals(command)) {
            return "mmdc.cmd";
        }
        return command;
    }

    private String getMermaidCode(ImageRequest request) {
        if (StrUtil.isNotBlank(request.getPrompt())) {
            return request.getPrompt();
        }
        return request.getKeywords();
    }

    private String normalizeMermaidCode(String mermaidCode) {
        if (mermaidCode == null) {
            return null;
        }
        String normalizedCode = mermaidCode.trim();
        if (normalizedCode.startsWith("```")) {
            normalizedCode = normalizedCode.replaceFirst("^```[a-zA-Z]*\\s*", "")
                    .replaceFirst("\\s*```$", "")
                    .trim();
        }
        return normalizedCode;
    }

    private ImageMediaTypeEnum resolveMediaType(String contentType) {
        return switch (contentType) {
            case "image/svg+xml" -> ImageMediaTypeEnum.SVG;
            case "application/pdf" -> ImageMediaTypeEnum.PDF;
            default -> ImageMediaTypeEnum.IMAGE_BYTES;
        };
    }

    private String resolveContentType() {
        return switch (normalizeOutputFormat()) {
            case "svg" -> "image/svg+xml";
            case "pdf" -> "application/pdf";
            default -> "image/png";
        };
    }

    private String normalizeOutputFormat() {
        String outputFormat = mermaidProperties.getOutputFormat();
        if (StrUtil.isBlank(outputFormat)) {
            return "svg";
        }
        String normalizedFormat = outputFormat.trim().toLowerCase(Locale.ROOT);
        if (!List.of("svg", "png", "pdf").contains(normalizedFormat)) {
            return "svg";
        }
        return normalizedFormat;
    }

    private boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }

    private void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.debug("Failed to delete temporary file: {}", path, e);
        }
    }

    private record RenderedMermaid(byte[] bytes, String contentType) {
    }
}
