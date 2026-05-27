package cn.nuist.aiarticlewriter.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an agent execution method for automatic logging and performance metrics.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AgentExecution {

    /**
     * Agent name, such as agent1_generate_titles.
     */
    String value();

    /**
     * Agent description.
     */
    String description() default "";
}
