package cn.nuist.aiarticlewriter.agent.support;

import cn.nuist.aiarticlewriter.model.state.article.ImageResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleContentAssemblerTest {

    private final ArticleContentAssembler assembler = new ArticleContentAssembler();

    @Test
    void shouldReplaceImagePlaceholdersWithMarkdownImages() {
        String content = """
                {{IMAGE_PLACEHOLDER_1}}

                ## Planning
                Useful content.
                {{IMAGE_PLACEHOLDER_2}}
                """;
        List<ImageResult> images = List.of(
                ImageResult.builder()
                        .position(1)
                        .url("https://example.com/cover.jpg")
                        .description("cover")
                        .placeholderId("{{IMAGE_PLACEHOLDER_1}}")
                        .build(),
                ImageResult.builder()
                        .position(2)
                        .url("https://example.com/section.jpg")
                        .description("section")
                        .placeholderId("{{IMAGE_PLACEHOLDER_2}}")
                        .build());

        String result = assembler.assemble(content, images);

        assertThat(result).contains("![cover](https://example.com/cover.jpg)");
        assertThat(result).contains("![section](https://example.com/section.jpg)");
        assertThat(result).doesNotContain("{{IMAGE_PLACEHOLDER_1}}");
        assertThat(result).doesNotContain("{{IMAGE_PLACEHOLDER_2}}");
    }

    @Test
    void shouldFallbackToHeadingInsertionWhenPlaceholdersAreMissing() {
        String content = """
                ## Planning
                Useful content.
                """;
        List<ImageResult> images = List.of(ImageResult.builder()
                .position(2)
                .url("https://example.com/section.jpg")
                .description("section")
                .sectionTitle("Planning")
                .build());

        String result = assembler.assemble(content, images);

        assertThat(result).contains("![section](https://example.com/section.jpg)");
    }
}
