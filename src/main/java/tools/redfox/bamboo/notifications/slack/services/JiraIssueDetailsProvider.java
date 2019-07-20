package tools.redfox.bamboo.notifications.slack.services;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.application.jira.JiraApplicationType;
import com.atlassian.bamboo.applinks.ImpersonationService;
import com.atlassian.bamboo.jira.jiraissues.JiraIssueDetails;
import com.atlassian.bamboo.jira.jiraissues.JiraRemoteIssueManager;
import com.atlassian.bamboo.jira.jiraissues.LinkedJiraIssue;
import com.atlassian.bamboo.resultsummary.ImmutableResultsSummary;
import com.atlassian.bamboo.utils.fage.Result;
import com.atlassian.plugin.spring.scanner.annotation.component.BambooComponent;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.github.seratch.jslack.api.model.block.DividerBlock;
import com.github.seratch.jslack.api.model.block.LayoutBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import tools.redfox.bamboo.notifications.slack.action.SlackConfigurationAction;
import tools.redfox.bamboo.notifications.slack.utils.BlockUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@BambooComponent
public class JiraIssueDetailsProvider {
    private final String integrationUser;
    private BlockUtils blockUtils;
    private final ImpersonationService impersonationService;
    private final ApplicationLinkService applicationLinkService;
    private JiraRemoteIssueManager jiraRemoteIssueManager;
    private static final Logger logger = LoggerFactory.getLogger(JiraIssueDetailsProvider.class);

    @Autowired
    public JiraIssueDetailsProvider(
            BlockUtils blockUtils,
            @ComponentImport ImpersonationService impersonationService,
            @ComponentImport ApplicationLinkService applicationLinkService,
            @ComponentImport JiraRemoteIssueManager jiraRemoteIssueManager,
            @ComponentImport PluginSettingsFactory pluginSettingsFactory
    ) {
        this.blockUtils = blockUtils;
        this.impersonationService = impersonationService;
        this.applicationLinkService = applicationLinkService;
        this.jiraRemoteIssueManager = jiraRemoteIssueManager;

        PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        integrationUser = (String)settings.get(SlackConfigurationAction.PLUGIN_STORAGE_KEY + SlackConfigurationAction.SLACK_BOT_JIRA_BAMBOO_USERNAME);
    }

    protected Set<LinkedJiraIssue> getJiraIssues(ImmutableResultsSummary resultsSummary) {
        Set<LinkedJiraIssue> issues;
        if (resultsSummary == null || (issues = resultsSummary.getJiraIssues()).size() == 0) {
            return null;
        }

        return issues;
    }

    public void attach(List<LayoutBlock> blocks, ImmutableResultsSummary resultsSummary) {
        Set<LinkedJiraIssue> issues = getJiraIssues(resultsSummary);
        if (issues == null) {
            return;
        }

        blocks.addAll(
                blockUtils.section(
                        "Jira issues",
                        (integrationUser.isEmpty() ? issues : details(issues)).stream().map(blockUtils::markdownLink).collect(Collectors.toList())
                )
        );
        blocks.add(new DividerBlock());
    }

    public List<JiraIssueDetails> details(Set<LinkedJiraIssue> issues) {
        Set<JiraIssue> issueList = issues
                .stream()
                .map(i -> new JiraIssue(i.getIssueKey()))
                .collect(Collectors.toSet());

        try {
            return impersonationService.runAsUser(integrationUser, (Callable<List<JiraIssueDetails>>) () -> {
                ApplicationLink link = applicationLinkService.getPrimaryApplicationLink(JiraApplicationType.class);
                List<JiraIssueDetails> details = new LinkedList<>();

                for (String project : issueList.stream().map(JiraIssue::getProject).collect(Collectors.toSet())) {
                    Set<String> projectIssues = issueList.stream().filter(i -> i.getProject().equals(project)).map(JiraIssue::toString).collect(Collectors.toSet());
                    Result<CredentialsRequiredException, List<JiraIssueDetails>> result = jiraRemoteIssueManager.fetchIssueDetailsFromJira(link, project, projectIssues);
                    details.addAll(result.getResult());
                }

                return details;
            }).call();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return Collections.emptyList();
    }

    private class JiraIssue {
        private final String issue;
        private final String project;

        JiraIssue(String issue) {
            String[] parts = issue.split("-");
            this.project = parts[0];
            this.issue = parts[1];
        }

        public String getIssue() {
            return issue;
        }

        public String getProject() {
            return project;
        }

        @Override
        public String toString() {
            return project + "-" + issue;
        }
    }
}
