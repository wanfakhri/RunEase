/* =====================================================================
   RunEase admin user management
   Asynchronous list / create / update / status change / delete against
   /api/admin/users. All rendering is done with DOM APIs so API values are
   never interpreted as HTML.
   ===================================================================== */
(function () {
  "use strict";

  var app = document.getElementById("usersApp");
  if (!app) {
    return;
  }

  var endpoint = app.getAttribute("data-endpoint");
  var currentUserId = parseInt(app.getAttribute("data-current-user") || "0", 10);
  var tbody = document.getElementById("usersTableBody");
  var pager = document.getElementById("usersPager");
  var count = document.getElementById("usersCount");
  var errorBox = document.getElementById("usersError");
  var errorText = document.getElementById("usersErrorText");
  var filters = document.getElementById("usersFilters");
  var search = document.getElementById("userSearch");
  var roleFilter = document.getElementById("userRole");
  var statusFilter = document.getElementById("userStatus");
  var resetBtn = document.getElementById("resetUserFilters");
  var newBtn = document.getElementById("newUserBtn");

  var modalEl = document.getElementById("userModal");
  var form = document.getElementById("userForm");
  var modalTitle = document.getElementById("userModalTitle");
  var saveBtn = document.getElementById("userSaveBtn");
  var formError = document.getElementById("userFormError");
  var formErrorText = document.getElementById("userFormErrorText");
  var passwordInput = document.getElementById("userPassword");
  var passwordReq = document.getElementById("passwordReq");
  var passwordHint = document.getElementById("passwordHint");
  var organizationWrap = document.getElementById("organizationWrap");
  var roleField = document.getElementById("userRoleField");

  var state = {
    page: 1,
    size: 10,
    q: "",
    role: "",
    status: "",
    sort: "created",
    dir: "desc"
  };
  var editingId = null;
  var modal = modalEl && window.bootstrap ? window.bootstrap.Modal.getOrCreateInstance(modalEl) : null;
  var searchTimer = null;

  /* ------------------------------------------------------------- helpers */
  function el(tag, className, text) {
    var node = document.createElement(tag);
    if (className) {
      node.className = className;
    }
    if (text !== undefined && text !== null) {
      node.textContent = text;
    }
    return node;
  }

  function pill(text, badge) {
    return el("span", "pill pill-" + (badge || "secondary"), text || "-");
  }

  function roleBadge(role) {
    if (role === "ADMIN") {
      return "dark";
    }
    if (role === "ORGANIZER") {
      return "primary";
    }
    return "outline";
  }

  function toast(message, type) {
    if (window.RunEase && window.RunEase.toast) {
      window.RunEase.toast(message, type);
    }
  }

  function confirmAction(options) {
    if (window.RunEase && window.RunEase.confirm) {
      return window.RunEase.confirm(options);
    }
    return Promise.resolve(window.confirm(options.message));
  }

  function showError(message) {
    if (!errorBox) {
      return;
    }
    if (message) {
      errorText.textContent = message;
      errorBox.classList.remove("d-none");
    } else {
      errorBox.classList.add("d-none");
    }
  }

  function csrf() {
    return window.RunEase && window.RunEase.csrfToken ? window.RunEase.csrfToken() : "";
  }

  function request(url, options) {
    options = options || {};
    options.credentials = "same-origin";
    options.headers = options.headers || {};
    options.headers.Accept = "application/json";
    return fetch(url, options).then(function (response) {
      return response.json().catch(function () {
        return { ok: false, message: "The server returned an unexpected response." };
      }).then(function (body) {
        return { status: response.status, body: body };
      });
    });
  }

  function queryString() {
    var params = new URLSearchParams();
    params.set("page", state.page);
    params.set("size", state.size);
    params.set("sort", state.sort);
    params.set("dir", state.dir);
    if (state.q) {
      params.set("q", state.q);
    }
    if (state.role) {
      params.set("role", state.role);
    }
    if (state.status) {
      params.set("status", state.status);
    }
    return params.toString();
  }

  /* ---------------------------------------------------------- list state */
  function loadingRow() {
    tbody.innerHTML = "";
    var tr = el("tr");
    var td = el("td");
    td.colSpan = 7;
    var wrap = el("div", "empty-state");
    wrap.style.padding = "34px";
    var spinner = el("div", "spinner-border text-dark mb-2");
    spinner.setAttribute("role", "status");
    spinner.setAttribute("aria-hidden", "true");
    wrap.appendChild(spinner);
    wrap.appendChild(el("p", "mb-0", "Loading users\u2026"));
    td.appendChild(wrap);
    tr.appendChild(td);
    tbody.appendChild(tr);
  }

  function emptyRow(message) {
    tbody.innerHTML = "";
    var tr = el("tr");
    var td = el("td");
    td.colSpan = 7;
    var wrap = el("div", "empty-state");
    var icon = el("div", "empty-icon");
    icon.innerHTML = '<i class="bi bi-person-x"></i>';
    wrap.appendChild(icon);
    wrap.appendChild(el("h3", null, "No accounts found"));
    wrap.appendChild(el("p", null, message || "Try a different search term, role or status."));
    var reset = el("button", "btn btn-ink", "Clear filters");
    reset.type = "button";
    reset.addEventListener("click", resetFilters);
    wrap.appendChild(reset);
    td.appendChild(wrap);
    tr.appendChild(td);
    tbody.appendChild(tr);
  }

  function actionButton(label, className, icon, handler) {
    var button = el("button", "btn " + className + " btn-sm");
    button.type = "button";
    if (icon) {
      var i = el("i", "bi " + icon);
      button.appendChild(i);
      button.appendChild(document.createTextNode(" " + label));
    } else {
      button.textContent = label;
    }
    button.addEventListener("click", handler);
    return button;
  }

  function renderRow(user) {
    var tr = el("tr");

    var userCell = el("td");
    var main = el("div", "cell-main");
    main.appendChild(el("span", "avatar sm", user.initials));
    var who = el("span", "who");
    who.appendChild(el("strong", null, user.fullName));
    who.appendChild(el("span", null, user.email));
    main.appendChild(who);
    userCell.appendChild(main);
    tr.appendChild(userCell);

    var roleCell = el("td");
    roleCell.appendChild(pill(user.roleLabel, roleBadge(user.role)));
    tr.appendChild(roleCell);

    var statusCell = el("td");
    statusCell.appendChild(pill(user.statusLabel, user.statusBadge));
    tr.appendChild(statusCell);

    var contactCell = el("td");
    contactCell.appendChild(el("span", "cell-strong", user.phone || "\u2014"));
    contactCell.appendChild(el("div", "cell-sub", user.city || "No city recorded"));
    tr.appendChild(contactCell);

    var loginCell = el("td", "small muted", user.lastLogin || "Never");
    tr.appendChild(loginCell);

    var joinedCell = el("td", "small muted", user.createdAt || "-");
    tr.appendChild(joinedCell);

    var actions = el("td", "actions");
    actions.appendChild(actionButton("Edit", "btn-ghost", "bi-pencil", function () {
      openEdit(user);
    }));

    var isSelf = currentUserId && user.userId === currentUserId;
    if (!isSelf) {
      var nextStatus = user.status === "ACTIVE" ? "SUSPENDED" : "ACTIVE";
      actions.appendChild(actionButton(
        user.status === "ACTIVE" ? "Suspend" : "Activate",
        user.status === "ACTIVE" ? "btn-danger-soft" : "btn-ghost",
        user.status === "ACTIVE" ? "bi-slash-circle" : "bi-check2-circle",
        function () {
          changeStatus(user, nextStatus);
        }
      ));
      actions.appendChild(actionButton("Delete", "btn-danger-soft", "bi-trash3", function () {
        removeUser(user);
      }));
    } else {
      actions.appendChild(el("span", "pill pill-lime", "You"));
    }
    tr.appendChild(actions);
    return tr;
  }

  function render(data) {
    tbody.innerHTML = "";
    if (!data.items || data.items.length === 0) {
      emptyRow();
    } else {
      data.items.forEach(function (user) {
        tbody.appendChild(renderRow(user));
      });
    }
    count.textContent = data.total + " account" + (data.total === 1 ? "" : "s") +
      " \u00b7 page " + data.page + " of " + data.totalPages;
    renderPager(data);
    updateSortIndicators();
  }

  function pageLink(label, targetPage, disabled, active, ariaLabel) {
    var li = el("li");
    var a = el("a", (active ? "active " : "") + (disabled ? "disabled" : ""), label);
    a.href = "#";
    if (ariaLabel) {
      a.setAttribute("aria-label", ariaLabel);
    }
    if (active) {
      a.setAttribute("aria-current", "page");
    }
    if (!disabled && !active) {
      a.addEventListener("click", function (event) {
        event.preventDefault();
        goTo(targetPage);
      });
    } else {
      a.addEventListener("click", function (event) {
        event.preventDefault();
      });
    }
    li.appendChild(a);
    return li;
  }

  function renderPager(data) {
    pager.innerHTML = "";
    var summary = el("div", "pager-summary");
    var start = data.total === 0 ? 0 : (data.page - 1) * data.size + 1;
    var end = Math.min(data.page * data.size, data.total);
    summary.innerHTML = "Showing <strong>" + start + "\u2013" + end + "</strong> of <strong>" +
      data.total + "</strong>";
    pager.appendChild(summary);

    var list = el("ul", "pager-list");
    var prev = el("li");
    var prevLink = el("a", data.page <= 1 ? "disabled" : "", "");
    prevLink.href = "#";
    prevLink.setAttribute("aria-label", "Previous page");
    prevLink.innerHTML = '<i class="bi bi-chevron-left"></i>';
    prevLink.addEventListener("click", function (event) {
      event.preventDefault();
      if (data.page > 1) {
        goTo(data.page - 1);
      }
    });
    prev.appendChild(prevLink);
    list.appendChild(prev);

    var startPage = Math.max(1, data.page - 2);
    var endPage = Math.min(data.totalPages, startPage + 4);
    startPage = Math.max(1, endPage - 4);
    for (var n = startPage; n <= endPage; n++) {
      list.appendChild(pageLink(String(n), n, false, n === data.page));
    }

    var next = el("li");
    var nextLink = el("a", data.page >= data.totalPages ? "disabled" : "", "");
    nextLink.href = "#";
    nextLink.setAttribute("aria-label", "Next page");
    nextLink.innerHTML = '<i class="bi bi-chevron-right"></i>';
    nextLink.addEventListener("click", function (event) {
      event.preventDefault();
      if (data.page < data.totalPages) {
        goTo(data.page + 1);
      }
    });
    next.appendChild(nextLink);
    list.appendChild(next);

    pager.appendChild(list);
  }

  function goTo(page) {
    state.page = Math.max(1, page);
    load();
  }

  function updateSortIndicators() {
    Array.prototype.forEach.call(document.querySelectorAll("[data-sort]"), function (button) {
      var active = button.getAttribute("data-sort") === state.sort;
      var label = button.getAttribute("data-label") || "";
      var icon = active
        ? (state.dir === "asc" ? "bi-sort-alpha-down" : "bi-sort-alpha-up")
        : "bi-arrow-down-up";
      button.innerHTML = "";
      button.appendChild(document.createTextNode(label + " "));
      button.appendChild(el("i", "bi " + icon));
      button.style.color = active ? "var(--ink)" : "";
    });
  }

  /* --------------------------------------------------------------- load */
  function load() {
    showError(null);
    loadingRow();
    request(endpoint + "?" + queryString()).then(function (result) {
      if (result.status === 401) {
        showError("Your session has expired. Reload the page and sign in again.");
        tbody.innerHTML = "";
        return;
      }
      if (result.status === 403) {
        showError("You are not authorised to manage users.");
        tbody.innerHTML = "";
        return;
      }
      if (!result.body || !result.body.ok) {
        showError((result.body && result.body.message) || "The user list could not be loaded.");
        tbody.innerHTML = "";
        return;
      }
      render(result.body.data);
    }).catch(function () {
      showError("The user list could not be loaded. Check your connection and try again.");
      tbody.innerHTML = "";
    });
  }

  /* --------------------------------------------------------------- form */
  function clearFormErrors() {
    formError.classList.add("d-none");
    Array.prototype.forEach.call(form.querySelectorAll(".is-invalid"), function (field) {
      field.classList.remove("is-invalid");
    });
    Array.prototype.forEach.call(form.querySelectorAll("[data-error-for]"), function (node) {
      node.textContent = "";
    });
  }

  function showFormErrors(message, errors) {
    if (message) {
      formErrorText.textContent = message;
      formError.classList.remove("d-none");
    }
    Object.keys(errors || {}).forEach(function (name) {
      var field = form.querySelector('[name="' + name + '"]');
      if (field) {
        field.classList.add("is-invalid");
      }
      var node = form.querySelector('[data-error-for="' + name + '"]');
      if (node) {
        node.textContent = errors[name];
      }
    });
  }

  function syncOrganizationField() {
    var isOrganizer = roleField.value === "ORGANIZER";
    organizationWrap.classList.toggle("d-none", !(isOrganizer && !editingId));
  }

  function openCreate() {
    editingId = null;
    clearFormErrors();
    form.reset();
    modalTitle.textContent = "New user";
    saveBtn.innerHTML = "";
    saveBtn.appendChild(el("i", "bi bi-person-plus"));
    saveBtn.appendChild(document.createTextNode(" Create user"));
    passwordReq.classList.remove("d-none");
    passwordHint.textContent = "Minimum 8 characters with upper case, lower case and a number.";
    passwordInput.setAttribute("placeholder", "At least 8 characters");
    passwordInput.setAttribute("required", "required");
    syncOrganizationField();
    if (modal) {
      modal.show();
    }
  }

  function openEdit(user) {
    editingId = user.userId;
    clearFormErrors();
    form.reset();
    form.elements.fullName.value = user.fullName || "";
    form.elements.email.value = user.email || "";
    form.elements.phone.value = user.phone || "";
    form.elements.city.value = user.city || "";
    form.elements.role.value = user.role || "PARTICIPANT";
    form.elements.status.value = user.status || "ACTIVE";
    form.elements.password.value = "";
    modalTitle.textContent = "Edit " + (user.fullName || "user");
    saveBtn.innerHTML = "";
    saveBtn.appendChild(el("i", "bi bi-check2"));
    saveBtn.appendChild(document.createTextNode(" Save changes"));
    passwordReq.classList.add("d-none");
    passwordHint.textContent = "Leave blank to keep the current password.";
    passwordInput.setAttribute("placeholder", "Leave blank to keep current password");
    passwordInput.removeAttribute("required");
    syncOrganizationField();
    if (modal) {
      modal.show();
    }
  }

  function submitForm(event) {
    event.preventDefault();
    clearFormErrors();

    var payload = {
      fullName: form.elements.fullName.value.trim(),
      email: form.elements.email.value.trim(),
      phone: form.elements.phone.value.trim(),
      city: form.elements.city.value.trim(),
      role: form.elements.role.value,
      status: form.elements.status.value
    };
    if (form.elements.password.value.trim()) {
      payload.password = form.elements.password.value;
    }
    if (!editingId && payload.role === "ORGANIZER") {
      payload.organizationName = form.elements.organizationName.value.trim();
    }

    var url = editingId ? endpoint + "/" + editingId : endpoint;
    var method = editingId ? "PUT" : "POST";
    var original = saveBtn.innerHTML;
    saveBtn.disabled = true;
    saveBtn.innerHTML = "";
    var spinner = el("span", "spinner-border spinner-border-sm me-2");
    spinner.setAttribute("role", "status");
    spinner.setAttribute("aria-hidden", "true");
    saveBtn.appendChild(spinner);
    saveBtn.appendChild(document.createTextNode("Saving\u2026"));

    request(url, {
      method: method,
      headers: {
        "Content-Type": "application/json",
        "X-CSRF-Token": csrf()
      },
      body: JSON.stringify(payload)
    }).then(function (result) {
      saveBtn.disabled = false;
      saveBtn.innerHTML = original;
      if (result.body && result.body.ok) {
        if (modal) {
          modal.hide();
        }
        toast(result.body.message || (editingId ? "User updated." : "User created."));
        if (!editingId) {
          state.page = 1;
        }
        load();
        return;
      }
      if (result.status === 401) {
        showFormErrors("Your session has expired. Reload the page and sign in again.", {});
        return;
      }
      showFormErrors((result.body && result.body.message) || "The account could not be saved.",
        result.body && result.body.errors);
    }).catch(function () {
      saveBtn.disabled = false;
      saveBtn.innerHTML = original;
      showFormErrors("The account could not be saved. Check your connection and try again.", {});
    });
  }

  /* ------------------------------------------------------ status / delete */
  function changeStatus(user, targetStatus) {
    var label = targetStatus === "ACTIVE" ? "Activate" : "Suspend";
    confirmAction({
      title: label + " account",
      message: label + " " + user.fullName + " (" + user.email + ")? " +
        (targetStatus === "ACTIVE" ? "They will be able to sign in again." : "They will be signed out and blocked immediately."),
      okText: label + " account",
      tone: targetStatus === "ACTIVE" ? "primary" : "danger"
    }).then(function (accepted) {
      if (!accepted) {
        return;
      }
      request(endpoint + "/" + user.userId + "/status", {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          "X-CSRF-Token": csrf()
        },
        body: JSON.stringify({ status: targetStatus })
      }).then(function (result) {
        if (result.body && result.body.ok) {
          toast(result.body.message || ("Account " + label.toLowerCase() + "d."));
          load();
        } else {
          toast((result.body && result.body.message) || "The status could not be changed.", "error");
        }
      }).catch(function () {
        toast("The status could not be changed. Try again.", "error");
      });
    });
  }

  function removeUser(user) {
    confirmAction({
      title: "Delete account",
      message: "Delete " + user.fullName + " (" + user.email + ")? The account is soft-deleted and the action is written to the audit trail.",
      okText: "Delete account",
      tone: "danger"
    }).then(function (accepted) {
      if (!accepted) {
        return;
      }
      request(endpoint + "/" + user.userId, {
        method: "DELETE",
        headers: { "X-CSRF-Token": csrf() }
      }).then(function (result) {
        if (result.body && result.body.ok) {
          toast(result.body.message || "User deleted.");
          load();
        } else {
          toast((result.body && result.body.message) || "The account could not be deleted.", "error");
        }
      }).catch(function () {
        toast("The account could not be deleted. Try again.", "error");
      });
    });
  }

  /* -------------------------------------------------------------- wiring */
  function resetFilters() {
    state.q = "";
    state.role = "";
    state.status = "";
    state.page = 1;
    search.value = "";
    roleFilter.value = "";
    statusFilter.value = "";
    load();
  }

  filters.addEventListener("submit", function (event) {
    event.preventDefault();
    state.q = search.value.trim();
    state.role = roleFilter.value;
    state.status = statusFilter.value;
    state.page = 1;
    load();
  });

  roleFilter.addEventListener("change", function () {
    state.role = roleFilter.value;
    state.page = 1;
    load();
  });

  statusFilter.addEventListener("change", function () {
    state.status = statusFilter.value;
    state.page = 1;
    load();
  });

  search.addEventListener("input", function () {
    window.clearTimeout(searchTimer);
    searchTimer = window.setTimeout(function () {
      state.q = search.value.trim();
      state.page = 1;
      load();
    }, 350);
  });

  resetBtn.addEventListener("click", resetFilters);
  newBtn.addEventListener("click", openCreate);

  Array.prototype.forEach.call(document.querySelectorAll("[data-sort]"), function (button) {
    button.addEventListener("click", function () {
      var field = button.getAttribute("data-sort");
      if (state.sort === field) {
        state.dir = state.dir === "asc" ? "desc" : "asc";
      } else {
        state.sort = field;
        state.dir = "asc";
      }
      state.page = 1;
      load();
    });
  });

  roleField.addEventListener("change", syncOrganizationField);
  form.addEventListener("submit", submitForm);

  load();
})();
