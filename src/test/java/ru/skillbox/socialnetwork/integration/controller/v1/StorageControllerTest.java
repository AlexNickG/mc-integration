package ru.skillbox.socialnetwork.integration.controller.v1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;
import ru.skillbox.socialnetwork.integration.dto.StorageDto;
import ru.skillbox.socialnetwork.integration.service.StorageService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class StorageControllerTest {

    @Mock
    private StorageService storageService;

    @InjectMocks
    private StorageController storageController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(storageController).build();
    }

    @Test
    void uploadFile_returns200_withStorageDto() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", MediaType.IMAGE_JPEG_VALUE, "image-data".getBytes());

        StorageDto dto = new StorageDto();
        dto.setFileName("https://storage.yandexcloud.net/bucket/uuid.jpg");
        when(storageService.saveUserImage(any())).thenReturn(dto);

        mockMvc.perform(multipart("/api/v1/storage/uploadUserImage").file(file))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.fileName").value("https://storage.yandexcloud.net/bucket/uuid.jpg"));

        verify(storageService).saveUserImage(any());
    }

    @Test
    void uploadFile_passesMultipartFileToService() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", MediaType.IMAGE_PNG_VALUE, "png-data".getBytes());

        StorageDto dto = new StorageDto();
        dto.setFileName("https://storage.yandexcloud.net/bucket/uuid.png");
        when(storageService.saveUserImage(any())).thenReturn(dto);

        mockMvc.perform(multipart("/api/v1/storage/uploadUserImage").file(file))
                .andExpect(status().isOk());

        ArgumentCaptor<MultipartFile> fileCaptor = ArgumentCaptor.forClass(MultipartFile.class);
        verify(storageService).saveUserImage(fileCaptor.capture());

        MultipartFile capturedFile = fileCaptor.getValue();
        assertThat(capturedFile.getName()).isEqualTo("file");
        assertThat(capturedFile.getOriginalFilename()).isEqualTo("avatar.png");
        assertThat(capturedFile.getContentType()).isEqualTo(MediaType.IMAGE_PNG_VALUE);
        assertThat(capturedFile.getBytes()).isEqualTo("png-data".getBytes());
    }

    @Test
    void uploadFile_returns400_whenFilePartMissing() throws Exception {
        mockMvc.perform(multipart("/api/v1/storage/uploadUserImage"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(storageService);
    }

    @Test
    void deleteByLink_returns204_noContent() throws Exception {
        String link = "https://storage.yandexcloud.net/bucket/uuid.jpg";
        doNothing().when(storageService).deleteUserImage(link);

        mockMvc.perform(delete("/api/v1/storage/deleteByLink")
                        .param("linkToDelete", link))
                .andExpect(status().isNoContent());

        verify(storageService).deleteUserImage(link);
    }

    @Test
    void deleteByLink_passesLinkToService() throws Exception {
        String link = "https://storage.yandexcloud.net/bucket/another-uuid.png";
        doNothing().when(storageService).deleteUserImage(link);

        mockMvc.perform(delete("/api/v1/storage/deleteByLink")
                        .param("linkToDelete", link))
                .andExpect(status().isNoContent());

        verify(storageService).deleteUserImage(link);
        verifyNoMoreInteractions(storageService);
    }

    @Test
    void deleteByLink_returns400_whenParamMissing() throws Exception {
        mockMvc.perform(delete("/api/v1/storage/deleteByLink"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(storageService);
    }
}
