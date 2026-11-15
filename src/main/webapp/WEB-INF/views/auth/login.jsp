<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="auth-grid">
  <aside class="auth-aside">
    <a class="brand brand-on-dark" href="${ctx}/">
      <span class="brand-mark"><i class="bi bi-lightning-charge-fill"></i></span>
      <span class="brand-text"><strong>Run</strong><em>Ease</em><small>Running events</small></span>
    </a>
    <h2>Welcome back to the start line.</h2>
    <p>One account keeps your registrations, bib numbers, announcements and official results together.</p>
    <ul class="auth-points">
      <li><i class="bi bi-check2-circle"></i><span>Track every entry from payment to finish time</span></li>
      <li><i class="bi bi-check2-circle"></i><span>Secure, role-based access for runners, organizers and admins</span></li>
      <li><i class="bi bi-check2-circle"></i><span>Live announcements from your race organizer</span></li>
    </ul>
    <div class="auth-aside-foot">RunEase &middot; CSC584 Enterprise Programming &middot; UiTM</div>
  </aside>

  <div class="auth-card">
    <h1>Sign in</h1>
    <p class="lede">Use your RunEase email and password.</p>

    <c:if test="${param.logout eq '1'}">
      <div class="notice lime mb-3"><i class="bi bi-check2-circle"></i><div><strong>You have signed out.</strong> Your session was ended safely.</div></div>
    </c:if>
    <c:if test="${param.expired eq '1'}">
      <div class="notice warning mb-3"><i class="bi bi-exclamation-triangle"></i><div><strong>Session ended.</strong> This account is no longer active. Please contact the administrator.</div></div>
    </c:if>
    <c:if test="${not empty loginError}">
      <div class="notice danger mb-3"><i class="bi bi-x-octagon"></i><div><strong>Sign-in failed.</strong> <c:out value="${loginError}"/></div></div>
    </c:if>

    <form method="post" action="${ctx}/login" novalidate>
      <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
      <input type="hidden" name="next" value="<c:out value='${param.next}'/>">
      <div class="mb-3">
        <label class="form-label" for="email">Email address <span class="req">*</span></label>
        <div class="input-icon">
          <i class="bi bi-envelope"></i>
          <input class="form-control ${not empty errors.email ? 'is-invalid' : ''}" type="email" id="email" name="email"
                 value="<c:out value='${empty form.email ? prefillEmail : form.email}'/>" placeholder="you@example.com"
                 autocomplete="email" required>
        </div>
        <c:if test="${not empty errors.email}"><span class="invalid-feedback"><c:out value="${errors.email}"/></span></c:if>
      </div>
      <div class="mb-3">
        <div class="d-flex justify-content-between">
          <label class="form-label" for="password">Password <span class="req">*</span></label>
          <span class="form-text">Minimum 8 characters</span>
        </div>
        <div class="input-icon">
          <i class="bi bi-lock"></i>
          <input class="form-control has-toggle ${not empty errors.password ? 'is-invalid' : ''}" type="password" id="password" name="password"
                 placeholder="Enter your password" autocomplete="current-password" required>
          <button class="pw-toggle" type="button" data-pw-toggle="#password" aria-label="Show password"><i class="bi bi-eye"></i></button>
        </div>
        <c:if test="${not empty errors.password}"><span class="invalid-feedback"><c:out value="${errors.password}"/></span></c:if>
      </div>

      <button class="btn btn-accent full btn-lg" type="submit">Sign in <i class="bi bi-arrow-right"></i></button>
    </form>

    <div class="demo-box">
      <strong>Demo accounts &middot; password <code>Password123!</code></strong>
      <div class="demo-row"><span>Administrator</span><code>admin@runease.my</code></div>
      <div class="demo-row"><span>Organizer</span><code>ahmad@paceevents.my</code></div>
      <div class="demo-row"><span>Participant</span><code>alia@student.uitm.edu.my</code></div>
    </div>

    <p class="auth-alt">New to RunEase? <a href="${ctx}/register">Create an account</a></p>
  </div>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
