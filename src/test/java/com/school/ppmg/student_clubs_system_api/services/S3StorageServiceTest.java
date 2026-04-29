package com.school.ppmg.student_clubs_system_api.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3StorageServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private S3Utilities s3Utilities;

    @Test
    void uploadRejectsUnsupportedContentType() {
        S3StorageService service = new S3StorageService(s3Client, s3Presigner, "school-bucket", 1024, 60);
        MockMultipartFile file = new MockMultipartFile("file", "notes.txt", "text/plain", "abc".getBytes());

        assertThatThrownBy(() -> service.upload(file, "clubs/1/media"))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(400));
    }

    @Test
    void uploadStoresImageWithNormalizedPrefixAndReturnsGeneratedUrl() throws Exception {
        S3StorageService service = new S3StorageService(s3Client, s3Presigner, "school-bucket", 1024, 60);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "photo.PNG",
                "image/png; charset=utf-8",
                "png-data".getBytes()
        );

        when(s3Client.utilities()).thenReturn(s3Utilities);
        when(s3Utilities.getUrl(any(GetUrlRequest.class)))
                .thenReturn(new URL("https://school-bucket.s3.eu-north-1.amazonaws.com/uploaded.png"));

        String url = service.upload(file, "/clubs/42/main-image/");

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), org.mockito.ArgumentMatchers.<RequestBody>any());

        PutObjectRequest request = requestCaptor.getValue();
        assertThat(request.bucket()).isEqualTo("school-bucket");
        assertThat(request.contentType()).isEqualTo("image/png");
        assertThat(request.key())
                .startsWith("clubs/42/main-image/")
                .endsWith(".png");
        assertThat(url).isEqualTo("https://school-bucket.s3.eu-north-1.amazonaws.com/uploaded.png");
    }

    @Test
    void deleteByUrlExtractsKeyFromBucketUrl() {
        S3StorageService service = new S3StorageService(s3Client, s3Presigner, "school-bucket", 1024, 60);

        service.deleteByUrl("https://school-bucket.s3.eu-north-1.amazonaws.com/clubs/42/media/image.jpg");

        ArgumentCaptor<DeleteObjectRequest> requestCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(requestCaptor.capture());
        assertThat(requestCaptor.getValue().bucket()).isEqualTo("school-bucket");
        assertThat(requestCaptor.getValue().key()).isEqualTo("clubs/42/media/image.jpg");
    }
}
