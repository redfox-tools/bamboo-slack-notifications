<div class="aui-tabs horizontal-tabs">
    <ul class="tabs-menu">
        <li class="menu-item active-tab">
            <a href="#tabs-transport">Basic</a>
        </li>
        <li class="menu-item">
            <a href="#tabs-details">Advanced</a>
        </li>
    </ul>
    <div class="tabs-pane active-pane" id="tabs-transport">
        [@ww.textfield label="Webhook URL" name="notificationSlackUrl" value="${notificationSlackUrl!}" class="long-field" required="true"/]
        [@ww.textfield label="Channel" name="notificationSlackChannel" value="${notificationSlackChannel!}" class="long-field" required="true"/]
    </div>
    <div class="tabs-pane" id="tabs-details">
        <p>Specify individual URL's for each event. Leave empty field to ignore given event.</p>
    </div>
</div>

<script>
    AJS.toInit(function () {
        AJS.tabs.setup();
    });
</script>
