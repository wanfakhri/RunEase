/* =====================================================================
   RunEase shared browser behaviour
   - CSRF token injection for every form
   - accessible confirmation dialog (Promise based)
   - sidebar / public navigation toggles
   - modal prefill helpers for create + edit forms
   - charts, meters, toasts, password toggles
   No inline scripts are used so the Content-Security-Policy stays strict.
   ===================================================================== */
(function () {
  "use strict";

  var csrf = document.querySelector('meta[name="csrf-token"]');
  var token = csrf ? csrf.getAttribute("content") : "";

  function ready(fn) {
    if (document.readyState === "loading") {
      document.addEventListener("DOMContentLoaded", fn);
    } else {
      fn();
    }
  }

  /* ------------------------------------------------------------- toasts */
  function toast(message, type) {
    var stack = document.getElementById("toastStack");
    if (!stack) {
      return;
    }
    var note = document.createElement("div");
    note.className = "toast-note" + (type === "error" ? " error" : "");
    note.setAttribute("role", "status");
    var icon = document.createElement("i");
    icon.className = "bi " + (type === "error" ? "bi-exclamation-octagon" : "bi-check-circle");
    var body = document.createElement("div");
    body.className = "flex-grow-1";
    body.textContent = message;
    var close = document.createElement("button");
    close.type = "button";
    close.className = "toast-close";
    close.setAttribute("aria-label", "Dismiss notification");
    close.innerHTML = '<i class="bi bi-x-lg"></i>';
    close.addEventListener("click", function () {
      note.remove();
    });
    note.appendChild(icon);
    note.appendChild(body);
    note.appendChild(close);
    stack.appendChild(note);
    window.setTimeout(function () {
      if (note.parentNode) {
        note.remove();
      }
    }, 5200);
  }

  /* ----------------------------------------------------- confirm dialog */
  var pendingForm = null;
  var pendingResolve = null;

  function confirmModal() {
    var el = document.getElementById("confirmModal");
    return el && window.bootstrap ? window.bootstrap.Modal.getOrCreateInstance(el) : null;
  }

  function askConfirm(options) {
    return new Promise(function (resolve) {
      var modalEl = document.getElementById("confirmModal");
      var modal = confirmModal();
      if (!modal || !modalEl) {
        resolve(window.confirm(options.message || "Are you sure?"));
        return;
      }
      pendingResolve = resolve;
      var title = document.getElementById("confirmTitle");
      var body = document.getElementById("confirmBody");
      var ok = document.getElementById("confirmOk");
      var icon = document.getElementById("confirmIcon");
      if (title) {
        title.textContent = options.title || "Please confirm";
      }
      if (body) {
        body.textContent = options.message || "Are you sure you want to continue?";
      }
      if (ok) {
        ok.textContent = options.okText || "Yes, continue";
        ok.className = "btn " + (options.tone === "primary" ? "btn-ink" : "btn-danger-soft");
      }
      if (icon) {
        icon.className = "confirm-icon" + (options.tone === "primary" ? " info" : "");
        icon.innerHTML = '<i class="bi ' + (options.tone === "primary" ? "bi-question-lg" : "bi-exclamation-triangle") + '"></i>';
      }
      modal.show();
    });
  }

  function settleConfirm(value) {
    if (pendingResolve) {
      var resolve = pendingResolve;
      pendingResolve = null;
      resolve(value);
    }
    pendingForm = null;
  }

  ready(function () {
    /* CSRF: guarantee every POST form carries the session token. */
    if (token) {
      Array.prototype.forEach.call(document.querySelectorAll("form"), function (form) {
        var method = (form.getAttribute("method") || "get").toLowerCase();
        if (method === "post" && !form.querySelector('input[name="_csrf"]')) {
          var field = document.createElement("input");
          field.type = "hidden";
          field.name = "_csrf";
          field.value = token;
          form.appendChild(field);
        }
      });
    }

    /* Intercept forms that need explicit confirmation. */
    document.addEventListener("submit", function (event) {
      var form = event.target;
      if (!(form instanceof HTMLFormElement) || !form.hasAttribute("data-confirm")) {
        return;
      }
      if (form.dataset.confirmed === "true") {
        delete form.dataset.confirmed;
        return;
      }
      event.preventDefault();
      pendingForm = form;
      askConfirm({
        title: form.getAttribute("data-confirm-title") || "Please confirm",
        message: form.getAttribute("data-confirm"),
        okText: form.getAttribute("data-confirm-ok") || "Yes, continue",
        tone: form.getAttribute("data-confirm-tone") || "danger"
      }).then(function (accepted) {
        if (accepted && pendingForm) {
          var target = pendingForm;
          pendingForm = null;
          target.dataset.confirmed = "true";
          if (target.requestSubmit) {
            target.requestSubmit();
          } else {
            target.submit();
          }
        } else {
          pendingForm = null;
        }
      });
    });

    var confirmEl = document.getElementById("confirmModal");
    if (confirmEl) {
      var ok = document.getElementById("confirmOk");
      if (ok) {
        ok.addEventListener("click", function () {
          var modal = confirmModal();
          if (modal) {
            modal.hide();
          }
          settleConfirm(true);
        });
      }
      confirmEl.addEventListener("hidden.bs.modal", function () {
        if (pendingResolve) {
          settleConfirm(false);
        }
      });
    }

    /* Sidebar (application shell). */
    var sidebarToggle = document.querySelector("[data-sidebar-toggle]");
    if (sidebarToggle) {
      sidebarToggle.addEventListener("click", function () {
        var open = document.body.classList.toggle("sidebar-open");
        sidebarToggle.setAttribute("aria-expanded", open ? "true" : "false");
      });
    }
    Array.prototype.forEach.call(document.querySelectorAll("[data-sidebar-close]"), function (scrim) {
      scrim.addEventListener("click", function () {
        document.body.classList.remove("sidebar-open");
        if (sidebarToggle) {
          sidebarToggle.setAttribute("aria-expanded", "false");
        }
      });
    });

    /* Public navigation (mobile). */
    var navToggle = document.querySelector("[data-nav-toggle]");
    if (navToggle) {
      navToggle.addEventListener("click", function () {
        var nav = document.getElementById("publicNav");
        var open = nav.classList.toggle("open");
        navToggle.setAttribute("aria-expanded", open ? "true" : "false");
      });
    }

    /* Flash dismissal. */
    Array.prototype.forEach.call(document.querySelectorAll("[data-dismiss-flash]"), function (button) {
      button.addEventListener("click", function () {
        var flash = button.closest(".flash");
        if (flash) {
          flash.remove();
        }
      });
    });

    /* Auto-submit selects and checkboxes inside filter bars. */
    Array.prototype.forEach.call(document.querySelectorAll("[data-autosubmit]"), function (field) {
      field.addEventListener("change", function () {
        var form = field.closest("form");
        if (form) {
          if (form.requestSubmit) {
            form.requestSubmit();
          } else {
            form.submit();
          }
        }
      });
    });

    /* Password reveal toggles. */
    Array.prototype.forEach.call(document.querySelectorAll("[data-pw-toggle]"), function (button) {
      button.addEventListener("click", function () {
        var input = document.querySelector(button.getAttribute("data-pw-toggle"));
        if (!input) {
          return;
        }
        var show = input.type === "password";
        input.type = show ? "text" : "password";
        button.innerHTML = '<i class="bi ' + (show ? "bi-eye-slash" : "bi-eye") + '"></i>';
        button.setAttribute("aria-label", show ? "Hide password" : "Show password");
      });
    });

    /* Print buttons. */
    Array.prototype.forEach.call(document.querySelectorAll("[data-print]"), function (button) {
      button.addEventListener("click", function () {
        window.print();
      });
    });

    /* Copy buttons. */
    Array.prototype.forEach.call(document.querySelectorAll("[data-copy]"), function (button) {
      button.addEventListener("click", function () {
        var value = button.getAttribute("data-copy");
        var done = function () {
          toast("Copied to clipboard.");
        };
        if (navigator.clipboard && navigator.clipboard.writeText) {
          navigator.clipboard.writeText(value).then(done, function () {
            toast("Copy failed. Select the text manually.", "error");
          });
        } else {
          var holder = document.createElement("textarea");
          holder.value = value;
          document.body.appendChild(holder);
          holder.select();
          document.execCommand("copy");
          holder.remove();
          done();
        }
      });
    });

    /* Clickable table rows. */
    Array.prototype.forEach.call(document.querySelectorAll("[data-href]"), function (row) {
      row.addEventListener("click", function (event) {
        if (event.target.closest("a, button, form, input, select, label")) {
          return;
        }
        window.location.href = row.getAttribute("data-href");
      });
    });

    /* Modal prefill for create / edit dialogs. */
    Array.prototype.forEach.call(document.querySelectorAll(".modal"), function (modalEl) {
      modalEl.addEventListener("show.bs.modal", function (event) {
        var trigger = event.relatedTarget;
        if (!trigger) {
          return;
        }
        var title = modalEl.querySelector("[data-modal-title]");
        if (title) {
          var heading = trigger.getAttribute("data-modal-heading")
            || modalEl.getAttribute("data-modal-default-heading");
          if (heading) {
            title.textContent = heading;
          }
        }
        var form = modalEl.querySelector("form");
        if (!form) {
          return;
        }
        if (trigger.getAttribute("data-form-action")) {
          form.setAttribute("action", trigger.getAttribute("data-form-action"));
        } else if (form.getAttribute("data-default-action")) {
          form.setAttribute("action", form.getAttribute("data-default-action"));
        }
        var data = {};
        var raw = trigger.getAttribute("data-prefill");
        if (raw) {
          try {
            data = JSON.parse(raw) || {};
          } catch (error) {
            data = {};
          }
        }
        /* data-field-<name> attributes let a trigger prefill a field without
           hand-building JSON, which keeps server-rendered values safe. */
        Array.prototype.forEach.call(trigger.attributes, function (attribute) {
          if (attribute.name.indexOf("data-field-") === 0) {
            data[attribute.name.substring("data-field-".length)] = attribute.value;
          }
        });
        Object.keys(data).forEach(function (key) {
          var field = form.querySelector('[name="' + key + '"]');
          if (!field) {
            return;
          }
          field.value = data[key] == null ? "" : data[key];
          field.dispatchEvent(new Event("change", { bubbles: true }));
        });
      });
      modalEl.addEventListener("hidden.bs.modal", function () {
        var title = modalEl.querySelector("[data-modal-title]");
        if (title && modalEl.getAttribute("data-modal-default-heading")) {
          title.textContent = modalEl.getAttribute("data-modal-default-heading");
        }
        var form = modalEl.querySelector("form");
        if (form && form.getAttribute("data-default-action")) {
          form.setAttribute("action", form.getAttribute("data-default-action"));
        }
        if (form && form.hasAttribute("data-reset-on-close")) {
          form.reset();
          Array.prototype.forEach.call(form.querySelectorAll(".is-invalid"), function (field) {
            field.classList.remove("is-invalid");
          });
          Array.prototype.forEach.call(form.querySelectorAll("[data-error-for]"), function (node) {
            node.textContent = "";
          });
        }
      });
    });

    /* Bar charts: scale each bar against the tallest value in its group. */
    Array.prototype.forEach.call(document.querySelectorAll("[data-chart]"), function (group) {
      var bars = Array.prototype.slice.call(group.querySelectorAll("[data-chart-bar]"));
      var max = 0;
      bars.forEach(function (bar) {
        max = Math.max(max, parseFloat(bar.getAttribute("data-value")) || 0);
      });
      bars.forEach(function (bar) {
        var value = parseFloat(bar.getAttribute("data-value")) || 0;
        var percent = max > 0 ? Math.max(4, Math.round((value / max) * 100)) : 4;
        bar.style.height = percent + "%";
      });
    });

    /* Meter rows: percentage widths from data attributes. */
    Array.prototype.forEach.call(document.querySelectorAll("[data-meter]"), function (meter) {
      var value = parseFloat(meter.getAttribute("data-value")) || 0;
      var max = parseFloat(meter.getAttribute("data-max")) || 100;
      var percent = max > 0 ? Math.min(100, Math.round((value / max) * 100)) : 0;
      meter.style.width = percent + "%";
    });

    /* Close mobile navigation after following a link. */
    Array.prototype.forEach.call(document.querySelectorAll(".public-nav .nav-links a"), function (link) {
      link.addEventListener("click", function () {
        var nav = document.getElementById("publicNav");
        if (nav) {
          nav.classList.remove("open");
        }
      });
    });
  });

  /* Public API used by users.js and future modules. */
  window.RunEase = {
    toast: toast,
    confirm: askConfirm,
    csrfToken: function () {
      return token;
    }
  };
})();
