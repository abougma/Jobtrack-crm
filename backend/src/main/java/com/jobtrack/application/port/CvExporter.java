package com.jobtrack.application.port;

import com.jobtrack.domain.cv.GeneratedCv;

public interface CvExporter {
    byte[] export(GeneratedCv cv, Format format);

    enum Format {
        DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx"),
        PDF("application/pdf", "pdf"),
        TXT("text/plain; charset=utf-8", "txt");

        private final String contentType;
        private final String extension;

        Format(String contentType, String extension) {
            this.contentType = contentType;
            this.extension = extension;
        }

        public String contentType() {
            return contentType;
        }

        public String extension() {
            return extension;
        }

        public static Format from(String value) {
            return value == null ? DOCX : Format.valueOf(value.trim().toUpperCase());
        }
    }
}
