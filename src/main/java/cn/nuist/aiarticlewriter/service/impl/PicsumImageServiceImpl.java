package cn.nuist.aiarticlewriter.service.impl;

import cn.nuist.aiarticlewriter.constant.ArticleConstant;
import cn.nuist.aiarticlewriter.model.enums.ImageMediaTypeEnum;
import cn.nuist.aiarticlewriter.model.enums.ImageMethodEnum;
import cn.nuist.aiarticlewriter.model.image.ImageAsset;
import cn.nuist.aiarticlewriter.model.image.ImageRequest;
import cn.nuist.aiarticlewriter.service.ImageService;
import org.springframework.stereotype.Service;

/**
 * Picsum fallback image service implementation.
 */
@Service
public class PicsumImageServiceImpl implements ImageService {

    @Override
    public ImageMethodEnum getMethod() {
        return ImageMethodEnum.PICSUM;
    }

    @Override
    public boolean supports(ImageRequest request) {
        return request != null;
    }

    @Override
    public ImageAsset acquire(ImageRequest request) {
        int safePosition = Math.max(request.getPosition() == null ? 1 : request.getPosition(), 1);
        String imageUrl = String.format(ArticleConstant.PICSUM_URL_TEMPLATE, safePosition);
        return ImageAsset.builder()
                .method(getMethod())
                .mediaType(ImageMediaTypeEnum.IMAGE_URL)
                .url(imageUrl)
                .contentType(ImageMediaTypeEnum.IMAGE_URL.getContentType())
                .fileName("picsum-" + safePosition + ".jpg")
                .altText(request.getSectionTitle())
                .description(request.getType())
                .build();
    }
}
