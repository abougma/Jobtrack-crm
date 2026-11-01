package com.jobtrack.application.service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobtrack.application.port.ApplicationFilter;
import com.jobtrack.domain.application.Application;
import com.jobtrack.domain.application.ApplicationStatus;
import com.jobtrack.domain.application.ContractType;
import com.jobtrack.domain.application.WorkMode;

@Service
public class ApplicationCsvService {
    private static final String[] HEADERS = {
            "company", "position", "location", "workMode", "contractType", "salaryMin", "salaryMax",
            "source", "offerUrl", "status", "appliedAt", "notes"
    };

    private final ApplicationService applications;

    public ApplicationCsvService(ApplicationService applications) {
        this.applications = applications;
    }

    @Transactional(readOnly = true)
    public byte[] export(UUID userId, ApplicationFilter filter) {
        List<Application> owned = applications.listForExport(userId, filter);
        try (StringWriter writer = new StringWriter();
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                     .setHeader(HEADERS).setDelimiter(';').build())) {
            for (Application application : owned) {
                printer.printRecord(
                        value(application.company()), value(application.position()), value(application.location()),
                        application.workMode() == null ? "" : application.workMode().name(),
                        application.contractType() == null ? "" : application.contractType().name(),
                        application.salaryMin() == null ? "" : application.salaryMin(),
                        application.salaryMax() == null ? "" : application.salaryMax(),
                        value(application.source()), value(application.offerUrl()),
                        application.status().name(),
                        application.appliedAt() == null ? "" : application.appliedAt().toString(),
                        value(application.notes()));
            }
            printer.flush();
            return writer.toString().getBytes(StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Impossible d'exporter les candidatures", exception);
        }
    }

    @Transactional
    public ImportResult importCsv(UUID userId, InputStream input) {
        String content;
        try {
            content = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new ApiExceptions.BadRequestException("Impossible de lire le fichier CSV");
        }
        if (content.startsWith("\uFEFF")) {
            content = content.substring(1);
        }
        if (content.isBlank()) {
            throw new ApiExceptions.BadRequestException("Le fichier CSV est vide");
        }
        char delimiter = detectDelimiter(content);
        int imported = 0;
        int skipped = 0;
        List<ImportError> errors = new ArrayList<>();
        try (CSVParser parser = CSVParser.parse(content, CSVFormat.DEFAULT.builder()
                .setHeader().setSkipHeaderRecord(true).setIgnoreEmptyLines(true).setTrim(true)
                .setDelimiter(delimiter).build())) {
            for (CSVRecord record : parser) {
                int line = (int) record.getRecordNumber() + 1;
                try {
                    String company = get(record, "company");
                    String position = get(record, "position");
                    if (company == null || company.isBlank() || position == null || position.isBlank()) {
                        throw new IllegalArgumentException("Les colonnes company et position sont obligatoires");
                    }
                    ApplicationStatus status = parseEnum(ApplicationStatus.class, get(record, "status"),
                            ApplicationStatus.TO_APPLY);
                    WorkMode workMode = parseEnum(WorkMode.class, get(record, "workMode"), null);
                    ContractType contractType = parseEnum(ContractType.class, get(record, "contractType"), null);
                    Integer salaryMin = parseInteger(get(record, "salaryMin"));
                    Integer salaryMax = parseInteger(get(record, "salaryMax"));
                    LocalDate appliedAt = parseDate(get(record, "appliedAt"));
                    applications.createEntity(userId, new ApplicationService.ApplicationCommand(
                            null, company, position, get(record, "location"), workMode, contractType,
                            salaryMin, salaryMax, get(record, "source"), get(record, "offerUrl"),
                            appliedAt, get(record, "notes"), status));
                    imported++;
                } catch (RuntimeException exception) {
                    skipped++;
                    errors.add(new ImportError(line, exception.getMessage()));
                }
            }
        } catch (IOException exception) {
            throw new ApiExceptions.BadRequestException("Fichier CSV invalide : " + exception.getMessage());
        }
        return new ImportResult(imported, skipped, errors);
    }

    private char detectDelimiter(String content) {
        String firstLine = content.lines().findFirst().orElse("");
        long semicolons = firstLine.chars().filter(value -> value == ';').count();
        long commas = firstLine.chars().filter(value -> value == ',').count();
        return semicolons >= commas ? ';' : ',';
    }

    private String get(CSVRecord record, String column) {
        if (!record.isMapped(column) || !record.isSet(column)) {
            return null;
        }
        String value = record.get(column);
        return value == null || value.isBlank() ? null : unescape(value.trim());
    }

    private <T extends Enum<T>> T parseEnum(Class<T> type, String value, T fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Valeur invalide pour " + type.getSimpleName() + " : " + value);
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Nombre invalide : " + value);
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Date invalide (format attendu AAAA-MM-JJ) : " + value);
        }
    }

    /**
     * Neutralise les valeurs interprétables comme des formules par un tableur
     * (=, +, -, @, tabulation, retour chariot) en les préfixant par une apostrophe.
     * L'import retire ce préfixe pour préserver l'aller-retour.
     */
    private String value(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        char first = input.charAt(0);
        boolean dangerous = first == '=' || first == '+' || first == '-' || first == '@'
                || first == '\t' || first == '\r';
        return dangerous ? "'" + input : input;
    }

    private String unescape(String input) {
        if (input != null && input.length() > 1 && input.charAt(0) == '\'') {
            char next = input.charAt(1);
            if (next == '=' || next == '+' || next == '-' || next == '@' || next == '\t' || next == '\r') {
                return input.substring(1);
            }
        }
        return input;
    }

    public record ImportResult(int imported, int skipped, List<ImportError> errors) {}
    public record ImportError(int line, String message) {}
}
