<style>
    .workspaceSummary {
        position: relative;
        display: inline-block;
        margin: 10px;
        font-size: 18px;
        max-width: 320px;
        min-height: 360px;
        max-height: 360px;
        height: 360px;
        padding: 10px;
        border:1px solid #ddd;
        border-radius: 4px;
        overflow-y: hidden;
    }

    .workspaceThumbnail {
        display: inline-block;
        width: 300px;
        height: 200px;
        min-height: 200px;
        max-height: 200px;
        overflow-y: hidden;
        margin-top: 10px;
    }
</style>

<%@ include file="/WEB-INF/fragments/quick-navigation.jspf" %>

<div id="dashboard" class="section" style="padding-bottom: 0px">
    <div class="container">
        <c:choose>
        <c:when test="${not empty workspaces}">
            <%@ include file="/WEB-INF/fragments/dashboard-workspaces.jspf" %>
        </c:when>
        <c:otherwise>
            <c:choose>
            <c:when test="${authenticated}">
                <div class="section">
                    <div class="centered">
                        <p>
                            This your dashboard, which will show the workspaces you have access to.
                            We recommend that a workspace contains the model, views, and documentation for a single software system - see <a href="https://structurizr.com/help/usage-recommendations" target="_blank">Usage recommendations</a> for more details.
                        </p>

                        <c:if test="${user.admin}">
                            <div class="workspaceSummary centered">
                                <div style="margin-top: 5px">
                                    New workspace
                                </div>

                                <br /><br /><br />

                                <div class="">
                                    <div class="workspaceThumbnail" style="margin-top: 10px">
                                        <a href="/workspace/create"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/folder-plus.svg" style="width: 100px; height: 100px"/></a>
                                    </div>
                                </div>
                            </div>
                        </c:if>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="section">
                    <div class="container centered">
                        No workspaces.
                    </div>
                </div>
            </c:otherwise>
            </c:choose>
        </c:otherwise>
        </c:choose>
    </div>
</div>