<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="page-head">
  <div>
    <span class="eyebrow">Event moderation</span>
    <h1><c:out value="${event.title}"/></h1>
    <p class="page-sub"><c:out value="${event.organizerName}"/> &middot; ${event.location} &middot; ${event.shortDateFormatted}</p>
  </div>
  <div class="page-actions">
    <a class="btn btn-ghost" href="${ctx}/admin/events"><i class="bi bi-arrow-left"></i> All events</a>
    <c:if test="${event.status eq 'APPROVED'}">
      <a class="btn btn-ghost" href="${ctx}/events/${event.eventId}" target="_blank" rel="noopener"><i class="bi bi-box-arrow-up-right"></i> Public page</a>
    </c:if>
  </div>
</div>

<c:if test="${event.status eq 'REJECTED' and not empty event.rejectionReason}">
  <div class="notice danger mb-3">
    <i class="bi bi-x-octagon"></i>
    <div><strong>Rejected.</strong> <c:out value="${event.rejectionReason}"/></div>
  </div>
</c:if>
<c:if test="${event.status eq 'PENDING'}">
  <div class="notice warning mb-3">
    <i class="bi bi-hourglass-split"></i>
    <div><strong>Awaiting decision.</strong> Approving publishes the event to the public catalogue immediately. Rejecting sends your reason back to the organizer.</div>
  </div>
</c:if>

<div class="detail-body">
  <div class="stack">
    <div class="detail-hero ${event.coverClass}">
      <div class="detail-hero-body">
        <span class="pill pill-${event.statusBadge}">${event.statusLabel}</span>
        <h2 class="mt-2"><c:out value="${event.title}"/></h2>
        <div class="hero-card-meta">
          <span><i class="bi bi-geo-alt-fill"></i> <c:out value="${event.venue}"/>, <c:out value="${event.city}"/></span>
          <span><i class="bi bi-calendar3"></i> ${event.eventDateFormatted}</span>
          <span><i class="bi bi-stopwatch"></i> ${event.flagOffFormatted} flag-off</span>
          <span><i class="bi bi-tag"></i> ${event.typeLabel}</span>
        </div>
      </div>
    </div>

    <div class="card-panel">
      <div class="card-head">
        <div><h2>About this event</h2><p class="sub">Organizer-supplied description.</p></div>
      </div>
      <div class="card-body">
        <p class="prose mb-0" style="white-space:pre-line"><c:out value="${event.description}"/></p>
      </div>
    </div>

    <div class="card-panel">
      <div class="card-head">
        <div><h2>Race categories</h2><p class="sub">Distances, fees, quotas and start times.</p></div>
        <span class="pill pill-lime">${fn:length(event.categories)} categor<c:choose><c:when test="${fn:length(event.categories) eq 1}">y</c:when><c:otherwise>ies</c:otherwise></c:choose></span>
      </div>
      <div class="table-shell">
        <table class="data-table">
          <thead><tr><th>Category</th><th>Distance</th><th class="num">Fee</th><th class="num">Quota</th><th class="num">Registered</th><th>Start</th></tr></thead>
          <tbody>
            <c:forEach var="category" items="${event.categories}">
              <tr>
                <td><span class="cell-strong"><c:out value="${category.name}"/></span></td>
                <td>${category.distanceLabel}</td>
                <td class="num">${category.priceFormatted}</td>
                <td class="num">${category.quota}</td>
                <td class="num">${category.registeredCount}<div class="cell-sub">${category.slotsLeft} left</div></td>
                <td>${category.startTimeFormatted}</td>
              </tr>
            </c:forEach>
            <c:if test="${empty event.categories}">
              <tr><td colspan="6"><div class="empty-state" style="padding:30px"><p class="mb-0">This event has no race categories. Ask the organizer to add at least one before approval.</p></div></td></tr>
            </c:if>
          </tbody>
        </table>
      </div>
    </div>
  </div>

  <aside class="stack">
    <div class="card-panel">
      <div class="card-head"><h2>Decision</h2></div>
      <div class="card-body">
        <div class="info-list">
          <div class="info-row"><span class="k">Status</span><span class="v"><span class="pill pill-${event.statusBadge}">${event.statusLabel}</span></span></div>
          <div class="info-row"><span class="k">Organizer</span><span class="v"><c:out value="${event.organizerName}"/></span></div>
          <div class="info-row"><span class="k">Race date</span><span class="v">${event.shortDateFormatted}</span></div>
          <div class="info-row"><span class="k">Registration</span><span class="v">${event.registrationWindow}</span></div>
          <div class="info-row"><span class="k">Categories</span><span class="v">${fn:length(event.categories)}</span></div>
          <div class="info-row"><span class="k">Capacity</span><span class="v">${event.totalQuota} slots</span></div>
          <div class="info-row"><span class="k">Registered</span><span class="v">${event.registeredCount}</span></div>
        </div>

        <c:choose>
          <c:when test="${event.status eq 'PENDING'}">
            <form method="post" action="${ctx}/admin/events/${event.eventId}/approve" class="mt-3"
                  data-confirm="Approve this event and publish it to the public catalogue?"
                  data-confirm-title="Approve event" data-confirm-tone="primary" data-confirm-ok="Approve and publish">
              <button class="btn btn-accent w-100" type="submit"><i class="bi bi-check2-circle"></i> Approve and publish</button>
            </form>
            <button class="btn btn-danger-soft w-100 mt-2" type="button" data-bs-toggle="modal" data-bs-target="#rejectModal">
              <i class="bi bi-x-octagon"></i> Reject with reason
            </button>
          </c:when>
          <c:otherwise>
            <div class="notice lime mt-3">
              <i class="bi bi-info-circle"></i>
              <div>This event is <strong>${event.statusLabel}</strong> and is not waiting for a decision.</div>
            </div>
          </c:otherwise>
        </c:choose>
      </div>
    </div>

    <div class="card-panel">
      <div class="card-head"><h2>Moderation checklist</h2></div>
      <div class="card-body">
        <div class="timeline">
          <div class="step done"><span class="dot"><i class="bi bi-check2"></i></span><div><strong>Details complete</strong><p>Name, venue, dates and description provided.</p></div></div>
          <div class="step ${not empty event.categories ? 'done' : ''}"><span class="dot"><i class="bi bi-signpost-split"></i></span><div><strong>Categories priced</strong><p>At least one category with quota.</p></div></div>
          <div class="step"><span class="dot"><i class="bi bi-calendar-check"></i></span><div><strong>Dates sensible</strong><p>Registration closes before race day.</p></div></div>
          <div class="step"><span class="dot"><i class="bi bi-shield-check"></i></span><div><strong>Decision recorded</strong><p>Approval or rejection is written to the audit trail.</p></div></div>
        </div>
      </div>
    </div>
  </aside>
</div>

<div class="modal fade" id="rejectModal" tabindex="-1" aria-labelledby="rejectModalTitle" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content">
      <form method="post" action="${ctx}/admin/events/${event.eventId}/reject" novalidate>
        <div class="modal-header">
          <h2 class="modal-title" id="rejectModalTitle">Reject event</h2>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <p class="muted">Explain what the organizer needs to fix. This reason is shown to them and written to the audit trail.</p>
          <div class="mb-3">
            <label class="form-label" for="rejectReason">Rejection reason <span class="req">*</span></label>
            <textarea class="form-control" id="rejectReason" name="reason" rows="5" minlength="10" maxlength="1000"
                      placeholder="e.g. Venue details are incomplete and the event date falls before the registration closing date." required></textarea>
            <div class="form-text">At least 10 characters.</div>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-ghost" data-bs-dismiss="modal">Cancel</button>
          <button type="submit" class="btn btn-danger-soft"><i class="bi bi-x-octagon"></i> Reject event</button>
        </div>
      </form>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
