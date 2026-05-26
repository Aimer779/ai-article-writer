package cn.nuist.aiarticlewriter;

import dev.langchain4j.model.chat.StreamingChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AiArticleWriterApplicationTests {

    @Autowired
    private ObjectProvider<StreamingChatModel> streamingChatModelProvider;

    @Test
    void contextLoads() {
    }

    @Test
    void shouldConfigureStreamingChatModel() {
        assertThat(streamingChatModelProvider.getIfAvailable()).isNotNull();
    }

}
