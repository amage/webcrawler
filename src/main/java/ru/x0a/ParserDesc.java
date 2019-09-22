package ru.x0a;

import java.util.HashSet;
import java.util.Set;

public class ParserDesc {
    private String encoding = "UTF-8";
    private final Set<String> seedUrsl = new HashSet<>();

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public Set<String> getSeedUrsl() {
        return seedUrsl;
    }
}
