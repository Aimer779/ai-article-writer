# Image Service Reference

Use this when changing image search, image acquisition methods, or image storage.

## Interfaces

- `ImageSearchService` abstracts image lookup by keywords.
- `ImageStorageService` abstracts image storage/upload.
- `ImageGenerationAgent` should coordinate image acquisition and storage.

## Methods

- Add image acquisition methods through `ImageMethodEnum`.
- `PEXELS` is the primary search source.
- `PICSUM` is the fallback source.

## Configuration

Keep local image API keys out of git. Pexels uses:

```yaml
image-search:
  pexels:
    api-key: your-key
```
