<%@ include file="/WEB-INF/fragments/workspace/javascript.jspf" %>

<script type="text/javascript" src="${structurizrConfiguration.cdnUrl}/js/structurizr-embed.js"></script>
<script type="text/javascript" src="${structurizrConfiguration.cdnUrl}/js/ace-1.5.0.min.js" charset="utf-8"></script>

<%@ include file="/WEB-INF/fragments/graphviz.jspf" %>
<%@ include file="/WEB-INF/fragments/tooltip.jspf" %>
<%@ include file="/WEB-INF/fragments/progress-message.jspf" %>
<%@ include file="/WEB-INF/fragments/dsl/introduction.jspf" %>

<script nonce="${scriptNonce}">
    progressMessage.show('<p>Loading workspace...</p>');
</script>

<style>
    #sourceTextArea {
        border: solid 1px #dddddd;
    }
    .section {
        padding-bottom: 0;
    }

    pre {
        padding: 5px;
    }

    #editorControls {
        padding: 0;
        border: none;
        box-shadow: none;
    }

    .ace_structurizr_keyword {
        color: #1168BD;
    }

    .ace_structurizr_keyword_disabled {
        color: #cccccc;
    }

    .ace_structurizr_variable {
        color: #772222;
    }

    .ace_structurizr_string {
        color: #555555;
    }

    .ace_structurizr_comment {
        color: #999999;
    }

    .ace_structurizr_default {
        color: #777777;
    }

    .ace_structurizr_brace {
        color: #1168BD;
    }

    .ace_structurizr_constant {
        color: #00aa00;
    }

    .ace_structurizr_whitespace {
    }
</style>

<div class="section" style="padding-top: 20px">
    <div class="row" style="margin-left: 0; margin-right: 0; padding-bottom: 0">
        <div id="sourcePanel" class="col-sm-6 centered">

            <form id="dslForm" action="/dsl" method="post">
                <input id="source" name="source" type="hidden" value="" />
                <input id="view" name="view" type="hidden" />
                <input id="workspaceAsJson" name="json" type="hidden" />

                <div style="text-align: left; margin-bottom: 10px">
                    <div id="editorControls" style="float: right">
                        <div class="btn-group">
                            <button id="homeButton" class="btn btn-default" title="Home"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/house.svg" class="icon-btn" /></button>
                        </div>

                        <div class="btn-group">
                            <label class="btn btn-default small">
                                <img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/cloud-upload.svg" class="icon-btn" />
                                Upload <input id="uploadFileInput" type="file" style="display: none;">
                            </label>
                        </div>

                        <div class="btn-group">
                            <button id="sourceButton" class="btn btn-default" title="Source"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/code-slash.svg" class="icon-btn" /></button>
                            <button id="diagramsButton" class="btn btn-default" title="Diagrams"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/bounding-box.svg" class="icon-btn" /></button>
                        </div>

                        <button id="renderButton" class="btn btn-default"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/play.svg" class="icon-btn" /> Render</button>
                    </div>

                    <div>
                        <%@ include file="/WEB-INF/fragments/dsl/language-reference.jspf" %>
                    </div>
                </div>

                <div id="sourceTextArea"><c:out value="${source}" /></div>
            </form>

            <div class="smaller" style="margin-top: 5px">
                Structurizr DSL <a href="https://github.com/structurizr/java/blob/master/changelog.md" target="_blank">v${dslVersion}</a> - some features (e.g. <code>!docs</code>, <code>!adrs</code>, <code>!script</code>, etc) are unavailable via this page; see <a href="https://docs.structurizr.com/dsl#comparison" target="_blank">DSL - comparison</a> for details.
            </div>
        </div>
        <div id="diagramsPanel" class="col-sm-6 centered">

            <c:choose>
            <c:when test="${not empty errorMessage}">
                <div class="alert alert-danger">
                    <c:out value="${errorMessage}" escapeXml="true" />
                </div>
            </c:when>
            <c:otherwise>

            <div id="viewListPanel" style="margin-bottom: 10px">
                <div class="form-inline">
                    <span id="diagramNavButtons" class="hidden">
                        <button id="viewSourceButton" class="btn btn-default" title="Source"><img src="${structurizrConfiguration.cdnUrl}/bootstrap-icons/code-slash.svg" class="icon-btn" /> View source</button>
                    </span>
                    <select id="viewsList" class="form-control"></select>
                </div>
            </div>

            <div>
                <div id="diagramEditor"></div>
            </div>

            </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<script nonce="${scriptNonce}">
    const LOCAL_STORAGE_SOURCE = 'structurizr/dsl/source';
    const LOCAL_STORAGE_JSON = 'structurizr/dsl/json';

    var viewInFocus = '<c:out value="${view}" />';
    var editor;
    var structurizrDiagramIframeRendered = false;

    window.onresize = resize;

    $('#homeButton').click(function(event) { event.preventDefault(); window.location.href = '/'; });
    $('#sourceButton').click(function(event) { sourceButtonClicked(event); });
    $('#diagramsButton').click(function(event) { diagramsButtonClicked(event); });
    $('#viewSourceButton').click(function(event) { sourceButtonClicked(event); });
    $('#renderButton').click(function() { refresh(); });

    $('#uploadFileInput').on('change', function() { importSourceFile(this.files); });

    function workspaceLoaded() {
        init();
    }

    function init() {
        var listOfViews = structurizr.workspace.getViews();
        if (listOfViews.length > 0) {
            const viewsList = $('#viewsList');
            viewsList.empty();

            for (var i = 0; i < listOfViews.length; i++) {
                var view = listOfViews[i];
                viewsList.append('<option value="' + structurizr.util.escapeHtml(view.key) + '">' + structurizr.util.escapeHtml(structurizr.ui.getTitleForView(view)) + ' (#' + view.key + ')</option>');
            }

            if (viewInFocus === undefined || viewInFocus === '') {
                viewInFocus = structurizr.workspace.getViews()[0].key;
            }

            if (structurizr.workspace.findViewByKey(viewInFocus) === undefined) {
                viewInFocus = structurizr.workspace.getViews()[0].key;
            }

            viewsList.val(viewInFocus);

            viewsList.change(function () {
                viewInFocus = $(this).val();
                changeView();
            });
        }

        editor = ace.edit("sourceTextArea");
        editor.setOption("printMargin", false);

        ace.config.set('basePath', '${structurizrConfiguration.cdnUrl}/js/ace');
        editor.session.setMode("ace/mode/structurizr");

        <c:if test="${method eq 'get'}">
        var sourceFromLocalStorage = localStorage.getItem(LOCAL_STORAGE_SOURCE);
        if (sourceFromLocalStorage && sourceFromLocalStorage.length > 0) {
            try {
                $('#source').val(structurizr.util.atob(sourceFromLocalStorage));

                const jsonAsString = localStorage.getItem(LOCAL_STORAGE_JSON);
                if (jsonAsString && jsonAsString.length > 0) {
                    $('#workspaceAsJson').val(structurizr.util.atob(jsonAsString));
                }

                document.getElementById('dslForm').submit();
            } catch (e) {
                editor.setValue('', -1);
            }
        }
        </c:if>

        resize();

        <c:if test="${line gt 0}">
        var line = ${line}-1;
        editor.moveCursorToPosition({row: line, column: 0});
        editor.selection.selectLine();
        editor.scrollToLine(line);
        </c:if>

        setUnsavedChanges(true);

        $(window).on("beforeunload", function () {
            if (unsavedChanges) {
                return "There are unsaved changes.";
            }
        });

        renderDiagram();

        progressMessage.hide();
    }

    function resize() {
        const editorControlsHeight = $('#editorControls').outerHeight();
        const verticalPadding = 60;

        $('#sourceTextArea').css('height', (window.innerHeight - editorControlsHeight - verticalPadding) + 'px');
        if (editor) {
            editor.resize(true);
        }
        structurizr.embed.setMaxHeight(window.innerHeight - verticalPadding - 20);
        structurizr.embed.resizeEmbeddedDiagrams();
    }

    function renderDiagram() {
        if (structurizrDiagramIframeRendered === false) {
            var diagramEditorDiv = $('#diagramEditor');
            diagramEditorDiv.empty();

            var diagramIdentifier = viewInFocus;
            var domId = 'embeddedStructurizrDiagram';
            var embedUrl = '/embed?view=' + encodeURIComponent(diagramIdentifier) + '&editable=true&iframe=' + domId;
            diagramEditorDiv.append('<div style="text-align: center"><iframe id="' + domId + '" class="structurizrEmbed thumbnail" src="' + embedUrl + '" width="100%" height="' + window.innerHeight + 'px" marginwidth="0" marginheight="0" frameborder="0" scrolling="no" allowfullscreen="true"></iframe></div>');

            setTimeout(function () {
                try {
                    document.getElementById('embeddedStructurizrDiagram').contentWindow.structurizr.scripting = undefined;

                    document.getElementById('embeddedStructurizrDiagram').contentWindow.structurizr.diagram.onViewChanged(function(view) {
                        document.getElementById('embeddedStructurizrDiagram').contentWindow.viewChanged(view);

                        if (document.getElementById('viewsList').value !== view) {
                            document.getElementById('viewsList').value = view;
                        }
                    });
                } catch (e) {
                }
            }, 2000);

            structurizrDiagramIframeRendered = true;
        } else {
            changeView();
        }
    }

    function changeView() {
        const diagramIdentifier = $('#viewsList').val();
        document.getElementById('embeddedStructurizrDiagram').contentWindow.location.hash = '#' + diagramIdentifier;
    }

    var unsavedChanges = false;

    function setUnsavedChanges(bool) {
        unsavedChanges = bool;
    }

    function refresh() {
        setUnsavedChanges(false);
        if (document.getElementById('embeddedStructurizrDiagram')) {
            document.getElementById('embeddedStructurizrDiagram').contentWindow.unsavedChanges = false;
        }

        const source = editor.getValue();
        if (source === undefined || source.length === 0) {
            $('#source').val('');
            $('#workspaceAsJson').val('');
            localStorage.removeItem(LOCAL_STORAGE_SOURCE);
            localStorage.removeItem(LOCAL_STORAGE_JSON);
        } else {
            $('#source').val(source);

            $('#view').val($('#viewsList').val());

            structurizr.workspace.views.configuration.lastSavedView = viewInFocus;
            const workspace = structurizr.workspace.getJson();

            if (workspace.properties === undefined) {
                workspace.properties = {};
            }
            workspace.properties['structurizr.dsl'] = structurizr.util.btoa(editor.getValue());
            workspace.views = structurizr.workspace.views;

            const jsonAsString = JSON.stringify(workspace);
            $('#workspaceAsJson').val(jsonAsString);

            localStorage.setItem(LOCAL_STORAGE_SOURCE, structurizr.util.btoa(editor.getValue()));
            localStorage.setItem(LOCAL_STORAGE_JSON, structurizr.util.btoa(jsonAsString));
        }

        return true;
    }

    var sourceVisible = true;
    var diagramsVisible = true;

    function sourceButtonClicked(e) {
        e.preventDefault();
        if (sourceVisible === false || diagramsVisible === false) {
            showSourceAndDiagrams();
        } else {
            hideDiagrams();
        }

        editor.focus();
    }

    function diagramsButtonClicked(e) {
        e.preventDefault();
        if (diagramsVisible === false || sourceVisible === false) {
            showSourceAndDiagrams();
        } else {
            hideSource();
        }

        $('#diagramEditorIframe').focus();
    }

    function hideSource() {
        $('#sourcePanel').addClass('hidden');
        $('#diagramsPanel').removeClass('col-sm-6');

        sourceVisible = false;
        $('#diagramNavButtons').removeClass('hidden');
        resize();
    }

    function showSourceAndDiagrams() {
        $('#sourcePanel').removeClass('hidden');
        $('#sourcePanel').addClass('col-sm-6');
        $('#diagramsPanel').removeClass('hidden');
        $('#diagramsPanel').addClass('col-sm-6');

        $('#diagramNavButtons').addClass('hidden');

        sourceVisible = true;
        diagramsVisible = true;

        resize();
    }

    function hideDiagrams() {
        $('#diagramsPanel').addClass('hidden');
        $('#sourcePanel').removeClass('col-sm-6');

        diagramsVisible = false;
        resize();
    }

    function hideSourceAndDiagrams(message) {
        $('#sourcePanel').addClass('hidden');
        $('#diagramsPanel').addClass('hidden');
    }

    function importSourceFile(files) {
        if (files && files.length > 0) {
            var reader = new FileReader();
            reader.onload = function (evt) {
                var content = evt.target.result;
                editor.setValue(content, -1);
            };

            reader.readAsText(files[0]);
        }
    }

    var searchParams = new URLSearchParams(window.location.search);
    if (searchParams.get('src')) {
        hideSource();
    }
</script>

<%@ include file="/WEB-INF/fragments/workspace/load-via-inline.jspf" %>