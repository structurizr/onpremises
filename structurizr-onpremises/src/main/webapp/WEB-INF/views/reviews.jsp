<style>
    .review {
        position: relative;
        display: inline-block;
        margin: 10px;
        font-size: 18px;
        max-width: 220px;
        min-height: 300px;
        max-height: 300px;
        height: 360px;
        padding: 10px;
        border:1px solid #ddd;
        border-radius: 4px;
        overflow-y: hidden;
    }
    .reviewThumbnail {
        display: inline-block;
        width: 200px;
        height: 200px;
        min-height: 200px;
        max-height: 200px;
        overflow-y: hidden;
        margin-top: 10px;
    }
</style>

<div class="section">
    <div class="container centered">
        <h1>Reviews</h1>
        <c:if test="${not empty workspaceId}">
        <p class="smaller">
            (<a href="/workspace/${workspaceId}">back to workspace summary</a>)
        </p>
        </c:if>

        <br />

        <c:choose>
        <c:when test="${not empty reviews}">
        <div class="centered">
        <c:forEach var="review" items="${reviews}">
            <div class="review">
                <div class="reviewThumbnail">
                <a href="/review/${review.id}" target="_blank"><img src="${review.diagrams[0].url}" class="img-responsive" width="200px" /></a>
                </div>
                <p class="smaller">
                    <b>${review.type}</b>
                    <c:choose>
                        <c:when test="${review.locked}">
                            <img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/lock.svg" class="icon-xs" />
                        </c:when>
                        <c:otherwise>
                            <img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/unlock.svg" class="icon-xs" />
                        </c:otherwise>
                    </c:choose>
                    <br />
                    <c:if test="${not empty review.workspaceId}">
                    <a href="/workspace/${review.workspaceId}">Workspace ${review.workspaceId}</a>
                    <br />
                    </c:if>
                    <fmt:formatDate value="${review.dateCreated}" pattern="EEE dd MMM yyyy HH:mm z" timeZone="${user.timeZone}" />
                </p>
            </div>
        </c:forEach>
        </div>

        </c:when>
            <c:otherwise>
        <p class="centered smaller" style="margin-top: 40px">
            No reviews have been created yet.
        </p>
            </c:otherwise>
        </c:choose>
    </div>
</div>