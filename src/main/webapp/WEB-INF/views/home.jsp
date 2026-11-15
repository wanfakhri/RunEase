<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<section class="hero">
  <div class="container hero-inner">
    <div>
      <span class="eyebrow on-dark">Malaysia&rsquo;s running calendar</span>
      <h1>Every start line.<br><em>One platform.</em></h1>
      <p class="hero-lede">Discover races across the country, secure your slot in minutes and keep every bib, result and announcement in one tidy runner profile.</p>
      <div class="hero-cta">
        <a class="btn btn-accent btn-lg" href="${ctx}/events">Find your next race <i class="bi bi-arrow-right"></i></a>
        <c:choose>
          <c:when test="${empty user}">
            <a class="btn btn-outline-light btn-lg" href="${ctx}/register">Create a free account</a>
          </c:when>
          <c:otherwise>
            <a class="btn btn-outline-light btn-lg" href="${ctx}${user.role.home}">Go to my dashboard</a>
          </c:otherwise>
        </c:choose>
      </div>
      <div class="hero-stats">
        <div class="hero-stat"><strong><fmt:formatNumber value="${stats.events}" /></strong><span>Open races</span></div>
        <div class="hero-stat"><strong><fmt:formatNumber value="${stats.runners}" /></strong><span>Entries managed</span></div>
        <div class="hero-stat"><strong><fmt:formatNumber value="${stats.cities}" /></strong><span>States covered</span></div>
      </div>
    </div>

    <div>
      <c:choose>
        <c:when test="${not empty featured}">
          <c:set var="hero" value="${featured[0]}" />
          <div class="hero-card">
            <span class="hero-tag">Featured race</span>
            <div class="cover ${hero.coverClass}">
              <span class="cover-caption"><i class="bi bi-geo-alt-fill"></i><c:out value="${hero.location}"/></span>
            </div>
            <div class="hero-card-body">
              <h3><c:out value="${hero.title}"/></h3>
              <div class="hero-card-meta">
                <span><i class="bi bi-calendar3"></i>${hero.shortDateFormatted}</span>
                <span><i class="bi bi-signpost-split"></i>${hero.distanceSummary}</span>
                <span><i class="bi bi-people"></i>${hero.slotsLeft} slots left</span>
              </div>
              <div class="d-flex align-items-center justify-content-between gap-3">
                <span class="pill pill-${hero.registrationStateBadge}">${hero.registrationStateLabel}</span>
                <a class="btn btn-accent" href="${ctx}/events/${hero.eventId}">Race details <i class="bi bi-arrow-up-right"></i></a>
              </div>
            </div>
          </div>
        </c:when>
        <c:otherwise>
          <div class="hero-card">
            <div class="cover cover-1"></div>
            <div class="hero-card-body">
              <h3>The next race is being prepared</h3>
              <p class="text-white-50 mb-3">Organizers are putting the finishing touches on new events. Check the calendar soon.</p>
              <a class="btn btn-accent" href="${ctx}/events">Browse the calendar</a>
            </div>
          </div>
        </c:otherwise>
      </c:choose>
    </div>
  </div>
</section>

<div class="marquee" aria-hidden="true">
  <div class="marquee-track">
    <span>Race registration</span><span>Bib collection</span><span>Live results</span><span>Organizer reports</span>
    <span>Race registration</span><span>Bib collection</span><span>Live results</span><span>Organizer reports</span>
  </div>
</div>

<section class="section section-paper">
  <div class="container">
    <div class="section-head">
      <div>
        <span class="eyebrow">Featured races</span>
        <h2>Pick your next finish line</h2>
        <p>Curated road, trail and fun-run events with transparent entry fees and live slot availability.</p>
      </div>
      <a class="section-link" href="${ctx}/events">See all events <i class="bi bi-arrow-right"></i></a>
    </div>

    <c:choose>
      <c:when test="${not empty featured}">
        <div class="event-grid">
          <c:forEach var="event" items="${featured}">
            <%@ include file="/WEB-INF/views/fragments/event-card.jspf" %>
          </c:forEach>
        </div>
      </c:when>
      <c:otherwise>
        <div class="card-panel">
          <div class="empty-state">
            <div class="empty-icon"><i class="bi bi-flag"></i></div>
            <h3>No public events yet</h3>
            <p>Once an administrator approves an organizer&rsquo;s event it will appear here.</p>
            <a class="btn btn-ink" href="${ctx}/register?type=organizer">Apply as an organizer</a>
          </div>
        </div>
      </c:otherwise>
    </c:choose>
  </div>
</section>

<section class="section section-white" id="how-it-works">
  <div class="container">
    <div class="section-head">
      <div>
        <span class="eyebrow">How RunEase works</span>
        <h2>From discovery to the finish line</h2>
        <p>Three steps for runners. A complete control room for organizers and administrators.</p>
      </div>
    </div>
    <div class="steps">
      <div class="step">
        <h3>Find the right race</h3>
        <p>Search by state, race type or date. Compare categories, entry fees and remaining slots before you commit.</p>
      </div>
      <div class="step">
        <h3>Register and pay</h3>
        <p>Pick your category and jersey size, add an emergency contact, then pay by FPX, card or e-wallet in a single checkout.</p>
      </div>
      <div class="step">
        <h3>Race and keep the record</h3>
        <p>Your bib, check-in status, announcements and official finish time stay attached to your runner profile.</p>
      </div>
    </div>
  </div>
</section>

<section class="section section-paper">
  <div class="container">
    <div class="cta-band">
      <div>
        <span class="eyebrow on-dark">Organizers</span>
        <h2>Run the race, not the spreadsheets</h2>
        <p>Publish events, manage categories and quotas, check participants in and export registration reports from one dashboard.</p>
      </div>
      <div class="d-flex gap-2 flex-wrap">
        <a class="btn btn-accent btn-lg" href="${ctx}/register?type=organizer">Apply to organize</a>
        <a class="btn btn-outline-light btn-lg" href="${ctx}/login">Organizer sign in</a>
      </div>
    </div>
  </div>
</section>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
