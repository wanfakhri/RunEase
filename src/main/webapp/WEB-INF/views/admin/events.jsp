<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="page-head">
  <div>
    <span class="eyebrow">Administration</span>
    <h1>Event moderation</h1>
    <p class="page-sub">Review every event on the platform, approve public listings and reject submissions with clear feedback.</p>
  </div>
  <div class="page-actions">
    <a class="btn btn-ghost" href="${ctx}/admin"><i class="bi bi-grid-1x2"></i> Dashboard</a>
    <a class="btn btn-accent" href="${ctx}/admin/events?status=PENDING">
      <i class="bi bi-hourglass-split"></i> Pending (${fn:length(pendingApprovals)})
    </a>
  </div>
</div>

<form class="filter-bar" method="get" action="${ctx}/admin/events" role="search">
  <div class="search-field">
    <i class="bi bi-search"></i>
    <input class="form-control" type="search" name="q" value="<c:out value='${filters.q}'/>"
           placeholder="Search event, organizer or venue" aria-label="Search events">
  </div>
  <select class="form-select" name="status" aria-label="Event status" data-autosubmit>
    <option value="">All statuses</option>
    <c:forEach var="status" items="${statuses}">
      <option value="${status.name()}" <c:if test="${filters.status eq status.name()}">selected</c:if>>${status.label}</option>
    </c:forEach>
  </select>
  <select class="form-select" name="dir" aria-label="Sort direction" data-autosubmit>
    <option value="desc" <c:if test="${pager.direction eq 'desc'}">selected</c:if>>Newest first</option>
    <option value="asc" <c:if test="${pager.direction eq 'asc'}">selected</c:if>>Oldest first</option>
  </select>
  <div class="filter-actions">
    <button class="btn btn-ink" type="submit"><i class="bi bi-funnel"></i> Filter</button>
    <a class="btn btn-ghost" href="${ctx}/admin/events">Reset</a>
  </div>
</form>

<div class="card-panel">
  <div class="card-head">
    <div>
      <h2>All events</h2>
      <p class="sub">${pager.total} event<c:if test="${pager.total ne 1}">s</c:if> across all organizers</p>
    </div>
  </div>
  <div class="table-shell">
    <table class="data-table">
      <thead>
        <tr>
          <th><a class="sort-link" href="${pager.sortUrl('title')}">Event <i class="bi ${pager.sortIcon('title')}"></i></a></th>
          <th><a class="sort-link" href="${pager.sortUrl('organizer')}">Organizer <i class="bi ${pager.sortIcon('organizer')}"></i></a></th>
          <th>Date</th>
          <th><a class="sort-link" href="${pager.sortUrl('status')}">Status <i class="bi ${pager.sortIcon('status')}"></i></a></th>
          <th class="num"><a class="sort-link" href="${pager.sortUrl('participants')}">Runners <i class="bi ${pager.sortIcon('participants')}"></i></a></th>
          <th class="actions">Actions</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="event" items="${events}">
          <tr data-href="${ctx}/admin/events/${event.eventId}">
            <td>
              <div class="cell-main">
                <span class="avatar sm soft"><i class="bi bi-flag"></i></span>
                <span class="who">
                  <strong><a href="${ctx}/admin/events/${event.eventId}"><c:out value="${event.title}"/></a></strong>
                  <span>${event.location} &middot; ${event.distanceSummary}</span>
                </span>
              </div>
            </td>
            <td><c:out value="${event.organizerName}"/></td>
            <td>${event.shortDateFormatted}</td>
            <td>
              <span class="pill pill-${event.statusBadge}">${event.statusLabel}</span>
              <c:if test="${event.status eq 'REJECTED' and not empty event.rejectionReason}">
                <div class="cell-sub text-danger"><c:out value="${event.rejectionReason}"/></div>
              </c:if>
            </td>
            <td class="num">${event.registeredCount}<div class="cell-sub">${event.paidCount} paid</div></td>
            <td class="actions">
              <a class="btn btn-ghost btn-sm" href="${ctx}/admin/events/${event.eventId}">Review</a>
              <c:if test="${event.status eq 'APPROVED'}">
                <a class="btn btn-ghost btn-sm" href="${ctx}/events/${event.eventId}" target="_blank" rel="noopener" aria-label="Open public page"><i class="bi bi-box-arrow-up-right"></i></a>
              </c:if>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty events}">
          <tr>
            <td colspan="6">
              <div class="empty-state">
                <div class="empty-icon"><i class="bi bi-shield-check"></i></div>
                <h3>No events found</h3>
                <p>Try a different status or search term. Pending submissions appear at the top of the queue.</p>
                <a class="btn btn-ink" href="${ctx}/admin/events">Clear filters</a>
              </div>
            </td>
          </tr>
        </c:if>
      </tbody>
    </table>
  </div>
  <%@ include file="/WEB-INF/views/layout/pagination.jspf" %>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
