[#if !configured ]
    <div class="aui-message error">
        You need to configure slack integration before using this notification. <span
                class="aui-icon icon-error"></span>
    </div>
[#else]
    [@ww.textfield label="Channel" name="notificationSlackChannel" value="${notificationSlackChannel!}" class="long-field" required="true"/]
[/#if]

<script>
    AJS.toInit(function () {
        var recipientType = AJS.$("#notification_notificationRecipientType option");
        AJS.$("#notification_conditionKey").on("change", (e) => {
            var selected = AJS.$(e.target).val();
            var hide = (selected === "tools.redfox.bamboo.slack-notifications:slack.buildProgress" || selected === "tools.redfox.bamboo.slack-notifications:slack.deploymentProgress");

            recipientType.each((idx, el, y) => {
                console.log(idx, el, y);
                var el = AJS.$(el);
                el.prop("hidden", (hide && el.val() !== "tools.redfox.bamboo.slack-notifications:recipient.slack.channel"));
                el.prop("selected", el.val() === "tools.redfox.bamboo.slack-notifications:recipient.slack.channel").change();
            });
        })
    });
</script>
