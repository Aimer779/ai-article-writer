package cn.nuist.aiarticlewriter.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.nuist.aiarticlewriter.agent.support.ArticleLlmClient;
import cn.nuist.aiarticlewriter.config.SvgDiagramProperties;
import cn.nuist.aiarticlewriter.constant.PromptConstant;
import cn.nuist.aiarticlewriter.model.enums.ImageMediaTypeEnum;
import cn.nuist.aiarticlewriter.model.enums.ImageMethodEnum;
import cn.nuist.aiarticlewriter.model.image.ImageAsset;
import cn.nuist.aiarticlewriter.model.image.ImageRequest;
import cn.nuist.aiarticlewriter.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * SVG conceptual diagram image service implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SvgDiagramImageServiceImpl implements ImageService {

    private static final String SVG_CONTENT_TYPE = "image/svg+xml";

    private static final Pattern UNSAFE_BLOCK_PATTERN = Pattern.compile(
            "(?is)<\\s*(script|foreignObject|animate|animateTransform|animateMotion|set|iframe|object|embed|audio|video|canvas|link|style)\\b[^>]*>.*?</\\s*\\1\\s*>"
    );

    private static final Pattern UNSAFE_SELF_CLOSING_PATTERN = Pattern.compile(
            "(?is)<\\s*(script|foreignObject|animate|animateTransform|animateMotion|set|iframe|object|embed|audio|video|canvas|link|style)\\b[^>]*/\\s*>"
    );

    private static final Pattern EVENT_ATTRIBUTE_PATTERN = Pattern.compile(
            "(?i)\\s+on[a-z]+\\s*=\\s*(\"[^\"]*\"|'[^']*'|[^\\s>]+)"
    );

    private static final Pattern EXTERNAL_HREF_PATTERN = Pattern.compile(
            "(?i)\\s+(?:xlink:)?href\\s*=\\s*(\"(?!#)[^\"]*\"|'(?!#)[^']*'|(?!(?:#|\\s|>))[^\\s>]+)"
    );

    private static final Pattern EXTERNAL_URL_PATTERN = Pattern.compile(
            "(?i)url\\(\\s*['\"]?(?!#)[^)]+\\)"
    );

    private final SvgDiagramProperties svgDiagramProperties;

    private final ArticleLlmClient articleLlmClient;

    @Override
    public ImageMethodEnum getMethod() {
        return ImageMethodEnum.SVG_DIAGRAM;
    }

    @Override
    public boolean supports(ImageRequest request) {
        return request != null && StrUtil.isNotBlank(getRequirement(request));
    }

    @Override
    public ImageAsset acquire(ImageRequest request) {
        String requirement = getRequirement(request);
        if (StrUtil.isBlank(requirement)) {
            return null;
        }
        try {
            String svgCode = generateSvg(requirement);
            if (StrUtil.isBlank(svgCode) || !isValidSvg(svgCode)) {
                log.warn("Generated SVG is invalid, position={}, sectionTitle={}",
                        request.getPosition(), request.getSectionTitle());
                return null;
            }
            byte[] bytes = svgCode.getBytes(StandardCharsets.UTF_8);
            log.info("SVG diagram generated successfully, position={}, size={} bytes",
                    request.getPosition(), bytes.length);
            return ImageAsset.builder()
                    .method(getMethod())
                    .mediaType(ImageMediaTypeEnum.SVG)
                    .content(svgCode)
                    .bytes(bytes)
                    .contentType(SVG_CONTENT_TYPE)
                    .fileName("svg-diagram-" + request.getPosition() + ".svg")
                    .storageFolder(svgDiagramProperties.getFolder())
                    .altText(request.getSectionTitle())
                    .description(requirement)
                    .build();
        } catch (Exception e) {
            log.warn("Failed to generate SVG diagram, position={}, sectionTitle={}",
                    request.getPosition(), request.getSectionTitle(), e);
            return null;
        }
    }

    private String generateSvg(String requirement) {
        String prompt = articleLlmClient.renderPrompt(PromptConstant.SVG_DIAGRAM_GENERATION_PROMPT, Map.of(
                "requirement", requirement,
                "width", String.valueOf(resolveWidth()),
                "height", String.valueOf(resolveHeight())
        ));
        log.info("Calling LLM to generate SVG diagram");
        String rawOutput = articleLlmClient.callLlm(prompt);
        return sanitizeSvg(extractSvgCode(rawOutput));
    }

    private String getRequirement(ImageRequest request) {
        if (StrUtil.isNotBlank(request.getPrompt())) {
            return request.getPrompt();
        }
        return request.getKeywords();
    }

    private String extractSvgCode(String text) {
        if (text == null) {
            return null;
        }
        String cleaned = text.trim()
                .replaceFirst("^```(?:xml|svg)?\\s*", "")
                .replaceFirst("\\s*```$", "")
                .trim();
        int svgStart = cleaned.indexOf("<svg");
        int svgEnd = cleaned.lastIndexOf("</svg>");
        if (svgStart >= 0 && svgEnd >= svgStart) {
            cleaned = cleaned.substring(svgStart, svgEnd + "</svg>".length()).trim();
        }
        return cleaned;
    }

    private String sanitizeSvg(String svgCode) {
        if (StrUtil.isBlank(svgCode)) {
            return null;
        }
        String sanitized = svgCode.replaceFirst("(?is)^\\s*<\\?xml[^>]*>\\s*", "").trim();
        if (sanitized.matches("(?is).*<!DOCTYPE.*") || sanitized.matches("(?is).*<!ENTITY.*")) {
            return null;
        }
        sanitized = UNSAFE_BLOCK_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = UNSAFE_SELF_CLOSING_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = EVENT_ATTRIBUTE_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = EXTERNAL_HREF_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = EXTERNAL_URL_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = ensureSvgAttributes(sanitized);
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + sanitized;
    }

    private String ensureSvgAttributes(String svgCode) {
        if (StrUtil.isBlank(svgCode)) {
            return svgCode;
        }
        int tagEnd = svgCode.indexOf('>');
        if (tagEnd < 0) {
            return svgCode;
        }
        String openTag = svgCode.substring(0, tagEnd);
        String rest = svgCode.substring(tagEnd);
        if (!openTag.matches("(?is).*\\sxmlns\\s*=.*")) {
            openTag += " xmlns=\"http://www.w3.org/2000/svg\"";
        }
        if (!openTag.matches("(?is).*\\swidth\\s*=.*")) {
            openTag += " width=\"" + resolveWidth() + "\"";
        }
        if (!openTag.matches("(?is).*\\sheight\\s*=.*")) {
            openTag += " height=\"" + resolveHeight() + "\"";
        }
        if (!openTag.matches("(?is).*\\sviewBox\\s*=.*")) {
            openTag += " viewBox=\"0 0 " + resolveWidth() + " " + resolveHeight() + "\"";
        }
        return openTag + rest;
    }

    private boolean isValidSvg(String svgCode) {
        if (StrUtil.isBlank(svgCode)) {
            return false;
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            factory.setNamespaceAware(true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(svgCode)));
            return document.getDocumentElement() != null
                    && "svg".equalsIgnoreCase(document.getDocumentElement().getLocalName());
        } catch (Exception e) {
            log.debug("SVG XML validation failed", e);
            return false;
        }
    }

    private int resolveWidth() {
        Integer width = svgDiagramProperties.getDefaultWidth();
        return width == null || width <= 0 ? 1200 : width;
    }

    private int resolveHeight() {
        Integer height = svgDiagramProperties.getDefaultHeight();
        return height == null || height <= 0 ? 675 : height;
    }
}
