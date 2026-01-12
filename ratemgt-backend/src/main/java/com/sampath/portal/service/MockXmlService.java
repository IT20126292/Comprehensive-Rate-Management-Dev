package com.sampath.portal.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.nio.file.Files;

@Service
public class MockXmlService {

    public String loadMockXml(String fileName) throws Exception {
        ClassPathResource resource = new ClassPathResource("mock/" + fileName);
        return Files.readString(resource.getFile().toPath());
    }
}