package com.linchpino.ai.service;

import org.apache.coyote.BadRequestException;

public interface RoadmapService {
    String getRoadmap(String aIServiceProviderName) throws BadRequestException;
}
