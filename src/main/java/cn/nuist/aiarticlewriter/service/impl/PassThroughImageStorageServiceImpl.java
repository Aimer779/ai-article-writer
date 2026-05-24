package cn.nuist.aiarticlewriter.service.impl;

import cn.nuist.aiarticlewriter.service.ImageStorageService;
import org.springframework.stereotype.Service;

/**
 * Temporary image storage implementation before COS integration is added.
 */
@Service
public class PassThroughImageStorageServiceImpl implements ImageStorageService {

    @Override
    public String uploadImageFromUrl(String imageUrl, String objectKey) {
        return imageUrl;
    }
}
