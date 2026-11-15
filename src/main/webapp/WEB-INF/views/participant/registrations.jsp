<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="page-head">
  <div>
    <span class="eyebrow">Runner space</span>
    <h1>My registrations</h1>
    <p class="page-sub">Every entry you have made through RunEase, including payment and bib status.</p>
  </div>
  <div class="page-actions">
    <a class="btn btn-ghost" href="${ctx}/dashboard"><i class="bi bi-grid-1x2"></i> Dashboard</a>
    <a class="btn btn-accent" href="${ctx}/events"><i class="bi bi-plus-lg"></i> New entry</a>
  </div>
</div>

<form class="filter-bar" method="get" action="${ctx}/registrations">
  <select class="form-select" name="status" aria-label="Registration status" data-autosubmit>
    <option value="">All statuses</option>
    <option value="PENDING_PAYMENT" <c:if test="${filters.status eq 'PENDING_PAYMENT'}">selected</c:if>>Pending payment</option>
    <option value="CONFIRMED" <c:if test="${filters.status eq 'CONFIRMED'}">selected</c:if>>Confirmed</option>
    <option value="CHECKED_IN" <c:if test="${filters.status eq 'CHECKED_IN'}">selected</c:if>>Checked in</option>
    <option value="FINISHED" <c:if test="${filters.status eq 'FINISHED'}">selected</c:if>>Finished</option>
    <option value="CANCELLED" <c:if test="${filters.status eq 'CANCELLED'}">selected</c:if>>Cancelled</option>
    <option value="DISQUALIFIED" <c:if test="${filters.status eq 'DISQUALIFIED'}">selected</c:if>>Disqualified</option>
  </select>
  <select class="form-select" name="sort" aria-label="Sort by" data-autosubmit>
    <option value="event" <c:if test="${pager.sort eq 'event'}">selected</c:if>>Event date</option>
    <option value="registered" <c:if test="${pager.sort eq 'registered'}">selected</c:if>>Registration date</option>
    <option value="status" <c:if test="${pager.sort eq 'status'}">selected</c:if>>Status</option>
  </select>
  <div class="filter-actions">
    <button class="btn btn-ink" type="submit"><i class="bi bi-funnel"></i> Filter</button>
    <a class="btn btn-ghost" href="${ctx}/registrations">Reset</a>
  </div>
</form>

<div class="card-panel">
  <div class="card-head">
    <div>
      <h2>Entries</h2>
      <p class="sub">${pager.total} registration(s) found</p>
    </div>
  </div>
  <div class="table-shell">
    <table class="data-table">
      <thead>
        <tr>
          <th>Event</th>
          <th>Category</th>
          <th><a class="sort-link" href="${pager.sortUrl('status')}">Status <i class="bi ${pager.sortIcon('status')}"></i></a></th>
          <th>Bib</th>
          <th class="num"><a class="sort-link" href="${pager.sortUrl('registered')}">Amount <i class="bi ${pager.sortIcon('registered')}"></i></a></th>
          <th class="actions">Actions</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="registration" items="${registrations}">
          <tr>
            <td>
              <div class="cell-main">
                <span class="avatar sm soft"><i class="bi bi-flag"></i></span>
                <span class="who">
                  <strong><a href="${ctx}/registrations/${registration.registrationId}"><c:out value="${registration.eventTitle}"/></a></strong>
                  <span>${registration.registrationCode} &middot; ${registration.eventDateFormatted}</span>
                </span>
              </div>
            </td>
            <td>${registration.categoryName}<div class="cell-sub">${registration.distanceLabel}</div></td>
            <td><span class="pill pill-${registration.statusBadge}">${registration.statusLabel}</span></td>
            <td><span class="code-chip">${registration.bibDisplay}</span></td>
            <td class="num">${registration.amountFormatted}</td>
            <td class="actions">
              <a class="btn btn-ghost btn-sm" href="${ctx}/registrations/${registration.registrationId}">Details</a>
              <c:if test="${registration.statusKey eq 'PENDING_PAYMENT'}">
                <a class="btn btn-accent btn-sm" href="${ctx}/registrations/${registration.registrationId}">Pay now</a>
              </c:if>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty registrations}">
          <tr>
            <td colspan="6">
              <div class="empty-state">
                <div class="empty-icon"><i class="bi bi-ticket-perforated"></i></div>
                <h3>No registrations found</h3>
                <p>Once you register for an event it will appear here with its payment and bib details.</p>
                <a class="btn btn-ink" href="${ctx}/events">Browse events</a>
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
