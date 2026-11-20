package com.driveaway.vehicule.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {

    public static final long MAX_FILE_SIZE_BYTES = (long) (2.5 * 1024 * 1024); // 2.5 MB = 2,621,440 bytes
    private final Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();

    public FileStorageService() {
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer le dossier d'upload", e);
        }
    }

    public String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            double sizeMb = (double) file.getSize() / (1024 * 1024);
            throw new IllegalArgumentException(String.format("Le fichier '%s' (%.2f Mo) dépasse la taille maximale autorisée de 2.5 Mo",
                    file.getOriginalFilename(), sizeMb));
        }

        String contentType = file.getContentType();
        if (contentType != null && !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Seuls les fichiers de type image sont acceptés");
        }

        try {
            String originalFilename = StringUtils.cleanPath(Objects.requireNonNullElse(file.getOriginalFilename(), "image.jpg"));
            String cleanName = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
            String storedFilename = UUID.randomUUID().toString().substring(0, 8) + "_" + cleanName;

            Path targetLocation = this.uploadDir.resolve(storedFilename);
            try (InputStream is = file.getInputStream()) {
                Files.copy(is, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            return "/uploads/" + storedFilename;
        } catch (IOException e) {
            throw new RuntimeException("Échec de l'enregistrement du fichier", e);
        }
    }

    public List<String> saveFiles(List<MultipartFile> files) {
        List<String> paths = new ArrayList<>();
        if (files == null || files.isEmpty()) {
            return paths;
        }
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                paths.add(saveFile(file));
            }
        }
        return paths;
    }
}
