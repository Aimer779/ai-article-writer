package cn.nuist.aiarticlewriter.agent.agents;

import cn.nuist.aiarticlewriter.model.state.article.ImageRequirement;
import cn.nuist.aiarticlewriter.model.state.article.TitleResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Slf4j
class ImageRequirementAgentTest {

    private static final String MAIN_TITLE = "Building Reliable AI Article Workflows";

    private static final Set<String> ALLOWED_SECTION_TITLES = Set.of(
            "Building Reliable AI Article Workflows",
            "Why Workflow Boundaries Matter",
            "Designing Agent Responsibilities",
            "Validating Structured Outputs",
            "Shipping the Final Article");

    @Autowired
    private ImageRequirementAgent imageRequirementAgent;

    @Value("${langchain4j.open-ai.chat-model.api-key:}")
    private String apiKey;

    @Test
    @Timeout(120)
    void shouldAnalyzeImageRequirementsWithRealLlm() {
        Assumptions.assumeTrue(apiKey != null && !apiKey.isBlank(), "AI model API key is not configured");

        TitleResult selectedTitle = TitleResult.builder()
                .mainTitle(MAIN_TITLE)
                .subTitle("A practical guide to title, outline, content, and image agents")
                .build();
        String outlineMarkdown = """
                # Building Reliable AI Article Workflows

                ## Why Workflow Boundaries Matter
                - Split user selection, generation, analysis, and assembly into clear stages.
                - Keep agent outputs typed and easy to validate.

                ## Designing Agent Responsibilities
                - Use one agent for one stable responsibility.
                - Prefer deterministic service code for image insertion and final merging.

                ## Validating Structured Outputs
                - Require JSON for title options and image requirements.
                - Reject missing positions, unsupported image types, and unknown section titles.

                ## Shipping the Final Article
                - Upload selected images before merging.
                - Insert inline images after their matching Markdown sections.
                """;
        String contentMarkdown = """
                # Building Reliable AI Article Workflows

                ## Why Workflow Boundaries Matter

                Reliable article generation starts with clear workflow boundaries. Title generation should produce
                options for the user, while outline and content generation can stream long-form Markdown back to the
                client. Separating these stages makes the product easier to reason about and easier to recover when
                one step fails.

                ## Designing Agent Responsibilities

                Each agent should own one stable responsibility. A title agent should return title choices, an outline
                agent should create the article structure, a content agent should write Markdown, and an image analysis
                agent should describe visual needs. Deterministic service code should handle final assembly.

                ## Validating Structured Outputs

                Structured outputs need strict validation. Image requirements must include a cover image at position 1,
                section images should start at position 2, and every sectionTitle should match a real Markdown heading
                so the final image insertion step can work without guessing.

                ## Shipping the Final Article

                The final step uploads images, records stable URLs, and inserts image Markdown after the matching
                section headings. This keeps the last mile predictable and keeps external image providers isolated
                behind service interfaces.
                """;

        List<ImageRequirement> requirements = imageRequirementAgent.analyzeImageRequirements(
                selectedTitle, outlineMarkdown, contentMarkdown);

        for (ImageRequirement requirement : requirements) {
            log.info("Generated image requirement: position={}, type={}, sectionTitle={}, keywords={}",
                    requirement.getPosition(), requirement.getType(), requirement.getSectionTitle(),
                    requirement.getKeywords());
        }

        assertThat(requirements).hasSizeBetween(3, 5);
        assertThat(requirements.getFirst().getPosition()).isEqualTo(1);
        assertThat(requirements.getFirst().getType()).isEqualTo("cover");
        assertThat(requirements.getFirst().getSectionTitle()).isEqualTo(MAIN_TITLE);
        assertThat(requirements)
                .allSatisfy(requirement -> {
                    assertThat(requirement.getPosition()).isNotNull();
                    assertThat(requirement.getType()).isIn("cover", "section");
                    assertThat(requirement.getSectionTitle()).isIn(ALLOWED_SECTION_TITLES);
                    assertThat(requirement.getKeywords()).isNotBlank();
                });
        for (int i = 0; i < requirements.size(); i++) {
            assertThat(requirements.get(i).getPosition()).isEqualTo(i + 1);
        }
    }
}
