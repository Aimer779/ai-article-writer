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

            Generate one main title and one subtitle for the article topic.

            Requirements:
            - The main title must be concise, specific, and attractive.
            - The subtitle must explain the article value and scope.
            - Avoid exaggerated, false, or clickbait wording.
            - Keep the language natural for English readers.
            - Return only valid JSON. Do not include Markdown code fences or explanations.

            Input:
            topic: {{topic}}
            userRequirement: {{userRequirement}}

            Output JSON schema:
            {
              "mainTitle": "string",
              "subTitle": "string"
            }
            """;

    /**
     * Prompt for generating a structured article outline.
     */
    public static final String OUTLINE_GENERATION_PROMPT = """
            You are a professional English article outline planning agent.

            Create a structured outline based on the topic and title.

            Requirements:
            - The outline must be logically ordered and suitable for a complete article.
            - Generate 4 to 6 sections.
            - Each section must include 3 to 5 concrete key points.
            - Section numbers must start from 1 and increase by 1.
            - Do not write full paragraphs.
            - Return only valid JSON. Do not include Markdown code fences or explanations.

            Input:
            topic: {{topic}}
            mainTitle: {{mainTitle}}
            subTitle: {{subTitle}}
            userRequirement: {{userRequirement}}

            Output JSON schema:
            {
              "sections": [
                {
                  "section": 1,
                  "title": "string",
                  "points": [
                    "string"
                  ]
                }
              ]
            }
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
            outlineJson: {{outlineJson}}
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
            - The image plan must support the article content instead of adding unrelated decoration.
            - Return only valid JSON array. Do not include Markdown code fences or explanations.

            Input:
            mainTitle: {{mainTitle}}
            subTitle: {{subTitle}}
            outlineJson: {{outlineJson}}
            contentMarkdown: {{contentMarkdown}}

            Output JSON schema:
            [
              {
                "position": 1,
                "type": "cover",
                "sectionTitle": "string",
                "keywords": "string"
              },
              {
                "position": 2,
                "type": "section",
                "sectionTitle": "string",
                "keywords": "string"
              }
            ]
            """;
}
