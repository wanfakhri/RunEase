<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="page-head">
  <div>
    <span class="eyebrow">Administration</span>
    <h1>Audit trail</h1>
    <p class="page-sub">Every privileged action is recorded with the actor, target record, IP address and timestamp.</p>
  </div>
  <div class="page-actions">
    <button class="btn btn-ghost" type="button" data-print><i class="bi bi-printer"></i> Print</button>
    <a class="btn btn-ghost" href="${ctx}/admin"><i class="bi bi-grid-1x2"></i> Dashboard</a>
  </div>
</div>

<form class="filter-bar" method="get" action="${ctx}/admin/audit" role="search">
  <div class="search-field">
    <i class="bi bi-search"></i>
    <input class="form-control" type="search" name="q" value="<c:out value='${q}'/>"
           placeholder="Search description, actor or entity" aria-label="Search audit log">
  </div>
  <select class="form-select" name="action" aria-label="Audit action" data-autosubmit>
    <option value="">All actions</option>
    <c:forEach var="item" items="${actions}">
      <option value="${item}" <c:if test="${action eq item}">selected</c:if>>${fn:replace(item, '_', ' ')}</option>
    </c:forEach>
  </select>
  <div class="filter-actions">
    <button class="btn btn-ink" type="submit"><i class="bi bi-funnel"></i> Filter</button>
    <a class="btn btn-ghost" href="${ctx}/admin/audit">Reset</a>
  </div>
</form>

<div class="card-panel">
  <div class="card-head">
    <div>
      <h2>Recorded activity</h2>
      <p class="sub">${pager.total} entr<c:choose><c:when test="${pager.total eq 1}">y</c:when><c:otherwise>ies</c:otherwise></c:choose> matching this view</p>
    </div>
    <span class="pill pill-lime"><i class="bi bi-shield-lock"></i> Immutable log</span>
  </div>
  <div class="table-shell">
    <table class="data-table">
      <thead>
        <tr>
          <th>When</th>
          <th>Actor</th>
          <th>Action</th>
          <th>Entity</th>
          <th>Description</th>
          <th>IP address</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="log" items="${logs}">
          <tr>
            <td class="small muted">${log.createdAtFormatted}</td>
            <td>
              <div class="cell-main">
                <span class="avatar sm soft">${log.actorInitials}</span>
                <span class="who">
                  <strong><c:out value="${log.userName}"/></strong>
                  <span>User #${empty log.userId ? 'system' : log.userId}</span>
                </span>
              </div>
            </td>
            <td><span class="pill pill-${log.actionBadge}">${log.actionLabel}</span></td>
            <td>
              <span class="code-chip"><c:out value="${log.entityType}"/></span>
              <c:if test="${not empty log.entityId}"><div class="cell-sub">#${log.entityId}</div></c:if>
            </td>
            <td><c:out value="${log.description}"/></td>
            <td class="small muted">${log.ipAddress}</td>
          </tr>
        </c:forEach>
        <c:if test="${empty logs}">
          <tr>
            <td colspan="6">
              <div class="empty-state">
                <div class="empty-icon"><i class="bi bi-clock-history"></i></div>
                <h3>No audit entries</h3>
                <p>Try a different action or search term. New activity is written here as users sign in and records change.</p>
                <a class="btn btn-ink" href="${ctx}/admin/audit">Clear filters</a>
              </div>
            </td>
          </tr>
        </c:if>
      </tbody>
    </table>
  </div>
  <%@ include file="/WEB-INF/views/layout/pagination.jspf" %>
</div>

<div class="notice lime mt-3">
  <i class="bi bi-info-circle"></i>
  <div><strong>Why this matters.</strong> The audit trail is append-only in the application layer: administrators can read and filter it but cannot edit or delete entries, which supports accountability and traceability for every role.</div>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
