<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="page-head">
  <div>
    <span class="eyebrow">Registration ${registration.registrationCode}</span>
    <h1><c:out value="${registration.eventTitle}"/></h1>
    <p class="page-sub">${registration.categoryName} &middot; ${registration.eventDateFormatted} &middot; <c:out value="${registration.venue}"/>, <c:out value="${registration.eventCity}"/></p>
  </div>
  <div class="page-actions">
    <a class="btn btn-ghost" href="${ctx}/registrations"><i class="bi bi-arrow-left"></i> All registrations</a>
    <button class="btn btn-soft" type="button" data-print><i class="bi bi-printer"></i> Print / save PDF</button>
  </div>
</div>

<div class="detail-body">
  <div class="stack">
    <div class="card-panel">
      <div class="card-head">
        <div>
          <h2>Entry summary</h2>
          <p class="sub">Keep this reference for bib collection and race-day queries.</p>
        </div>
        <span class="pill pill-${registration.statusBadge}">${registration.statusLabel}</span>
      </div>
      <div class="card-body">
        <div class="spec-grid">
          <div class="spec"><div class="k">Registration code</div><div class="v mono">${registration.registrationCode}</div></div>
          <div class="spec"><div class="k">Category</div><div class="v">${registration.categoryName}</div></div>
          <div class="spec"><div class="k">Distance</div><div class="v">${registration.distanceLabel}</div></div>
          <div class="spec"><div class="k">Event date</div><div class="v">${registration.eventDateFormatted}</div></div>
          <div class="spec"><div class="k">Jersey size</div><div class="v">${registration.jerseySize}</div></div>
          <div class="spec"><div class="k">Bib number</div><div class="v mono">${registration.bibDisplay}</div></div>
        </div>

        <hr class="hr">
        <div class="info-list">
          <div class="info-row"><span class="k">Participant</span><span class="v"><c:out value="${registration.participantName}"/></span></div>
          <div class="info-row"><span class="k">Email</span><span class="v"><c:out value="${registration.participantEmail}"/></span></div>
          <div class="info-row"><span class="k">Phone</span><span class="v"><c:out value="${registration.participantPhone}"/></span></div>
          <div class="info-row"><span class="k">Emergency contact</span><span class="v"><c:out value="${registration.emergencyName}"/> &middot; <c:out value="${registration.emergencyPhone}"/></span></div>
          <div class="info-row"><span class="k">Registered at</span><span class="v">${registration.registeredAtFormatted}</span></div>
          <div class="info-row"><span class="k">Checked in at</span><span class="v">${registration.checkedInAtFormatted}</span></div>
        </div>
        <c:if test="${not empty registration.medicalNotes}">
          <div class="notice warning mt-3"><i class="bi bi-heart-pulse"></i><div><strong>Medical notes:</strong> <c:out value="${registration.medicalNotes}"/></div></div>
        </c:if>
      </div>
    </div>

    <div class="card-panel">
      <div class="card-head">
        <div>
          <h2>Payment</h2>
          <p class="sub">Entry fee and transaction record.</p>
        </div>
        <span class="display" style="font-size:1.3rem">${registration.amountFormatted}</span>
      </div>
      <div class="card-body">
        <c:choose>
          <c:when test="${not empty registration.payment}">
            <div class="info-list">
              <div class="info-row"><span class="k">Reference</span><span class="v mono">${registration.payment.referenceNo}</span></div>
              <div class="info-row"><span class="k">Method</span><span class="v">${registration.payment.methodLabel}</span></div>
              <div class="info-row"><span class="k">Status</span><span class="v"><span class="pill pill-${registration.payment.statusBadge}">${registration.payment.statusLabel}</span></span></div>
              <div class="info-row"><span class="k">Paid at</span><span class="v">${registration.payment.paidAtFormatted}</span></div>
            </div>
          </c:when>
          <c:otherwise>
            <div class="notice warning">
              <i class="bi bi-exclamation-triangle"></i>
              <div><strong>Payment required.</strong> Your slot is reserved but not yet confirmed. Complete payment below to receive a bib number.</div>
            </div>
          </c:otherwise>
        </c:choose>

        <c:if test="${registration.statusKey eq 'PENDING_PAYMENT'}">
          <hr class="hr">
          <form method="post" action="${ctx}/registrations/${registration.registrationId}">
            <input type="hidden" name="action" value="pay">
            <div class="row-flex align-items-end">
              <div class="flex-grow-1" style="min-width:220px">
                <label class="form-label" for="method">Payment method</label>
                <select class="form-select" id="method" name="method" required>
                  <option value="FPX">FPX online banking</option>
                  <option value="CARD">Credit / debit card</option>
                  <option value="EWALLET">E-wallet</option>
                </select>
              </div>
              <button class="btn btn-accent" type="submit"><i class="bi bi-credit-card"></i> Pay ${registration.amountFormatted}</button>
            </div>
            <p class="form-text mt-2 mb-0">Demo environment: payments are simulated and no card details are collected.</p>
          </form>
        </c:if>
      </div>
    </div>

    <c:if test="${not empty registration.result}">
      <div class="card-panel">
        <div class="card-head">
          <div>
            <h2>Official result</h2>
            <p class="sub">Recorded by the event organizer.</p>
          </div>
          <span class="pill pill-primary">${registration.result.finishTimeFormatted}</span>
        </div>
        <div class="card-body">
          <div class="spec-grid">
            <div class="spec"><div class="k">Finish time</div><div class="v">${registration.result.finishTimeFormatted}</div></div>
            <div class="spec"><div class="k">Overall position</div><div class="v">${registration.result.overallPositionLabel}</div></div>
            <div class="spec"><div class="k">Category position</div><div class="v">${registration.result.categoryPositionLabel}</div></div>
            <div class="spec"><div class="k">Average pace</div><div class="v">${registration.result.paceFormatted}</div></div>
            <div class="spec"><div class="k">Recorded at</div><div class="v">${registration.result.recordedAtFormatted}</div></div>
          </div>
        </div>
      </div>
    </c:if>

    <c:if test="${not empty announcements}">
      <div class="card-panel">
        <div class="card-head"><h2>Race announcements</h2></div>
        <div class="card-body">
          <c:forEach var="announcement" items="${announcements}">
            <div class="announcement">
              <h4><c:out value="${announcement.title}"/></h4>
              <p><c:out value="${announcement.body}"/></p>
              <div class="meta">${announcement.publishedAtFormatted} &middot; ${announcement.audienceLabel}</div>
            </div>
          </c:forEach>
        </div>
      </div>
    </c:if>
  </div>

  <aside class="stack">
    <div class="bib-card">
      <div class="bib-label">RunEase race bib</div>
      <div class="bib-number">${registration.bibDisplay}</div>
      <div class="small" style="color:rgba(255,255,255,.6)"><c:out value="${registration.eventTitle}"/></div>
      <div class="bib-grid">
        <div><div class="k">Runner</div><div class="v"><c:out value="${registration.participantName}"/></div></div>
        <div><div class="k">Category</div><div class="v">${registration.categoryName}</div></div>
        <div><div class="k">Date</div><div class="v">${registration.eventDateFormatted}</div></div>
        <div><div class="k">Jersey</div><div class="v">${registration.jerseySize}</div></div>
      </div>
      <div class="barcode" aria-hidden="true"></div>
      <button class="btn btn-outline-light full mt-3" type="button" data-copy="${registration.registrationCode}">
        <i class="bi bi-clipboard"></i> Copy registration code
      </button>
    </div>

    <div class="card-panel">
      <div class="card-head"><h2>Entry progress</h2></div>
      <div class="card-body">
        <ul class="timeline">
          <li class="done"><strong>Registered</strong><span>${registration.registeredAtFormatted}</span></li>
          <li class="${registration.confirmed ? 'done' : 'current'}"><strong>Payment confirmed</strong>
            <span><c:choose><c:when test="${registration.confirmed}">${registration.payment.paidAtFormatted}</c:when><c:otherwise>Awaiting payment</c:otherwise></c:choose></span>
          </li>
          <li class="${registration.statusKey eq 'CHECKED_IN' or registration.statusKey eq 'FINISHED' ? 'done' : ''}"><strong>Race-day check-in</strong><span>${registration.checkedInAtFormatted}</span></li>
          <li class="${not empty registration.result ? 'done' : ''}"><strong>Official result</strong><span>${empty registration.result ? 'Published after the race' : registration.result.finishTimeFormatted}</span></li>
        </ul>
      </div>
    </div>

    <c:if test="${registration.cancellable}">
      <div class="card-panel">
        <div class="card-head"><h2>Cancel entry</h2></div>
        <div class="card-body">
          <p class="small muted">Cancelling releases your slot. Any completed payment is recorded as refunded.</p>
          <form method="post" action="${ctx}/registrations/${registration.registrationId}"
                data-confirm="Cancel this registration? Your slot will be released and this cannot be undone."
                data-confirm-title="Cancel registration" data-confirm-ok="Yes, cancel entry">
            <input type="hidden" name="action" value="cancel">
            <button class="btn btn-danger-soft full" type="submit"><i class="bi bi-x-circle"></i> Cancel my entry</button>
          </form>
        </div>
      </div>
    </c:if>
  </aside>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
