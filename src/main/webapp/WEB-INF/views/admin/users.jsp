<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="page-head">
  <div>
    <span class="eyebrow">Administration</span>
    <h1>User management</h1>
    <p class="page-sub">Create accounts, change roles and suspend access. Every action is validated on the server and written to the audit trail.</p>
  </div>
  <div class="page-actions">
    <a class="btn btn-ghost" href="${ctx}/admin"><i class="bi bi-grid-1x2"></i> Dashboard</a>
    <button class="btn btn-accent" type="button" id="newUserBtn"><i class="bi bi-person-plus"></i> New user</button>
  </div>
</div>

<div id="usersApp" data-endpoint="${ctx}/api/admin/users" data-current-user="${user.userId}">
  <form class="filter-bar" id="usersFilters" role="search" autocomplete="off">
    <div class="search-field">
      <i class="bi bi-search"></i>
      <input class="form-control" type="search" id="userSearch" name="q" placeholder="Search name, email or phone" aria-label="Search users">
    </div>
    <select class="form-select" id="userRole" name="role" aria-label="Filter by role">
      <option value="">All roles</option>
      <c:forEach var="role" items="${roles}">
        <option value="${role.name()}">${role.label}</option>
      </c:forEach>
    </select>
    <select class="form-select" id="userStatus" name="status" aria-label="Filter by status">
      <option value="">All statuses</option>
      <c:forEach var="status" items="${statuses}">
        <option value="${status.name()}">${status.label}</option>
      </c:forEach>
    </select>
    <div class="filter-actions">
      <button class="btn btn-ink" type="submit"><i class="bi bi-funnel"></i> Apply</button>
      <button class="btn btn-ghost" type="button" id="resetUserFilters">Reset</button>
    </div>
  </form>

  <div class="card-panel">
    <div class="card-head">
      <div>
        <h2>Accounts</h2>
        <p class="sub"><span id="usersCount">Loading&hellip;</span></p>
      </div>
      <span class="pill pill-lime"><i class="bi bi-bolt"></i> Live JSON API</span>
    </div>

    <div class="notice danger m-3 d-none" id="usersError" role="alert">
      <i class="bi bi-exclamation-octagon"></i>
      <div id="usersErrorText">The user list could not be loaded.</div>
    </div>

    <div class="table-shell">
      <table class="data-table" aria-describedby="usersCount">
        <thead>
          <tr>
            <th>
              <button class="sort-link border-0 bg-transparent p-0" type="button" data-sort="name" data-label="User">
                User <i class="bi bi-arrow-down-up"></i>
              </button>
            </th>
            <th>
              <button class="sort-link border-0 bg-transparent p-0" type="button" data-sort="role" data-label="Role">
                Role <i class="bi bi-arrow-down-up"></i>
              </button>
            </th>
            <th>
              <button class="sort-link border-0 bg-transparent p-0" type="button" data-sort="status" data-label="Status">
                Status <i class="bi bi-arrow-down-up"></i>
              </button>
            </th>
            <th>Contact</th>
            <th>
              <button class="sort-link border-0 bg-transparent p-0" type="button" data-sort="lastLogin" data-label="Last sign-in">
                Last sign-in <i class="bi bi-arrow-down-up"></i>
              </button>
            </th>
            <th>
              <button class="sort-link border-0 bg-transparent p-0" type="button" data-sort="created" data-label="Joined">
                Joined <i class="bi bi-arrow-down-up"></i>
              </button>
            </th>
            <th class="actions">Actions</th>
          </tr>
        </thead>
        <tbody id="usersTableBody">
          <tr>
            <td colspan="7">
              <div class="empty-state" style="padding:34px">
                <div class="spinner-border text-dark mb-2" role="status" aria-hidden="true"></div>
                <p class="mb-0">Loading users&hellip;</p>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <nav class="pager" id="usersPager" aria-label="User pagination"></nav>
  </div>

  <div class="notice lime mt-3">
    <i class="bi bi-code-slash"></i>
    <div><strong>Asynchronous CRUD.</strong> This screen loads, creates, updates, suspends and deletes accounts through <code>/api/admin/users</code> without a full page reload. The server re-validates every field and rejects unauthorised requests.</div>
  </div>
</div>

<div class="modal fade" id="userModal" tabindex="-1" aria-labelledby="userModalTitle" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered modal-lg">
    <div class="modal-content">
      <form id="userForm" novalidate>
        <div class="modal-header">
          <h2 class="modal-title" id="userModalTitle">New user</h2>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <div class="notice danger d-none mb-3" id="userFormError" role="alert">
            <i class="bi bi-exclamation-octagon"></i>
            <div id="userFormErrorText">Please review the highlighted fields.</div>
          </div>

          <div class="field-grid">
            <div class="mb-3 span-2">
              <label class="form-label" for="userFullName">Full name <span class="req">*</span></label>
              <input class="form-control" type="text" id="userFullName" name="fullName" maxlength="120" autocomplete="name" required>
              <div class="invalid-feedback" data-error-for="fullName"></div>
            </div>
            <div class="mb-3">
              <label class="form-label" for="userEmail">Email address <span class="req">*</span></label>
              <input class="form-control" type="email" id="userEmail" name="email" maxlength="150" autocomplete="email" required>
              <div class="invalid-feedback" data-error-for="email"></div>
            </div>
            <div class="mb-3">
              <label class="form-label" for="userPhone">Phone number</label>
              <input class="form-control" type="tel" id="userPhone" name="phone" maxlength="20" autocomplete="tel">
              <div class="invalid-feedback" data-error-for="phone"></div>
            </div>
            <div class="mb-3">
              <label class="form-label" for="userCity">City</label>
              <input class="form-control" type="text" id="userCity" name="city" maxlength="80" autocomplete="address-level2">
              <div class="invalid-feedback" data-error-for="city"></div>
            </div>
            <div class="mb-3">
              <label class="form-label" for="userRoleField">Role <span class="req">*</span></label>
              <select class="form-select" id="userRoleField" name="role" required>
                <c:forEach var="role" items="${roles}">
                  <option value="${role.name()}">${role.label}</option>
                </c:forEach>
              </select>
              <div class="invalid-feedback" data-error-for="role"></div>
            </div>
            <div class="mb-3">
              <label class="form-label" for="userStatusField">Account status <span class="req">*</span></label>
              <select class="form-select" id="userStatusField" name="status" required>
                <c:forEach var="status" items="${statuses}">
                  <option value="${status.name()}">${status.label}</option>
                </c:forEach>
              </select>
              <div class="invalid-feedback" data-error-for="status"></div>
            </div>
            <div class="mb-3">
              <label class="form-label" for="userPassword">Password <span class="req" id="passwordReq">*</span></label>
              <div class="input-icon">
                <i class="bi bi-lock"></i>
                <input class="form-control has-toggle" type="password" id="userPassword" name="password"
                       autocomplete="new-password" placeholder="At least 8 characters">
                <button class="pw-toggle" type="button" data-pw-toggle="#userPassword" aria-label="Show password"><i class="bi bi-eye"></i></button>
              </div>
              <div class="invalid-feedback" data-error-for="password"></div>
              <div class="form-text" id="passwordHint">Minimum 8 characters with upper case, lower case and a number.</div>
            </div>
            <div class="mb-3 d-none" id="organizationWrap">
              <label class="form-label" for="userOrganization">Organization name</label>
              <input class="form-control" type="text" id="userOrganization" name="organizationName" maxlength="150" placeholder="e.g. Pace Events Sdn Bhd">
              <div class="form-text">Only used when creating a new organizer account.</div>
            </div>
          </div>

          <div class="notice warning">
            <i class="bi bi-shield-lock"></i>
            <div>Administrators cannot remove their own admin role or deactivate their own account. Passwords are hashed with BCrypt and never returned by the API.</div>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-ghost" data-bs-dismiss="modal">Cancel</button>
          <button type="submit" class="btn btn-accent" id="userSaveBtn"><i class="bi bi-check2"></i> Create user</button>
        </div>
      </form>
    </div>
  </div>
</div>

<noscript>
  <div class="notice warning m-3">
    <i class="bi bi-exclamation-triangle"></i>
    <div>User management needs JavaScript for its asynchronous API. Enable JavaScript or manage accounts through the database console.</div>
  </div>
</noscript>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
