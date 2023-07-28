<div class="section">
    <div class="container">
        <h1><c:out value="${query}" /></h1>

        <div class="centered">
            <form id="searchForm" method="get" action="${searchBaseUrl}search" style="display: inline-block">
                <div class="form-inline" style="margin-top: 8px">
                    <div class="form-group">
                        <div class="btn-group">
                            <input type="text" name="query" class="form-control" placeholder="Search" value="${query}" style="width: 400px" />
                            <c:if test="${not empty workspaceId}">
                                <input type="hidden" name="workspaceId" class="form-control" value="${workspaceId}" />
                            </c:if>
                            <select class="form-control" name="category">
                                <option value="">All</option>
                                <c:if test="${empty workspaceId}">
                                    <option value="workspace" <c:if test="${category eq 'workspace'}">selected="selected"</c:if>>Workspaces</option>
                                </c:if>
                                <option value="diagram" <c:if test="${category eq 'diagram'}">selected="selected"</c:if>>Diagrams</option>
                                <option value="documentation" <c:if test="${category eq 'documentation'}">selected="selected"</c:if>>Documentation</option>
                                <option value="decision" <c:if test="${category eq 'decision'}">selected="selected"</c:if>>Decisions</option>
                            </select>
                        </div>
                        <div class="btn-group">
                            <button class="btn btn-default" title="Search" onclick="$('#searchForm').submit();" style="height: 33px;"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/search.svg" class="icon-btn" /></button>
                        </div>
                    </div>
                </div>
            </form>
        </div>

        <br />

        <c:choose>
            <c:when test="${not empty results}">
                <table class="table table-striped small">

                    <c:forEach var="result" items="${results}">
                        <tr>
                            <td width="90px">
                                <div>
                                    <img src="${urlPrefix}/${result.workspaceId}/images/thumbnail.png" width="70px" alt="<c:out value='${result.workspace.name}' escapeXml='true' />" class="thumbnail" onerror="this.onerror = null; this.src='/static/img/thumbnail-not-available.png';" />
                                </div>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${result.type eq 'workspace'}">
                                        <img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/folder.svg" class="icon-sm" />
                                    </c:when>
                                    <c:when test="${result.type eq 'documentation'}">
                                        <img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/book.svg" class="icon-sm" />
                                    </c:when>
                                    <c:when test="${result.type eq 'decision'}">
                                        <img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/journal-text.svg" class="icon-sm" />
                                    </c:when>
                                    <c:when test="${result.type eq 'diagram'}">
                                        <img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/bounding-box.svg" class="icon-sm" />
                                    </c:when>
                                </c:choose>
                                <a href="${urlPrefix}/${result.workspaceId}${result.url}"><c:out value="${result.name}" escapeXml="true" /></a>
                                <c:if test="${result.type ne 'workspace'}">
                                <div class="smaller">
                                    (from <a href="${urlPrefix}/${result.workspace.id}"><c:out value="${result.workspace.name}" escapeXml="true" /></a>)
                                </div>
                                </c:if>
                                <div class="smaller" style="margin-top: 5px">
                                    <c:out value="${result.description}" escapeXml="true" />
                                </div>
                            </td>
                        </tr>
                    </c:forEach>

                </table>
            </c:when>
            <c:otherwise>
                <p>
                    No results found.
                </p>
            </c:otherwise>
        </c:choose>
    </div>
</div>