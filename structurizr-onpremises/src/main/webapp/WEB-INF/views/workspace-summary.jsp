<%@ include file="/WEB-INF/fragments/workspace/javascript.jspf" %>
<script type="text/javascript" src="${structurizrConfiguration.cdnUrl}/js/structurizr-ui${structurizrConfiguration.versionSuffix}.js"></script>

<style>
    h2 {
        margin-bottom: 20px;
    }
    .container {
        width: auto;
        margin-left: 30px;
        margin-right: 30px;
    }

    #navigationPanel {
        margin-top: 20px;
    }

    .navigationItem {
        font-size: 14px;
        margin-bottom: 10px;
    }

    .navigationItemHeading {
        font-size: 30px;
        margin-bottom: 20px;
    }

    .navigationItemSeparator {
        margin-bottom: 40px;
    }
</style>

<div class="row" style="padding-bottom: 0; margin-left: 0; margin-right: 0">

    <div class="col-sm-2" style="padding-left: 30px; margin-bottom: 100px">
        <div id="navigationPanel" class="hidden-xs">

            <div class="navigationItemHeading">
                Workspace
            </div>

            <c:if test="${not empty param.version}">
            <div class="navigationItem">
                <span class="label label-version"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/clock-history.svg" class="icon-xs icon-white" /> ${workspace.internalVersion}</span>
            </div>
            </c:if>

            <c:if test="${workspace.clientEncrypted}">
            <div class="navigationItem">
                <img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/file-earmark-lock.svg" class="icon-sm" /> Client-side encrypted
            </div>
            </c:if>

            <c:if test="${fn:startsWith(urlPrefix, '/workspace')}">
            <div class="navigationItem">
                <a href="/workspace/${workspace.id}"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/lock.svg" class="icon-sm" /> Private link</a>
            </div>

            <c:if test="${workspace.shareable && workspace.ownerUserType.allowedToShareWorkspacesWithLink}">
            <div class="navigationItem">
                <a href="/share/${workspace.id}/${workspace.sharingToken}" target="_blank"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/link.svg" class="icon-sm" /> Sharing link</a>
            </div>
            </c:if>

            <c:if test="${workspace.open}">
            <div class="navigationItem">
                <a href="/share/${workspace.id}" target="_blank"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/unlock.svg" class="icon-sm" /> Public link</a>
            </div>
            </c:if>

            <div class="navigationItemSeparator"></div>
            </c:if>
            
            <c:if test="${fn:startsWith(urlPrefix, '/workspace') && structurizrConfiguration.dslEditorEnabled}">
            <div class="navigationItem dslEditorNavigation">
                <a href="${urlPrefix}/dsl${urlSuffix}"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/code-slash.svg" class="icon-sm" /> DSL editor</a>
            </div>
            </c:if>

            <div id="diagramsLink" class="navigationItem hidden">
            <a href="${urlPrefix}/diagrams${urlSuffix}"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/bounding-box.svg" class="icon-sm" /> Diagrams</a>
            </div>

            <div id="documentationLink"  class="navigationItem hidden">
                <a href="${urlPrefix}/documentation${urlSuffix}"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/book.svg" class="icon-sm" /> Documentation</a>
            </div>
            <div id="decisionsLink" class="navigationItem hidden">
                <a href="${urlPrefix}/decisions${urlSuffix}"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/journal-text.svg" class="icon-sm" /> Decisions</a>
            </div>
            <div id="exploreLink" class="navigationItem hidden">
                <a href="${urlPrefix}/explore${urlSuffix}"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/diagram-3.svg" class="icon-sm" /> Explore</a>
            </div>

            <div id="themeLink" class="navigationItem hidden">
                <a href="${urlPrefix}/theme${urlSuffix}" target="_blank"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/palette.svg" class="icon-sm" /> Theme</a>
            </div>

            <div id="imagesLink" class="navigationItem hidden">
                <a href="${urlPrefix}/images"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/filetype-png.svg" class="icon-sm" /> Published images</a>
            </div>

            <div id="exportJsonLinkNavItem" class="navigationItem">
                <a id="exportJsonLink" href=""><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/filetype-json.svg" class="icon-sm" /> Export JSON</a>
            </div>

            <div id="exportDslLinkNavItem" class="navigationItem">
                <a id="exportDslLink" href=""><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/filetype-txt.svg" class="icon-sm" /> Export DSL</a>
            </div>

            <c:if test="${workspace.editable && not workspace.locked}">
            <div class="navigationItem">
                <a id="importJsonLink1" href=""><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/upload.svg" class="icon-sm" /> Import JSON</a>
                <input id="importFromJsonControl" type="file" style="display: none;" />
            </div>
            </c:if>

            <div class="navigationItemSeparator"></div>
            <c:if test="${fn:startsWith(urlPrefix, '/workspace')}">
            <div class="navigationItem">
                <a href="${urlPrefix}/settings"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/gear.svg" class="icon-sm" /> Settings</a>
            </div>

            <c:if test="${workspace.editable eq true and workspace.locked eq true}">
            <div class="navigationItem">
                <a href="" onclick="unlockWorkspace(event)"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/unlock.svg" class="icon-sm" /> Unlock</a>
            </div>
            </c:if>

            <div class="navigationItem">
                <a href="${urlPrefix}/users"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/people.svg" class="icon-sm" /> Users</a>
            </div>
            </c:if>
        </div>
    </div>

    <div class="col-sm-10">
        <div class="section" style="padding-bottom: 20px">
            <div class="container centered">
                <h1><span id="workspaceName" class="workspace${workspace.id}Name"><c:out value="${workspace.name}" escapeXml="true" /></span></h1>
                <p id="workspaceDescription" class="workspace${workspace.id}Description">
                    <c:out value="${workspace.description}" escapeXml="true" />
                </p>

                <p class="workspaceMetaData">
                    <c:if test="${empty param.version}">
                    Last modified: <fmt:formatDate value="${workspace.lastModifiedDate}" pattern="EEE dd MMM yyyy HH:mm:ss z" timeZone="${user.timeZone}" />
                    <c:if test="${workspace.editable eq true}">by ${workspace.lastModifiedUser} using ${workspace.lastModifiedAgent}</c:if>

                    <c:if test="${not empty workspace.version}">
                    | Version: ${workspace.version}
                    </c:if>
                    <c:if test="${workspace.size gt 0}">
                    |
                    Size: <fmt:formatNumber minFractionDigits="2" maxFractionDigits="2" value="${workspace.sizeInMegaBytes}" /> MB
                    </c:if>
                    </c:if>
                </p>

                <c:if test="${fn:startsWith(urlPrefix, '/workspace') && not empty versions}">
                <div class="centered" style="margin-top: 20px">
                    <form class="form-inline" style="display: inline-block" method="get" action="${urlPrefix}">
                        <select id="workspaceVersion" name="version" class="form-control">
                            <c:forEach var="version" items="${versions}">
                                <option value="${version.versionId}"><fmt:formatDate value="${version.lastModifiedDate}" pattern="EEE dd MMM yyyy HH:mm:ss z" timeZone="${user.timeZone}" /></option>
                            </c:forEach>
                        </select>
                        <input type="submit" class="btn btn-default" value="Load version" style="height: 35.5px"/>
                    </form>
                    <c:if test="${not empty param.version && workspace.editable && not workspace.locked}">
                        <button id="revertButton" class="btn btn-default small"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/clock-history.svg" class="icon-btn" /> Revert to ${workspace.internalVersion}</button>
                    </c:if>
                </div>
                </c:if>

                <div id="gettingStarted" class="centered hidden">
                    <c:if test="${workspace.editable && not workspace.locked}">
                    <br />
                    <p>
                        This is your workspace summary page, and from here you can access your diagrams, documentation, and architecture decision records.
                        We recommend that a workspace contains the model, views, and documentation for a single software system
                        - see <a href="https://structurizr.com/help/usage-recommendations" target="_blank">usage recommendations</a> for more details.
                        This workspace is empty, and you can use one of the following authoring options to create content.
                    </p>

                    <div class="row" style="margin-top: 20px; margin-bottom: 20px">
                        <div class="col-sm-4 centered">
                            <div style="padding: 10px; margin: 10px">
                                <div style="margin-top: 5px; font-size: 20px">
                                    <a href="${urlPrefix}/dsl"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/code-slash.svg" class="icon-lg"/> DSL editor</a>
                                </div>
                                <div class="small">
                                    Open the online DSL editor, and create your workspace using the Structurizr DSL.
                                </div>
                            </div>
                        </div>
                        <div class="col-sm-4 centered">
                            <div style="padding: 10px; margin: 10px">
                                <div style="margin-top: 5px; font-size: 20px">
                                    <a id="importJsonLink2" href=""><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/upload.svg" class="icon-lg" /> Import JSON</a>
                                </div>
                                <div class="small">
                                    Import an existing workspace from a JSON file.
                                </div>
                            </div>
                        </div>
                        <div class="col-sm-4 centered">
                            <div style="padding: 10px; margin: 10px">
                                <div style="margin-top: 5px; font-size: 20px">
                                    <a href="${urlPrefix}/settings#api"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/gear.svg" class="icon-lg" /> Settings</a>
                                </div>
                                <div class="small">
                                    Use the workspace settings page to find your API key/secret, for uploading your workspace with the Structurizr CLI or code-based libraries.
                                </div>
                            </div>
                        </div>
                    </div>
                    </c:if>
                </div>
            </div>
        </div>

        <div class="section workspaceContent">
            <div class="container centered">
                <div id="diagrams"></div>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/fragments/progress-message.jspf" %>

<script nonce="${scriptNonce}">

    progressMessage.show('<p>Loading workspace...</p>');

    <c:if test="${not empty param.version}">
    $('#workspaceVersion').val("${workspace.internalVersion}");
    </c:if>

    function addOnClickHandler(domId, handler) {
        var element = document.getElementById(domId);
        if (element) {
            element.onclick = handler;
        }
    }

    addOnClickHandler('revertButton', revertToLoadedVersion);

    addOnClickHandler('exportJsonLink', function(e) {
        structurizr.util.exportWorkspace(structurizr.workspace.id, structurizr.workspace.getJson());
        e.preventDefault();
    });

    addOnClickHandler('exportDslLink', function(e) {
        var dslSource = structurizr.workspace.getProperty('structurizr.dsl');
        if (dslSource !== undefined) {
            dslSource = structurizr.util.atob(dslSource);
            const filename = 'structurizr-' + structurizr.workspace.id + '-workspace.dsl';
            structurizr.util.downloadFile(dslSource, "text/plain;charset=utf-8", filename);
        }

        e.preventDefault();
    });

    addOnClickHandler('importJsonLink1', function(e) {
        $('#importFromJsonControl').trigger('click');
        e.preventDefault();
    });

    addOnClickHandler('importJsonLink2', function(e) {
        $('#importFromJsonControl').trigger('click');
        e.preventDefault();
    });

    var importFromJsonControl = document.getElementById('importFromJsonControl');
    if (importFromJsonControl) {
        importFromJsonControl.onchange = function() {
            importWorkspaceFromFile(importFromJsonControl.files[0], structurizr.workspace.id);
        };
    }

    function workspaceLoaded() {
        if (structurizr.workspace.getProperty('structurizr.dslEditor') === 'false') {
            $('.dslEditorNavigation').addClass('hidden');
        }

        $('#workspaceName').html(structurizr.util.escapeHtml(structurizr.workspace.name));
        $('#workspaceDescription').html(structurizr.util.escapeHtml(structurizr.workspace.description));
        var diagramsDiv = $('#diagrams');

        structurizr.ui.applyBranding();

        if (structurizr.workspace.hasViews()) {
            var maxNumberOfViews = 10;
            var count = 1;
            var views = structurizr.workspace.getViews();
            const thumbnailSize = 200;
            var html = '';

            views.forEach(function(view) {
                var url = '${urlPrefix}/diagrams${urlSuffix}#' + structurizr.util.escapeHtml(view.key);
                var title = structurizr.util.escapeHtml(structurizr.ui.getTitleForView(view));

                if (count <= maxNumberOfViews) {
                    html += '<div class="centered" style="display: inline-block; margin: 10px 10px 40px 10px; width: ' + thumbnailSize + 'px;">';

                    if (view.type === structurizr.constants.IMAGE_VIEW_TYPE) {
                        html += '  <a href="' + url + '"><img src="' + view.content + '" class="img-thumbnail" style="margin-bottom: 10px; max-height: ' + thumbnailSize + 'px" onerror="this.onerror = null; this.src=\'/static/img/thumbnail-not-available.png\';" /></a>';
                    } else {
                    <c:choose>
                    <c:when test="${not empty param.version}">
                    html += '  <a href="' + url + '"><img src="/static/img/thumbnail-not-available.png" class="img-thumbnail" style="margin-bottom: 10px" /></a>';
                    </c:when>
                    <c:otherwise>
                    html += '  <a href="' + url + '"><img src="${thumbnailUrl}' + structurizr.util.escapeHtml(view.key) + '-thumbnail.png" class="img-thumbnail" style="margin-bottom: 10px; max-height: ' + thumbnailSize + 'px" onerror="this.onerror = null; this.src=\'/static/img/thumbnail-not-available.png\';" /></a>';
                    </c:otherwise>
                    </c:choose>
                    }

                    html += '  <div class="smaller">';
                    html += '    <a href="' + url + '">' + title + '</a><br />';
                    html += '    #' + structurizr.util.escapeHtml(view.key) + '';
                    html += '  </div>';
                    html += '</div>';
                }
                count++;
            });

            if (views.length > maxNumberOfViews) {
                html += '<div class="small"><a href="${urlPrefix}/diagrams${urlSuffix}">More diagrams...</a></div>';
            }

            diagramsDiv.addClass('centered');
            diagramsDiv.append(html);

            $('#diagramsLink').removeClass('hidden');
            $('#imagesLink').removeClass('hidden');
        }

        if (structurizr.workspace.hasDocumentation()) {
            $('#documentationLink').removeClass('hidden');
        }

        if (structurizr.workspace.hasDecisions()) {
            $('#decisionsLink').removeClass('hidden');
        }

        if (structurizr.workspace.hasElements()) {
            $('#exploreLink').removeClass('hidden');
        }

        if (structurizr.workspace.hasStyles()) {
            $('#themeLink').removeClass('hidden');
        }

        if (!structurizr.workspace.hasElements() && !structurizr.workspace.hasViews() && !structurizr.workspace.hasDocumentation() && !structurizr.workspace.hasDecisions()) {
            <c:if test="${workspace.editable}">
            $('#gettingStarted').removeClass('hidden');
            </c:if>
            $('.workspaceContent').addClass('hidden');
            $('#exportJsonLinkNavItem').addClass('hidden');
        }

        if (structurizr.workspace.getProperty('structurizr.dsl') === undefined) {
            $('#exportDslLinkNavItem').addClass('hidden');
        }

        progressMessage.hide();
    }

    function formatDate(dateAsString) {
        return new Date(dateAsString).toLocaleDateString('en-GB', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' });
    }

    function revertToLoadedVersion() {
        if (confirm('Are you sure you want to revert to this version?')) {
            structurizrApiClient.resetRevision();
            structurizr.saveWorkspace(function() {
                const indexOfVersionParameter = window.location.href.indexOf('?version=');
                if (indexOfVersionParameter > -1) {
                    window.location.href = window.location.href.substr(0, indexOfVersionParameter);
                } else {
                    location.reload();
                }
            });
        }
    }

    <c:if test="${workspace.editable eq true and workspace.locked eq true}">
    function unlockWorkspace(e) {
        e.preventDefault();

        if (confirm('${workspace.lockedUser} will lose any unsaved changes - are you sure you want to unlock this workspace?')) {
            window.location.href = '/workspace/${workspace.id}/unlock';
        }
    }
    </c:if>

    function importWorkspaceFromFile(file, workspaceId) {
        var reader = new FileReader();
        reader.readAsText(file, "UTF-8");

        reader.onload = function (evt) {
            try {
                var json = JSON.parse(evt.target.result);
                json.id = workspaceId;
            } catch(err) {
                alert("Sorry, there was a problem reading the file: " + err);
                console.log(err);
                return;
            }

            progressMessage.show('<p>Saving workspace...</p>');
            structurizrEncryptionStrategy = undefined;
            structurizrApiClient.resetRevision();
            structurizrApiClient.putWorkspace(json, function(response) {
                if (response.success) {
                    window.location.reload();
                } else {
                    progressMessage.show('<p>' + response.message + '</p>');
                }
            });
        };

        reader.onerror = function (evt) {
            alert("Sorry, there was a problem reading the file.");
        };
    }

    $("#workspaceVersion").change(function () {
        $('#revertButton').prop('disabled', true);
    });
</script>

<c:choose>
    <c:when test="${not empty workspaceAsJson}">
        <%@ include file="/WEB-INF/fragments/workspace/load-via-inline.jspf" %>
    </c:when>
    <c:otherwise>
        <%@ include file="/WEB-INF/fragments/workspace/load-via-api.jspf" %>
    </c:otherwise>
</c:choose>