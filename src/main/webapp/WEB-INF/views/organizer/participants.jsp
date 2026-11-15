<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<c:set var="backTo" value="/organizer/participants" />
<c:if test="${not empty filters.eventId}">
  <c:set var="backTo" value="${backTo}?eventId=${filters.eventId}" />
</c:if>

<div class="page-head">
  <div>
    <span class="eyebrow">Race day operations</span>
    <h1>Participants</h1>
    <p class="page-sub">Search every entry across your races, verify payments and check runners in at the start line.</p>
  </div>
  <div class="page-actions">
    <a class="btn btn-ghost" href="${ctx}/organizer/reports"><i class="bi bi-download"></i> Export report</a>
    <a class="btn btn-accent" href="${ctx}/organizer/results"><i class="bi bi-stopwatch"></i> Record results</a>
  </div>
</div>

<form class="filter-bar" method="get" action="${ctx}/organizer/participants" role="search">
  <div class="search-field">
    <i class="bi bi-search"></i>
    <input class="form-control" type="search" name="q" value="<c:out value='${filters.q}'/>"
           placeholder="Name, email or registration code" aria-label="Search participants">
  </div>
  <select class="form-select" name="eventId" aria-label="Filter by event" data-autosubmit>
    <option value="">All my events</option>
    <c:forEach var="event" items="${organizedEvents}">
      <option value="${event.eventId}" <c:if test="${selectedEventId eq event.eventId}">selected</c:if>>${fn:escapeXml(event.title)}</option>
    </c:forEach>
  </select>
  <select class="form-select" name="status" aria-label="Registration status" data-autosubmit>
    <option value="">All statuses</option>
    <c:forEach var="status" items="${statuses}">
      <option value="${status.name()}" <c:if test="${filters.status eq status.name()}">selected</c:if>>${status.label}</option>
    </c:forEach>
  </select>
  <div class="filter-actions">
    <button class="btn btn-ink" type="submit"><i class="bi bi-funnel"></i> Filter</button>
    <a class="btn btn-ghost" href="${ctx}/organizer/participants">Reset</a>
  </div>
</form>

<div class="card-panel">
  <div class="card-head">
    <div>
      <h2>Entry list</h2>
      <p class="sub">${pager.total} registration<c:if test="${pager.total ne 1}">s</c:if> matching this view</p>
    </div>
    <span class="pill pill-lime"><i class="bi bi-people-fill"></i> ${pager.total} runners</span>
  </div>
  <div class="table-shell">
    <table class="data-table">
      <thead>
        <tr>
          <th><a class="sort-link" href="${pager.sortUrl('name')}">Participant <i class="bi ${pager.sortIcon('name')}"></i></a></th>
          <th><a class="sort-link" href="${pager.sortUrl('event')}">Event <i class="bi ${pager.sortIcon('event')}"></i></a></th>
          <th><a class="sort-link" href="${pager.sortUrl('status')}">Status <i class="bi ${pager.sortIcon('status')}"></i></a></th>
          <th><a class="sort-link" href="${pager.sortUrl('bib')}">Bib <i class="bi ${pager.sortIcon('bib')}"></i></a></th>
          <th><a class="sort-link" href="${pager.sortUrl('registered')}">Registered <i class="bi ${pager.sortIcon('registered')}"></i></a></th>
          <th class="actions">Actions</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="registration" items="${registrations}">
          <tr>
            <td>
              <div class="cell-main">
                <span class="avatar sm">${registration.participantInitials}</span>
                <span class="who">
                  <strong><c:out value="${registration.participantName}"/></strong>
                  <span><c:out value="${registration.participantEmail}"/></span>
                </span>
              </div>
            </td>
            <td>
              <span class="cell-strong"><c:out value="${registration.eventTitle}"/></span>
              <div class="cell-sub">${registration.categoryName} &middot; ${registration.distanceLabel}</div>
            </td>
            <td>
              <span class="pill pill-${registration.statusBadge}">${registration.statusLabel}</span>
              <c:if test="${not empty registration.checkedInAt}">
                <div class="cell-sub">${registration.checkedInAtFormatted}</div>
              </c:if>
            </td>
            <td><span class="code-chip">${registration.bibDisplay}</span></td>
            <td class="small muted">${registration.registeredAtFormatted}</td>
            <td class="actions">
              <c:choose>
                <c:when test="${registration.statusKey eq 'CONFIRMED'}">
                  <form method="post" action="${ctx}/organizer/participants/${registration.registrationId}/check-in"
                        data-confirm="Check in ${fn:escapeXml(registration.participantName)} for ${fn:escapeXml(registration.eventTitle)}?"
                        data-confirm-title="Confirm check-in" data-confirm-tone="primary" data-confirm-ok="Check in runner">
                    <input type="hidden" name="returnTo" value="${backTo}">
                    <button class="btn btn-accent btn-sm" type="submit"><i class="bi bi-person-check"></i> Check in</button>
                  </form>
                </c:when>
                <c:when test="${registration.statusKey eq 'CHECKED_IN'}">
                  <span class="pill pill-info"><i class="bi bi-check2-circle"></i> Checked in</span>
                </c:when>
                <c:when test="${registration.statusKey eq 'PENDING_PAYMENT'}">
                  <span class="pill pill-warning">Awaiting payment</span>
                </c:when>
                <c:otherwise>
                  <a class="btn btn-ghost btn-sm" href="${ctx}/organizer/results?q=${registration.registrationCode}">Result</a>
                </c:otherwise>
              </c:choose>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty registrations}">
          <tr>
            <td colspan="6">
              <div class="empty-state">
                <div class="empty-icon"><i class="bi bi-people"></i></div>
                <h3>No participants found</h3>
                <p>Try a different event or status. Entries appear here as soon as runners register.</p>
                <a class="btn btn-ink" href="${ctx}/organizer/participants">Clear filters</a>
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
  <i class="bi bi-shield-check"></i>
  <div><strong>Check-in rules.</strong> Only confirmed or already checked-in runners can be processed. Each check-in is written to the audit trail with your user account and IP address.</div>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
