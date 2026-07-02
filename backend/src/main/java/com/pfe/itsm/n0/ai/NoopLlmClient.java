package com.pfe.itsm.n0.ai;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(LlmClient.class)
public class NoopLlmClient implements LlmClient {

    @Override
    public boolean isConfigured() {
        return false;
    }

    @Override
    public List<Double> embedForDocument(String title, String content) {
        return List.of();
    }

    @Override
    public List<Double> embedForQuery(String question) {
        return List.of();
    }

    @Override
    public GeneratedAnswer generateAnswer(String prompt) {
        return new GeneratedAnswer("", true);
    }
}
