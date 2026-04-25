package com.school.ppmg.student_clubs_system_api.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class S3StorageService {

    private static final Map<String, String> CONTENT_TYPE_EXTENSIONS = Map.ofEntries(
            Map.entry("image/jpeg", "jpg"),
            Map.entry("image/jpg", "jpg"),
            Map.entry("image/png", "png"),
            Map.entry("image/webp", "webp")
    );

    private static final Set<String> ALLOWED_CONTENT_TYPES = CONTENT_TYPE_EXTENSIONS.keySet();

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;
    private final long maxFileSizeBytes;
    private final Duration getUrlTtl;

    public S3StorageService(
            S3Client s3Client,
            S3Presigner s3Presigner,
            @Value("${app.s3.bucket}") String bucket,
            @Value("${app.s3.max-file-size-bytes:10485760}") long maxFileSizeBytes,
            @Value("${app.s3.get-url-ttl-minutes:60}") long getUrlTtlMinutes
    ) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucket = bucket;
        this.maxFileSizeBytes = maxFileSizeBytes;
        this.getUrlTtl = Duration.ofMinutes(Math.max(getUrlTtlMinutes, 1));
    }

    public String upload(MultipartFile file, String keyPrefix) {
        if (bucket == null || bucket.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "S3 bucket is not configured");
        }
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
        }
        if (file.getSize() > maxFileSizeBytes) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File exceeds max size");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content type is required");
        }
        contentType = contentType.toLowerCase();
        int separatorIndex = contentType.indexOf(';');
        if (separatorIndex > 0) {
            contentType = contentType.substring(0, separatorIndex).trim();
        }
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported content type: " + contentType);
        }

        String extension = normalizeExtension(file.getOriginalFilename(), contentType);
        String filename = UUID.randomUUID().toString().replace("-", "");
        if (extension != null && !extension.isBlank()) {
            filename = filename + "." + extension;
        }

        String prefix = normalizePrefix(keyPrefix);
        String key = prefix.isBlank() ? filename : prefix + "/" + filename;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        try (InputStream inputStream = file.getInputStream()) {
            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, file.getSize()));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read upload", e);
        } catch (S3Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file", e);
        }

        return s3Client.utilities()
                .getUrl(GetUrlRequest.builder().bucket(bucket).key(key).build())
                .toString();
    }

    public String resolveReadUrl(String value) {
        if (value == null || value.isBlank() || bucket == null || bucket.isBlank()) {
            return value;
        }

        String key = extractKey(value);
        if (key == null || key.isBlank()) {
            return value;
        }

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            return s3Presigner.presignGetObject(GetObjectPresignRequest.builder()
                            .signatureDuration(getUrlTtl)
                            .getObjectRequest(getObjectRequest)
                            .build())
                    .url()
                    .toString();
        } catch (RuntimeException ignored) {
            return value;
        }
    }

    public void delete(String key) {
        if (bucket == null || bucket.isBlank() || key == null || key.isBlank()) {
            return;
        }
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
    }

    public void deleteByUrl(String url) {
        if (url == null || url.isBlank() || bucket == null || bucket.isBlank()) {
            return;
        }

        String key = extractKey(url);
        if (key == null || key.isBlank()) {
            return;
        }

        delete(key);
    }

    private static String normalizePrefix(String keyPrefix) {
        if (keyPrefix == null) {
            return "";
        }
        String trimmed = keyPrefix.trim();
        while (trimmed.startsWith("/")) {
            trimmed = trimmed.substring(1);
        }
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private static String normalizeExtension(String originalFilename, String contentType) {
        String extension = StringUtils.getFilenameExtension(originalFilename);
        if (extension != null && !extension.isBlank()) {
            return extension.toLowerCase();
        }
        return CONTENT_TYPE_EXTENSIONS.get(contentType);
    }

    private String extractKey(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isBlank()) {
            return null;
        }

        if (!normalized.contains("://")) {
            return normalized;
        }

        if (bucket == null || bucket.isBlank()) {
            return null;
        }

        try {
            URI uri = URI.create(normalized);
            String host = uri.getHost();
            String path = uri.getPath();
            if (host == null || host.isBlank() || path == null || path.isBlank()) {
                return null;
            }

            String key = path.startsWith("/") ? path.substring(1) : path;
            String bucketPrefix = bucket + "/";

            if (host.equals(bucket) || host.startsWith(bucket + ".")) {
                return URLDecoder.decode(key, StandardCharsets.UTF_8);
            }

            if (key.startsWith(bucketPrefix)) {
                return URLDecoder.decode(key.substring(bucketPrefix.length()), StandardCharsets.UTF_8);
            }

            return null;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
