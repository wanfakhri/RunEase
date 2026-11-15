<%@ include file="/WEB-INF/views/layout/header.jspf" %>
<c:set var="organizerMode" value="${mode eq 'organizer'}" />

<div class="auth-grid">
  <aside class="auth-aside">
    <a class="brand brand-on-dark" href="${ctx}/">
      <span class="brand-mark"><i class="bi bi-lightning-charge-fill"></i></span>
      <span class="brand-text"><strong>Run</strong><em>Ease</em><small>Running events</small></span>
    </a>
    <c:choose>
      <c:when test="${organizerMode}">
        <h2>Bring your race to thousands of runners.</h2>
        <p>Apply for an organizer account to publish events, manage quotas and export participant reports.</p>
        <ul class="auth-points">
          <li><i class="bi bi-check2-circle"></i><span>Structured event and category management</span></li>
          <li><i class="bi bi-check2-circle"></i><span>Participant check-in and results entry</span></li>
          <li><i class="bi bi-check2-circle"></i><span>Filterable, sortable reports with CSV export</span></li>
        </ul>
      </c:when>
      <c:otherwise>
        <h2>Your next finish line starts here.</h2>
        <p>Create a free runner account to register for races, keep your bib details and track official results.</p>
        <ul class="auth-points">
          <li><i class="bi bi-check2-circle"></i><span>Secure checkout with FPX, card or e-wallet</span></li>
          <li><i class="bi bi-check2-circle"></i><span>Registration status and bib number at a glance</span></li>
          <li><i class="bi bi-check2-circle"></i><span>Race-day announcements in one inbox</span></li>
        </ul>
      </c:otherwise>
    </c:choose>
    <div class="auth-aside-foot">RunEase &middot; CSC584 Enterprise Programming &middot; UiTM</div>
  </aside>

  <div class="auth-card">
    <h1>Create an account</h1>
    <p class="lede">Choose the account type that matches how you use RunEase.</p>

    <div class="role-toggle" role="tablist" aria-label="Account type">
      <a class="${not organizerMode ? 'active' : ''}" href="${ctx}/register?type=participant" role="tab" aria-selected="${not organizerMode}">I&rsquo;m a runner</a>
      <a class="${organizerMode ? 'active' : ''}" href="${ctx}/register?type=organizer" role="tab" aria-selected="${organizerMode}">I&rsquo;m an organizer</a>
    </div>

    <c:if test="${not empty errors.form}">
      <div class="notice danger mb-3"><i class="bi bi-x-octagon"></i><div><c:out value="${errors.form}"/></div></div>
    </c:if>

    <form method="post" action="${ctx}/register" novalidate>
      <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
      <input type="hidden" name="mode" value="${organizerMode ? 'organizer' : 'participant'}">
      <div class="field-grid">
        <div class="span-2 mb-3">
          <label class="form-label" for="fullName">Full name <span class="req">*</span></label>
          <input class="form-control ${not empty errors.fullName ? 'is-invalid' : ''}" type="text" id="fullName" name="fullName"
                 value="<c:out value='${form.fullName}'/>" placeholder="e.g. Nur Aisyah Rahman" autocomplete="name" required>
          <c:if test="${not empty errors.fullName}"><span class="invalid-feedback"><c:out value="${errors.fullName}"/></span></c:if>
        </div>

        <div class="mb-3">
          <label class="form-label" for="email">Email address <span class="req">*</span></label>
          <input class="form-control ${not empty errors.email ? 'is-invalid' : ''}" type="email" id="email" name="email"
                 value="<c:out value='${form.email}'/>" placeholder="you@example.com" autocomplete="email" required>
          <c:if test="${not empty errors.email}"><span class="invalid-feedback"><c:out value="${errors.email}"/></span></c:if>
        </div>

        <div class="mb-3">
          <label class="form-label" for="phone">Phone number <span class="req">*</span></label>
          <input class="form-control ${not empty errors.phone ? 'is-invalid' : ''}" type="tel" id="phone" name="phone"
                 value="<c:out value='${form.phone}'/>" placeholder="012-345 6789" autocomplete="tel" required>
          <c:if test="${not empty errors.phone}"><span class="invalid-feedback"><c:out value="${errors.phone}"/></span></c:if>
        </div>

        <c:choose>
          <c:when test="${organizerMode}">
            <div class="mb-3">
              <label class="form-label" for="organizationName">Organization name <span class="req">*</span></label>
              <input class="form-control ${not empty errors.organizationName ? 'is-invalid' : ''}" type="text" id="organizationName" name="organizationName"
                     value="<c:out value='${form.organizationName}'/>" placeholder="e.g. Pace Events Sdn Bhd" required>
              <c:if test="${not empty errors.organizationName}"><span class="invalid-feedback"><c:out value="${errors.organizationName}"/></span></c:if>
            </div>
            <div class="mb-3">
              <label class="form-label" for="registrationNo">SSM registration no.</label>
              <input class="form-control ${not empty errors.registrationNo ? 'is-invalid' : ''}" type="text" id="registrationNo" name="registrationNo"
                     value="<c:out value='${form.registrationNo}'/>" placeholder="Optional">
              <c:if test="${not empty errors.registrationNo}"><span class="invalid-feedback"><c:out value="${errors.registrationNo}"/></span></c:if>
            </div>
            <div class="span-2 mb-3">
              <label class="form-label" for="website">Website</label>
              <input class="form-control ${not empty errors.website ? 'is-invalid' : ''}" type="text" id="website" name="website"
                     value="<c:out value='${form.website}'/>" placeholder="https://yourclub.example">
              <c:if test="${not empty errors.website}"><span class="invalid-feedback"><c:out value="${errors.website}"/></span></c:if>
            </div>
          </c:when>
          <c:otherwise>
            <div class="mb-3">
              <label class="form-label" for="city">City</label>
              <input class="form-control ${not empty errors.city ? 'is-invalid' : ''}" type="text" id="city" name="city"
                     value="<c:out value='${form.city}'/>" placeholder="e.g. Shah Alam" autocomplete="address-level2">
              <c:if test="${not empty errors.city}"><span class="invalid-feedback"><c:out value="${errors.city}"/></span></c:if>
            </div>
          </c:otherwise>
        </c:choose>

        <div class="mb-3">
          <label class="form-label" for="password">Password <span class="req">*</span></label>
          <div class="input-icon">
            <input class="form-control has-toggle ${not empty errors.password ? 'is-invalid' : ''}" type="password" id="password" name="password"
                   placeholder="At least 8 characters" autocomplete="new-password" required>
            <button class="pw-toggle" type="button" data-pw-toggle="#password" aria-label="Show password"><i class="bi bi-eye"></i></button>
          </div>
          <span class="form-text">Upper and lower case, a number and a special character.</span>
          <c:if test="${not empty errors.password}"><span class="invalid-feedback"><c:out value="${errors.password}"/></span></c:if>
        </div>

        <div class="mb-3">
          <label class="form-label" for="confirmPassword">Confirm password <span class="req">*</span></label>
          <div class="input-icon">
            <input class="form-control has-toggle ${not empty errors.confirmPassword ? 'is-invalid' : ''}" type="password" id="confirmPassword" name="confirmPassword"
                   placeholder="Repeat your password" autocomplete="new-password" required>
            <button class="pw-toggle" type="button" data-pw-toggle="#confirmPassword" aria-label="Show password"><i class="bi bi-eye"></i></button>
          </div>
          <c:if test="${not empty errors.confirmPassword}"><span class="invalid-feedback"><c:out value="${errors.confirmPassword}"/></span></c:if>
        </div>
      </div>

      <div class="mb-3">
        <div class="form-check">
          <input class="form-check-input ${not empty errors.terms ? 'is-invalid' : ''}" type="checkbox" id="terms" name="terms" value="agree" required>
          <label class="form-check-label" for="terms">
            I agree to the RunEase terms of use and the race organizer&rsquo;s event rules.
          </label>
        </div>
        <c:if test="${not empty errors.terms}"><span class="invalid-feedback"><c:out value="${errors.terms}"/></span></c:if>
      </div>

      <c:if test="${organizerMode}">
        <div class="notice warning mb-3">
          <i class="bi bi-hourglass-split"></i>
          <div><strong>Approval required.</strong> Organizer accounts are reviewed by an administrator before you can publish events.</div>
        </div>
      </c:if>

      <button class="btn btn-accent full btn-lg" type="submit">Create account <i class="bi bi-arrow-right"></i></button>
    </form>

    <p class="auth-alt">Already have an account? <a href="${ctx}/login">Sign in</a></p>
  </div>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
