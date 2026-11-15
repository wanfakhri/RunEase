<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="page-head">
  <div>
    <span class="eyebrow">Administration</span>
    <h1>Platform overview</h1>
    <p class="page-sub">Moderate events, verify organizers and keep the marketplace healthy.</p>
  </div>
  <div class="page-actions">
    <a class="btn btn-ghost" href="${ctx}/admin/reports"><i class="bi bi-bar-chart"></i> Reports</a>
    <a class="btn btn-accent" href="${ctx}/admin/events?status=PENDING"><i class="bi bi-shield-check"></i> Review queue</a>
  </div>
</div>

<div class="stat-grid mb-3">
  <div class="stat-card">
    <div class="stat-top"><span class="stat-label">Active users</span><span class="stat-icon blue"><i class="bi bi-people"></i></span></div>
    <div class="stat-value">${stats.totalUsers}</div>
    <div class="stat-note"><i class="bi bi-person-badge"></i> ${stats.pendingOrganizers} organizer application(s)</div>
  </div>
  <div class="stat-card">
    <div class="stat-top"><span class="stat-label">Events</span><span class="stat-icon"><i class="bi bi-flag"></i></span></div>
    <div class="stat-value">${stats.totalEvents}</div>
    <div class="stat-note"><i class="bi bi-check2-circle"></i> ${stats.approvedEvents} approved &middot; ${stats.pendingEvents} pending</div>
  </div>
  <div class="stat-card">
    <div class="stat-top"><span class="stat-label">Registrations</span><span class="stat-icon amber"><i class="bi bi-ticket-perforated"></i></span></div>
    <div class="stat-value">${stats.totalRegistrations}</div>
    <div class="stat-note"><i class="bi bi-person-check"></i> ${stats.confirmedRegistrations} confirmed</div>
  </div>
  <div class="stat-card accent-card">
    <div class="stat-top"><span class="stat-label">Platform revenue</span><span class="stat-icon"><i class="bi bi-cash-stack"></i></span></div>
    <div class="stat-value"><small>RM</small> <fmt:formatNumber value="${stats.totalRevenue}" minFractionDigits="2" maxFractionDigits="2"/></div>
    <div class="stat-note"><i class="bi bi-calendar3"></i> RM <fmt:formatNumber value="${stats.revenueThisMonth}" minFractionDigits="2" maxFractionDigits="2"/> this month</div>
  </div>
</div>

<div class="split">
  <div class="stack">
    <div class="card-panel">
      <div class="card-head">
        <div>
          <h2>Pending event approvals</h2>
          <p class="sub">Newest submissions waiting for a decision.</p>
        </div>
        <span class="pill pill-warning"><i class="bi bi-hourglass-split"></i> ${fn:length(pendingApprovals)} waiting</span>
      </div>
      <div class="table-shell">
        <table class="data-table">
          <thead><tr><th>Event</th><th>Organizer</th><th>Date</th><th>Categories</th><th class="actions">Review</th></tr></thead>
          <tbody>
            <c:forEach var="event" items="${pendingApprovals}">
              <tr data-href="${ctx}/admin/events/${event.eventId}">
                <td>
                  <div class="cell-main">
                    <span class="avatar sm soft"><i class="bi bi-flag"></i></span>
                    <span class="who"><strong><c:out value="${event.title}"/></strong><span>${event.location}</span></span>
                  </div>
                </td>
                <td><c:out value="${event.organizerName}"/></td>
                <td>${event.shortDateFormatted}</td>
                <td>${event.distanceSummary}</td>
                <td class="actions"><a class="btn btn-accent btn-sm" href="${ctx}/admin/events/${event.eventId}">Review</a></td>
              </tr>
            </c:forEach>
            <c:if test="${empty pendingApprovals}">
              <tr><td colspan="5"><div class="empty-state" style="padding:30px"><div class="empty-icon"><i class="bi bi-check2-circle"></i></div><h3>Queue cleared</h3><p class="mb-0">No events are waiting for approval right now.</p></div></td></tr>
            </c:if>
          </tbody>
        </table>
      </div>
    </div>

    <div class="card-panel">
      <div class="card-head">
        <div><h2>Registration activity</h2><p class="sub">New entries over the last six months.</p></div>
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
        <div><h2>Recent registrations</h2><p class="sub">Latest activity across the platform.</p></div>
        <a class="btn btn-ghost btn-sm" href="${ctx}/admin/reports">View report</a>
      </div>
      <div class="table-shell">
        <table class="data-table">
          <thead><tr><th>Runner</th><th>Event</th><th>Status</th><th>Registered</th></tr></thead>
          <tbody>
            <c:forEach var="registration" items="${stats.recentRegistrations}">
              <tr>
                <td>
                  <div class="cell-main">
                    <span class="avatar sm">${registration.participantInitials}</span>
                    <span class="who"><strong><c:out value="${registration.participantName}"/></strong><span><c:out value="${registration.participantEmail}"/></span></span>
                  </div>
                </td>
                <td><c:out value="${registration.eventTitle}"/><div class="cell-sub">${registration.categoryName}</div></td>
                <td><span class="pill pill-${registration.statusBadge}">${registration.statusLabel}</span></td>
                <td class="small muted">${registration.registeredAtFormatted}</td>
              </tr>
            </c:forEach>
            <c:if test="${empty stats.recentRegistrations}">
              <tr><td colspan="4"><p class="muted mb-0 p-3">No registrations yet.</p></td></tr>
            </c:if>
          </tbody>
        </table>
      </div>
    </div>
  </div>

  <aside class="stack">
    <div class="card-panel">
      <div class="card-head"><h2>Organizer verification</h2></div>
      <div class="card-body">
        <c:choose>
          <c:when test="${not empty pendingOrganizers}">
            <div class="stack" style="gap:12px">
              <c:forEach var="organizer" items="${pendingOrganizers}">
                <div class="category-row mb-0">
                  <div class="d-flex align-items-center gap-3">
                    <span class="avatar soft">${organizer.initials}</span>
                    <div>
                      <div class="cat-name"><c:out value="${organizer.organizationName}"/></div>
                      <div class="cat-meta"><c:out value="${organizer.userEmail}"/></div>
                    </div>
                  </div>
                  <a class="btn btn-ghost btn-sm" href="${ctx}/admin/organizers?status=PENDING">Review</a>
                </div>
              </c:forEach>
            </div>
          </c:when>
          <c:otherwise>
            <p class="muted mb-0">No organizer applications are waiting for verification.</p>
          </c:otherwise>
        </c:choose>
      </div>
    </div>

    <div class="card-panel">
      <div class="card-head"><h2>Users by role</h2></div>
      <div class="card-body">
        <div class="meter-list">
          <c:forEach var="entry" items="${stats.usersByRole}">
            <div class="meter-row">
              <span class="meter-name">${entry.key}</span>
              <div class="meter-track"><div class="meter-fill lime" data-meter data-value="${entry.value}" data-max="${stats.totalUsers}"></div></div>
              <span class="meter-value">${entry.value}</span>
            </div>
          </c:forEach>
        </div>
        <hr class="hr">
        <div class="spec-grid" style="grid-template-columns:1fr 1fr">
          <div class="spec"><div class="k">Check-ins today</div><div class="v">${stats.checkInsToday}</div></div>
          <div class="spec"><div class="k">Confirmed</div><div class="v">${stats.confirmedRegistrations}</div></div>
        </div>
      </div>
    </div>

    <div class="card-panel">
      <div class="card-head"><h2>Quick actions</h2></div>
      <div class="card-body">
        <div class="stack" style="gap:10px">
          <a class="btn btn-ghost w-100 justify-content-start" href="${ctx}/admin/events?status=PENDING"><i class="bi bi-shield-check"></i> Moderate events</a>
          <a class="btn btn-ghost w-100 justify-content-start" href="${ctx}/admin/users"><i class="bi bi-person-gear"></i> Manage users</a>
          <a class="btn btn-ghost w-100 justify-content-start" href="${ctx}/admin/organizers"><i class="bi bi-patch-check"></i> Verify organizers</a>
          <a class="btn btn-ghost w-100 justify-content-start" href="${ctx}/admin/audit"><i class="bi bi-clock-history"></i> Open audit trail</a>
        </div>
      </div>
    </div>
  </aside>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
