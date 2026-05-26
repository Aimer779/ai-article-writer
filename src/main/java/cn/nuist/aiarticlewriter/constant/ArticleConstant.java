package cn.nuist.aiarticlewriter.constant;

/**
 * Article generation constants.
 */
public interface ArticleConstant {

    /**
     * SSE connection timeout in milliseconds: 30 minutes.
     */
    long SSE_TIMEOUT_MS = 30 * 60 * 1000L;

    /**
     * SSE reconnect time in milliseconds: 3 seconds.
     */
    long SSE_RECONNECT_TIME_MS = 3000L;

    /**
     * Pexels API URL.
     */
    String PEXELS_API_URL = "https://api.pexels.com/v1/search";

    /**
     * Pexels image count per page.
     */
    int PEXELS_PER_PAGE = 1;

    /**
     * Pexels landscape image orientation.
     */
    String PEXELS_ORIENTATION_LANDSCAPE = "landscape";

    /**
     * Picsum random image URL template.
     */
    String PICSUM_URL_TEMPLATE = "https://picsum.photos/800/600?random=%d";
}
