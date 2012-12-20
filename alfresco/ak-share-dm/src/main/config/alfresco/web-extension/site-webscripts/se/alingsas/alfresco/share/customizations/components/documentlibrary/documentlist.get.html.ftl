<@markup id="customDocLibView" target="documentListContainer" action="after">
    <script type="text/javascript">//<![CDATA[
        YAHOO.Bubbling.subscribe("postSetupViewRenderers", function(layer, args) {
            var scope = args[1].scope;
            var newDetailedViewRenderer = new Alfresco.DocumentListNewDetailedViewRenderer("newdetailed");
            scope.registerViewRenderer(newDetailedViewRenderer);
        });
    //]]></script>
</@>