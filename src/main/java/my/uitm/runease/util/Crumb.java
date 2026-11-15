package my.uitm.runease.util;

public class Crumb {

    private final String label;
    private final String url;

    public Crumb(String label, String url) {
        this.label = label;
        this.url = url;
    }

    public static Crumb of(String label) {
        return new Crumb(label, null);
    }

    public static Crumb link(String label, String url) {
        return new Crumb(label, url);
    }

    public String getLabel() {
        return label;
    }

    public String getUrl() {
        return url;
    }

    public boolean isLink() {
        return url != null && !url.isBlank();
    }
}
