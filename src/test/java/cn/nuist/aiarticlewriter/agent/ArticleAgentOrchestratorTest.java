package cn.nuist.aiarticlewriter.agent;

import cn.nuist.aiarticlewriter.agent.agents.ContentAgent;
import cn.nuist.aiarticlewriter.agent.agents.ImageRequirementAgent;
import cn.nuist.aiarticlewriter.agent.agents.OutlineAgent;
import cn.nuist.aiarticlewriter.agent.agents.TitleAgent;
import cn.nuist.aiarticlewriter.model.enums.ArticleStatusEnum;
import cn.nuist.aiarticlewriter.model.enums.ArticleStepEnum;
import cn.nuist.aiarticlewriter.model.state.article.ArticleState;
import cn.nuist.aiarticlewriter.model.state.article.ImageRequirement;
import cn.nuist.aiarticlewriter.model.state.article.OutlineResult;
import cn.nuist.aiarticlewriter.model.state.article.OutlineSection;
import cn.nuist.aiarticlewriter.model.state.article.TitleResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArticleAgentOrchestratorTest {

    @Test
    void shouldRunSerialArticleAgentWorkflow() {
        ArticleAgentOrchestrator orchestrator = new ArticleAgentOrchestrator(
                new FakeTitleAgent(),
                new FakeOutlineAgent(),
                new FakeContentAgent(),
                new FakeImageRequirementAgent(),
                new ArticleAgentContextFactory(new ObjectMapper()),
                new ArticleAgentResultValidator());

        ArticleState state = orchestrator.generate("task-1", 1001L, "AI article workflow", "Use a practical tone");

        assertThat(state.getStatus()).isEqualTo(ArticleStatusEnum.COMPLETED);
        assertThat(state.getCurrentStep()).isEqualTo(ArticleStepEnum.COMPLETED);
        assertThat(state.getTitle().getMainTitle()).isEqualTo("Building Reliable AI Article Workflows");
        assertThat(state.getOutline().getSections()).hasSize(2);
        assertThat(state.getContent()).contains("## Planning the Article");
        assertThat(state.getImageRequirements()).hasSize(2);
        assertThat(state.getErrorMessage()).isNull();
    }

    @Test
    void shouldMarkStateFailedWhenAgentOutputIsInvalid() {
        ArticleAgentOrchestrator orchestrator = new ArticleAgentOrchestrator(
                new InvalidTitleAgent(),
                new FakeOutlineAgent(),
                new FakeContentAgent(),
                new FakeImageRequirementAgent(),
                new ArticleAgentContextFactory(new ObjectMapper()),
                new ArticleAgentResultValidator());
        ArticleState state = ArticleState.builder()
                .taskId("task-2")
                .userId(1001L)
                .topic("AI article workflow")
                .currentStep(ArticleStepEnum.INIT)
                .status(ArticleStatusEnum.PENDING)
                .build();

        assertThatThrownBy(() -> orchestrator.generate(state, null))
                .isInstanceOf(ArticleAgentException.class)
                .hasMessageContaining("Main title");

        assertThat(state.getStatus()).isEqualTo(ArticleStatusEnum.FAILED);
        assertThat(state.getCurrentStep()).isEqualTo(ArticleStepEnum.TITLE);
        assertThat(state.getErrorMessage()).contains("Main title");
    }

    private static class FakeTitleAgent implements TitleAgent {

        @Override
        public TitleResult generate(String topic, String userRequirement) {
            return title();
        }
    }

    private static class InvalidTitleAgent implements TitleAgent {

        @Override
        public TitleResult generate(String topic, String userRequirement) {
            return TitleResult.builder()
                    .mainTitle("")
                    .subTitle("Subtitle")
                    .build();
        }
    }

    private static class FakeOutlineAgent implements OutlineAgent {

        @Override
        public OutlineResult generate(String topic, String mainTitle, String subTitle, String userRequirement) {
            return outline();
        }
    }

    private static class FakeContentAgent implements ContentAgent {

        @Override
        public String generate(String topic, String mainTitle, String subTitle, String outlineJson,
                String userRequirement) {
            return content();
        }
    }

    private static class FakeImageRequirementAgent implements ImageRequirementAgent {

        @Override
        public List<ImageRequirement> analyze(String mainTitle, String subTitle, String outlineJson,
                String contentMarkdown) {
            return List.of(
                    ImageRequirement.builder()
                            .position(1)
                            .type("cover")
                            .sectionTitle(mainTitle)
                            .keywords("ai article writing, editorial workflow")
                            .build(),
                    ImageRequirement.builder()
                            .position(2)
                            .type("section")
                            .sectionTitle("Planning the Article")
                            .keywords("article planning, outline")
                            .build());
        }
    }

    private static TitleResult title() {
        return TitleResult.builder()
                .mainTitle("Building Reliable AI Article Workflows")
                .subTitle("A practical guide to typed agents and validation")
                .build();
    }

    private static OutlineResult outline() {
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

    private static String content() {
        return """
                ## Planning the Article
                Useful content.

                ## Validating the Output
                More useful content.
                """;
    }
}
