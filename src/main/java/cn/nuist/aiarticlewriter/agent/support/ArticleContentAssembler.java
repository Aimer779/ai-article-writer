package cn.nuist.aiarticlewriter.agent.support;

import cn.nuist.aiarticlewriter.model.state.article.ImageResult;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Assembles Markdown article content with generated images.
 */
@Component
public class ArticleContentAssembler {

    private static final Pattern MARKDOWN_HEADING_PATTERN = Pattern.compile("^(#{1,6})\\s+(.+?)\\s*$");

    /**
     * Insert images by placeholders first, then fall back to matching Markdown headings.
     *
     * @param content article Markdown content
     * @param images generated image results
     * @return assembled Markdown content
     */
    public String assemble(String content, List<ImageResult> images) {
        if (content == null || content.isBlank() || images == null || images.isEmpty()) {
            return content;
        }
        String placeholderContent = replaceImagePlaceholders(content, images);
        if (!placeholderContent.equals(content)) {
            return placeholderContent.trim();
        }
        StringBuilder fullContent = new StringBuilder();
        String[] lines = content.split("\\R", -1);
        for (String line : lines) {
            fullContent.append(line).append("\n");
            Matcher matcher = MARKDOWN_HEADING_PATTERN.matcher(line);
            if (matcher.matches()) {
                insertImageAfterSection(fullContent, images, matcher.group(2).trim());
            }
        }
        return fullContent.toString().trim();
    }

    private String replaceImagePlaceholders(String content, List<ImageResult> images) {
        String result = content;
        List<ImageResult> sortedImages = images.stream()
                .filter(image -> image.getPlaceholderId() != null && !image.getPlaceholderId().isBlank())
                .sorted(Comparator.comparing(ImageResult::getPosition))
                .toList();
        for (ImageResult image : sortedImages) {
            if (result.contains(image.getPlaceholderId())) {
                result = result.replace(image.getPlaceholderId(), buildImageMarkdown(image));
            }
        }
        return result;
    }

    private void insertImageAfterSection(StringBuilder fullContent, List<ImageResult> images, String sectionTitle) {
        for (ImageResult image : images) {
            if (image.getPosition() != null && image.getPosition() > 1
                    && image.getSectionTitle() != null
                    && sectionTitle.equals(image.getSectionTitle().trim())) {
                fullContent.append("\n").append(buildImageMarkdown(image)).append("\n");
                break;
            }
        }
    }

    private String buildImageMarkdown(ImageResult image) {
        return "!["
                + (image.getDescription() == null ? "article image" : image.getDescription())
                + "]("
                + image.getUrl()
                + ")";
    }
}
