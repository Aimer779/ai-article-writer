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

    /**
     * Bing image search URL.
     */
    String BING_IMAGE_SEARCH_URL = "https://cn.bing.com/images/async";

    /**
     * Meme keyword suffix appended by the service.
     */
    String EMOJI_PACK_SUFFIX = "表情包";

    /**
     * Maximum Bing image search result count.
     */
    int BING_MAX_IMAGES = 30;

    /**
     * Default SVG diagram width.
     */
    int SVG_DEFAULT_WIDTH = 1200;

    /**
     * Default SVG diagram height.
     */
    int SVG_DEFAULT_HEIGHT = 675;
}
