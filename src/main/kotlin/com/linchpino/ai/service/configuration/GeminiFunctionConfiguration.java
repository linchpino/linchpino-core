package com.linchpino.ai.service.configuration;

import com.linchpino.ai.service.ProfileService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;


@Configuration
public class GeminiFunctionConfiguration {

    private final ProfileService profileService;

    public GeminiFunctionConfiguration(ProfileService profileService) {
        this.profileService = profileService;
    }

    @Bean
    @Description("Get the profile info by this service") // function description
    public Function<String, String> getProfileInfo() {
        return profileService;
    }
}
