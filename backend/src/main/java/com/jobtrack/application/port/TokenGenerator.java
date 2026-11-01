package com.jobtrack.application.port;

public interface TokenGenerator {
    String generate();
    String hash(String value);
}
