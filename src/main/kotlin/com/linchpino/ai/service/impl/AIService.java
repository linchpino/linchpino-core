package com.linchpino.ai.service.impl;

import com.linchpino.ai.service.domain.InteractionType;
import com.linchpino.ai.service.domain.RequestDetail;

public interface AIService {

    String talkToAI(InteractionType interactionType, RequestDetail requestDetail);
}
