package com.pfe.itsm.n0.ai;

import java.util.List;

public interface LlmClient {

    boolean isConfigured();

    List<Double> embedForDocument(String title, String content);

    List<Double> embedForQuery(String question);

    GeneratedAnswer generateAnswer(String prompt);
}
