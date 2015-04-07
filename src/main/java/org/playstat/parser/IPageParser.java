package org.playstat.parser;

import java.io.InputStream;

public interface IPageParser {
    void setDataInputStream(InputStream in) throws ParserException;
}
