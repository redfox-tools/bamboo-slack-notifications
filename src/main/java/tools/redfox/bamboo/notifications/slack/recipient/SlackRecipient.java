package tools.redfox.bamboo.notifications.slack.recipient;

import com.atlassian.bamboo.notification.NotificationTransport;
import com.atlassian.bamboo.notification.recipients.AbstractNotificationRecipient;
import com.atlassian.bamboo.template.TemplateRenderer;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.utils.error.SimpleErrorCollection;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import tools.redfox.bamboo.notifications.slack.slack.SlackService;
import tools.redfox.bamboo.notifications.slack.transport.SlackTransport;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SlackRecipient extends AbstractNotificationRecipient {
    private TemplateRenderer templateRenderer;
    private SlackService slackService;
    private Map<String, String> settings = new HashMap<>();

    @Autowired
    public SlackRecipient(@ComponentImport TemplateRenderer templateRenderer, SlackService slackService) {
        this.templateRenderer = templateRenderer;
        this.slackService = slackService;
    }

    @Override
    public void init(String configurationData) {
        Gson gson = new Gson();
        this.settings = gson.fromJson(configurationData, new TypeToken<Map<String, String>>() {
        }.getType());
    }

    @Override
    public List<NotificationTransport> getTransports() {
        return new LinkedList<NotificationTransport>() {{
            add(new SlackTransport(slackService));
        }};
    }

    @Override
    public String getRecipientConfig() {
        Gson gson = new Gson();
        return gson.toJson(settings);
    }

    @Override
    public void populate(Map<String, String[]> params) {
        settings.put("notificationSlackChannel", getParam("notificationSlackChannel", params));
    }

    @Override
    public ErrorCollection validate(Map<String, String[]> params) {
        ErrorCollection errorCollection = new SimpleErrorCollection();

        if (!slackService.isConfigured()) {
            errorCollection.addError("slack", "You need to configure Slack integration");
            return errorCollection;
        }

        String[] channel = params.get("notificationSlackChannel");
        if (channel != null && channel.length != 0) {
            settings.put("notificationSlackChannel", this.getParam("notificationSlackChannel", params));
            if (settings.get("notificationSlackChannel").isEmpty()) {
                errorCollection.addError("notificationSlackChannel", "Please enter a valid channel name");
            }
        }

        return errorCollection;
    }

    public Map<String, String> getSettings() {
        return settings;
    }

    @Override
    public String getEditHtml() {
        String editTemplateLocation = this.notificationRecipientModuleDescriptor.getEditTemplate();
        Map<String, Object> context = new HashMap<>();
        settings.forEach(context::put);
        context.put("configured", slackService.isConfigured());

        return StringUtils.defaultString(this.templateRenderer.render(editTemplateLocation, context));
    }
}
