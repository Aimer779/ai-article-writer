# Image Service Reference

Use this when changing image search, image acquisition methods, or image storage.

## Pipeline

Image generation follows this runtime path:

1. `ImageGenerationAgent` builds an `ImageRequest` from each `ImageRequirement`.
2. `ImageStrategySelector` selects an `ImageService` by preferred method or visual type.
3. The selected provider returns an `ImageAsset`.
4. `ImageStorageService` uploads the asset and returns the final article image URL.
5. Any provider, download, or storage failure falls back to Picsum.

Do not store temporary third-party URLs as final article image URLs when COS storage is enabled. Providers may return a URL, bytes, text content, or base64 content through `ImageAsset`; storage adapters are responsible for persisting the asset.

## Interfaces

- `ImageService` abstracts image acquisition by method.
- `ImageStrategySelector` routes requests to the best provider.
- `ImageStorageService` abstracts image storage/upload.
- `ImageGenerationAgent` should coordinate image acquisition and storage.

## Methods

- Add image acquisition methods through `ImageMethodEnum`.
- `PEXELS` searches real-world photos.
- `AI_GENERATION` calls DashScope Qwen-Image and returns a generated PNG URL for COS upload.
- `MERMAID` renders process or sequence diagrams through Mermaid CLI.
- `MEME` searches meme images through Bing image scraping.
- `SVG_DIAGRAM` generates conceptual SVG diagrams through the LLM.
- `PICSUM` is the final fallback source.

New providers should:

- Implement `ImageService`.
- Return a complete `ImageAsset` with method, media type, content type, and file name.
- Set `storageFolder` when the provider needs a stable COS subdirectory.
- Return `null` or throw on failure so `ImageGenerationAgent` can fall back to Picsum.

## Storage

- COS is enabled by `image-storage.cos.enabled=true`.
- `CosImageStorageServiceImpl` uploads URL, byte, text, and base64 assets to Tencent Cloud COS.
- `PassThroughImageStorageServiceImpl` is used when COS is disabled.
- Signed or pre-encoded remote image URLs must be downloaded through URI-based HTTP calls to avoid changing query signatures.
- If a custom COS domain is configured without a protocol, the storage service normalizes it to `https://`.

## Configuration

Keep local image API keys out of git.

Pexels uses:

```yaml
image-search:
  pexels:
    api-key: your-key
```

DashScope Qwen-Image uses:

```yaml
ai-image:
  api-key: your-key
  base-url: https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation
  model: qwen-image-2.0
  size: 1664*928
  prompt-extend: true
  watermark: false
```

COS uses:

```yaml
image-storage:
  cos:
    enabled: true
    secret-id: your-secret-id
    secret-key: your-secret-key
    region: ap-shanghai
    bucket-name: your-bucket
    domain: optional-custom-domain
```
