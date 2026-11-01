package com.jobtrack.infrastructure.export;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
import com.lowagie.text.pdf.PdfWriter;

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
            addDocxParagraph(document, cv.location(), false, 10);
            addDocxParagraph(document, cv.title(), true, 14);
            addDocxParagraph(document, cv.headline(), true, 14);
            addDocxParagraph(document, cv.summary(), false, 10);
            addDocxHeading(document, "Compétences");
            addDocxParagraph(document, String.join(" · ", cv.skills()), false, 10);
            addDocxHeading(document, "Expériences");
            for (CvExperience experience : cv.experiences()) {
                addDocxParagraph(document, experience.jobTitle() + " — " + experience.company(), true, 11);
                addDocxParagraph(document, period(experience.startedOn(), experience.endedOn()), false, 9);
                addDocxParagraph(document, experience.description(), false, 10);
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
                    addDocxParagraph(document, period(education.startedOn(), education.endedOn()), false, 9);
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
            Document document = new Document(com.lowagie.text.PageSize.A4, 42, 42, 42, 42);
            PdfWriter.getInstance(document, out);
            document.open();
            Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font heading = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font small = FontFactory.getFont(FontFactory.HELVETICA, 9);

            document.add(new Paragraph(cv.fullName(), title));
            if (cv.location() != null && !cv.location().isBlank()) document.add(new Paragraph(cv.location(), small));
            document.add(new Paragraph(cv.title(), bold));
            document.add(new Paragraph(cv.headline(), bold));
            document.add(new Paragraph(cv.summary(), normal));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Compétences", heading));
            document.add(new Paragraph(String.join(" · ", cv.skills()), normal));
            document.add(new Paragraph("Expériences", heading));
            for (CvExperience experience : cv.experiences()) {
                document.add(new Paragraph(experience.jobTitle() + " — " + experience.company(), bold));
                document.add(new Paragraph(period(experience.startedOn(), experience.endedOn()), small));
                document.add(new Paragraph(experience.description(), normal));
            }
            if (!cv.projects().isEmpty()) {
                document.add(new Paragraph("Projets", heading));
                for (CvProject project : cv.projects()) {
                    document.add(new Paragraph(project.name(), bold));
                    document.add(new Paragraph(project.description(), normal));
                    if (project.technologies() != null && !project.technologies().isBlank()) {
                        document.add(new Paragraph("Technologies : " + project.technologies(), small));
                    }
                }
            }
            if (!cv.educations().isEmpty()) {
                document.add(new Paragraph("Formation", heading));
                for (CvEducation education : cv.educations()) {
                    document.add(new Paragraph(education.degree() + " — " + education.institution(), bold));
                    document.add(new Paragraph(period(education.startedOn(), education.endedOn()), small));
                }
            }
            document.close();
            return out.toByteArray();
        } catch (Exception exception) {
            throw new IllegalStateException("Impossible de générer le PDF", exception);
        }
    }

    private String text(GeneratedCv cv) {
        StringBuilder builder = new StringBuilder();
        builder.append(cv.fullName()).append("\n");
        if (cv.location() != null && !cv.location().isBlank()) builder.append(cv.location()).append("\n");
        builder.append(cv.title()).append("\n");
        builder.append(cv.headline()).append("\n\n");
        builder.append(cv.summary()).append("\n\n");
        builder.append("COMPÉTENCES\n").append(String.join(" · ", cv.skills())).append("\n\n");
        builder.append("EXPÉRIENCES\n");
        for (CvExperience experience : cv.experiences()) {
            builder.append(experience.jobTitle()).append(" — ").append(experience.company()).append("\n");
            builder.append(period(experience.startedOn(), experience.endedOn())).append("\n");
            builder.append(experience.description()).append("\n\n");
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
                builder.append(period(education.startedOn(), education.endedOn())).append("\n");
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    private void addDocxParagraph(XWPFDocument document, String text, boolean bold, int size) {
        if (text == null || text.isBlank()) {
            return;
        }
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setBold(bold);
        run.setFontSize(size);
        run.setFontFamily("Calibri");
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
