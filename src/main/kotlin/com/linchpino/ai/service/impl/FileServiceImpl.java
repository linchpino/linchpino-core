package com.linchpino.ai.service.impl;

import com.linchpino.ai.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class FileServiceImpl implements FileService {

    @Value("${file.upload-dir}")
    private String fileUploadDir;

    @Override
    public File saveFile(MultipartFile file) {
        File convFile = new File(fileUploadDir + File.separator + file.getOriginalFilename());
        try {
            file.transferTo(convFile);
        } catch (IOException e) {
            throw new RuntimeException("Error in saving file: " + e.getMessage(), e);
        }
        return convFile;
    }
}
