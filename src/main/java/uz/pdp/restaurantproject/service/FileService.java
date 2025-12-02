package uz.pdp.restaurantproject.service;

import jakarta.servlet.http.Part;

public class FileService {
    private static FileService instance;

    public static FileService getInstance() {
        if (instance == null) {
            instance = new  FileService();
        }

        return instance;
    }

    public String upload(Part image) {
        return image.getSubmittedFileName();
    }
}
