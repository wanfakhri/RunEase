<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="page-head">
  <div>
    <span class="eyebrow">Account settings</span>
    <h1>Your RunEase profile</h1>
    <p class="page-sub">Keep your contact details current so organizers can reach you on race day.</p>
  </div>
  <div class="page-actions">
    <a class="btn btn-ghost" href="${ctx}${user.role.home}"><i class="bi bi-arrow-left"></i> Back to dashboard</a>
  </div>
</div>

<c:if test="${not empty errors.form}">
  <div class="notice danger mb-3"><i class="bi bi-x-octagon"></i><div><strong>Profile not saved.</strong> <c:out value="${errors.form}"/></div></div>
</c:if>

<div class="detail-body">
  <div class="stack">
    <div class="card-panel">
      <div class="card-head">
        <div>
          <h2>Personal details</h2>
          <p class="sub">Your name appears on race packs, bibs and results.</p>
        </div>
        <span class="pill pill-${account.statusBadge}">${account.statusLabel}</span>
      </div>
      <div class="card-body">
        <form method="post" action="${ctx}/profile" novalidate>
          <div class="field-grid">
            <div class="mb-3 span-2">
              <label class="form-label" for="fullName">Full name <span class="req">*</span></label>
              <input class="form-control ${not empty errors.fullName ? 'is-invalid' : ''}" type="text" id="fullName"
                     name="fullName" maxlength="120" value="<c:out value='${form.fullName}'/>" autocomplete="name" required>
              <c:if test="${not empty errors.fullName}"><span class="invalid-feedback"><c:out value="${errors.fullName}"/></span></c:if>
            </div>
            <div class="mb-3">
              <label class="form-label" for="email">Email address</label>
              <input class="form-control" type="email" id="email" value="<c:out value='${account.email}'/>" disabled>
              <div class="form-text">Contact an administrator to change your sign-in email.</div>
            </div>
            <div class="mb-3">
              <label class="form-label" for="phone">Phone number</label>
              <input class="form-control ${not empty errors.phone ? 'is-invalid' : ''}" type="tel" id="phone"
                     name="phone" maxlength="20" value="<c:out value='${form.phone}'/>" placeholder="012-3456789" autocomplete="tel">
              <c:if test="${not empty errors.phone}"><span class="invalid-feedback"><c:out value="${errors.phone}"/></span></c:if>
            </div>
            <div class="mb-3">
              <label class="form-label" for="gender">Gender</label>
              <select class="form-select ${not empty errors.gender ? 'is-invalid' : ''}" id="gender" name="gender">
                <option value="">Prefer not to say</option>
                <option value="MALE" <c:if test="${form.gender eq 'MALE'}">selected</c:if>>Male</option>
                <option value="FEMALE" <c:if test="${form.gender eq 'FEMALE'}">selected</c:if>>Female</option>
              </select>
              <c:if test="${not empty errors.gender}"><span class="invalid-feedback"><c:out value="${errors.gender}"/></span></c:if>
            </div>
            <div class="mb-3">
              <label class="form-label" for="dateOfBirth">Date of birth</label>
              <input class="form-control ${not empty errors.dateOfBirth ? 'is-invalid' : ''}" type="date" id="dateOfBirth"
                     name="dateOfBirth" value="<c:out value='${form.dateOfBirth}'/>" autocomplete="bday">
              <c:if test="${not empty errors.dateOfBirth}"><span class="invalid-feedback"><c:out value="${errors.dateOfBirth}"/></span></c:if>
            </div>
            <div class="mb-3">
              <label class="form-label" for="city">City</label>
              <input class="form-control ${not empty errors.city ? 'is-invalid' : ''}" type="text" id="city"
                     name="city" maxlength="80" value="<c:out value='${form.city}'/>" placeholder="Shah Alam" autocomplete="address-level2">
              <c:if test="${not empty errors.city}"><span class="invalid-feedback"><c:out value="${errors.city}"/></span></c:if>
            </div>
          </div>
          <div class="row-flex mt-2">
            <button class="btn btn-accent" type="submit"><i class="bi bi-check2"></i> Save changes</button>
            <a class="btn btn-ghost" href="${ctx}/profile">Reset</a>
          </div>
        </form>
      </div>
    </div>

    <div class="card-panel">
      <div class="card-head">
        <div>
          <h2>Change password</h2>
          <p class="sub">Use at least 8 characters with upper case, lower case and a number.</p>
        </div>
        <i class="bi bi-shield-lock muted"></i>
      </div>
      <div class="card-body">
        <form method="post" action="${ctx}/profile" novalidate>
          <div class="field-grid">
            <div class="mb-3 span-2">
              <label class="form-label" for="currentPassword">Current password</label>
              <div class="input-icon">
                <i class="bi bi-lock"></i>
                <input class="form-control has-toggle ${not empty errors.currentPassword ? 'is-invalid' : ''}" type="password"
                       id="currentPassword" name="currentPassword" autocomplete="current-password" placeholder="Your current password">
                <button class="pw-toggle" type="button" data-pw-toggle="#currentPassword" aria-label="Show password"><i class="bi bi-eye"></i></button>
              </div>
              <c:if test="${not empty errors.currentPassword}"><span class="invalid-feedback"><c:out value="${errors.currentPassword}"/></span></c:if>
            </div>
            <div class="mb-3">
              <label class="form-label" for="newPassword">New password</label>
              <div class="input-icon">
                <i class="bi bi-key"></i>
                <input class="form-control has-toggle ${not empty errors.newPassword ? 'is-invalid' : ''}" type="password"
                       id="newPassword" name="newPassword" autocomplete="new-password" placeholder="New password">
                <button class="pw-toggle" type="button" data-pw-toggle="#newPassword" aria-label="Show password"><i class="bi bi-eye"></i></button>
              </div>
              <c:if test="${not empty errors.newPassword}"><span class="invalid-feedback"><c:out value="${errors.newPassword}"/></span></c:if>
            </div>
            <div class="mb-3">
              <label class="form-label" for="confirmPassword">Confirm new password</label>
              <div class="input-icon">
                <i class="bi bi-key-fill"></i>
                <input class="form-control has-toggle ${not empty errors.confirmPassword ? 'is-invalid' : ''}" type="password"
                       id="confirmPassword" name="confirmPassword" autocomplete="new-password" placeholder="Repeat new password">
                <button class="pw-toggle" type="button" data-pw-toggle="#confirmPassword" aria-label="Show password"><i class="bi bi-eye"></i></button>
              </div>
              <c:if test="${not empty errors.confirmPassword}"><span class="invalid-feedback"><c:out value="${errors.confirmPassword}"/></span></c:if>
            </div>
          </div>
          <div class="row-flex mt-2">
            <button class="btn btn-ink" type="submit"><i class="bi bi-shield-check"></i> Update password</button>
          </div>
          <p class="form-text mt-2 mb-0">Leave the password fields blank if you only want to update your contact details.</p>
        </form>
      </div>
    </div>
  </div>

  <aside class="stack">
    <div class="card-panel">
      <div class="card-body text-center">
        <span class="avatar xl lime mx-auto">${account.initials}</span>
        <h2 class="mt-3 mb-1"><c:out value="${account.fullName}"/></h2>
        <p class="small muted mb-2"><c:out value="${account.email}"/></p>
        <span class="pill pill-dark">${account.roleLabel}</span>
      </div>
      <div class="card-body border-top" style="border-top:1px solid var(--line)">
        <div class="info-list">
          <div class="info-row"><span class="k">Member since</span><span class="v">${account.createdAtFormatted}</span></div>
          <div class="info-row"><span class="k">Last sign-in</span><span class="v">${account.lastLoginFormatted}</span></div>
          <div class="info-row"><span class="k">Account status</span><span class="v"><span class="pill pill-${account.statusBadge}">${account.statusLabel}</span></span></div>
        </div>
      </div>
    </div>

    <c:choose>
      <c:when test="${not empty organizer}">
        <div class="card-panel">
          <div class="card-head">
            <div>
              <h2>Organizer profile</h2>
              <p class="sub">Verification is managed by RunEase administrators.</p>
            </div>
            <span class="pill pill-${organizer.statusBadge}">${organizer.statusLabel}</span>
          </div>
          <div class="card-body">
            <div class="info-list">
              <div class="info-row"><span class="k">Organization</span><span class="v"><c:out value="${organizer.organizationName}"/></span></div>
              <div class="info-row"><span class="k">Registration no.</span><span class="v"><c:out value="${organizer.registrationNo}"/></span></div>
              <div class="info-row"><span class="k">Contact person</span><span class="v"><c:out value="${organizer.contactPerson}"/></span></div>
              <div class="info-row"><span class="k">Website</span><span class="v"><c:out value="${organizer.website}"/></span></div>
              <div class="info-row"><span class="k">Verified at</span><span class="v">${organizer.verifiedAtFormatted}</span></div>
              <div class="info-row"><span class="k">Events created</span><span class="v">${organizer.eventCount}</span></div>
            </div>
          </div>
        </div>
      </c:when>
      <c:otherwise>
        <div class="notice lime">
          <i class="bi bi-lightning-charge"></i>
          <div><strong>Want to organize a race?</strong> Create an organizer account from the registration page and an administrator will verify your organization.</div>
        </div>
      </c:otherwise>
    </c:choose>
  </aside>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
