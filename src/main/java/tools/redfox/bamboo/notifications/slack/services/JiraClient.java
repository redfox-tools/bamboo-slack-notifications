package tools.redfox.bamboo.notifications.slack.services;

import com.atlassian.bamboo.jira.jiraissues.InternalLinkedJiraIssue;
import com.atlassian.bamboo.jira.jiraissues.JiraIssueDetailsBuilderImpl;
import com.atlassian.bamboo.jira.jiraissues.LinkedJiraIssue;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.spring.scanner.annotation.component.BambooComponent;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import kong.unirest.GetRequest;
import kong.unirest.Unirest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import tools.redfox.bamboo.notifications.slack.action.SlackConfigurationAction;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Set;
import java.util.stream.Collectors;

@BambooComponent
public class JiraClient {
    private URI uri;
    private String password;
    private String username;

    private static final Logger logger = LoggerFactory.getLogger(JiraClient.class);

    @Autowired
    public JiraClient(@ComponentImport PluginAccessor pluginAccessor, PluginSettingsFactory pluginSettingsFactory) {
        PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        String pluginVersion = pluginAccessor.getPlugin("tools.redfox.bamboo.slack-notifications").getPluginInformation().getVersion();

        try {
            uri = new URI((String) settings.get(SlackConfigurationAction.PLUGIN_STORAGE_KEY + SlackConfigurationAction.SLACK_BOT_JIRA_URL));
            username = (String) settings.get(SlackConfigurationAction.PLUGIN_STORAGE_KEY + SlackConfigurationAction.SLACK_BOT_JIRA_USERNAME);
            password = (String) settings.get(SlackConfigurationAction.PLUGIN_STORAGE_KEY + SlackConfigurationAction.SLACK_BOT_JIRA_PASSWORD);
        } catch (URISyntaxException | NullPointerException e) {
            logger.info("Jira integration for slack is disabled due to missing config");
        }

        Unirest
                .config()
                .setDefaultHeader("Content-Type", "application/json")
                .setDefaultHeader("User-Agent", String.format("BambooSlackPlugin/%s", pluginVersion))
                .setDefaultBasicAuth(username, password);
    }

    public void getIssueDetails(Set<LinkedJiraIssue> issues) {
        String jql = "issueKeys IN " + issues.stream().map(InternalLinkedJiraIssue::getIssueKey).collect(Collectors.joining(", ","[","]"));
        GetRequest result = Unirest.get(uri + "/rest/api/2/search?jql=issueKey IN " + URLEncoder.encode(jql));
        int i = 1;
    }

    public boolean isConfigured() {
        return uri != null && username != null && password != null;
    }

    public void resetClient() {
    }
}
