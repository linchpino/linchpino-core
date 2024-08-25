package com.linchpino.ai.service.impl;

import com.linchpino.ai.service.ProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProfileServiceImpl implements ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileServiceImpl.class);

    @Override
    public String apply(String profileUrl) {
        log.info("Url : {}", profileUrl);
        // calling the profile loader
        String response = "I am Mohammad Masoomi with Java background and doing Java coding.";
        log.info("Weather API Response: {}", response);
        return response;
    }
}
