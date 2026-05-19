package uz.pdp.restaurantproject.service;

import jakarta.servlet.http.Part;

import java.nio.file.Paths;

/**
 * Persists the original submitted file name. The actual upload to disk happens
 * in the servlets (which need access to the {@code Part} contents and the
 * destination directory). This service exists so the rest of the code can stay
 * decoupled from the multipart machinery.
 */
public final class FileService {
    private static final FileService INSTANCE = new FileService();

    private FileService() {}

    public static FileService getInstance() {
        return INSTANCE;
    }

    /** Returns just the file name (no directory components) for persistence. */
    public String upload(Part image) {
        if (image == null || image.getSubmittedFileName() == null) {
            return null;
        }
        return Paths.get(image.getSubmittedFileName()).getFileName().toString();
    }
}
