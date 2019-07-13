package tools.redfox.bamboo.notifications.slack.services;

import com.atlassian.bamboo.jira.jiraissues.JiraIssueDetailsBuilderImpl;
import com.atlassian.bamboo.jira.jiraissues.LinkedJiraIssue;
import com.atlassian.bamboo.jira.jirametadata.JiraTypeImpl;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.BambooComponent;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import tools.redfox.bamboo.notifications.slack.action.SlackConfigurationAction;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@BambooComponent
public class JiraClient {
    private URI uri;
    private String password;
    private String username;
    private JiraRestClient client;

    private static final Logger logger = LoggerFactory.getLogger(JiraClient.class);

    @Autowired
    public JiraClient(PluginSettingsFactory pluginSettingsFactory) {
        PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        try {
            uri = new URI((String) settings.get(SlackConfigurationAction.PLUGIN_STORAGE_KEY + SlackConfigurationAction.SLACK_BOT_JIRA_URL));
            username = (String) settings.get(SlackConfigurationAction.PLUGIN_STORAGE_KEY + SlackConfigurationAction.SLACK_BOT_JIRA_USERNAME);
            password = (String) settings.get(SlackConfigurationAction.PLUGIN_STORAGE_KEY + SlackConfigurationAction.SLACK_BOT_JIRA_PASSWORD);
        } catch (URISyntaxException | NullPointerException e) {
            logger.info("Jira integration for slack is disabled due to missing config");
        }
    }

    public void getIssueDetails(Set<LinkedJiraIssue> issues) {
        Map<String, LinkedJiraIssue> mappedIssues = new HashMap<String, LinkedJiraIssue>() {{
            for (LinkedJiraIssue issue : issues) {
                put(issue.getIssueKey(), issue);
            }
        }};

        getClient()
                .getSearchClient().searchJql("issueKey = BT-1")
                .done(searchResult -> {
                    searchResult.getIssues().forEach(issue -> {
                        mappedIssues.get(issue.getKey()).setJiraIssueDetails(
                                new JiraIssueDetailsBuilderImpl()
                                        .issueKey(issue.getKey())
                                        .summary(issue.getSummary())
                                        .type(new JiraTypeImpl(issue.getIssueType().getName(), ""))
                                        .build()
                        );
                    });
                })
                .fail(r -> {
                    resetClient();
                })
                .claim();
    }

    protected JiraRestClient getClient() {
        if (client == null) {
            AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
            client = factory.createWithBasicHttpAuthentication(uri, username, password);
        }

        return client;
    }

    public boolean isConfigured() {
        return uri != null && username != null && password != null;
    }

    public void resetClient() {
        client = null;
    }
}
