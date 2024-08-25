package com.linchpino.ai.service.impl;

import com.linchpino.ai.service.FileService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Service
public class FileServiceImpl implements FileService {

    @Value("${file.upload-dir}")
    private String fileUploadDir;

    @Override
    public File saveFile(MultipartFile file) {
        if (file == null) {
            throw new IllegalArgumentException("File is null");
        }
        File localTempFile;
        try {
            String originalFilename = FilenameUtils.getName(file.getOriginalFilename());
            InputStream initialStream = file.getInputStream();
            byte[] buffer = new byte[initialStream.available()];
            int readBytes = initialStream.read(buffer);
            if (readBytes == -1) {
                throw new IllegalArgumentException("Error in reading file");
            }
            localTempFile = new File(fileUploadDir + File.separator + originalFilename);
            try (OutputStream outStream = new FileOutputStream(localTempFile)) {
                outStream.write(buffer);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error in saving file: " + e.getMessage(), e);
        }
        return localTempFile;
    }
}
