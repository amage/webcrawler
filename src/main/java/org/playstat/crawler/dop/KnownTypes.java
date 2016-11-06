package org.playstat.crawler.dop;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.LocaleUtils;
import org.jsoup.nodes.Element;

public enum KnownTypes {
    STRING(String.class),
    INTEGER(Integer.class),
    FLOAT(Float.class),
    BOOLEAN(Boolean.class),
    ELEMENT(Element.class),
    LIST(List.class),
    DATE(Date.class);

    private final Class<?> clazz;

    KnownTypes(Class<?> clazz) {
        this.clazz = clazz;
    }

    public static boolean isKnown(Class<?> clazz) {
        if(clazz.getSimpleName().equals("int")) {
            return true;
        }
        if(clazz.getSimpleName().equals("float")) {
            return true;
        }
        if(clazz.getSimpleName().equals("boolean")) {
            return true;
        }
        return Arrays.stream(values()).map(t -> t.clazz).filter(t -> t.equals(clazz)).findAny().isPresent();
    }
    public static <T> T instanceForNode(final Element node, final Selector s, final Class<T> c) {
        final String attribute = s.attr();
        final String format = s.format();
        final String locale = s.locale();
        final String defValue = s.defValue();

        String value;

        try {
            if (c.equals(Element.class)) {
                return (T) node;
            }

            if (attribute != null && !attribute.isEmpty()) {
                if (attribute.equals("html")) {
                    value = node.html();
                } else if (attribute.equals("outerHtml")) {
                    value = node.outerHtml();
                } else {
                    value = node.attr(attribute);
                }
            } else {
                value = node.text();
            }

            if (!c.equals(Date.class) && format != null && !format.equals(Selector.NOVALUE)) {
                final Pattern p = Pattern.compile(format);
                final Matcher matcher = p.matcher(value);
                final boolean found = matcher.find();
                if (found) {
                    value = matcher.group(1);
                    if (value.isEmpty()) {
                        value = defValue;
                    }
                } else {
                    value = defValue;
                }
            }

            if (c.equals(String.class)) {
                return (T) value;
            }

            if (c.equals(Date.class)) {
                Locale loc = Locale.getDefault();
                if (!locale.equals(Selector.NOVALUE)) {
                    loc = LocaleUtils.toLocale(locale);
                }
                final DateFormat df = new SimpleDateFormat(format, loc);
                return (T) df.parse(value);
            }

            if (c.equals(Integer.class) || c.getSimpleName().equals("int")) {
                return (T) Integer.valueOf(value);
            }

            if (c.equals(Float.class) || c.getSimpleName().equals("float")) {
                return (T) Float.valueOf(value);
            }

            if (c.equals(Boolean.class) || c.getSimpleName().equals("boolean")) {
                return (T) Boolean.valueOf(value);
            }

        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        return (T) value;
    }
}
