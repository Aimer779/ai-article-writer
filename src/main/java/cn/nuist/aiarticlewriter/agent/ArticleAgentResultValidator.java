package cn.nuist.aiarticlewriter.agent;

import cn.nuist.aiarticlewriter.model.state.article.ImageRequirement;
import cn.nuist.aiarticlewriter.model.state.article.OutlineResult;
import cn.nuist.aiarticlewriter.model.state.article.OutlineSection;
import cn.nuist.aiarticlewriter.model.state.article.TitleResult;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates structured outputs produced by article agents.
 */
@Component
public class ArticleAgentResultValidator {

    private static final int MAX_TOPIC_LENGTH = 500;

    private static final Pattern MARKDOWN_HEADING_PATTERN = Pattern.compile("^#{1,6}\\s+(.+?)\\s*$");

    /**
     * Validate initial article workflow input.
     *
     * @param topic article topic
     */
    public void validateTopic(String topic) {
        if (topic == null || topic.isBlank()) {
            throw new ArticleAgentException("Article topic cannot be blank");
        }
        if (topic.trim().length() > MAX_TOPIC_LENGTH) {
            throw new ArticleAgentException("Article topic is too long");
        }
    }

    /**
     * Validate title generation result.
     *
     * @param title title result
     */
    public void validateTitle(TitleResult title) {
        if (title == null) {
            throw new ArticleAgentException("Title result cannot be null");
        }
        if (isBlank(title.getMainTitle())) {
            throw new ArticleAgentException("Main title cannot be blank");
        }
        if (isBlank(title.getSubTitle())) {
            throw new ArticleAgentException("Subtitle cannot be blank");
        }
    }

    /**
     * Validate outline generation result.
     *
     * @param outline outline result
     */
    public void validateOutline(OutlineResult outline) {
        if (outline == null || outline.getSections() == null || outline.getSections().isEmpty()) {
            throw new ArticleAgentException("Outline sections cannot be empty");
        }
        Set<String> titles = new HashSet<>();
        int expectedSection = 1;
        for (OutlineSection section : outline.getSections()) {
            if (section == null) {
                throw new ArticleAgentException("Outline section cannot be null");
            }
            if (section.getSection() == null || section.getSection() != expectedSection) {
                throw new ArticleAgentException("Outline section numbers must start from 1 and increase by 1");
            }
            if (isBlank(section.getTitle())) {
                throw new ArticleAgentException("Outline section title cannot be blank");
            }
            if (!titles.add(section.getTitle().trim())) {
                throw new ArticleAgentException("Outline section title cannot be duplicated");
            }
            if (section.getPoints() == null || section.getPoints().isEmpty()) {
                throw new ArticleAgentException("Outline section points cannot be empty");
            }
            expectedSection++;
        }
    }

    /**
     * Validate Markdown content and ensure all outline headings are present.
     *
     * @param markdownContent article body
     * @param outline outline result
     */
    public void validateContent(String markdownContent, OutlineResult outline) {
        if (isBlank(markdownContent)) {
            throw new ArticleAgentException("Article content cannot be blank");
        }
        Set<String> markdownHeadings = extractMarkdownHeadings(markdownContent);
        for (OutlineSection section : outline.getSections()) {
            if (!markdownHeadings.contains(section.getTitle().trim())) {
                throw new ArticleAgentException("Article content is missing outline heading: " + section.getTitle());
            }
        }
    }

    /**
     * Validate image requirements against title, outline, and generated content.
     *
     * @param requirements image requirements
     * @param title title result
     * @param outline outline result
     * @param markdownContent article body
     */
    public void validateImageRequirements(List<ImageRequirement> requirements, TitleResult title, OutlineResult outline,
            String markdownContent) {
        if (requirements == null || requirements.isEmpty()) {
            throw new ArticleAgentException("Image requirements cannot be empty");
        }
        Set<String> markdownHeadings = extractMarkdownHeadings(markdownContent);
        Set<Integer> positions = new HashSet<>();
        for (int i = 0; i < requirements.size(); i++) {
            ImageRequirement requirement = requirements.get(i);
            if (requirement == null) {
                throw new ArticleAgentException("Image requirement cannot be null");
            }
            int expectedPosition = i + 1;
            if (requirement.getPosition() == null || requirement.getPosition() != expectedPosition) {
                throw new ArticleAgentException("Image requirement positions must start from 1 and increase by 1");
            }
            if (!positions.add(requirement.getPosition())) {
                throw new ArticleAgentException("Image requirement position cannot be duplicated");
            }
            if (isBlank(requirement.getKeywords())) {
                throw new ArticleAgentException("Image requirement keywords cannot be blank");
            }
            validateImageRequirementType(requirement, title, markdownHeadings);
        }
        ensureOutlineSupportsSectionImages(requirements, outline);
    }

    private void validateImageRequirementType(ImageRequirement requirement, TitleResult title, Set<String> markdownHeadings) {
        if (requirement.getPosition() == 1) {
            if (!"cover".equals(requirement.getType())) {
                throw new ArticleAgentException("Image requirement at position 1 must use type cover");
            }
            if (!title.getMainTitle().equals(requirement.getSectionTitle())) {
                throw new ArticleAgentException("Cover image sectionTitle must match the main title");
            }
            return;
        }
        if (!"section".equals(requirement.getType())) {
            throw new ArticleAgentException("Section image requirements must use type section");
        }
        if (isBlank(requirement.getSectionTitle()) || !markdownHeadings.contains(requirement.getSectionTitle().trim())) {
            throw new ArticleAgentException("Section image sectionTitle must match a Markdown heading");
        }
    }

    private void ensureOutlineSupportsSectionImages(List<ImageRequirement> requirements, OutlineResult outline) {
        Set<String> outlineTitles = new HashSet<>();
        for (OutlineSection section : outline.getSections()) {
            outlineTitles.add(section.getTitle().trim());
        }
        for (ImageRequirement requirement : requirements) {
            if (requirement.getPosition() > 1 && !outlineTitles.contains(requirement.getSectionTitle().trim())) {
                throw new ArticleAgentException("Section image sectionTitle must match an outline section");
            }
        }
    }

    private Set<String> extractMarkdownHeadings(String markdownContent) {
        Set<String> headings = new HashSet<>();
        String[] lines = markdownContent.split("\\R");
        for (String line : lines) {
            Matcher matcher = MARKDOWN_HEADING_PATTERN.matcher(line);
            if (matcher.matches()) {
                headings.add(matcher.group(1).trim());
            }
        }
        return headings;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
