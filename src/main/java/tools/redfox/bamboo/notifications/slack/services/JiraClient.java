package tools.redfox.bamboo.notifications.slack.services;

import com.atlassian.bamboo.jira.jiraissues.InternalLinkedJiraIssue;
import com.atlassian.bamboo.jira.jiraissues.JiraIssueDetailsBuilderImpl;
import com.atlassian.bamboo.jira.jiraissues.LinkedJiraIssue;
import com.atlassian.bamboo.jira.jirametadata.JiraTypeImpl;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.spring.scanner.annotation.component.BambooComponent;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.google.gson.JsonObject;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import tools.redfox.bamboo.notifications.slack.action.SlackConfigurationAction;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
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
                .setDefaultHeader("accept", "application/json")
                .setDefaultHeader("User-Agent", String.format("BambooSlackPlugin/%s", pluginVersion))
                .setDefaultBasicAuth(username, password)
        ;
    }

    public void getIssueDetails(Set<LinkedJiraIssue> issues) {
        try {
            String jql = "issueKey IN " + issues.stream().map(InternalLinkedJiraIssue::getIssueKey).collect(Collectors.joining(", ", "(", ")"));
            Map<String, LinkedJiraIssue> mapped = new HashMap<String, LinkedJiraIssue>() {{
                issues.forEach(i -> put(i.getIssueKey(), i));
            }};

            HttpResponse<JsonNode> response = Unirest
                    .get(uri + "rest/api/2/search")
                    .queryString("jql", jql)
                    .asJson();

            for (Object issue : response.getBody().getObject().getJSONArray("issues")) {
                JSONObject i = (JSONObject) issue;
                mapped
                        .get(i.getString("key"))
                        .setJiraIssueDetails(
                                new JiraIssueDetailsBuilderImpl()
                                        .issueKey(i.getString("key"))
                                        .summary(i.getJSONObject("fields").getString("summary"))
                                        .type(new JiraTypeImpl(i.getJSONObject("fields").getJSONObject("issuetype").getString("name"), ""))
                                        .build()
                        );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isConfigured() {
        return uri != null && username != null && password != null;
    }
}
