<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<c:url var="csvUrl" value="/organizer/reports">
  <c:param name="export" value="csv" />
  <c:param name="eventId" value="${filters.eventId}" />
  <c:param name="q" value="${filters.q}" />
  <c:param name="status" value="${filters.status}" />
</c:url>

<div class="page-head">
  <div>
    <span class="eyebrow">Organizer analytics</span>
    <h1>Reports</h1>
    <p class="page-sub">Track entries, revenue and race-day readiness across every event you manage.</p>
  </div>
  <div class="page-actions">
    <button class="btn btn-ghost" type="button" data-print><i class="bi bi-printer"></i> Print</button>
    <a class="btn btn-accent" href="${csvUrl}"><i class="bi bi-filetype-csv"></i> Export CSV</a>
  </div>
</div>

<div class="stat-grid mb-3">
  <div class="stat-card">
    <div class="stat-top"><span class="stat-label">Registrations</span><span class="stat-icon blue"><i class="bi bi-people"></i></span></div>
    <div class="stat-value">${stats.totalRegistrations}</div>
    <div class="stat-note"><i class="bi bi-person-check"></i> ${stats.confirmedRegistrations} confirmed</div>
  </div>
  <div class="stat-card">
    <div class="stat-top"><span class="stat-label">Revenue</span><span class="stat-icon"><i class="bi bi-cash-stack"></i></span></div>
    <div class="stat-value"><small>RM</small> <fmt:formatNumber value="${stats.revenue}" minFractionDigits="2" maxFractionDigits="2"/></div>
    <div class="stat-note"><i class="bi bi-calendar3"></i> RM <fmt:formatNumber value="${stats.revenueThisMonth}" minFractionDigits="2" maxFractionDigits="2"/> this month</div>
  </div>
  <div class="stat-card">
    <div class="stat-top"><span class="stat-label">Checked in</span><span class="stat-icon amber"><i class="bi bi-person-badge"></i></span></div>
    <div class="stat-value">${stats.checkedInRegistrations}</div>
    <div class="stat-note"><i class="bi bi-clock-history"></i> ${stats.checkInsToday} today</div>
  </div>
  <div class="stat-card accent-card">
    <div class="stat-top"><span class="stat-label">Pending payment</span><span class="stat-icon"><i class="bi bi-hourglass-split"></i></span></div>
    <div class="stat-value">${stats.pendingPayments}</div>
    <div class="stat-note"><i class="bi bi-exclamation-circle"></i> Follow up before race day</div>
  </div>
</div>

<div class="split even mb-3">
  <div class="card-panel">
    <div class="card-head">
      <div><h2>Registrations by month</h2><p class="sub">Last six months of entries.</p></div>
      <span class="pill pill-lime">${stats.registrationsThisMonth} this month</span>
    </div>
    <div class="card-body">
      <div class="chart-wrap">
        <div class="chart-bars" data-chart>
          <c:forEach var="entry" items="${stats.monthlyRegistrations}">
            <div class="chart-col">
              <span class="chart-value">${entry.value}</span>
              <div class="chart-bar" data-chart-bar data-value="${entry.value}"></div>
              <span class="chart-label">${entry.key}</span>
            </div>
          </c:forEach>
        </div>
      </div>
    </div>
  </div>

  <div class="card-panel">
    <div class="card-head">
      <div><h2>Revenue by month</h2><p class="sub">Paid entries only, in ringgit.</p></div>
      <span class="pill pill-dark"><i class="bi bi-graph-up-arrow"></i> RM <fmt:formatNumber value="${stats.revenueThisMonth}" minFractionDigits="0" maxFractionDigits="0"/></span>
    </div>
    <div class="card-body">
      <div class="chart-wrap">
        <div class="chart-bars" data-chart>
          <c:forEach var="entry" items="${stats.monthlyRevenue}">
            <div class="chart-col">
              <span class="chart-value"><fmt:formatNumber value="${entry.value}" minFractionDigits="0" maxFractionDigits="0"/></span>
              <div class="chart-bar" data-chart-bar data-value="${entry.value}"></div>
              <span class="chart-label">${entry.key}</span>
            </div>
          </c:forEach>
        </div>
      </div>
    </div>
  </div>
</div>

<form class="filter-bar" method="get" action="${ctx}/organizer/reports" role="search">
  <div class="search-field">
    <i class="bi bi-search"></i>
    <input class="form-control" type="search" name="q" value="<c:out value='${filters.q}'/>"
           placeholder="Runner name, email, code or event" aria-label="Search registrations">
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
    <a class="btn btn-ghost" href="${ctx}/organizer/reports">Reset</a>
  </div>
</form>

<div class="card-panel">
  <div class="card-head">
    <div>
      <h2>Registration report</h2>
      <p class="sub">${pager.total} row<c:if test="${pager.total ne 1}">s</c:if> &middot; sort any column, then export the same view to CSV.</p>
    </div>
  </div>
  <div class="table-shell">
    <table class="data-table">
      <thead>
        <tr>
          <th><a class="sort-link" href="${pager.sortUrl('name')}">Runner <i class="bi ${pager.sortIcon('name')}"></i></a></th>
          <th><a class="sort-link" href="${pager.sortUrl('event')}">Event <i class="bi ${pager.sortIcon('event')}"></i></a></th>
          <th><a class="sort-link" href="${pager.sortUrl('category')}">Category <i class="bi ${pager.sortIcon('category')}"></i></a></th>
          <th><a class="sort-link" href="${pager.sortUrl('status')}">Status <i class="bi ${pager.sortIcon('status')}"></i></a></th>
          <th class="num">Amount</th>
          <th>Payment</th>
          <th><a class="sort-link" href="${pager.sortUrl('registered')}">Registered <i class="bi ${pager.sortIcon('registered')}"></i></a></th>
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
                  <span>${registration.registrationCode}</span>
                </span>
              </div>
            </td>
            <td><c:out value="${registration.eventTitle}"/><div class="cell-sub">${registration.eventDateFormatted}</div></td>
            <td>${registration.categoryName}<div class="cell-sub">${registration.distanceLabel}</div></td>
            <td><span class="pill pill-${registration.statusBadge}">${registration.statusLabel}</span></td>
            <td class="num">${registration.amountFormatted}</td>
            <td>
              <c:choose>
                <c:when test="${not empty registration.payment}">
                  <span class="pill pill-${registration.payment.statusBadge}">${registration.payment.statusLabel}</span>
                  <div class="cell-sub">${registration.payment.methodLabel}</div>
                </c:when>
                <c:otherwise><span class="muted">No payment</span></c:otherwise>
              </c:choose>
            </td>
            <td class="small muted">${registration.registeredAtFormatted}</td>
          </tr>
        </c:forEach>
        <c:if test="${empty registrations}">
          <tr>
            <td colspan="7">
              <div class="empty-state">
                <div class="empty-icon"><i class="bi bi-bar-chart"></i></div>
                <h3>No rows in this report</h3>
                <p>Adjust the event, status or search filters, or export once registrations start arriving.</p>
                <a class="btn btn-ink" href="${ctx}/organizer/reports">Clear filters</a>
              </div>
            </td>
          </tr>
        </c:if>
      </tbody>
    </table>
  </div>
  <%@ include file="/WEB-INF/views/layout/pagination.jspf" %>
</div>

<div class="split side-left mt-3">
  <aside class="stack">
    <div class="card-panel">
      <div class="card-head"><h2>Registrations by status</h2></div>
      <div class="card-body">
        <div class="meter-list">
          <c:forEach var="entry" items="${stats.registrationsByStatus}">
            <div class="meter-row">
              <span class="meter-name">${fn:replace(entry.key, '_', ' ')}</span>
              <div class="meter-track"><div class="meter-fill lime" data-meter data-value="${entry.value}" data-max="${stats.totalRegistrations}"></div></div>
              <span class="meter-value">${entry.value}</span>
            </div>
          </c:forEach>
          <c:if test="${empty stats.registrationsByStatus}">
            <p class="muted mb-0">No registrations recorded yet.</p>
          </c:if>
        </div>
      </div>
    </div>
  </aside>

  <div class="card-panel">
    <div class="card-head">
      <div><h2>Top events by participation</h2><p class="sub">Ranked by active runners.</p></div>
    </div>
    <div class="table-shell">
      <table class="data-table">
        <thead><tr><th>Event</th><th>Date</th><th>Status</th><th class="num">Runners</th><th class="num">Revenue</th></tr></thead>
        <tbody>
          <c:forEach var="row" items="${stats.topEvents}">
            <tr>
              <td><span class="cell-strong"><c:out value="${row.title}"/></span></td>
              <td>${row.eventDateLabel}</td>
              <td><span class="pill pill-${row.statusBadge}">${row.statusLabel}</span></td>
              <td class="num">${row.participants}</td>
              <td class="num">${row.revenueFormatted}</td>
            </tr>
          </c:forEach>
          <c:if test="${empty stats.topEvents}">
            <tr><td colspan="5"><p class="muted mb-0 p-3">No event data yet.</p></td></tr>
          </c:if>
        </tbody>
      </table>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
