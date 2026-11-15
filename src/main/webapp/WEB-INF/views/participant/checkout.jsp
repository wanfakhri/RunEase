<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="page-head">
  <div>
    <span class="eyebrow">Secure checkout</span>
    <h1>Complete your entry</h1>
    <p class="page-sub">Confirm your race details, add an emergency contact and settle the entry fee.</p>
  </div>
  <div class="page-actions">
    <a class="btn btn-ghost" href="${ctx}/events/${selection.event.eventId}"><i class="bi bi-arrow-left"></i> Back to event</a>
  </div>
</div>

<c:if test="${not empty errors.form}">
  <div class="notice danger mb-3"><i class="bi bi-x-octagon"></i><div><strong>Registration not completed.</strong> <c:out value="${errors.form}"/></div></div>
</c:if>

<form method="post" action="${ctx}/checkout" novalidate>
  <input type="hidden" name="categoryId" value="${selection.category.categoryId}">

  <div class="detail-body">
    <div class="stack">
      <div class="card-panel">
        <div class="card-head">
          <div>
            <h2>Runner details</h2>
            <p class="sub">These details are printed on your race pack and kept with your entry.</p>
          </div>
          <span class="pill pill-lime">${selection.category.name}</span>
        </div>
        <div class="card-body">
          <div class="form-section-title">Race entry</div>
          <div class="field-grid">
            <div class="mb-3">
              <label class="form-label" for="jerseySize">Jersey size <span class="req">*</span></label>
              <select class="form-select ${not empty errors.jerseySize ? 'is-invalid' : ''}" id="jerseySize" name="jerseySize" required>
                <c:forEach var="size" items="${jerseySizes}">
                  <option value="${size}" <c:if test="${form.jerseySize eq size}">selected</c:if>>${size}</option>
                </c:forEach>
              </select>
              <c:if test="${not empty errors.jerseySize}"><span class="invalid-feedback"><c:out value="${errors.jerseySize}"/></span></c:if>
              <div class="form-text">Sizes follow the organizer&rsquo;s unisex technical tee chart.</div>
            </div>
            <div class="mb-3">
              <label class="form-label" for="medicalNotes">Medical notes <span class="muted">(optional)</span></label>
              <input class="form-control ${not empty errors.medicalNotes ? 'is-invalid' : ''}" type="text" id="medicalNotes"
                     name="medicalNotes" maxlength="255" value="<c:out value='${form.medicalNotes}'/>"
                     placeholder="Allergies, asthma, medication">
              <c:if test="${not empty errors.medicalNotes}"><span class="invalid-feedback"><c:out value="${errors.medicalNotes}"/></span></c:if>
            </div>
          </div>

          <div class="form-section-title">Emergency contact</div>
          <div class="field-grid">
            <div class="mb-3">
              <label class="form-label" for="emergencyName">Contact name <span class="req">*</span></label>
              <input class="form-control ${not empty errors.emergencyName ? 'is-invalid' : ''}" type="text" id="emergencyName"
                     name="emergencyName" maxlength="120" value="<c:out value='${form.emergencyName}'/>"
                     placeholder="Full name" autocomplete="name" required>
              <c:if test="${not empty errors.emergencyName}"><span class="invalid-feedback"><c:out value="${errors.emergencyName}"/></span></c:if>
            </div>
            <div class="mb-3">
              <label class="form-label" for="emergencyPhone">Contact number <span class="req">*</span></label>
              <input class="form-control ${not empty errors.emergencyPhone ? 'is-invalid' : ''}" type="tel" id="emergencyPhone"
                     name="emergencyPhone" maxlength="20" value="<c:out value='${form.emergencyPhone}'/>"
                     placeholder="012-3456789" autocomplete="tel" required>
              <c:if test="${not empty errors.emergencyPhone}"><span class="invalid-feedback"><c:out value="${errors.emergencyPhone}"/></span></c:if>
              <div class="form-text">Someone we can call on race day if needed.</div>
            </div>
          </div>

          <div class="notice lime mt-2">
            <i class="bi bi-shield-check"></i>
            <div><strong>One entry per event.</strong> Your slot is reserved as soon as you submit. Unpaid entries can be cancelled from your dashboard at any time before race day.</div>
          </div>
        </div>
      </div>

      <div class="card-panel">
        <div class="card-head">
          <div>
            <h2>Payment method</h2>
            <p class="sub">Demo payments are simulated instantly &mdash; no card details are collected.</p>
          </div>
        </div>
        <div class="card-body">
          <div class="pay-methods">
            <label class="pay-method">
              <input type="radio" name="method" value="FPX" <c:if test="${form.method eq 'FPX'}">checked</c:if>>
              <span class="pay-icon"><i class="bi bi-bank"></i></span>
              <span><strong>FPX online banking</strong><span>Maybank2u, CIMB Clicks, RHB Now and more</span></span>
              <i class="bi bi-check-circle-fill check"></i>
            </label>
            <label class="pay-method">
              <input type="radio" name="method" value="CARD" <c:if test="${form.method eq 'CARD'}">checked</c:if>>
              <span class="pay-icon"><i class="bi bi-credit-card"></i></span>
              <span><strong>Credit / debit card</strong><span>Visa and Mastercard</span></span>
              <i class="bi bi-check-circle-fill check"></i>
            </label>
            <label class="pay-method">
              <input type="radio" name="method" value="EWALLET" <c:if test="${form.method eq 'EWALLET'}">checked</c:if>>
              <span class="pay-icon"><i class="bi bi-phone"></i></span>
              <span><strong>E-wallet</strong><span>Touch &rsquo;n Go, GrabPay and Boost</span></span>
              <i class="bi bi-check-circle-fill check"></i>
            </label>
          </div>
          <c:if test="${not empty errors.method}"><span class="invalid-feedback d-block"><c:out value="${errors.method}"/></span></c:if>

          <hr class="hr">
          <div class="row-flex">
            <button class="btn btn-accent btn-lg" type="submit" name="payNow" value="1">
              <i class="bi bi-lock"></i> Pay ${selection.category.priceFormatted} now
            </button>
            <button class="btn btn-ghost btn-lg" type="submit" name="reserve" value="1">
              Reserve and pay later
            </button>
          </div>
          <p class="form-text mt-2 mb-0">Reserved entries stay pending until payment is completed. A bib number is only issued after payment.</p>
        </div>
      </div>
    </div>

    <aside class="summary-card">
      <div class="cover ${selection.event.coverClass}">
        <span class="cover-date"><strong>${selection.event.dateDay}</strong><span>${selection.event.dateMonth}</span></span>
        <span class="cover-caption"><i class="bi bi-geo-alt-fill"></i><c:out value="${selection.event.city}"/></span>
      </div>
      <div class="summary-body">
        <div class="eyebrow">Order summary</div>
        <h3 class="mb-1"><c:out value="${selection.event.title}"/></h3>
        <p class="small muted mb-3">${selection.event.eventDateFormatted} &middot; ${selection.event.flagOffFormatted} flag-off</p>

        <div class="summary-line"><span class="k">Category</span><span>${selection.category.name}</span></div>
        <div class="summary-line"><span class="k">Distance</span><span>${selection.category.distanceLabel}</span></div>
        <div class="summary-line"><span class="k">Venue</span><span class="text-end"><c:out value="${selection.event.venue}"/></span></div>
        <div class="summary-line"><span class="k">Slots left</span><span>${selection.category.slotsLeft}</span></div>

        <div class="summary-total">
          <span>Total due</span>
          <strong>${selection.category.priceFormatted}</strong>
        </div>
        <p class="tiny muted mt-2 mb-0">Entry fees are set by the organizer. RunEase does not add a booking fee.</p>
      </div>
    </aside>
  </div>
</form>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
