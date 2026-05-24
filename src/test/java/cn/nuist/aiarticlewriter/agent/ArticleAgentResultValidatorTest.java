package cn.nuist.aiarticlewriter.agent;

import cn.nuist.aiarticlewriter.model.state.article.ImageRequirement;
import cn.nuist.aiarticlewriter.model.state.article.OutlineResult;
import cn.nuist.aiarticlewriter.model.state.article.OutlineSection;
import cn.nuist.aiarticlewriter.model.state.article.TitleResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArticleAgentResultValidatorTest {

    private final ArticleAgentResultValidator validator = new ArticleAgentResultValidator();

    @Test
    void shouldAcceptValidAgentResults() {
        TitleResult title = title();
        OutlineResult outline = outline();
        String content = content();
        List<ImageRequirement> requirements = List.of(
                ImageRequirement.builder()
                        .position(1)
                        .type("cover")
                        .sectionTitle(title.getMainTitle())
                        .keywords("ai article writing, editorial workflow")
                        .build(),
                ImageRequirement.builder()
                        .position(2)
                        .type("section")
                        .sectionTitle("Planning the Article")
                        .keywords("article planning, outline")
                        .build());

        assertThatCode(() -> {
            validator.validateTopic("AI article writing");
            validator.validateTitle(title);
            validator.validateOutline(outline);
            validator.validateContent(content, outline);
            validator.validateImageRequirements(requirements, title, outline, content);
        }).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectContentMissingOutlineHeading() {
        assertThatThrownBy(() -> validator.validateContent("## Another Heading\nBody", outline()))
                .isInstanceOf(ArticleAgentException.class)
                .hasMessageContaining("missing outline heading");
    }

    @Test
    void shouldRejectInvalidCoverImageType() {
        TitleResult title = title();
        OutlineResult outline = outline();
        String content = content();
        List<ImageRequirement> requirements = List.of(ImageRequirement.builder()
                .position(1)
                .type("section")
                .sectionTitle(title.getMainTitle())
                .keywords("cover")
                .build());

        assertThatThrownBy(() -> validator.validateImageRequirements(requirements, title, outline, content))
                .isInstanceOf(ArticleAgentException.class)
                .hasMessageContaining("position 1");
    }

    @Test
    void shouldRejectSectionTitleThatDoesNotMatchMarkdownHeading() {
        TitleResult title = title();
        OutlineResult outline = outline();
        String content = content();
        List<ImageRequirement> requirements = List.of(
                ImageRequirement.builder()
                        .position(1)
                        .type("cover")
                        .sectionTitle(title.getMainTitle())
                        .keywords("cover")
                        .build(),
                ImageRequirement.builder()
                        .position(2)
                        .type("section")
                        .sectionTitle("Missing Heading")
                        .keywords("section")
                        .build());

        assertThatThrownBy(() -> validator.validateImageRequirements(requirements, title, outline, content))
                .isInstanceOf(ArticleAgentException.class)
                .hasMessageContaining("Markdown heading");
    }

    private TitleResult title() {
        return TitleResult.builder()
                .mainTitle("Building Reliable AI Article Workflows")
                .subTitle("A practical guide to typed agents and validation")
                .build();
    }

    private OutlineResult outline() {
        return OutlineResult.builder()
                .sections(List.of(
                        OutlineSection.builder()
                                .section(1)
                                .title("Planning the Article")
                                .points(List.of("Define the audience", "Choose the structure"))
                                .build(),
                        OutlineSection.builder()
                                .section(2)
                                .title("Validating the Output")
                                .points(List.of("Check headings", "Check image requirements"))
                                .build()))
                .build();
    }

    private String content() {
        return """
                ## Planning the Article
                Useful content.

                ## Validating the Output
                More useful content.
                """;
    }
}
