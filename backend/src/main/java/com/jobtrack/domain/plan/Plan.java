package com.jobtrack.domain.plan;

import java.util.List;

public enum Plan {
    FREE(10, List.of("10 analyses CV / mois", "CRM complet", "Exports CSV")),
    PRO(100, List.of("100 analyses CV / mois", "Exports illimités", "Préparation d'entretien")),
    ORGANIZATION(50, List.of("50 analyses CV / mois", "Espace organisation", "Indicateurs agrégés"));

    private final int cvAnalysisLimit;
    private final List<String> features;

    Plan(int cvAnalysisLimit, List<String> features) {
        this.cvAnalysisLimit = cvAnalysisLimit;
        this.features = features;
    }

    public int cvAnalysisLimit() {
        return cvAnalysisLimit;
    }

    public List<String> features() {
        return features;
    }
}
