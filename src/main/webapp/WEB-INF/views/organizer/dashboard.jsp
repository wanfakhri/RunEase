<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="page-head">
  <div>
    <span class="eyebrow">Organizer dashboard</span>
    <h1><c:out value="${stats.organizer.organizationName}"/></h1>
    <p class="page-sub">Publish races, watch entries come in and keep every participant ready for race day.</p>
  </div>
  <div class="page-actions">
    <a class="btn btn-ghost" href="${ctx}/organizer/reports"><i class="bi bi-bar-chart"></i> Reports</a>
    <a class="btn btn-accent" href="${ctx}/organizer/events/new"><i class="bi bi-plus-lg"></i> New event</a>
  </div>
</div>

<div class="stat-grid mb-3">
  <div class="stat-card">
    <div class="stat-top"><span class="stat-label">My events</span><span class="stat-icon"><i class="bi bi-flag"></i></span></div>
    <div class="stat-value">${stats.totalEvents}</div>
    <div class="stat-note"><i class="bi bi-check2-circle"></i> ${stats.approvedEvents} approved &middot; ${stats.pendingEvents} pending</div>
  </div>
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
  <div class="stat-card accent-card">
    <div class="stat-top"><span class="stat-label">Awaiting payment</span><span class="stat-icon"><i class="bi bi-hourglass-split"></i></span></div>
    <div class="stat-value">${stats.pendingPayments}</div>
    <div class="stat-note"><i class="bi bi-clipboard-check"></i> ${stats.checkInsToday} check-in(s) today</div>
  </div>
</div>

<div class="split">
  <div class="stack">
    <div class="card-panel">
      <div class="card-head">
        <div>
          <h2>Registration trend</h2>
          <p class="sub">New entries over the last six months.</p>
        </div>
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
        <div>
          <h2>Top events by participation</h2>
          <p class="sub">Where runners are spending their weekends.</p>
        </div>
        <a class="btn btn-ghost btn-sm" href="${ctx}/organizer/events">All events</a>
      </div>
      <div class="table-shell">
        <table class="data-table">
          <thead>
            <tr><th>Event</th><th>Status</th><th class="num">Runners</th><th class="num">Revenue</th><th></th></tr>
          </thead>
          <tbody>
            <c:forEach var="row" items="${stats.topEvents}">
              <tr>
                <td>
                  <div class="cell-main">
                    <span class="avatar sm soft"><i class="bi bi-flag"></i></span>
                    <span class="who"><strong><c:out value="${row.title}"/></strong><span>${row.eventDateLabel}</span></span>
                  </div>
                </td>
                <td><span class="pill pill-${row.statusBadge}">${row.statusLabel}</span></td>
                <td class="num">${row.participants}</td>
                <td class="num">${row.revenueFormatted}</td>
                <td class="actions"><a class="btn btn-ghost btn-sm" href="${ctx}/organizer/events/${row.eventId}">Manage</a></td>
              </tr>
            </c:forEach>
            <c:if test="${empty stats.topEvents}">
              <tr><td colspan="5"><div class="empty-state" style="padding:30px"><p class="mb-0">No events yet. Create your first race to start collecting entries.</p></div></td></tr>
            </c:if>
          </tbody>
        </table>
      </div>
    </div>

    <div class="card-panel">
      <div class="card-head">
        <div>
          <h2>Recent registrations</h2>
          <p class="sub">The latest runners to join your races.</p>
        </div>
        <a class="btn btn-ghost btn-sm" href="${ctx}/organizer/participants">All participants</a>
      </div>
      <div class="table-shell">
        <table class="data-table">
          <thead><tr><th>Runner</th><th>Event</th><th>Status</th><th>Registered</th><th></th></tr></thead>
          <tbody>
            <c:forEach var="registration" items="${stats.recentRegistrations}">
              <tr>
                <td>
                  <div class="cell-main">
                    <span class="avatar sm">${registration.participantInitials}</span>
                    <span class="who"><strong><c:out value="${registration.participantName}"/></strong><span><c:out value="${registration.participantEmail}"/></span></span>
                  </div>
                </td>
                <td>${registration.eventTitle}<div class="cell-sub">${registration.categoryName}</div></td>
                <td><span class="pill pill-${registration.statusBadge}">${registration.statusLabel}</span></td>
                <td class="small muted">${registration.registeredAtFormatted}</td>
                <td class="actions"><a class="btn btn-ghost btn-sm" href="${ctx}/organizer/participants?q=${registration.registrationCode}">View</a></td>
              </tr>
            </c:forEach>
            <c:if test="${empty stats.recentRegistrations}">
              <tr><td colspan="5"><div class="empty-state" style="padding:30px"><p class="mb-0">No registrations yet.</p></div></td></tr>
            </c:if>
          </tbody>
        </table>
      </div>
    </div>
  </div>

  <aside class="stack">
    <div class="card-panel">
      <div class="card-head">
        <div>
          <h2>Upcoming events</h2>
          <p class="sub">Your next four start lines.</p>
        </div>
      </div>
      <div class="card-body">
        <c:choose>
          <c:when test="${not empty stats.upcomingEvents}">
            <div class="stack" style="gap:12px">
              <c:forEach var="event" items="${stats.upcomingEvents}">
                <div class="category-row mb-0">
                  <div class="d-flex align-items-center gap-3">
                    <span class="avatar soft"><strong>${event.dateDay}</strong></span>
                    <div>
                      <div class="cat-name"><a href="${ctx}/organizer/events/${event.eventId}"><c:out value="${event.title}"/></a></div>
                      <div class="cat-meta">${event.shortDateFormatted} &middot; ${event.distanceSummary}</div>
                    </div>
                  </div>
                  <span class="pill pill-${event.statusBadge}">${event.statusLabel}</span>
                </div>
              </c:forEach>
            </div>
          </c:when>
          <c:otherwise>
            <div class="empty-state" style="padding:24px 8px">
              <div class="empty-icon"><i class="bi bi-calendar2-plus"></i></div>
              <h3>No upcoming events</h3>
              <p>Create an event and submit it for approval to appear in the public catalogue.</p>
              <a class="btn btn-ink btn-sm" href="${ctx}/organizer/events/new">Create event</a>
            </div>
          </c:otherwise>
        </c:choose>
      </div>
    </div>

    <div class="card-panel">
      <div class="card-head"><h2>Event pipeline</h2></div>
      <div class="card-body">
        <div class="meter-list">
          <div class="meter-row">
            <span class="meter-name">Approved</span>
            <div class="meter-track"><div class="meter-fill lime" data-meter data-value="${stats.approvedEvents}" data-max="${stats.totalEvents}"></div></div>
            <span class="meter-value">${stats.approvedEvents}</span>
          </div>
          <div class="meter-row">
            <span class="meter-name">Pending</span>
            <div class="meter-track"><div class="meter-fill amber" data-meter data-value="${stats.pendingEvents}" data-max="${stats.totalEvents}"></div></div>
            <span class="meter-value">${stats.pendingEvents}</span>
          </div>
          <div class="meter-row">
            <span class="meter-name">Draft</span>
            <div class="meter-track"><div class="meter-fill" data-meter data-value="${stats.draftEvents}" data-max="${stats.totalEvents}"></div></div>
            <span class="meter-value">${stats.draftEvents}</span>
          </div>
        </div>
        <hr class="hr">
        <div class="spec-grid" style="grid-template-columns:1fr 1fr">
          <div class="spec"><div class="k">Checked in</div><div class="v">${stats.checkedInRegistrations}</div></div>
          <div class="spec"><div class="k">Confirmed</div><div class="v">${stats.confirmedRegistrations}</div></div>
        </div>
      </div>
    </div>

    <div class="notice lime">
      <i class="bi bi-lightbulb"></i>
      <div><strong>Approval reminder.</strong> New events start as drafts. Add at least one race category, then submit the event so an administrator can approve it.</div>
    </div>
  </aside>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
