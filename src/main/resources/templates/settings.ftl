<html>
<head>
    <meta name="decorator" content="atl.admin">
    <title>Slack integration settings</title>
</head>
<body>
[#if saved ]
    <div class="aui-message success">
        Slack configuration has been updated.        <span class="aui-icon icon-success"></span>
    </div>
    <br/>
[/#if]
[@ui.bambooSection titleKey="tools.redfox.bamboo.notifications.slack.configuration.title" descriptionKey="tools.redfox.bamboo.notifications.slack.configuration.description"]
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
<br />
<br />
${webResourceManager.requireResource("tools.redfox.bamboo.slack-notifications:howto")}
[@ui.bambooSection title="How to configure your Slack workspace"]
    <div class="aui-message info">
        <b>Why do I need to setup <a href="https://api.slack.com/internal-integrations">internal application</a></b><br/>
        <ul>
            <li>Slack is deprecating legacy incoming webhooks (<a href="https://api.slack.com/custom-integrations/incoming-webhooks">more</a>)</li>
            <li>This integration is using chat <a href="https://api.slack.com/methods">API methods</a> for interactive messages</li>
            <li>Future versions of the application will include <a href="https://api.slack.com/slash-commands">slash commands</a></li>
        </ul>
        <span class="aui-icon icon-info"></span>
    </div>

    <h3>1. Create new Slack application</h3>
    <p>Go to <a href="https://api.slack.com/apps?new_app=1">https://api.slack.com/apps?new_app=1</a> and fill the form</p>
    <img src="${req.contextPath}/download/resources/tools.redfox.bamboo.slack-notifications:howto/01_create_app.png">

    <h3>2. Configure bot</h3>
    <img src="${req.contextPath}/download/resources/tools.redfox.bamboo.slack-notifications:howto/02_configure_bot.png">

    <h3>3. Add bot user</h3>
    <img src="${req.contextPath}/download/resources/tools.redfox.bamboo.slack-notifications:howto/03_add_bot_user.png">

    <h3>4. Configure bot user</h3>
    <img src="${req.contextPath}/download/resources/tools.redfox.bamboo.slack-notifications:howto/04_configure_bot_user.png">

    <h3>5. Configure permissions</h3>
    <img src="${req.contextPath}/download/resources/tools.redfox.bamboo.slack-notifications:howto/05_configure_permissions.png">

    <h3>6. Install application</h3>
    <img src="${req.contextPath}/download/resources/tools.redfox.bamboo.slack-notifications:howto/06_install_app.png">

    <h3>7. Accept new application</h3>
    <img src="${req.contextPath}/download/resources/tools.redfox.bamboo.slack-notifications:howto/07_accept.png">

    <h3>8. Get bot token</h3>
    <img src="${req.contextPath}/download/resources/tools.redfox.bamboo.slack-notifications:howto/08_token.png">

[/@ui.bambooSection]
</body>
</html>
