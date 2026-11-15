<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="page-head">
  <div>
    <span class="eyebrow">Organizer space</span>
    <h1>My events</h1>
    <p class="page-sub">Draft, submit and manage every race you publish through RunEase.</p>
  </div>
  <div class="page-actions">
    <a class="btn btn-ghost" href="${ctx}/organizer"><i class="bi bi-grid-1x2"></i> Dashboard</a>
    <a class="btn btn-accent" href="${ctx}/organizer/events/new"><i class="bi bi-plus-lg"></i> New event</a>
  </div>
</div>

<form class="filter-bar" method="get" action="${ctx}/organizer/events" role="search">
  <div class="search-field">
    <i class="bi bi-search"></i>
    <input class="form-control" type="search" name="q" value="<c:out value='${filters.q}'/>"
           placeholder="Search race name, venue or city" aria-label="Search events">
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
    <a class="btn btn-ghost" href="${ctx}/organizer/events">Reset</a>
  </div>
</form>

<div class="card-panel">
  <div class="card-head">
    <div>
      <h2>Event portfolio</h2>
      <p class="sub">${pager.total} event<c:if test="${pager.total ne 1}">s</c:if> in your workspace</p>
    </div>
    <span class="pill pill-lime"><i class="bi bi-lightning-charge-fill"></i> Live catalogue sync</span>
  </div>
  <div class="table-shell">
    <table class="data-table">
      <thead>
        <tr>
          <th><a class="sort-link" href="${pager.sortUrl('title')}">Event <i class="bi ${pager.sortIcon('title')}"></i></a></th>
          <th>Categories</th>
          <th><a class="sort-link" href="${pager.sortUrl('status')}">Status <i class="bi ${pager.sortIcon('status')}"></i></a></th>
          <th class="num"><a class="sort-link" href="${pager.sortUrl('participants')}">Runners <i class="bi ${pager.sortIcon('participants')}"></i></a></th>
          <th class="num">Revenue</th>
          <th class="actions">Actions</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="event" items="${events}">
          <tr data-href="${ctx}/organizer/events/${event.eventId}">
            <td>
              <div class="cell-main">
                <span class="avatar sm soft"><i class="bi bi-flag"></i></span>
                <span class="who">
                  <strong><a href="${ctx}/organizer/events/${event.eventId}"><c:out value="${event.title}"/></a></strong>
                  <span>${event.shortDateFormatted} &middot; <c:out value="${event.city}"/></span>
                </span>
              </div>
            </td>
            <td>
              <span class="cell-strong">${event.distanceSummary}</span>
              <div class="cell-sub">${event.registeredCount} / ${event.totalQuota} slots taken</div>
            </td>
            <td>
              <span class="pill pill-${event.statusBadge}">${event.statusLabel}</span>
              <c:if test="${event.status eq 'REJECTED' and not empty event.rejectionReason}">
                <div class="cell-sub text-danger"><c:out value="${event.rejectionReason}"/></div>
              </c:if>
            </td>
            <td class="num">
              <span class="cell-strong">${event.paidCount}</span>
              <div class="cell-sub">${event.registeredCount} registered</div>
            </td>
            <td class="num">${event.revenueFormatted}</td>
            <td class="actions">
              <a class="btn btn-ghost btn-sm" href="${ctx}/organizer/events/${event.eventId}">Manage</a>
              <c:if test="${event.status eq 'DRAFT' or event.status eq 'REJECTED'}">
                <form method="post" action="${ctx}/organizer/events/${event.eventId}/submit"
                      data-confirm="Submit &quot;${fn:escapeXml(event.title)}&quot; for administrator approval? You will not be able to edit it while it is under review."
                      data-confirm-title="Submit for approval" data-confirm-tone="primary"
                      data-confirm-ok="Submit event">
                  <button class="btn btn-accent btn-sm" type="submit"><i class="bi bi-send"></i> Submit</button>
                </form>
              </c:if>
              <c:if test="${event.registeredCount eq 0}">
                <form method="post" action="${ctx}/organizer/events/${event.eventId}/delete"
                      data-confirm="Delete &quot;${fn:escapeXml(event.title)}&quot;? This removes the event and its race categories."
                      data-confirm-title="Delete event" data-confirm-ok="Delete event">
                  <button class="btn btn-danger-soft btn-sm" type="submit"><i class="bi bi-trash3"></i></button>
                </form>
              </c:if>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty events}">
          <tr>
            <td colspan="6">
              <div class="empty-state">
                <div class="empty-icon"><i class="bi bi-flag"></i></div>
                <h3>No events match this view</h3>
                <p>Create a new race or clear the filters to see every event in your portfolio.</p>
                <div class="row-flex justify-content-center">
                  <a class="btn btn-ink" href="${ctx}/organizer/events/new">Create event</a>
                  <a class="btn btn-ghost" href="${ctx}/organizer/events">Clear filters</a>
                </div>
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
  <div><strong>Approval workflow.</strong> Draft &rarr; Pending review &rarr; Approved. An event needs at least one race category before it can be submitted. Events with existing registrations cannot be deleted, only cancelled.</div>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
