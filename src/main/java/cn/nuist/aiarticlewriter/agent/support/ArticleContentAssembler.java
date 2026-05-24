package cn.nuist.aiarticlewriter.agent.support;

import cn.nuist.aiarticlewriter.model.state.article.ImageResult;
import org.springframework.stereotype.Component;

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
     * Insert section images after matching Markdown headings.
     *
     * @param content article Markdown content
     * @param images generated image results
     * @return assembled Markdown content
     */
    public String assemble(String content, List<ImageResult> images) {
        if (content == null || content.isBlank() || images == null || images.isEmpty()) {
            return content;
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

    private void insertImageAfterSection(StringBuilder fullContent, List<ImageResult> images, String sectionTitle) {
        for (ImageResult image : images) {
            if (image.getPosition() != null && image.getPosition() > 1
                    && image.getSectionTitle() != null
                    && sectionTitle.equals(image.getSectionTitle().trim())) {
                fullContent.append("\n![")
                        .append(image.getDescription() == null ? "section image" : image.getDescription())
                        .append("](")
                        .append(image.getUrl())
                        .append(")\n");
                break;
            }
        }
    }
}
