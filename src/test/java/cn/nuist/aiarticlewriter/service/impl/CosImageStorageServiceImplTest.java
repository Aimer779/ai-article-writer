package cn.nuist.aiarticlewriter.service.impl;

import cn.nuist.aiarticlewriter.service.ImageStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * COS storage integration test.
 *
 * <p>This test uploads a small SVG file to Tencent Cloud COS. It is disabled by default to avoid accidental
 * external network calls and storage writes.</p>
 *
 * <p>Run manually with:
 * mvn test -Dtest=CosImageStorageServiceImplTest -Dcos.test.enabled=true</p>
 */
@SpringBootTest(properties = "image-storage.cos.enabled=true")
@EnabledIfSystemProperty(named = "cos.test.enabled", matches = "true")
class CosImageStorageServiceImplTest {

    @Autowired
    private ImageStorageService imageStorageService;

    @Test
    void uploadSvgTextToCos() {
        String objectKey = "article-images/test/cos-storage-test.svg";
        String svgContent = """
                <svg xmlns="http://www.w3.org/2000/svg" width="320" height="120" viewBox="0 0 320 120">
                  <rect width="320" height="120" fill="#f8fafc"/>
                  <rect x="16" y="16" width="288" height="88" rx="8" fill="#2563eb"/>
                  <text x="160" y="68" text-anchor="middle" font-size="24" fill="#ffffff">COS OK</text>
                </svg>
                """;

        String url = imageStorageService.uploadText(svgContent, objectKey, "image/svg+xml");

        assertNotNull(url);
        assertTrue(url.contains(objectKey));
        System.out.println("Uploaded COS test object: " + url);
    }
}
