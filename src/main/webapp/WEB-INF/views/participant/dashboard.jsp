<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="page-head">
  <div>
    <span class="eyebrow">Runner dashboard</span>
    <h1>Good to see you, <c:out value="${user.firstName}"/>.</h1>
    <p class="page-sub">Your entries, next start lines and organizer announcements in one place.</p>
  </div>
  <div class="page-actions">
    <a class="btn btn-ghost" href="${ctx}/profile"><i class="bi bi-person"></i> Account settings</a>
    <a class="btn btn-accent" href="${ctx}/events"><i class="bi bi-compass"></i> Find a race</a>
  </div>
</div>

<div class="stat-grid mb-3">
  <div class="stat-card">
    <div class="stat-top"><span class="stat-label">Total entries</span><span class="stat-icon"><i class="bi bi-ticket-perforated"></i></span></div>
    <div class="stat-value">${stats.totalRegistrations}</div>
    <div class="stat-note"><i class="bi bi-flag"></i> Across all RunEase events</div>
  </div>
  <div class="stat-card">
    <div class="stat-top"><span class="stat-label">Confirmed</span><span class="stat-icon blue"><i class="bi bi-check2-circle"></i></span></div>
    <div class="stat-value">${stats.confirmed}</div>
    <div class="stat-note"><i class="bi bi-shield-check"></i> Payment settled</div>
  </div>
  <div class="stat-card">
    <div class="stat-top"><span class="stat-label">Awaiting payment</span><span class="stat-icon amber"><i class="bi bi-hourglass-split"></i></span></div>
    <div class="stat-value">${stats.pendingPayments}</div>
    <div class="stat-note"><i class="bi bi-exclamation-circle"></i> Complete to secure your slot</div>
  </div>
  <div class="stat-card accent-card">
    <div class="stat-top"><span class="stat-label">Distance entered</span><span class="stat-icon"><i class="bi bi-signpost-split"></i></span></div>
    <div class="stat-value"><fmt:formatNumber value="${stats.totalDistance}" maxFractionDigits="1" /> <small>km</small></div>
    <div class="stat-note"><i class="bi bi-stopwatch"></i> ${stats.finished} race(s) finished</div>
  </div>
</div>

<div class="split">
  <div class="stack">
    <div class="card-panel">
      <div class="card-head">
        <div>
          <h2>Your next start lines</h2>
          <p class="sub">The next three events on your calendar.</p>
        </div>
        <a class="btn btn-ghost btn-sm" href="${ctx}/registrations">All registrations</a>
      </div>
      <div class="card-body">
        <c:choose>
          <c:when test="${not empty stats.nextEvents}">
            <div class="stack" style="gap:12px">
              <c:forEach var="registration" items="${stats.nextEvents}">
                <div class="category-row mb-0">
                  <div class="d-flex align-items-center gap-3">
                    <span class="avatar soft"><i class="bi bi-flag"></i></span>
                    <div>
                      <div class="cat-name"><a href="${ctx}/registrations/${registration.registrationId}"><c:out value="${registration.eventTitle}"/></a></div>
                      <div class="cat-meta">${registration.categoryName} &middot; ${registration.eventDateFormatted} &middot; <c:out value="${registration.venue}"/></div>
                    </div>
                  </div>
                  <div class="text-end">
                    <span class="pill pill-${registration.statusBadge}">${registration.statusLabel}</span>
                    <div class="cat-meta mt-1">
                      <c:choose>
                        <c:when test="${registration.daysToEvent lt 0}">Completed</c:when>
                        <c:when test="${registration.daysToEvent eq 0}">Race day today</c:when>
                        <c:otherwise>${registration.daysToEvent} day(s) to go</c:otherwise>
                      </c:choose>
                    </div>
                  </div>
                </div>
              </c:forEach>
            </div>
          </c:when>
          <c:otherwise>
            <div class="empty-state" style="padding:32px 16px">
              <div class="empty-icon"><i class="bi bi-calendar2-plus"></i></div>
              <h3>No upcoming races</h3>
              <p>Your calendar is clear. Browse the race catalogue and pick your next challenge.</p>
              <a class="btn btn-ink" href="${ctx}/events">Browse events</a>
            </div>
          </c:otherwise>
        </c:choose>
      </div>
    </div>

    <div class="card-panel">
      <div class="card-head">
        <div>
          <h2>Recent registrations</h2>
          <p class="sub">Your five most recent entries.</p>
        </div>
      </div>
      <div class="table-shell">
        <table class="data-table">
          <thead>
            <tr><th>Event</th><th>Category</th><th>Status</th><th class="num">Amount</th><th></th></tr>
          </thead>
          <tbody>
            <c:forEach var="registration" items="${stats.registrations}">
              <tr>
                <td>
                  <div class="cell-main">
                    <span class="avatar sm soft">${registration.participantInitials}</span>
                    <span class="who"><strong><c:out value="${registration.eventTitle}"/></strong><span>${registration.registrationCode}</span></span>
                  </div>
                </td>
                <td>${registration.categoryName}<div class="cell-sub">${registration.eventDateFormatted}</div></td>
                <td><span class="pill pill-${registration.statusBadge}">${registration.statusLabel}</span></td>
                <td class="num">${registration.amountFormatted}</td>
                <td class="actions"><a class="btn btn-ghost btn-sm" href="${ctx}/registrations/${registration.registrationId}">View</a></td>
              </tr>
            </c:forEach>
            <c:if test="${empty stats.registrations}">
              <tr><td colspan="5"><div class="empty-state" style="padding:30px"><p class="mb-0">No registrations yet.</p></div></td></tr>
            </c:if>
          </tbody>
        </table>
      </div>
    </div>
  </div>

  <aside class="stack">
    <div class="card-panel">
      <div class="card-head"><h2>Announcements</h2></div>
      <div class="card-body">
        <c:choose>
          <c:when test="${not empty stats.announcements}">
            <c:forEach var="announcement" items="${stats.announcements}" end="3">
              <div class="announcement">
                <h4><c:out value="${announcement.title}"/></h4>
                <p><c:out value="${announcement.excerpt}"/></p>
                <div class="meta"><c:out value="${announcement.eventTitle}"/> &middot; ${announcement.publishedAtFormatted}</div>
              </div>
            </c:forEach>
          </c:when>
          <c:otherwise>
            <p class="muted mb-0">No announcements yet. Organizers will post race-day updates here.</p>
          </c:otherwise>
        </c:choose>
      </div>
    </div>

    <div class="card-panel">
      <div class="card-body">
        <div class="eyebrow">Runner checklist</div>
        <ul class="timeline mt-2">
          <li class="${stats.confirmed gt 0 ? 'done' : ''}"><strong>Confirm your entry</strong><span>Complete payment before the registration close date.</span></li>
          <li class="${stats.checkedIn gt 0 ? 'done' : ''}"><strong>Collect your bib</strong><span>Show your registration code at the race pack collection.</span></li>
          <li class="${stats.finished gt 0 ? 'done' : ''}"><strong>Finish and review</strong><span>Official results appear on your registration page.</span></li>
        </ul>
      </div>
    </div>
  </aside>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
