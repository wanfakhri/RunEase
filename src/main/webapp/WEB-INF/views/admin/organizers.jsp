<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="page-head">
  <div>
    <span class="eyebrow">Administration</span>
    <h1>Organizer verification</h1>
    <p class="page-sub">Check organizer credentials before they can publish events on the platform.</p>
  </div>
  <div class="page-actions">
    <a class="btn btn-ghost" href="${ctx}/admin"><i class="bi bi-grid-1x2"></i> Dashboard</a>
    <a class="btn btn-accent" href="${ctx}/admin/organizers?status=PENDING"><i class="bi bi-patch-check"></i> Pending queue</a>
  </div>
</div>

<form class="filter-bar" method="get" action="${ctx}/admin/organizers">
  <select class="form-select" name="status" aria-label="Verification status" data-autosubmit>
    <option value="">All verification states</option>
    <c:forEach var="status" items="${statuses}">
      <option value="${status.name()}" <c:if test="${filters.status eq status.name()}">selected</c:if>>${status.label}</option>
    </c:forEach>
  </select>
  <div class="filter-actions">
    <button class="btn btn-ink" type="submit"><i class="bi bi-funnel"></i> Filter</button>
    <a class="btn btn-ghost" href="${ctx}/admin/organizers">Reset</a>
  </div>
</form>

<div class="card-panel">
  <div class="card-head">
    <div>
      <h2>Organizer directory</h2>
      <p class="sub">${fn:length(organizers)} organizer<c:if test="${fn:length(organizers) ne 1}">s</c:if> in this view</p>
    </div>
    <span class="pill pill-lime"><i class="bi bi-patch-check"></i> Admin verified</span>
  </div>
  <div class="table-shell">
    <table class="data-table">
      <thead>
        <tr>
          <th>Organization</th>
          <th>Contact</th>
          <th>Registration no.</th>
          <th class="num">Events</th>
          <th>Status</th>
          <th>Verified</th>
          <th class="actions">Decision</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="organizer" items="${organizers}">
          <tr>
            <td>
              <div class="cell-main">
                <span class="avatar sm soft">${organizer.initials}</span>
                <span class="who">
                  <strong><c:out value="${organizer.organizationName}"/></strong>
                  <span><c:out value="${organizer.userName}"/></span>
                </span>
              </div>
            </td>
            <td>
              <span class="cell-strong"><c:out value="${organizer.userEmail}"/></span>
              <div class="cell-sub"><c:out value="${organizer.userPhone}"/></div>
            </td>
            <td><span class="code-chip"><c:out value="${organizer.registrationNo}"/></span></td>
            <td class="num">${organizer.eventCount}</td>
            <td><span class="pill pill-${organizer.statusBadge}">${organizer.statusLabel}</span></td>
            <td class="small muted">${organizer.verifiedAtFormatted}</td>
            <td class="actions">
              <c:if test="${organizer.verificationStatus.name() eq 'PENDING'}">
                <form method="post" action="${ctx}/admin/organizers/${organizer.organizerId}/verify"
                      data-confirm="Verify ${fn:escapeXml(organizer.organizationName)} and allow them to publish events?"
                      data-confirm-title="Verify organizer" data-confirm-tone="primary" data-confirm-ok="Verify organizer">
                  <input type="hidden" name="status" value="VERIFIED">
                  <button class="btn btn-accent btn-sm" type="submit"><i class="bi bi-check2-circle"></i> Verify</button>
                </form>
              </c:if>
              <c:if test="${organizer.verificationStatus.name() ne 'REJECTED'}">
                <form method="post" action="${ctx}/admin/organizers/${organizer.organizerId}/verify"
                      data-confirm="Reject ${fn:escapeXml(organizer.organizationName)}? They will not be able to publish events until they re-apply."
                      data-confirm-title="Reject organizer" data-confirm-ok="Reject organizer">
                  <input type="hidden" name="status" value="REJECTED">
                  <button class="btn btn-danger-soft btn-sm" type="submit"><i class="bi bi-x-octagon"></i> Reject</button>
                </form>
              </c:if>
              <c:if test="${organizer.verificationStatus.name() eq 'VERIFIED'}">
                <span class="pill pill-success"><i class="bi bi-check2-all"></i> Verified</span>
              </c:if>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty organizers}">
          <tr>
            <td colspan="7">
              <div class="empty-state">
                <div class="empty-icon"><i class="bi bi-patch-check"></i></div>
                <h3>No organizers in this view</h3>
                <p>Organizer applications appear here as soon as someone registers an organization account.</p>
                <a class="btn btn-ink" href="${ctx}/admin/organizers">Clear filters</a>
              </div>
            </td>
          </tr>
        </c:if>
      </tbody>
    </table>
  </div>
</div>

<div class="notice lime mt-3">
  <i class="bi bi-shield-check"></i>
  <div><strong>Verification rules.</strong> Only verified organizers can create, submit or publish events. Verifying and rejecting are both written to the audit trail with your account and IP address.</div>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
