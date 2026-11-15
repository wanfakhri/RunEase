package my.uitm.runease.service;

import java.util.Locale;

public final class Slug {

    private Slug() {
    }

    public static String of(String value) {
        if (value == null) {
            return "event";
        }
        String slug = value.toLowerCase(Locale.ENGLISH)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return slug.isBlank() ? "event" : slug;
    }

    public static String unique(String value, int suffix) {
        return of(value) + "-" + suffix;
    }
}
