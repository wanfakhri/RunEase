<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<section class="section-tight">
  <div class="container">
    <div class="detail-hero">
      <div class="detail-hero-body">
        <div>
          <span class="eyebrow on-dark">${event.typeLabel} &middot; <c:out value="${event.organizerName}"/></span>
          <h1><c:out value="${event.title}"/></h1>
          <div class="hero-meta">
            <span><i class="bi bi-calendar3"></i>${event.eventDateFormatted}</span>
            <span><i class="bi bi-geo-alt"></i><c:out value="${event.location}"/></span>
            <span><i class="bi bi-flag"></i>Flag-off ${event.flagOffFormatted}</span>
          </div>
          <div class="row-flex mt-3">
            <span class="pill pill-${event.registrationStateBadge}">${event.registrationStateLabel}</span>
            <span class="pill pill-outline">${event.distanceSummary}</span>
            <span class="pill pill-outline"><i class="bi bi-people"></i> ${event.slotsLeft} slots left</span>
          </div>
        </div>
        <div class="cover ${event.coverClass}">
          <span class="cover-caption"><i class="bi bi-geo-alt-fill"></i><c:out value="${event.venue}"/></span>
        </div>
      </div>
    </div>

    <div class="detail-body">
      <div class="stack">
        <div class="card-panel">
          <div class="card-head">
            <div>
              <h2>About this race</h2>
              <p class="sub">Everything the organizer wants you to know before race day.</p>
            </div>
          </div>
          <div class="card-body">
            <div class="prose"><c:out value="${event.description}"/></div>
          </div>
        </div>

        <div class="card-panel" id="categories">
          <div class="card-head">
            <div>
              <h2>Race categories</h2>
              <p class="sub">Choose the distance that fits your training block.</p>
            </div>
            <span class="small muted">${event.registrationWindow}</span>
          </div>
          <div class="card-body">
            <c:choose>
              <c:when test="${not empty event.categories}">
                <c:forEach var="category" items="${event.categories}">
                  <div class="category-row ${category.full ? 'is-full' : ''}">
                    <div>
                      <div class="cat-name">${category.name} &middot; ${category.distanceLabel}</div>
                      <div class="cat-meta">
                        Start ${category.startTimeFormatted} &middot; ${category.quotaLabel} registered
                        <c:if test="${category.full}"> &middot; <span class="text-danger">Category full</span></c:if>
                      </div>
                    </div>
                    <div class="d-flex align-items-center gap-3">
                      <span class="cat-price">${category.priceFormatted}</span>
                      <c:choose>
                        <c:when test="${event.registrationOpen and not category.full}">
                          <c:choose>
                            <c:when test="${empty user}">
                              <a class="btn btn-accent btn-sm" href="${ctx}/login?next=${ctx}/checkout?category=${category.categoryId}">Register</a>
                            </c:when>
                            <c:when test="${user.participant and registered}">
                              <span class="pill pill-success"><i class="bi bi-check2-circle"></i> Entered</span>
                            </c:when>
                            <c:when test="${user.participant}">
                              <a class="btn btn-accent btn-sm" href="${ctx}/checkout?category=${category.categoryId}">Register</a>
                            </c:when>
                            <c:otherwise>
                              <span class="pill pill-secondary">Participant login required</span>
                            </c:otherwise>
                          </c:choose>
                        </c:when>
                        <c:otherwise>
                          <span class="pill pill-secondary">${category.full ? 'Full' : event.registrationStateLabel}</span>
                        </c:otherwise>
                      </c:choose>
                    </div>
                  </div>
                </c:forEach>
              </c:when>
              <c:otherwise>
                <p class="muted mb-0">Category details will be published soon.</p>
              </c:otherwise>
            </c:choose>
          </div>
        </div>

        <c:if test="${not empty announcements}">
          <div class="card-panel">
            <div class="card-head">
              <div>
                <h2>Race announcements</h2>
                <p class="sub">Updates published by the organizer.</p>
              </div>
            </div>
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

        <c:if test="${not empty similar}">
          <div>
            <div class="section-head mb-3">
              <div>
                <span class="eyebrow">You might also like</span>
                <h2>Similar races</h2>
              </div>
            </div>
            <div class="event-grid cols-2">
              <c:forEach var="event" items="${similar}">
                <%@ include file="/WEB-INF/views/fragments/event-card.jspf" %>
              </c:forEach>
            </div>
          </div>
        </c:if>
      </div>

      <aside class="stack">
        <div class="card-panel">
          <div class="card-body">
            <div class="eyebrow">Entry fee</div>
            <div class="d-flex align-items-end justify-content-between gap-3 mb-3">
              <div class="display" style="font-size:2rem">${event.minPriceFormatted}</div>
              <span class="pill pill-${event.registrationStateBadge}">${event.registrationStateLabel}</span>
            </div>
            <div class="progress-soft mb-2" role="presentation">
              <span style="width:${event.fillPercent}%"></span>
            </div>
            <p class="small muted mb-3">${event.paidCount} of ${event.totalQuota} slots taken &middot; ${event.slotsLeft} remaining</p>
            <c:choose>
              <c:when test="${empty user}">
                <a class="btn btn-accent full" href="${ctx}/login?next=${ctx}/events/${event.eventId}">Sign in to register</a>
                <a class="btn btn-ghost full mt-2" href="${ctx}/register">Create an account</a>
              </c:when>
              <c:when test="${user.participant and registered}">
                <a class="btn btn-ink full" href="${ctx}/registrations"><i class="bi bi-ticket-perforated"></i> View my registration</a>
              </c:when>
              <c:when test="${user.participant}">
                <a class="btn btn-accent full" href="#categories"><i class="bi bi-flag"></i> Choose a category</a>
              </c:when>
              <c:otherwise>
                <a class="btn btn-ink full" href="${ctx}${user.role.home}">Open my dashboard</a>
              </c:otherwise>
            </c:choose>
            <button class="btn btn-ghost full mt-2" type="button" data-copy="${ctx}/events/${event.eventId}">
              <i class="bi bi-link-45deg"></i> Copy race link
            </button>
          </div>
        </div>

        <div class="card-panel">
          <div class="card-head"><h2>Key details</h2></div>
          <div class="card-body">
            <div class="info-list">
              <div class="info-row"><span class="k">Date</span><span class="v">${event.shortDateFormatted}</span></div>
              <div class="info-row"><span class="k">Flag-off</span><span class="v">${event.flagOffFormatted}</span></div>
              <div class="info-row"><span class="k">Venue</span><span class="v"><c:out value="${event.venue}"/></span></div>
              <div class="info-row"><span class="k">City</span><span class="v"><c:out value="${event.location}"/></span></div>
              <div class="info-row"><span class="k">Registration</span><span class="v">${event.registrationWindow}</span></div>
              <div class="info-row"><span class="k">Organizer</span><span class="v"><c:out value="${event.organizerName}"/></span></div>
            </div>
          </div>
        </div>

        <div class="notice lime">
          <i class="bi bi-shield-check"></i>
          <div><strong>Verified organizer.</strong> This event was reviewed and approved by the RunEase administrator before publication.</div>
        </div>
      </aside>
    </div>
  </div>
</section>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
