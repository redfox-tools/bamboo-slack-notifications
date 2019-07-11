<html>
<head>
    <meta name="decorator" content="atl.admin">
    <title>Slack integration settings</title>
</head>
<body>
[@ui.bambooSection titleKey="tools.redfox.bamboo.notifications.sentry.configuration.section.title" descriptionKey="tools.redfox.bamboo.notifications.sentry.global.description"]
[#--    [#if saved ]--]
[#--        <div class="aui-message success">--]
[#--            Sentry configuration has been updated.        <span class="aui-icon icon-success"></span>--]
[#--        </div>--]
[#--    [/#if]--]
    <form id="slackSettings" method="post" class="aui">
        <div class="field-group required">
            [@ww.textfield labelKey="tools.redfox.bamboo.notifications.slack.oauth.bot.label" name="slackOAuthBot" class="long-field" required="true"/]
        </div>
        <div class="buttons-container">
            <div class="buttons">
                <input type="submit" name="save" value="Save" class="assistive"
                       id="updateSlackSettings_defaultSave" tabindex="-1">
                <input type="submit" name="save" value="Save" class="aui-button aui-button-primary"
                       id="updateSlackSettings_save" accesskey="S">
                <a class="cancel" accesskey="`" href="${cancelUrl}">Cancel</a>
            </div>
        </div>
        <input type="hidden" name="atl_token" value="${action.getToken()}" />
    </form>
[/@ui.bambooSection]
</body>
</html>
