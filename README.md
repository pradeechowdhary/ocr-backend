# OCR API (Spring Boot + Tess4J)

A minimal OCR HTTP API using Tesseract (via Tess4J).

## Run (Docker)

```bash
docker build -t ocr-api .
docker run --rm -p 8080:8080 ocr-api
```

## Test

```bash
curl -F "file=@/path/to/sample.png" http://localhost:8080/ocr
```

If you need other languages, install packages in the Dockerfile (e.g., `tesseract-ocr-spa`) and call `?lang=spa`.
