<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<c:set var="editing" value="${mode eq 'edit'}" />
<c:set var="locked" value="${editing and (event.status eq 'COMPLETED' or event.status eq 'CANCELLED')}" />

<div class="page-head">
  <div>
    <span class="eyebrow">${editing ? 'Event workspace' : 'New race'}</span>
    <h1>${editing ? 'Manage event' : 'Create an event'}</h1>
    <p class="page-sub">
      <c:choose>
        <c:when test="${editing}"><c:out value="${event.title}"/></c:when>
        <c:otherwise>Start with the race details, then add categories and submit for approval.</c:otherwise>
      </c:choose>
    </p>
  </div>
  <div class="page-actions">
    <a class="btn btn-ghost" href="${ctx}/organizer/events"><i class="bi bi-arrow-left"></i> All events</a>
    <c:if test="${editing and event.status eq 'APPROVED'}">
      <a class="btn btn-ghost" href="${ctx}/events/${event.eventId}" target="_blank" rel="noopener">
        <i class="bi bi-box-arrow-up-right"></i> View public page
      </a>
    </c:if>
  </div>
</div>

<c:if test="${not empty errors.form}">
  <div class="notice danger mb-3"><i class="bi bi-x-octagon"></i><div><strong>Event not saved.</strong> <c:out value="${errors.form}"/></div></div>
</c:if>
<c:if test="${locked}">
  <div class="notice warning mb-3"><i class="bi bi-lock"></i><div><strong>Read-only event.</strong> Completed and cancelled events can no longer be edited.</div></div>
</c:if>

<div class="detail-body">
  <div class="stack">
    <div class="card-panel">
      <div class="card-head">
        <div>
          <h2>Race details</h2>
          <p class="sub">Public runners see this information in the catalogue.</p>
        </div>
        <c:if test="${editing}">
          <span class="pill pill-${event.statusBadge}">${event.statusLabel}</span>
        </c:if>
      </div>
      <div class="card-body">
        <form method="post" action="${ctx}/organizer/events/${editing ? event.eventId : 'new'}" novalidate>
          <div class="field-grid">
            <div class="mb-3 span-2">
              <label class="form-label" for="title">Event title <span class="req">*</span></label>
              <input class="form-control ${not empty errors.title ? 'is-invalid' : ''}" type="text" id="title"
                     name="title" maxlength="150" value="<c:out value='${form.title}'/>"
                     placeholder="e.g. Shah Alam Night Run 2026" ${locked ? 'disabled' : 'required'}>
              <c:if test="${not empty errors.title}"><span class="invalid-feedback"><c:out value="${errors.title}"/></span></c:if>
            </div>

            <div class="mb-3 span-2">
              <label class="form-label" for="description">Description <span class="req">*</span></label>
              <textarea class="form-control ${not empty errors.description ? 'is-invalid' : ''}" id="description"
                        name="description" rows="5" maxlength="4000" placeholder="Route highlights, race pack, cut-off times..."
                        ${locked ? 'disabled' : 'required'}><c:out value="${form.description}"/></textarea>
              <c:if test="${not empty errors.description}"><span class="invalid-feedback"><c:out value="${errors.description}"/></span></c:if>
              <div class="form-text">At least 30 characters. Line breaks are preserved on the public event page.</div>
            </div>

            <div class="mb-3">
              <label class="form-label" for="eventType">Race type <span class="req">*</span></label>
              <select class="form-select ${not empty errors.eventType ? 'is-invalid' : ''}" id="eventType" name="eventType" ${locked ? 'disabled' : ''}>
                <c:forEach var="type" items="${types}">
                  <option value="${type.name()}" <c:if test="${form.eventType eq type.name()}">selected</c:if>>${type.label}</option>
                </c:forEach>
              </select>
              <c:if test="${not empty errors.eventType}"><span class="invalid-feedback"><c:out value="${errors.eventType}"/></span></c:if>
            </div>

            <div class="mb-3">
              <label class="form-label" for="eventDate">Event date <span class="req">*</span></label>
              <input class="form-control ${not empty errors.eventDate ? 'is-invalid' : ''}" type="date" id="eventDate"
                     name="eventDate" value="<c:out value='${form.eventDate}'/>" ${locked ? 'disabled' : 'required'}>
              <c:if test="${not empty errors.eventDate}"><span class="invalid-feedback"><c:out value="${errors.eventDate}"/></span></c:if>
            </div>

            <div class="mb-3 span-2">
              <label class="form-label" for="venue">Venue <span class="req">*</span></label>
              <input class="form-control ${not empty errors.venue ? 'is-invalid' : ''}" type="text" id="venue"
                     name="venue" maxlength="150" value="<c:out value='${form.venue}'/>"
                     placeholder="e.g. Dataran Kemerdekaan" ${locked ? 'disabled' : 'required'}>
              <c:if test="${not empty errors.venue}"><span class="invalid-feedback"><c:out value="${errors.venue}"/></span></c:if>
            </div>

            <div class="mb-3">
              <label class="form-label" for="city">City <span class="req">*</span></label>
              <input class="form-control ${not empty errors.city ? 'is-invalid' : ''}" type="text" id="city"
                     name="city" maxlength="80" value="<c:out value='${form.city}'/>"
                     placeholder="Shah Alam" ${locked ? 'disabled' : 'required'}>
              <c:if test="${not empty errors.city}"><span class="invalid-feedback"><c:out value="${errors.city}"/></span></c:if>
            </div>

            <div class="mb-3">
              <label class="form-label" for="state">State <span class="req">*</span></label>
              <select class="form-select ${not empty errors.state ? 'is-invalid' : ''}" id="state" name="state" ${locked ? 'disabled' : 'required'}>
                <option value="">Select a state</option>
                <c:forTokens var="stateName" delims="|"
                             items="Johor|Kedah|Kelantan|Melaka|Negeri Sembilan|Pahang|Perak|Perlis|Penang|Sabah|Sarawak|Selangor|Terengganu|Kuala Lumpur|Labuan|Putrajaya">
                  <option value="${stateName}" <c:if test="${form.state eq stateName}">selected</c:if>>${stateName}</option>
                </c:forTokens>
              </select>
              <c:if test="${not empty errors.state}"><span class="invalid-feedback"><c:out value="${errors.state}"/></span></c:if>
            </div>

            <div class="mb-3">
              <label class="form-label" for="flagOffTime">Flag-off time</label>
              <input class="form-control ${not empty errors.flagOffTime ? 'is-invalid' : ''}" type="time" id="flagOffTime"
                     name="flagOffTime" value="<c:out value='${form.flagOffTime}'/>" ${locked ? 'disabled' : ''}>
              <c:if test="${not empty errors.flagOffTime}"><span class="invalid-feedback"><c:out value="${errors.flagOffTime}"/></span></c:if>
            </div>

            <div class="mb-3">
              <label class="form-label" for="registrationOpen">Registration opens <span class="req">*</span></label>
              <input class="form-control ${not empty errors.registrationOpen ? 'is-invalid' : ''}" type="date"
                     id="registrationOpen" name="registrationOpen" value="<c:out value='${form.registrationOpen}'/>" ${locked ? 'disabled' : 'required'}>
              <c:if test="${not empty errors.registrationOpen}"><span class="invalid-feedback"><c:out value="${errors.registrationOpen}"/></span></c:if>
            </div>

            <div class="mb-3">
              <label class="form-label" for="registrationClose">Registration closes <span class="req">*</span></label>
              <input class="form-control ${not empty errors.registrationClose ? 'is-invalid' : ''}" type="date"
                     id="registrationClose" name="registrationClose" value="<c:out value='${form.registrationClose}'/>" ${locked ? 'disabled' : 'required'}>
              <c:if test="${not empty errors.registrationClose}"><span class="invalid-feedback"><c:out value="${errors.registrationClose}"/></span></c:if>
              <div class="form-text">Closing must be on or after opening, and before race day.</div>
            </div>
          </div>

          <div class="row-flex mt-2">
            <button class="btn btn-accent" type="submit" ${locked ? 'disabled' : ''}>
              <i class="bi bi-check2"></i> ${editing ? 'Save changes' : 'Save draft'}
            </button>
            <a class="btn btn-ghost" href="${ctx}/organizer/events">Cancel</a>
          </div>
        </form>
      </div>
    </div>

    <c:choose>
      <c:when test="${editing}">
        <div class="card-panel">
          <div class="card-head">
            <div>
              <h2>Race categories</h2>
              <p class="sub">Each distance needs its own price, quota and start time.</p>
            </div>
            <button class="btn btn-ink btn-sm" type="button" data-bs-toggle="modal" data-bs-target="#categoryModal">
              <i class="bi bi-plus-lg"></i> Add category
            </button>
          </div>
          <div class="table-shell">
            <table class="data-table">
              <thead>
                <tr><th>Category</th><th>Distance</th><th class="num">Price</th><th class="num">Quota</th><th>Start</th><th class="actions">Actions</th></tr>
              </thead>
              <tbody>
                <c:forEach var="category" items="${event.categories}">
                  <tr>
                    <td>
                      <div class="cell-main">
                        <span class="avatar sm lime"><i class="bi bi-signpost-split"></i></span>
                        <span class="who"><strong><c:out value="${category.name}"/></strong><span>${category.quotaLabel} slots used</span></span>
                      </div>
                    </td>
                    <td>${category.distanceLabel}</td>
                    <td class="num">${category.priceFormatted}</td>
                    <td class="num">${category.quota}</td>
                    <td>${category.startTimeFormatted}</td>
                    <td class="actions">
                      <button class="btn btn-ghost btn-sm" type="button" data-bs-toggle="modal" data-bs-target="#categoryModal"
                              data-modal-heading="Edit race category"
                              data-form-action="${ctx}/organizer/events/${event.eventId}/categories/${category.categoryId}"
                              data-field-name="<c:out value='${category.name}'/>"
                              data-field-distanceKm="${category.distanceKm}"
                              data-field-price="${category.price}"
                              data-field-quota="${category.quota}"
                              data-field-startTime="${category.startTime}">
                        <i class="bi bi-pencil"></i> Edit
                      </button>
                      <form method="post" action="${ctx}/organizer/events/${event.eventId}/categories/${category.categoryId}/delete"
                            data-confirm="Remove the ${fn:escapeXml(category.name)} category? This cannot be undone."
                            data-confirm-title="Remove category" data-confirm-ok="Remove category">
                        <button class="btn btn-danger-soft btn-sm" type="submit" ${locked ? 'disabled' : ''}><i class="bi bi-trash3"></i></button>
                      </form>
                    </td>
                  </tr>
                </c:forEach>
                <c:if test="${empty event.categories}">
                  <tr>
                    <td colspan="6">
                      <div class="empty-state" style="padding:30px">
                        <div class="empty-icon"><i class="bi bi-signpost-split"></i></div>
                        <h3>No race categories yet</h3>
                        <p>Add at least one category so runners can choose a distance and the event can be submitted for approval.</p>
                        <button class="btn btn-ink" type="button" data-bs-toggle="modal" data-bs-target="#categoryModal">
                          <i class="bi bi-plus-lg"></i> Add first category
                        </button>
                      </div>
                    </td>
                  </tr>
                </c:if>
              </tbody>
            </table>
          </div>
        </div>
      </c:when>
      <c:otherwise>
        <div class="notice lime">
          <i class="bi bi-signpost-split"></i>
          <div><strong>Next step: race categories.</strong> Save this draft first, then add distances, prices and quotas before submitting the event for approval.</div>
        </div>
      </c:otherwise>
    </c:choose>
  </div>

  <aside class="stack">
    <c:if test="${editing}">
      <div class="card-panel">
        <div class="card-head">
          <div><h2>Workflow</h2><p class="sub">Where this event sits right now.</p></div>
        </div>
        <div class="card-body">
          <div class="info-list">
            <div class="info-row"><span class="k">Status</span><span class="v"><span class="pill pill-${event.statusBadge}">${event.statusLabel}</span></span></div>
            <div class="info-row"><span class="k">Organizer</span><span class="v"><c:out value="${event.organizerName}"/></span></div>
            <div class="info-row"><span class="k">Categories</span><span class="v">${fn:length(event.categories)}</span></div>
            <div class="info-row"><span class="k">Registered</span><span class="v">${event.registeredCount} / ${event.totalQuota}</span></div>
            <div class="info-row"><span class="k">Paid entries</span><span class="v">${event.paidCount}</span></div>
            <div class="info-row"><span class="k">Revenue</span><span class="v">${event.revenueFormatted}</span></div>
            <div class="info-row"><span class="k">Registration</span><span class="v">${event.registrationWindow}</span></div>
          </div>

          <c:if test="${event.status eq 'REJECTED' and not empty event.rejectionReason}">
            <div class="notice danger mt-3">
              <i class="bi bi-x-octagon"></i>
              <div><strong>Administrator feedback.</strong> <c:out value="${event.rejectionReason}"/></div>
            </div>
          </c:if>

          <c:if test="${event.status eq 'DRAFT' or event.status eq 'REJECTED'}">
            <form method="post" action="${ctx}/organizer/events/${event.eventId}/submit" class="mt-3"
                  data-confirm="Submit this event for administrator approval? You will not be able to edit it while it is under review."
                  data-confirm-title="Submit for approval" data-confirm-tone="primary" data-confirm-ok="Submit event">
              <button class="btn btn-accent w-100" type="submit"><i class="bi bi-send"></i> Submit for approval</button>
            </form>
          </c:if>

          <c:if test="${event.registeredCount eq 0}">
            <form method="post" action="${ctx}/organizer/events/${event.eventId}/delete" class="mt-2"
                  data-confirm="Delete this event and all of its race categories? This cannot be undone."
                  data-confirm-title="Delete event" data-confirm-ok="Delete event">
              <button class="btn btn-danger-soft w-100" type="submit"><i class="bi bi-trash3"></i> Delete event</button>
            </form>
          </c:if>
        </div>
      </div>
    </c:if>

    <div class="card-panel">
      <div class="card-head"><h2>Publishing checklist</h2></div>
      <div class="card-body">
        <div class="timeline">
          <div class="step done">
            <span class="dot"><i class="bi bi-check2"></i></span>
            <div><strong>Race details</strong><p>Name, venue, dates and description.</p></div>
          </div>
          <div class="step ${not empty event.categories ? 'done' : ''}">
            <span class="dot"><i class="bi bi-signpost-split"></i></span>
            <div><strong>Categories</strong><p>Distance, price, quota and start time.</p></div>
          </div>
          <div class="step ${event.status eq 'PENDING' or event.status eq 'APPROVED' ? 'done' : ''}">
            <span class="dot"><i class="bi bi-send"></i></span>
            <div><strong>Submit for review</strong><p>An administrator checks the event.</p></div>
          </div>
          <div class="step ${event.status eq 'APPROVED' ? 'done' : ''}">
            <span class="dot"><i class="bi bi-broadcast"></i></span>
            <div><strong>Published</strong><p>Runners can register from the catalogue.</p></div>
          </div>
        </div>
      </div>
    </div>
  </aside>
</div>

<c:if test="${editing}">
  <div class="modal fade" id="categoryModal" tabindex="-1" aria-labelledby="categoryModalTitle" aria-hidden="true"
       data-modal-default-heading="Add race category">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <form method="post" action="${ctx}/organizer/events/${event.eventId}/categories"
              data-default-action="${ctx}/organizer/events/${event.eventId}/categories" data-reset-on-close novalidate>
          <div class="modal-header">
            <h2 class="modal-title" id="categoryModalTitle" data-modal-title>Add race category</h2>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label class="form-label" for="categoryName">Category name <span class="req">*</span></label>
              <input class="form-control" type="text" id="categoryName" name="name" maxlength="60" placeholder="e.g. 10KM Open" required>
            </div>
            <div class="field-grid">
              <div class="mb-3">
                <label class="form-label" for="categoryDistance">Distance (km) <span class="req">*</span></label>
                <input class="form-control" type="number" id="categoryDistance" name="distanceKm" min="0.5" max="500" step="0.1" placeholder="10" required>
              </div>
              <div class="mb-3">
                <label class="form-label" for="categoryPrice">Entry fee (RM) <span class="req">*</span></label>
                <input class="form-control" type="number" id="categoryPrice" name="price" min="0" max="5000" step="0.01" placeholder="75.00" required>
              </div>
              <div class="mb-3">
                <label class="form-label" for="categoryQuota">Quota <span class="req">*</span></label>
                <input class="form-control" type="number" id="categoryQuota" name="quota" min="1" max="100000" step="1" placeholder="500" required>
              </div>
              <div class="mb-3">
                <label class="form-label" for="categoryStart">Start time</label>
                <input class="form-control" type="time" id="categoryStart" name="startTime">
              </div>
            </div>
            <div class="notice lime">
              <i class="bi bi-info-circle"></i>
              <div>Quota cannot be lowered below the number of active registrations. Categories with registrations cannot be removed.</div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-ghost" data-bs-dismiss="modal">Cancel</button>
            <button type="submit" class="btn btn-accent"><i class="bi bi-check2"></i> Save category</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</c:if>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
