package tools.redfox.bamboo.notifications.slack.services;

import com.atlassian.bamboo.jira.jiraissues.LinkedJiraIssue;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.domain.Issue;
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
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@BambooComponent
public class JiraClient {
    private JiraRestClient client;

    private static final Logger logger = LoggerFactory.getLogger(JiraClient.class);

    @Autowired
    public JiraClient(PluginSettingsFactory pluginSettingsFactory) {
        PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        URI uri = null;
        try {
            uri = new URI((String) settings.get(SlackConfigurationAction.PLUGIN_STORAGE_KEY + ".jira.url"));
            client = factory.createWithBasicHttpAuthentication(
                    uri,
                    (String) settings.get(SlackConfigurationAction.PLUGIN_STORAGE_KEY + ".jira.username"),
                    (String) settings.get(SlackConfigurationAction.PLUGIN_STORAGE_KEY + ".jira.password")
            );
        } catch (URISyntaxException | NullPointerException e) {
            logger.info("Jira integration for slack is disabled due to missing config");
        }
    }

    public Set<Issue> getIssueDetails(Set<LinkedJiraIssue> issues) {
        Set<Issue> remoteIssues = new LinkedHashSet<>();


        client.getSearchClient().searchJql("issueKey = BT-1").done(searchResult -> {
            remoteIssues.addAll(
                    StreamSupport
                            .stream(searchResult.getIssues().spliterator(), false)
                            .collect(Collectors.toSet())
            );
        });

        return remoteIssues;
    }
}
