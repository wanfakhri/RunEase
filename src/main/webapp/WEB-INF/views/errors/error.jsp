<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- The servlet layer sets "status"/"message"; container error pages expose the
     Jakarta Servlet error attributes instead, so fall back to those.
     Compute the heading before the shared header so the document title and
     breadcrumb are populated even for container-generated error pages. --%>
<c:set var="errorStatus" value="${empty status ? requestScope['jakarta.servlet.error.status_code'] : status}" />
<c:set var="errorMessage" value="${empty message ? requestScope['jakarta.servlet.error.message'] : message}" />
<c:if test="${empty errorStatus}"><c:set var="errorStatus" value="500" /></c:if>

<c:choose>
  <c:when test="${errorStatus eq 403 or errorStatus eq '403'}">
    <c:set var="errorCode" value="403" />
    <c:set var="errorHeading" value="You do not have access to this page" />
    <c:set var="errorIcon" value="bi-shield-lock" />
    <c:set var="errorNote" value="Your account is signed in, but this area belongs to a different role. Return to your own dashboard to continue." />
  </c:when>
  <c:when test="${errorStatus eq 404 or errorStatus eq '404'}">
    <c:set var="errorCode" value="404" />
    <c:set var="errorHeading" value="We could not find that page" />
    <c:set var="errorIcon" value="bi-compass" />
    <c:set var="errorNote" value="The link may be broken, or the event or record may have been removed. Try the catalogue or head back to your dashboard." />
  </c:when>
  <c:when test="${errorStatus eq 405 or errorStatus eq '405'}">
    <c:set var="errorCode" value="405" />
    <c:set var="errorHeading" value="That action is not allowed" />
    <c:set var="errorIcon" value="bi-slash-circle" />
    <c:set var="errorNote" value="The page was reached with an unsupported request method. Use the navigation below to continue." />
  </c:when>
  <c:when test="${errorStatus eq 400 or errorStatus eq '400'}">
    <c:set var="errorCode" value="400" />
    <c:set var="errorHeading" value="That request could not be processed" />
    <c:set var="errorIcon" value="bi-exclamation-octagon" />
    <c:set var="errorNote" value="Some of the submitted information was missing or invalid. Go back, check the form and try again." />
  </c:when>
  <c:otherwise>
    <c:set var="errorCode" value="500" />
    <c:set var="errorHeading" value="Something went wrong on our side" />
    <c:set var="errorIcon" value="bi-tools" />
    <c:set var="errorNote" value="The request could not be completed. Nothing was saved twice, so it is safe to try again. If it keeps happening, contact the RunEase team." />
  </c:otherwise>
</c:choose>

<c:set var="pageTitle" value="${errorHeading}" />

<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<section class="error-wrap">
  <div class="error-card">
    <div class="error-code">${errorCode}</div>
    <span class="avatar xl lime mx-auto mb-3"><i class="bi ${errorIcon}"></i></span>
    <h1>${errorHeading}</h1>
    <p>
      <c:choose>
        <c:when test="${not empty errorMessage and errorCode ne '500'}"><c:out value="${errorMessage}" /></c:when>
        <c:otherwise>${errorNote}</c:otherwise>
      </c:choose>
    </p>
    <div class="error-actions">
      <c:choose>
        <c:when test="${not empty user}">
          <a class="btn btn-accent" href="${ctx}${user.role.home}"><i class="bi bi-grid-1x2"></i> Back to dashboard</a>
        </c:when>
        <c:otherwise>
          <a class="btn btn-accent" href="${ctx}/login"><i class="bi bi-box-arrow-in-right"></i> Sign in</a>
        </c:otherwise>
      </c:choose>
      <a class="btn btn-ghost" href="${ctx}/events"><i class="bi bi-compass"></i> Browse events</a>
      <a class="btn btn-ghost" href="${ctx}/"><i class="bi bi-house"></i> Home</a>
    </div>
    <p class="mt-3 mb-0 small muted">
      Reference: <span class="code-chip">RUN-${errorCode}</span>
      <c:if test="${not empty requestScope['jakarta.servlet.error.request_uri']}">
        &middot; <c:out value="${requestScope['jakarta.servlet.error.request_uri']}" />
      </c:if>
    </p>
  </div>
</section>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
