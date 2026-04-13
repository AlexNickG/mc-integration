package ru.skillbox.socialnetwork.integration.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skillbox.socialnetwork.integration.dto.StorageDto;
import ru.skillbox.socialnetwork.integration.service.StorageService;

@RestController
@RequestMapping("api/v1/storage")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    @PostMapping("/storageUserImage")
    public ResponseEntity<StorageDto> storageUserImage(@RequestPart MultipartFile file) {
        return ResponseEntity.ok(storageService.saveUserImage(file));
    }

    @GetMapping("/deleteByLink")
    public ResponseEntity<Void> deleteByLink(@RequestParam("linkToDelete") String linkToDelete) {
        storageService.deleteUserImage(linkToDelete);
        return ResponseEntity.noContent().build();
    }
}
