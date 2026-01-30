package ru.skillbox.socialnetwork.integration.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skillbox.socialnetwork.integration.service.StorageService;

@RestController
@RequestMapping("api/v1/storage")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    @PostMapping("/storageUserImage")
    //@PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<String> storageUserImage(@RequestPart MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }
        return ResponseEntity.ok(storageService.saveUserImage(file));
    }
    @GetMapping("/deleteByLink/{url}")
    public ResponseEntity<Void> deleteByLink(@PathVariable String url) {
        storageService.deleteUserImage(url);
        return ResponseEntity.noContent().build();
    }
}
