package my.uitm.runease.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import my.uitm.runease.config.Db;
import my.uitm.runease.dao.AuditDao;
import my.uitm.runease.model.AuditLog;
import my.uitm.runease.util.Page;

public class AuditService {

    private final AuditDao auditDao = new AuditDao();

    public Page<AuditLog> page(String q, String action, int page, int size) throws Exception {
        int safePage = Math.max(1, page);
        long total = Db.read(c -> auditDao.count(c, q, action));
        List<AuditLog> logs = Db.read(c -> auditDao.list(c, q, action, (safePage - 1) * size, size));
        Map<String, String> filters = new LinkedHashMap<>();
        filters.put("q", q);
        filters.put("action", action);
        return new Page<>(logs, safePage, size, total, filters, "created", "desc");
    }
}
