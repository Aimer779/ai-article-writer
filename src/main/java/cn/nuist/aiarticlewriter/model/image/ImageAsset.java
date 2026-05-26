package cn.nuist.aiarticlewriter.model.image;

import cn.nuist.aiarticlewriter.model.enums.ImageMediaTypeEnum;
import cn.nuist.aiarticlewriter.model.enums.ImageMethodEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * Raw image asset returned by an image service before storage.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageAsset implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Image acquisition method.
     */
    private ImageMethodEnum method;

    /**
     * Asset media type.
     */
    private ImageMediaTypeEnum mediaType;

    /**
     * Remote image URL.
     */
    private String url;

    /**
     * Text, SVG, Mermaid, or base64 content.
     */
    private String content;

    /**
     * Suggested file name.
     */
    private String fileName;

    /**
     * MIME content type.
     */
    private String contentType;

    /**
     * Image alt text.
     */
    private String altText;

    /**
     * Image description.
     */
    private String description;
}
