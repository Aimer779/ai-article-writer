package cn.nuist.aiarticlewriter.agent;

/**
 * Exception thrown by the article agent workflow.
 */
public class ArticleAgentException extends RuntimeException {

    public ArticleAgentException(String message) {
        super(message);
    }

    public ArticleAgentException(String message, Throwable cause) {
        super(message, cause);
    }
}
