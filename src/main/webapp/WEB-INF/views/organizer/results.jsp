<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<c:set var="backTo" value="/organizer/results" />
<c:if test="${not empty filters.eventId}">
  <c:set var="backTo" value="${backTo}?eventId=${filters.eventId}" />
</c:if>

<div class="page-head">
  <div>
    <span class="eyebrow">Race day operations</span>
    <h1>Results</h1>
    <p class="page-sub">Record finish times, publish rankings and keep every runner&rsquo;s official result accurate.</p>
  </div>
  <div class="page-actions">
    <a class="btn btn-ghost" href="${ctx}/organizer/participants"><i class="bi bi-people"></i> Participants</a>
    <a class="btn btn-ghost" href="${ctx}/organizer/reports"><i class="bi bi-bar-chart"></i> Reports</a>
  </div>
</div>

<form class="filter-bar" method="get" action="${ctx}/organizer/results" role="search">
  <div class="search-field">
    <i class="bi bi-search"></i>
    <input class="form-control" type="search" name="q" value="<c:out value='${filters.q}'/>"
           placeholder="Runner name, email or bib" aria-label="Search runners">
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
    <a class="btn btn-ghost" href="${ctx}/organizer/results">Reset</a>
  </div>
</form>

<div class="card-panel">
  <div class="card-head">
    <div>
      <h2>Finish times</h2>
      <p class="sub">${pager.total} entry<c:if test="${pager.total ne 1}">s</c:if> in this view</p>
    </div>
    <span class="pill pill-dark"><i class="bi bi-trophy"></i> Rankings recalculate automatically</span>
  </div>
  <div class="table-shell">
    <table class="data-table">
      <thead>
        <tr>
          <th><a class="sort-link" href="${pager.sortUrl('name')}">Runner <i class="bi ${pager.sortIcon('name')}"></i></a></th>
          <th><a class="sort-link" href="${pager.sortUrl('event')}">Event <i class="bi ${pager.sortIcon('event')}"></i></a></th>
          <th>Category</th>
          <th class="num">Finish time</th>
          <th class="num">Pace</th>
          <th class="num">Overall</th>
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
                  <span>${registration.bibDisplay} &middot; ${registration.statusLabel}</span>
                </span>
              </div>
            </td>
            <td>
              <span class="cell-strong"><c:out value="${registration.eventTitle}"/></span>
              <div class="cell-sub">${registration.eventDateFormatted}</div>
            </td>
            <td>${registration.categoryName}<div class="cell-sub">${registration.distanceLabel}</div></td>
            <td class="num">
              <c:choose>
                <c:when test="${not empty registration.result}"><span class="code-chip">${registration.result.finishTimeFormatted}</span></c:when>
                <c:otherwise><span class="muted">&mdash;</span></c:otherwise>
              </c:choose>
            </td>
            <td class="num">${empty registration.result ? '&mdash;' : registration.result.paceFormatted}</td>
            <td class="num">${empty registration.result ? '&mdash;' : registration.result.overallPositionLabel}</td>
            <td class="actions">
              <c:choose>
                <c:when test="${registration.confirmed}">
                  <button class="btn btn-accent btn-sm" type="button" data-bs-toggle="modal" data-bs-target="#resultModal"
                          data-modal-heading="${empty registration.result ? 'Record finish time' : 'Edit finish time'}"
                          data-form-action="${ctx}/organizer/results/${registration.registrationId}"
                          data-field-duration="${empty registration.result ? '' : registration.result.finishTimeFormatted}">
                    <i class="bi bi-stopwatch"></i> ${empty registration.result ? 'Add result' : 'Edit'}
                  </button>
                  <c:if test="${not empty registration.result}">
                    <form method="post" action="${ctx}/organizer/results/${registration.registrationId}"
                          data-confirm="Remove the finish time for ${fn:escapeXml(registration.participantName)}? Rankings will be recalculated."
                          data-confirm-title="Remove result" data-confirm-ok="Remove result">
                      <input type="hidden" name="action" value="delete">
                      <input type="hidden" name="returnTo" value="${backTo}">
                      <button class="btn btn-danger-soft btn-sm" type="submit"><i class="bi bi-trash3"></i></button>
                    </form>
                  </c:if>
                </c:when>
                <c:otherwise>
                  <span class="pill pill-${registration.statusBadge}">${registration.statusLabel}</span>
                </c:otherwise>
              </c:choose>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty registrations}">
          <tr>
            <td colspan="7">
              <div class="empty-state">
                <div class="empty-icon"><i class="bi bi-stopwatch"></i></div>
                <h3>No runners match this view</h3>
                <p>Only confirmed or checked-in runners can receive a result. Adjust the filters or check participants in first.</p>
                <div class="row-flex justify-content-center">
                  <a class="btn btn-ink" href="${ctx}/organizer/participants">Open participants</a>
                  <a class="btn btn-ghost" href="${ctx}/organizer/results">Clear filters</a>
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

<div class="modal fade" id="resultModal" tabindex="-1" aria-labelledby="resultModalTitle" aria-hidden="true"
     data-modal-default-heading="Record finish time">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content">
      <form method="post" action="${ctx}/organizer/results" data-default-action="${ctx}/organizer/results" data-reset-on-close novalidate>
        <input type="hidden" name="action" value="save">
        <input type="hidden" name="returnTo" value="${backTo}">
        <div class="modal-header">
          <h2 class="modal-title" id="resultModalTitle" data-modal-title>Record finish time</h2>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <div class="mb-3">
            <label class="form-label" for="duration">Finish time <span class="req">*</span></label>
            <input class="form-control" type="text" id="duration" name="duration" placeholder="1:45:30 or 45:30" required>
            <div class="form-text">Use hh:mm:ss or mm:ss. Minimum finish time is 5 minutes.</div>
          </div>
          <div class="notice lime">
            <i class="bi bi-trophy"></i>
            <div>Overall and category positions are recalculated from every recorded finish time after you save.</div>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-ghost" data-bs-dismiss="modal">Cancel</button>
          <button type="submit" class="btn btn-accent"><i class="bi bi-stopwatch"></i> Save result</button>
        </div>
      </form>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
