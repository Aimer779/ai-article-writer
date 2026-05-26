package cn.nuist.aiarticlewriter.constant;

/**
 * Prompt templates for article generation agents.
 */
public final class PromptConstant {

    private PromptConstant() {
    }

    /**
     * Prompt for generating an article title.
     */
    public static final String TITLE_GENERATION_PROMPT = """
            You are a professional English article title planning agent.

            Generate 3 to 5 candidate titles for the article topic.

            Requirements:
            - Each main title must be concise, specific, and attractive.
            - Each subtitle must explain the article value and scope.
            - Avoid exaggerated, false, or clickbait wording.
            - Keep the language natural for English readers.
            - Return only a valid JSON array. Do not include Markdown code fences or explanations.

            Input:
            topic: {{topic}}
            userRequirement: {{userRequirement}}

            Output JSON schema:
            [
              {
                "mainTitle": "string",
                "subTitle": "string"
              }
            ]
            """;

    /**
     * Prompt for generating a structured article outline.
     */
    public static final String OUTLINE_GENERATION_PROMPT = """
            You are a professional English article outline planning agent.

            Create a structured Markdown outline based on the topic and title.

            Requirements:
            - The outline must be logically ordered and suitable for a complete article.
            - Generate 4 to 6 sections.
            - Each section must include 3 to 5 concrete key points.
            - Use Markdown headings and bullet lists.
            - Do not write full paragraphs.
            - Return only Markdown content. Do not include explanations outside the outline.

            Input:
            topic: {{topic}}
            mainTitle: {{mainTitle}}
            subTitle: {{subTitle}}
            userRequirement: {{userRequirement}}

            Output format:
            ## Section Title
            - Key point
            - Key point
            """;

    /**
     * Prompt for writing the article body in Markdown.
     */
    public static final String CONTENT_CREATION_PROMPT = """
            You are a professional English article writing agent.

            Write the complete article body based on the topic, title, subtitle, and outline.

            Requirements:
            - Use fluent, clear, and professional English.
            - Follow the outline structure strictly.
            - Use Markdown headings that match the outline section titles.
            - Expand every key point into useful and concrete content.
            - Keep the tone objective and practical.
            - Do not invent unverifiable facts, statistics, citations, or case studies.
            - Do not include image placeholders.
            - Return only Markdown content. Do not include explanations outside the article.

            Input:
            topic: {{topic}}
            mainTitle: {{mainTitle}}
            subTitle: {{subTitle}}
            userRequirement: {{userRequirement}}
            outlineMarkdown: {{outlineMarkdown}}
            """;

    /**
     * Prompt for analyzing image requirements from article content.
     */
    public static final String IMAGE_REQUIREMENT_ANALYSIS_PROMPT = """
            You are a professional article visual planning agent.

            Analyze the article and produce image requirements for cover and inline illustrations.

            Requirements:
            - Generate 1 cover image requirement and 2 to 4 section image requirements.
            - position 1 must be the cover image.
            - For section images, position must start from 2 and increase by 1.
            - type must be one of: cover, section.
            - sectionTitle must exactly match the related heading text in contentMarkdown. Use the main title for the cover image.
            - keywords must be concise search or generation keywords, separated by commas.
            - visualType must describe the best visual form: photo, ai_image, flowchart, sequence, architecture_diagram, concept_diagram, meme, or svg_diagram.
            - preferredMethod must be one of: PEXELS, AI_GENERATION, MERMAID, MEME, SVG_DIAGRAM.
            - Prefer PEXELS for real-world photos, AI_GENERATION for abstract or hard-to-search scenes, MERMAID for process or sequence diagrams, SVG_DIAGRAM for conceptual diagrams, and MEME only for humorous sections.
            - prompt must be a precise generation instruction for non-Pexels methods, and can reuse keywords for Pexels.
            - aspectRatio should usually be 16:9.
            - style should be concise, such as realistic, editorial, minimal, flat, or humorous.
            - The image plan must support the article content instead of adding unrelated decoration.
            - Return only valid JSON array. Do not include Markdown code fences or explanations.

            Input:
            mainTitle: {{mainTitle}}
            subTitle: {{subTitle}}
            outlineMarkdown: {{outlineMarkdown}}
            contentMarkdown: {{contentMarkdown}}

            Output JSON schema:
            [
              {
                "position": 1,
                "type": "cover",
                "sectionTitle": "string",
                "keywords": "string",
                "prompt": "string",
                "visualType": "photo",
                "aspectRatio": "16:9",
                "style": "realistic",
                "preferredMethod": "PEXELS"
              },
              {
                "position": 2,
                "type": "section",
                "sectionTitle": "string",
                "keywords": "string",
                "prompt": "string",
                "visualType": "flowchart",
                "aspectRatio": "16:9",
                "style": "minimal",
                "preferredMethod": "MERMAID"
              }
            ]
            """;
}
