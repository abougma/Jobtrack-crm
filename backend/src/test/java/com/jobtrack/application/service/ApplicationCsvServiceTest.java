package com.jobtrack.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.jobtrack.application.port.ApplicationFilter;
import com.jobtrack.domain.application.Application;
import com.jobtrack.domain.application.ApplicationStatus;

class ApplicationCsvServiceTest {

    private final ApplicationService applications = mock(ApplicationService.class);
    private final ApplicationCsvService csv = new ApplicationCsvService(applications);
    private final UUID userId = UUID.randomUUID();

    @Test
    void exportNeutralisesSpreadsheetFormulas() {
        Application application = Application.draft(userId, null, "Acme", "Développeur", "Paris",
                null, null, 45000, 55000, "LinkedIn", "https://example.com", LocalDate.of(2026, 10, 1),
                "=SUM(A1:A2)", ApplicationStatus.APPLIED);
        when(applications.listForExport(eq(userId), any(ApplicationFilter.class)))
                .thenReturn(List.of(application));

        String output = new String(csv.export(userId, new ApplicationFilter(null, null, null, null, null, null, null)),
                StandardCharsets.UTF_8);

        assertThat(output).startsWith("company;position;");
        assertThat(output).contains("Acme;Développeur;Paris");
        assertThat(output).contains("'=SUM(A1:A2)");
        assertThat(output).doesNotContain(";=SUM(A1:A2)");
    }

    @Test
    void importRemovesFormulaProtectionAndKeepsRealValues() {
        String content = """
                company;position;status;salaryMin;appliedAt;notes
                Acme;Développeur;APPLIED;45000;2026-10-01;'=SUM(A1:A2)
                ;Poste sans entreprise;;;;
                Beta;DevOps;NOT_A_STATUS;;;
                """;

        ApplicationCsvService.ImportResult result = csv.importCsv(userId,
                new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));

        ArgumentCaptor<ApplicationService.ApplicationCommand> captor =
                ArgumentCaptor.forClass(ApplicationService.ApplicationCommand.class);
        verify(applications).createEntity(eq(userId), captor.capture());

        ApplicationService.ApplicationCommand command = captor.getValue();
        assertThat(command.company()).isEqualTo("Acme");
        assertThat(command.position()).isEqualTo("Développeur");
        assertThat(command.notes()).isEqualTo("=SUM(A1:A2)");
        assertThat(command.salaryMin()).isEqualTo(45000);
        assertThat(command.appliedAt()).isEqualTo(LocalDate.of(2026, 10, 1));
        assertThat(command.status()).isEqualTo(ApplicationStatus.APPLIED);

        assertThat(result.imported()).isEqualTo(1);
        assertThat(result.skipped()).isEqualTo(2);
        assertThat(result.errors()).hasSize(2);
        assertThat(result.errors().get(0).message()).contains("company et position");
        assertThat(result.errors().get(1).message()).contains("ApplicationStatus");
    }

    @Test
    void importAcceptsCommaSeparatedFiles() {
        String content = "company,position\nAcme,Développeur\n";

        ApplicationCsvService.ImportResult result = csv.importCsv(userId,
                new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));

        assertThat(result.imported()).isEqualTo(1);
        assertThat(result.skipped()).isZero();
    }
}
