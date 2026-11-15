package my.uitm.runease.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic pagination holder that also builds sort / page links while preserving
 * the active filters. Used by every listing and report screen.
 */
public class Page<T> {

    private final List<T> items;
    private final int page;
    private final int size;
    private final long total;
    private final String sort;
    private final String direction;
    private final Map<String, String> filters;

    public Page(List<T> items, int page, int size, long total,
                Map<String, String> filters, String sort, String direction) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.total = total;
        this.filters = filters == null ? new LinkedHashMap<>() : new LinkedHashMap<>(filters);
        this.sort = sort;
        this.direction = direction;
    }

    public List<T> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotal() {
        return total;
    }

    public String getSort() {
        return sort;
    }

    public String getDirection() {
        return direction;
    }

    public int getTotalPages() {
        return Math.max(1, (int) Math.ceil(total / (double) size));
    }

    public boolean isHasPrevious() {
        return page > 1;
    }

    public boolean isHasNext() {
        return page < getTotalPages();
    }

    public long getStartRecord() {
        return total == 0 ? 0 : (long) (page - 1) * size + 1;
    }

    public long getEndRecord() {
        return Math.min((long) page * size, total);
    }

    public int getPreviousPage() {
        return Math.max(1, page - 1);
    }

    public int getNextPage() {
        return Math.min(getTotalPages(), page + 1);
    }

    public List<Integer> getPageNumbers() {
        List<Integer> numbers = new ArrayList<>();
        int start = Math.max(1, page - 2);
        int end = Math.min(getTotalPages(), start + 4);
        start = Math.max(1, end - 4);
        for (int i = start; i <= end; i++) {
            numbers.add(i);
        }
        return numbers;
    }

    /**
     * EL calls these by their literal method name, so they deliberately do not
     * use the JavaBean get-prefix. Accepts a boxed page number because EL
     * passes forEach variables and getter results as Integer instances.
     */
    public String pageUrl(Integer targetPage) {
        return "?" + buildQuery(targetPage == null ? 1 : targetPage, sort, direction);
    }

    public String sortUrl(String field) {
        String nextDirection = field.equals(sort) && "asc".equals(direction) ? "desc" : "asc";
        return "?" + buildQuery(1, field, nextDirection);
    }

    public String sortIcon(String field) {
        if (!field.equals(sort)) {
            return "bi-arrow-down-up";
        }
        return "asc".equals(direction) ? "bi-sort-alpha-down" : "bi-sort-alpha-up";
    }

    public String getFilterQuery() {
        return buildQuery(1, sort, direction);
    }

    private String buildQuery(int targetPage, String sortField, String sortDirection) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isBlank()) {
                continue;
            }
            sb.append(encode(entry.getKey())).append('=').append(encode(entry.getValue())).append('&');
        }
        if (sortField != null && !sortField.isBlank()) {
            sb.append("sort=").append(encode(sortField)).append('&');
            sb.append("dir=").append(encode(sortDirection == null ? "asc" : sortDirection)).append('&');
        }
        sb.append("page=").append(Math.max(1, targetPage));
        return sb.toString();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
