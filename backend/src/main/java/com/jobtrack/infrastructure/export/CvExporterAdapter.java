package com.jobtrack.infrastructure.export;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Component;

import com.jobtrack.application.port.CvExporter;
import com.jobtrack.domain.cv.CvEducation;
import com.jobtrack.domain.cv.CvExperience;
import com.jobtrack.domain.cv.CvProject;
import com.jobtrack.domain.cv.GeneratedCv;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPTable;

@Component
public class CvExporterAdapter implements CvExporter {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MM/yyyy");

    @Override
    public byte[] export(GeneratedCv cv, Format format) {
        return switch (format) {
            case DOCX -> docx(cv);
            case PDF -> pdf(cv);
            case TXT -> text(cv).getBytes(java.nio.charset.StandardCharsets.UTF_8);
        };
    }

    private byte[] docx(GeneratedCv cv) {
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            addDocxParagraph(document, cv.fullName(), true, 20);
            addDocxParagraph(document, contactLine(cv), false, 10);
            addDocxParagraph(document, cv.title(), true, 14);
            addDocxParagraph(document, cv.headline(), true, 14);
            addDocxParagraph(document, cv.summary(), false, 10);
            addDocxHeading(document, "Compétences");
            for (Map.Entry<String, List<String>> group : skillGroups(cv).entrySet()) {
                addDocxParagraph(document, group.getKey(), true, 10);
                addDocxParagraph(document, String.join(" · ", group.getValue()), false, 10);
            }
            addDocxHeading(document, "Expériences");
            for (CvExperience experience : cv.experiences()) {
                addDocxParagraph(document, experience.jobTitle() + " — " + experience.company(), true, 11);
                addDocxParagraph(document, period(experience.startedOn(), experience.endedOn()), false, 9);
                for (String item : descriptionLines(experience.description())) {
                    addDocxParagraph(document, item, false, 10);
                }
            }
            if (!cv.projects().isEmpty()) {
                addDocxHeading(document, "Projets");
                for (CvProject project : cv.projects()) {
                    addDocxParagraph(document, project.name(), true, 11);
                    addDocxParagraph(document, project.description(), false, 10);
                    if (project.technologies() != null && !project.technologies().isBlank()) {
                        addDocxParagraph(document, "Technologies : " + project.technologies(), false, 9);
                    }
                }
            }
            if (!cv.educations().isEmpty()) {
                addDocxHeading(document, "Formation");
                for (CvEducation education : cv.educations()) {
                    addDocxParagraph(document, education.degree() + " — " + education.institution(), true, 11);
                    addDocxParagraph(document, period(education.startedOn(), education.endedOn()), false, 9, 120);
                }
            }
            document.write(out);
            return out.toByteArray();
        } catch (Exception exception) {
            throw new IllegalStateException("Impossible de générer le DOCX", exception);
        }
    }

    private byte[] pdf(GeneratedCv cv) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(com.lowagie.text.PageSize.A4, 34, 34, 24, 24);
            PdfWriter.getInstance(document, out);
            document.open();
            Font name = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            Font heading = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 9.5f);
            Font small = FontFactory.getFont(FontFactory.HELVETICA, 8.5f);

            Paragraph nameParagraph = centered(cv.fullName(), name);
            nameParagraph.setSpacingAfter(3);
            document.add(nameParagraph);
            Paragraph titleParagraph = centered(firstNonBlank(cv.title(), cv.headline()), title);
            titleParagraph.setSpacingAfter(4);
            document.add(titleParagraph);
            if (!contactLine(cv).isBlank()) {
                Paragraph contact = centered(contactLine(cv), small);
                contact.setSpacingAfter(7);
                document.add(contact);
            }
            if (cv.summary() != null && !cv.summary().isBlank()) {
                addPdfSection(document, "Profil", heading);
                document.add(compact(cv.summary(), normal));
            }
            if (!cv.experiences().isEmpty()) {
                addPdfSection(document, "Expériences professionnelles", heading);
                cv.experiences().stream()
                        .sorted(Comparator.comparing(CvExperience::startedOn,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                        .forEach(experience -> addPdfExperience(document, experience, bold, normal, small));
            }
            if (!cv.projects().isEmpty()) {
                addPdfSection(document, "Projets personnels", heading);
                for (CvProject project : cv.projects()) {
                    document.add(compact(project.name(), bold));
                    document.add(bullet(project.description(), normal));
                    if (project.technologies() != null && !project.technologies().isBlank()) {
                        document.add(bullet("Technologies : " + project.technologies(), small));
                    }
                }
            }
            if (!cv.educations().isEmpty()) {
                addPdfSection(document, "Formation", heading);
                for (CvEducation education : cv.educations()) {
                    addPdfDateRow(document, education.degree() + " — " + education.institution(),
                            period(education.startedOn(), education.endedOn()), bold, small, 5);
                }
            }
            if (!cv.skills().isEmpty()) {
                addPdfSection(document, "Compétences", heading);
                for (Map.Entry<String, List<String>> group : skillGroups(cv).entrySet()) {
                    document.add(compact(group.getKey() + " : " + String.join(" · ", group.getValue()), normal));
                }
            }
            document.close();
            return out.toByteArray();
        } catch (Exception exception) {
            throw new IllegalStateException("Impossible de générer le PDF", exception);
        }
    }

    private void addPdfExperience(Document document, CvExperience experience, Font bold, Font normal, Font small) {
        addPdfDateRow(document, experience.jobTitle() + " — " + experience.company(),
                period(experience.startedOn(), experience.endedOn()), bold, small, 4);
        if (experience.description() != null && !experience.description().isBlank()) {
            for (String item : descriptionLines(experience.description())) {
                document.add(descriptionParagraph(item, normal));
            }
        }
    }

    private void addPdfSection(Document document, String label, Font font) {
        Paragraph heading = new Paragraph(label.toUpperCase(), font);
        heading.setSpacingBefore(8);
        heading.setSpacingAfter(3);
        document.add(heading);
        PdfPTable rule = new PdfPTable(1);
        rule.setWidthPercentage(100);
        rule.setSpacingAfter(4);
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderWidthBottom(0.8f);
        cell.setPadding(0);
        rule.addCell(cell);
        document.add(rule);
    }

    private void addPdfDateRow(Document document, String label, String date, Font bold, Font small) {
        addPdfDateRow(document, label, date, bold, small, 1);
    }

    private void addPdfDateRow(Document document, String label, String date, Font bold, Font small,
                               int spacingAfter) {
        PdfPTable table = new PdfPTable(new float[] { 4.7f, 1.3f });
        table.setWidthPercentage(100);
        table.setSpacingAfter(spacingAfter);
        PdfPCell left = cell(new Phrase(label, bold), Rectangle.NO_BORDER);
        PdfPCell right = cell(new Phrase(date, small), Rectangle.NO_BORDER);
        right.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        table.addCell(left);
        table.addCell(right);
        document.add(table);
    }

    private Paragraph bullet(String text, Font font) {
        Paragraph paragraph = new Paragraph("•  " + text, font);
        paragraph.setIndentationLeft(8);
        paragraph.setFirstLineIndent(-8);
        paragraph.setSpacingAfter(2);
        return paragraph;
    }

    private Paragraph compact(String text, Font font) {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setSpacingAfter(3);
        paragraph.setLeading(12);
        return paragraph;
    }

    private Paragraph centered(String text, Font font) {
        Paragraph paragraph = compact(text, font);
        paragraph.setAlignment(Paragraph.ALIGN_CENTER);
        return paragraph;
    }

    private PdfPCell cell(Phrase phrase, int border) {
        PdfPCell cell = new PdfPCell(phrase);
        cell.setBorder(border);
        cell.setPadding(0);
        return cell;
    }

    private String firstNonBlank(String first, String fallback) {
        return first != null && !first.isBlank() ? first : fallback;
    }

    private String text(GeneratedCv cv) {
        StringBuilder builder = new StringBuilder();
        builder.append(cv.fullName()).append("\n");
        if (!contactLine(cv).isBlank()) builder.append(contactLine(cv)).append("\n");
        builder.append(cv.title()).append("\n");
        builder.append(cv.headline()).append("\n\n");
        builder.append(cv.summary()).append("\n\n");
        builder.append("COMPÉTENCES\n");
        skillGroups(cv).forEach((group, skills) ->
                builder.append(group).append(" : ").append(String.join(" · ", skills)).append("\n"));
        builder.append("\n");
        builder.append("EXPÉRIENCES\n");
        for (CvExperience experience : cv.experiences()) {
            builder.append(experience.jobTitle()).append(" — ").append(experience.company()).append("\n");
            builder.append(period(experience.startedOn(), experience.endedOn())).append("\n");
            descriptionLines(experience.description()).forEach(item -> builder.append(item).append("\n"));
            builder.append("\n");
        }

        if (!cv.projects().isEmpty()) {
            builder.append("PROJETS\n");
            for (CvProject project : cv.projects()) {
                builder.append(project.name()).append("\n").append(project.description()).append("\n");
                if (project.technologies() != null && !project.technologies().isBlank()) {
                    builder.append("Technologies : ").append(project.technologies()).append("\n");
                }
                builder.append("\n");
            }
        }
        if (!cv.educations().isEmpty()) {
            builder.append("FORMATION\n");
            for (CvEducation education : cv.educations()) {
                builder.append(education.degree()).append(" — ").append(education.institution()).append("\n");
                builder.append(period(education.startedOn(), education.endedOn())).append("\n\n");
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    private String contactLine(GeneratedCv cv) {
        return java.util.stream.Stream.of(cv.location(), cv.email(), cv.phone(), cv.linkedinUrl(), cv.portfolioUrl())
                .filter(value -> value != null && !value.isBlank())
                .collect(java.util.stream.Collectors.joining(" | "));
    }

    private List<String> descriptionLines(String description) {
        List<String> items = new java.util.ArrayList<>();
        if (description == null || description.isBlank()) {
            return items;
        }
        String normalized = description.replace('\r', '\n');
        String[] blocks = normalized.split("(?=\\s*[•●▪◦])|\\R+(?=\\s*(?:[-*]|\\d+[.)])\\s+)");
        for (String block : blocks) {
            String line = block.trim()
                    .replaceAll("\\s*\\R\\s*", " ")
                    .replaceAll("\\s{2,}", " ")
                    .trim();
            if (!line.isEmpty()) {
                items.add(line);
            }
        }
        return items;
    }

    private Paragraph descriptionParagraph(String text, Font font) {
        Paragraph paragraph = new Paragraph(text, font);
        if (text.matches("^(?:[•●▪◦*-]|\\d+[.)])\\s+.*")) {
            paragraph.setIndentationLeft(8);
            paragraph.setFirstLineIndent(-8);
        }
        paragraph.setSpacingAfter(2);
        return paragraph;
    }

    private void addDocxParagraph(XWPFDocument document, String text, boolean bold, int size) {
        addDocxParagraph(document, text, bold, size, 0);
    }

    private void addDocxParagraph(XWPFDocument document, String text, boolean bold, int size, int spacingAfter) {
        if (text == null || text.isBlank()) {
            return;
        }
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setSpacingAfter(spacingAfter);
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setBold(bold);
        run.setFontSize(size);
        run.setFontFamily("Calibri");
    }

    private Map<String, List<String>> skillGroups(GeneratedCv cv) {
        if (cv.skillGroups() != null && !cv.skillGroups().isEmpty()) {
            return cv.skillGroups();
        }
        List<String> skills = cv.skills();
        Map<String, List<String>> groups = new LinkedHashMap<>();
        groups.put("Backend", new java.util.ArrayList<>());
        groups.put("Frontend", new java.util.ArrayList<>());
        groups.put("Bases de données", new java.util.ArrayList<>());
        groups.put("Gestion de projet", new java.util.ArrayList<>());
        groups.put("Autres", new java.util.ArrayList<>());
        for (String skill : skills) {
            groups.get(skillCategory(skill)).add(skill);
        }
        groups.values().removeIf(List::isEmpty);
        return groups;
    }

    private String skillCategory(String skill) {
        String normalized = skill.toLowerCase(java.util.Locale.ROOT);
        if (containsAny(normalized, "java", "spring", "node", "python", "php", "c#", "c++",
                "kotlin", "ruby", "go", "rust", "django", "laravel", "api", "rest", "graphql")) {
            return "Backend";
        }
        if (containsAny(normalized, "javascript", "typescript", "react", "angular", "vue", "html",
                "css", "tailwind", "next.js", "nextjs", "vite", "bootstrap")) {
            return "Frontend";
        }
        if (containsAny(normalized, "sql", "postgres", "postgresql", "mysql", "mariadb", "mongodb",
                "redis", "oracle", "sqlite", "database", "base de données")) {
            return "Bases de données";
        }
        if (containsAny(normalized, "jira", "trello", "scrum", "agile", "kanban", "project",
                "projet", "management", "gestion")) {
            return "Gestion de projet";
        }
        return "Autres";
    }

    private boolean containsAny(String value, String... terms) {
        for (String term : terms) {
            if (value.contains(term)) {
                return true;
            }
        }
        return false;
    }

    private void addDocxHeading(XWPFDocument document, String text) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setSpacingBefore(200);
        XWPFRun run = paragraph.createRun();
        run.setText(text.toUpperCase());
        run.setBold(true);
        run.setFontSize(11);
        run.setFontFamily("Calibri");
    }

    private String period(LocalDate start, LocalDate end) {
        String from = start == null ? "—" : DATE.format(start);
        String to = end == null ? "Aujourd'hui" : DATE.format(end);
        return from + " – " + to;
    }
}
