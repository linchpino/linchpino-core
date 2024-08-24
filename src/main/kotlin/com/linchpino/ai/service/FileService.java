package com.linchpino.ai.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface FileService {

    File saveFile(MultipartFile file);
}
