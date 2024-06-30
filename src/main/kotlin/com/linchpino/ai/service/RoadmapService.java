package com.linchpino.ai.service;

import com.linchpino.ai.service.domain.RequestDetail;

public interface RoadmapService {
    String getRoadmap(String aIServiceProviderName, String interactionType, RequestDetail requestDetail);
}
