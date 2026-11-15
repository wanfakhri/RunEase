<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<section class="section-tight">
  <div class="container">
    <div class="page-head">
      <div>
        <span class="eyebrow">Race calendar</span>
        <h1>Find your next start line</h1>
        <p class="page-sub">Browse approved events across Malaysia. Filter by state, race type or registration window.</p>
      </div>
      <div class="page-actions">
        <c:if test="${not empty user and user.participant}">
          <a class="btn btn-ghost" href="${ctx}/registrations"><i class="bi bi-ticket-perforated"></i> My registrations</a>
        </c:if>
      </div>
    </div>

    <form class="filter-bar" method="get" action="${ctx}/events" role="search">
      <div class="search-field">
        <i class="bi bi-search"></i>
        <input class="form-control" type="search" name="q" value="<c:out value='${filters.q}'/>" placeholder="Search race name, city or venue" aria-label="Search events">
      </div>
      <select class="form-select" name="type" aria-label="Race type" data-autosubmit>
        <option value="">All race types</option>
        <c:forEach var="type" items="${types}">
          <option value="${type.name()}" <c:if test="${filters.type eq type.name()}">selected</c:if>>${type.label}</option>
        </c:forEach>
      </select>
      <select class="form-select" name="state" aria-label="State" data-autosubmit>
        <option value="">All states</option>
        <c:forEach var="state" items="${states}">
          <option value="<c:out value='${state}'/>" <c:if test="${filters.state eq state}">selected</c:if>><c:out value="${state}"/></option>
        </c:forEach>
      </select>
      <select class="form-select" name="when" aria-label="Date range" data-autosubmit>
        <option value="upcoming" <c:if test="${filters.when eq 'upcoming'}">selected</c:if>>Upcoming only</option>
        <option value="all" <c:if test="${filters.when eq 'all'}">selected</c:if>>All events</option>
      </select>
      <select class="form-select" name="sort" aria-label="Sort events" data-autosubmit>
        <option value="date" <c:if test="${pager.sort eq 'date'}">selected</c:if>>Event date</option>
        <option value="title" <c:if test="${pager.sort eq 'title'}">selected</c:if>>Race name</option>
        <option value="price" <c:if test="${pager.sort eq 'price'}">selected</c:if>>Entry fee</option>
      </select>
      <select class="form-select" name="dir" aria-label="Sort direction" data-autosubmit>
        <option value="asc" <c:if test="${pager.direction eq 'asc'}">selected</c:if>>Ascending</option>
        <option value="desc" <c:if test="${pager.direction eq 'desc'}">selected</c:if>>Descending</option>
      </select>
      <div class="filter-actions">
        <button class="btn btn-ink" type="submit"><i class="bi bi-funnel"></i> Apply</button>
        <a class="btn btn-ghost" href="${ctx}/events">Reset</a>
      </div>
    </form>

    <div class="between mb-3">
      <span class="filter-count"><strong>${pager.total}</strong> event<c:if test="${pager.total ne 1}">s</c:if> found</span>
      <span class="small muted">Registration status is checked live against each event&rsquo;s dates.</span>
    </div>

    <c:choose>
      <c:when test="${not empty events}">
        <div class="event-grid">
          <c:forEach var="event" items="${events}">
            <%@ include file="/WEB-INF/views/fragments/event-card.jspf" %>
          </c:forEach>
        </div>
      </c:when>
      <c:otherwise>
        <div class="card-panel">
          <div class="empty-state">
            <div class="empty-icon"><i class="bi bi-search"></i></div>
            <h3>No races match those filters</h3>
            <p>Try a broader search, a different state, or include past events to see the full archive.</p>
            <a class="btn btn-ink" href="${ctx}/events">Clear filters</a>
          </div>
        </div>
      </c:otherwise>
    </c:choose>

    <div class="mt-3">
      <%@ include file="/WEB-INF/views/layout/pagination.jspf" %>
    </div>
  </div>
</section>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
