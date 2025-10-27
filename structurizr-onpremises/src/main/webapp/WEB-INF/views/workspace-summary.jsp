<%@ include file="/WEB-INF/fragments/workspace/javascript.jspf" %>

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

    .navigationItemSeparator {
        margin-bottom: 40px;
    }
</style>

<div class="row" style="padding-bottom: 0; margin-left: 0; margin-right: 0">

    <div class="col-sm-2" style="padding-left: 30px; margin-bottom: 100px">
        <div id="navigationPanel" class="hidden-xs">

            <c:if test="${not empty workspace.branch || not empty param.version}">
            <div class="navigationItem">
                <c:if test="${not empty workspace.branch}">
                <div style="margin-bottom: 10px">
                    <span class="label label-branch"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/bezier2.svg" class="icon-sm icon-white" /> ${workspace.branch}</span>
                </div>
                </c:if>
                <c:if test="${not empty param.version}">
                <div style="margin-bottom: 10px">
                    <span class="label label-version"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/clock-history.svg" class="icon-sm icon-white" /> ${workspace.internalVersion}</span>
                </div>
                </c:if>
            </div>

            <div class="navigationItemSeparator"></div>
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
                <a href="<c:out value="${urlPrefix}" />/dsl<c:out value="${urlSuffix}" escapeXml="false" />"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/code-slash.svg" class="icon-sm" /> DSL editor</a>
            </div>
            </c:if>

            <div id="diagramsLink" class="navigationItem hidden">
            <a href="<c:out value="${urlPrefix}" />/diagrams<c:out value="${urlSuffix}" escapeXml="false" />"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/bounding-box.svg" class="icon-sm" /> Diagrams</a>
            </div>

            <div id="documentationLink"  class="navigationItem hidden">
                <a href="<c:out value="${urlPrefix}" />/documentation<c:out value="${urlSuffix}" escapeXml="false" />"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/book.svg" class="icon-sm" /> Documentation</a>
            </div>
            <div id="decisionsLink" class="navigationItem hidden">
                <a href="<c:out value="${urlPrefix}" />/decisions<c:out value="${urlSuffix}" escapeXml="false" />"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/journal-text.svg" class="icon-sm" /> Decisions</a>
            </div>
            <div id="exploreLink" class="navigationItem hidden">
                <a href="<c:out value="${urlPrefix}" />/explore<c:out value="${urlSuffix}" escapeXml="false" />"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/diagram-3.svg" class="icon-sm" /> Explore</a>
            </div>

            <div id="themeLink" class="navigationItem hidden">
                <a href="<c:out value="${urlPrefix}" />/theme<c:out value="${urlSuffix}" escapeXml="false" />" target="_blank"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/palette.svg" class="icon-sm" /> Theme</a>
            </div>

            <div id="imagesLink" class="navigationItem hidden">
                <a href="<c:out value="${urlPrefix}" />/images"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/filetype-png.svg" class="icon-sm" /> Published images</a>
            </div>

            <c:if test="${reviewsEnabled}">
            <div class="navigationItem">
                <a href="<c:out value="${urlPrefix}" />/reviews"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/chat.svg" class="icon-sm" /> Reviews</a>
            </div>
            </c:if>

            <div id="exportJsonLinkNavItem" class="navigationItem">
                <img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/filetype-json.svg" class="icon-sm" /> JSON - <c:if test="${fn:startsWith(urlPrefix, '/share')}"><a href="<c:out value="${urlPrefix}" />/json<c:out value="${urlSuffix}" escapeXml="false" />" target="_blank">View</a> | </c:if><a id="exportJsonLink" href="">Export</a>
            </div>

            <div id="exportDslLinkNavItem" class="navigationItem">
                <img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/filetype-txt.svg" class="icon-sm" /> DSL - <c:if test="${fn:startsWith(urlPrefix, '/share')}"><a href="<c:out value="${urlPrefix}" />/dsl<c:out value="${urlSuffix}" escapeXml="false" />" target="_blank">View</a> | </c:if><a id="exportDslLink" href="">Export</a>
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
                <a href="<c:out value="${urlPrefix}" />/inspections<c:out value="${urlSuffix}" escapeXml="false" />"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/clipboard-pulse.svg" class="icon-sm" /> Inspections</a>
            </div>

            <div class="navigationItem">
                <a href="<c:out value="${urlPrefix}" />/settings"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/gear.svg" class="icon-sm" /> Settings</a>
            </div>

            <c:if test="${workspace.editable eq true and workspace.locked eq true}">
            <div class="navigationItem">
                <a id="unlockWorkspaceLink" href=""><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/unlock.svg" class="icon-sm" /> Unlock</a>
            </div>
            </c:if>

            <div class="navigationItem">
                <a href="<c:out value="${urlPrefix}" />/users"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/people.svg" class="icon-sm" /> Users</a>
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

                <c:if test="${fn:startsWith(urlPrefix, '/workspace')}">
                <div class="centered" style="margin-top: 20px">
                    <c:if test="${not empty branches}">
                    <form id="workspaceBranchForm" class="form-inline" style="display: inline-block" method="get" action="<c:out value="${urlPrefix}" />">
                        <select id="workspaceBranch" name="branch" class="form-control">
                            <option value="">main</option>
                            <c:forEach var="branch" items="${branches}">
                                <option value="${branch.name}">[branch] ${branch.name}</option>
                            </c:forEach>
                        </select>
                    </form>
                    </c:if>

                    <c:if test="${not empty versions && versions.size() > 1}">
                    <form id="workspaceVersionForm" class="form-inline" style="display: inline-block" method="get" action="<c:out value="${urlPrefix}" />">
                        <c:if test="${not empty branch}">
                        <input type="hidden" name="branch" value="${workspace.branch}" />
                        </c:if>
                        <select id="workspaceVersion" name="version" class="form-control">
                            <c:forEach var="version" items="${versions}">
                                <option value="${version.versionId}"><fmt:formatDate value="${version.lastModifiedDate}" pattern="EEE dd MMM yyyy HH:mm:ss z" timeZone="${user.timeZone}" /></option>
                            </c:forEach>
                        </select>
                    </form>
                    </c:if>

                    <div style="margin-top: 10px">
                    <c:if test="${not empty param.version && workspace.editable && not workspace.locked}">
                        <button id="revertButton" class="btn btn-default small"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/clock-history.svg" class="icon-btn" /> Revert to this version</button>
                    </c:if>

                    <c:if test="${workspace.editable && not workspace.locked && branchesEnabled}">
                    <button id="copyToBranchButton" class="btn btn-default small"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/bezier2.svg" class="icon-btn" /> Copy to branch</button>
                    </c:if>

                    <c:if test="${workspace.editable && not workspace.locked && branchesEnabled && not empty branch}">
                    <form id="deleteBranchForm" action="/workspace/${workspace.id}/branch/${branch}/delete" method="post" class="form-inline" style="display: inline-block">
                        <button class="btn btn-default small" type="submit"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/trash.svg" class="icon-btn" /> Delete branch</button>
                    </form>
                    </c:if>
                    </div>
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
                                    <a href="<c:out value="${urlPrefix}" />/dsl"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/code-slash.svg" class="icon-lg"/> DSL editor</a>
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
                                    <a href="<c:out value="${urlPrefix}" />/settings#api"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/gear.svg" class="icon-lg" /> Settings</a>
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

    $('#unlockWorkspaceLink').click(function (event) {
        unlockWorkspace(event);
    });

    <c:if test="${not empty workspace.branch}">
    $('#workspaceBranch').val("<c:out value="${workspace.branch}" />");
    </c:if>

    $('#workspaceBranch').on('change', function() {
        $('#workspaceBranchForm').submit();
    });

    $('#workspaceVersion').on('change', function() {
        $('#workspaceVersionForm').submit();
    });

    <c:if test="${not empty param.version}">
    $('#workspaceVersion').val("<c:out value="${workspace.internalVersion}" />");
    </c:if>

    function addOnClickHandler(domId, handler) {
        var element = document.getElementById(domId);
        if (element) {
            element.onclick = handler;
        }
    }

    addOnClickHandler('copyToBranchButton', copyWorkspaceToBranch);
    addOnClickHandler('revertButton', revertToLoadedVersion);
    $('#deleteBranchForm').on('submit', function() { return deleteBranch(); });

    addOnClickHandler('exportJsonLink', function (e) {
        structurizr.util.exportWorkspace(structurizr.workspace.id, structurizr.workspace.getJson());
        e.preventDefault();
    });

    addOnClickHandler('exportDslLink', function (e) {
        var dslSource = structurizr.workspace.getProperty('structurizr.dsl');
        if (dslSource !== undefined) {
            dslSource = structurizr.util.atob(dslSource);
            const filename = 'structurizr-' + structurizr.workspace.id + '-workspace.dsl';
            structurizr.util.downloadFile(dslSource, "text/plain;charset=utf-8", filename);
        }

        e.preventDefault();
    });

    addOnClickHandler('importJsonLink1', function (e) {
        $('#importFromJsonControl').trigger('click');
        e.preventDefault();
    });

    addOnClickHandler('importJsonLink2', function (e) {
        $('#importFromJsonControl').trigger('click');
        e.preventDefault();
    });

    var importFromJsonControl = document.getElementById('importFromJsonControl');
    if (importFromJsonControl) {
        importFromJsonControl.onchange = function () {
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

            views.forEach(function (view) {
                var url = '<c:out value="${urlPrefix}" />/diagrams<c:out value="${urlSuffix}" escapeXml="false" />#' + structurizr.util.escapeHtml(view.key);
                var title = structurizr.util.escapeHtml(structurizr.ui.getTitleForView(view));

                if (count <= maxNumberOfViews) {
                    html += '<div class="centered" style="display: inline-block; margin: 10px 10px 40px 10px; width: ' + thumbnailSize + 'px;">';

                    <c:choose>
                    <c:when test="${not empty workspace.branch or not empty param.version}">
                    html += '  <a href="' + url + '"><img src="/static/img/thumbnail-not-available.png" class="img-thumbnail" style="margin-bottom: 10px" /></a>';
                    </c:when>
                    <c:otherwise>
                    html += '  <a href="' + url + '">';
                    html += '<img src="${thumbnailUrl}' + structurizr.util.escapeHtml(view.key) + '-thumbnail.png" class="img-light img-thumbnail viewThumbnail" style="margin-bottom: 10px; max-height: ' + thumbnailSize + 'px" />';
                    html += '<img src="${thumbnailUrl}' + structurizr.util.escapeHtml(view.key) + '-thumbnail-dark.png" class="img-dark img-thumbnail viewThumbnail" style="margin-bottom: 10px; max-height: ' + thumbnailSize + 'px" />';
                    html += '</a>';
                    </c:otherwise>
                    </c:choose>

                    html += '  <div class="smaller">';
                    html += '    <a href="' + url + '">' + title + '</a><br />';
                    html += '    #' + structurizr.util.escapeHtml(view.key) + '';
                    html += '  </div>';
                    html += '</div>';
                }
                count++;
            });

            if (views.length > maxNumberOfViews) {
                html += '<div class="small"><a href="<c:out value="${urlPrefix}" />/diagrams<c:out value="${urlSuffix}" escapeXml="false" />">More diagrams...</a></div>';
            }

            diagramsDiv.addClass('centered');
            diagramsDiv.append(html);

            $('.viewThumbnail').on('error', function () {
                $(this).on('error', undefined);
                $(this).attr('src', '/static/img/thumbnail-not-available.png');
            });

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
        return new Date(dateAsString).toLocaleDateString('en-GB', {
            weekday: 'long',
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    }

    function copyWorkspaceToBranch() {
        const branch = prompt("Branch name");

        if (branch !== undefined) {
            progressMessage.show('<p>Copying to branch...</p>');

            structurizrApiClient.setBranch(branch);

            structurizr.saveWorkspace(function(response) {
                if (response.success === true) {
                    progressMessage.hide();

                    if (branch.length > 0) {
                        window.location.href = '<c:out value="${urlPrefix}"/>?branch=' + branch;
                    } else {
                        window.location.href = '<c:out value="${urlPrefix}"/>';
                    }
                } else {
                    if (response.message) {
                        console.log(response.message);
                        if (progressMessage) {
                            progressMessage.show('<p>Error</p><p style="font-size: 75%">' + structurizr.util.escapeHtml(response.message) + '</p>');
                        }
                    }
                }
            });
        }
    }

    function revertToLoadedVersion() {
        if (confirm('Are you sure you want to revert to this version?')) {
            structurizr.saveWorkspace(function () {
                const branch = '<c:out value="${workspace.branch}" />';

                if (branch.length > 0) {
                    window.location.href = '<c:out value="${urlPrefix}"/>?branch=' + branch;
                } else {
                    window.location.href = '<c:out value="${urlPrefix}"/>';
                }
            });
        }
    }

    function deleteBranch() {
        return confirm('Are you sure you want to delete this branch?');
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
            } catch (err) {
                alert("Sorry, there was a problem reading the file: " + err);
                console.log(err);
                return;
            }

            progressMessage.show('<p>Saving workspace...</p>');
            structurizrEncryptionStrategy = undefined;
            structurizrApiClient.putWorkspace(json, function (response) {
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