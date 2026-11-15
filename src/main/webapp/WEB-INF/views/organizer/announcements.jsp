<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="page-head">
  <div>
    <span class="eyebrow">Runner communications</span>
    <h1>Announcements</h1>
    <p class="page-sub">Publish race-day instructions, route updates and reminders straight to participant dashboards.</p>
  </div>
  <div class="page-actions">
    <a class="btn btn-ghost" href="${ctx}/organizer/events"><i class="bi bi-flag"></i> My events</a>
  </div>
</div>

<form class="filter-bar" method="get" action="${ctx}/organizer/announcements">
  <select class="form-select" name="eventId" aria-label="Filter by event" data-autosubmit>
    <option value="">All my events</option>
    <c:forEach var="event" items="${organizedEvents}">
      <option value="${event.eventId}" <c:if test="${selectedEventId eq event.eventId}">selected</c:if>>${fn:escapeXml(event.title)}</option>
    </c:forEach>
  </select>
  <div class="filter-actions">
    <button class="btn btn-ink" type="submit"><i class="bi bi-funnel"></i> Filter</button>
    <a class="btn btn-ghost" href="${ctx}/organizer/announcements">Reset</a>
  </div>
</form>

<div class="detail-body">
  <div class="stack">
    <c:choose>
      <c:when test="${empty announcements}">
        <div class="card-panel">
          <div class="empty-state">
            <div class="empty-icon"><i class="bi bi-megaphone"></i></div>
            <h3>No announcements yet</h3>
            <p>Use the form on the right to publish your first update. Approved and completed events can receive announcements.</p>
          </div>
        </div>
      </c:when>
      <c:otherwise>
        <c:forEach var="announcement" items="${announcements}">
          <article class="card-panel">
            <div class="card-head">
              <div>
                <div class="eyebrow"><c:out value="${announcement.eventTitle}"/></div>
                <h2 class="mb-1"><c:out value="${announcement.title}"/></h2>
                <p class="sub mb-0">
                  ${announcement.publishedAtFormatted}
                  <span class="dot-sep">&middot;</span>
                  <c:out value="${announcement.authorName}"/>
                  <span class="dot-sep">&middot;</span>
                  ${announcement.audienceLabel}
                </p>
              </div>
              <span class="pill pill-lime"><i class="bi bi-broadcast"></i> Published</span>
            </div>
            <div class="card-body">
              <p class="prose mb-0"><c:out value="${announcement.body}"/></p>
              <div class="row-flex mt-3">
                <button class="btn btn-ghost btn-sm" type="button" data-bs-toggle="modal" data-bs-target="#announcementModal"
                        data-modal-heading="Edit announcement"
                        data-form-action="${ctx}/organizer/announcements/${announcement.announcementId}"
                        data-field-eventId="${announcement.eventId}"
                        data-field-title="<c:out value='${announcement.title}'/>"
                        data-field-body="<c:out value='${announcement.body}'/>"
                        data-field-audience="${announcement.audience}">
                  <i class="bi bi-pencil"></i> Edit
                </button>
                <form method="post" action="${ctx}/organizer/announcements/${announcement.announcementId}/delete"
                      data-confirm="Delete &quot;${fn:escapeXml(announcement.title)}&quot;? Participants will no longer see this update."
                      data-confirm-title="Delete announcement" data-confirm-ok="Delete announcement">
                  <input type="hidden" name="eventId" value="${announcement.eventId}">
                  <button class="btn btn-danger-soft btn-sm" type="submit"><i class="bi bi-trash3"></i> Delete</button>
                </form>
              </div>
            </div>
          </article>
        </c:forEach>
      </c:otherwise>
    </c:choose>
  </div>

  <aside class="stack">
    <div class="card-panel">
      <div class="card-head">
        <div>
          <h2>New announcement</h2>
          <p class="sub">Participants see updates on their dashboard and event page.</p>
        </div>
        <i class="bi bi-megaphone muted"></i>
      </div>
      <div class="card-body">
        <c:choose>
          <c:when test="${empty organizedEvents}">
            <div class="notice warning">
              <i class="bi bi-exclamation-triangle"></i>
              <div>You need an approved or completed event before publishing an announcement.</div>
            </div>
          </c:when>
          <c:otherwise>
            <form method="post" action="${ctx}/organizer/announcements" novalidate>
              <div class="mb-3">
                <label class="form-label" for="announcementEvent">Event <span class="req">*</span></label>
                <select class="form-select" id="announcementEvent" name="eventId" required>
                  <option value="">Select an event</option>
                  <c:forEach var="event" items="${organizedEvents}">
                    <option value="${event.eventId}" <c:if test="${selectedEventId eq event.eventId}">selected</c:if>>${fn:escapeXml(event.title)}</option>
                  </c:forEach>
                </select>
              </div>
              <div class="mb-3">
                <label class="form-label" for="announcementTitle">Title <span class="req">*</span></label>
                <input class="form-control" type="text" id="announcementTitle" name="title" maxlength="150"
                       placeholder="e.g. Revised flag-off time" required>
              </div>
              <div class="mb-3">
                <label class="form-label" for="announcementBody">Message <span class="req">*</span></label>
                <textarea class="form-control" id="announcementBody" name="body" rows="5" maxlength="2000"
                          placeholder="Share the details runners need to know..." required></textarea>
              </div>
              <div class="mb-3">
                <label class="form-label" for="announcementAudience">Audience</label>
                <select class="form-select" id="announcementAudience" name="audience">
                  <option value="ALL">Everyone</option>
                  <option value="CONFIRMED">Confirmed participants</option>
                </select>
              </div>
              <button class="btn btn-accent w-100" type="submit"><i class="bi bi-send"></i> Publish announcement</button>
            </form>
          </c:otherwise>
        </c:choose>
      </div>
    </div>

    <div class="notice lime">
      <i class="bi bi-lightbulb"></i>
      <div><strong>Keep it short.</strong> Lead with what changed and what runners must do. Confirm the event, audience and date before publishing.</div>
    </div>
  </aside>
</div>

<div class="modal fade" id="announcementModal" tabindex="-1" aria-labelledby="announcementModalTitle" aria-hidden="true"
     data-modal-default-heading="Edit announcement">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content">
      <form method="post" action="${ctx}/organizer/announcements" data-default-action="${ctx}/organizer/announcements" data-reset-on-close novalidate>
        <input type="hidden" name="eventId" value="${selectedEventId}">
        <div class="modal-header">
          <h2 class="modal-title" id="announcementModalTitle" data-modal-title>Edit announcement</h2>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <div class="mb-3">
            <label class="form-label" for="editAnnouncementTitle">Title <span class="req">*</span></label>
            <input class="form-control" type="text" id="editAnnouncementTitle" name="title" maxlength="150" required>
          </div>
          <div class="mb-3">
            <label class="form-label" for="editAnnouncementBody">Message <span class="req">*</span></label>
            <textarea class="form-control" id="editAnnouncementBody" name="body" rows="6" maxlength="2000" required></textarea>
          </div>
          <div class="mb-3">
            <label class="form-label" for="editAnnouncementAudience">Audience</label>
            <select class="form-select" id="editAnnouncementAudience" name="audience">
              <option value="ALL">Everyone</option>
              <option value="CONFIRMED">Confirmed participants</option>
            </select>
          </div>
          <div class="notice warning">
            <i class="bi bi-exclamation-triangle"></i>
            <div>Editing changes the message shown to participants immediately. The original publish date is kept.</div>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-ghost" data-bs-dismiss="modal">Cancel</button>
          <button type="submit" class="btn btn-accent"><i class="bi bi-check2"></i> Save changes</button>
        </div>
      </form>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
