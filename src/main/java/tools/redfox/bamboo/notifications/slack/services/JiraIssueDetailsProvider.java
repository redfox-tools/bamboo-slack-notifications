package tools.redfox.bamboo.notifications.slack.services;

import com.atlassian.bamboo.jira.jiraissues.JiraRemoteIssueManager;
import com.atlassian.bamboo.jira.jiraissues.LinkedJiraIssue;
import com.atlassian.bamboo.resultsummary.ImmutableResultsSummary;
import com.atlassian.plugin.spring.scanner.annotation.component.BambooComponent;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.github.seratch.jslack.api.model.block.DividerBlock;
import com.github.seratch.jslack.api.model.block.LayoutBlock;
import org.springframework.beans.factory.annotation.Autowired;
import tools.redfox.bamboo.notifications.slack.utils.BlockUtils;

import java.util.*;
import java.util.stream.Collectors;

@BambooComponent
public class JiraIssueDetailsProvider {
    private BlockUtils blockUtils;
    private JiraRemoteIssueManager jiraRemoteIssueManager;
    private JiraClient jiraClient;

    @Autowired
    public JiraIssueDetailsProvider(
            BlockUtils blockUtils,
            @ComponentImport JiraRemoteIssueManager jiraRemoteIssueManager,
            JiraClient jiraClient
    ) {
        this.blockUtils = blockUtils;
        this.jiraRemoteIssueManager = jiraRemoteIssueManager;
        this.jiraClient = jiraClient;
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

        getIssueDetails(issues);

        blocks.addAll(
                blockUtils.section(
                        "Jira issues",
                        issues.stream().map(blockUtils::markdownLink).collect(Collectors.toList())
                )
        );
        blocks.add(new DividerBlock());
    }

    protected void getIssueDetails(Set<LinkedJiraIssue> issues) {
        Map<String, Set<LinkedJiraIssue>> issuesByProject = new HashMap<>();

        for (LinkedJiraIssue issue : issues) {
            String projectKey = issue.getIssueKey().split("-")[0];
            if (!issuesByProject.containsKey(projectKey)) {
                issuesByProject.put(projectKey, new LinkedHashSet<>());
            }
            issuesByProject.get(projectKey).add(issue);
        }

        for (String key : issuesByProject.keySet()) {
//            Result<CredentialsRequiredException, List<JiraIssueDetails>> details = jiraRemoteIssueManager.fetchIssueDetailsFromJira(
//                    link,
//                    key,
//                    issuesByProject.get(key).stream().map(LinkedJiraIssue::getIssueKey).collect(Collectors.toSet())
//            );
            int i = 1;
        }
    }
}
