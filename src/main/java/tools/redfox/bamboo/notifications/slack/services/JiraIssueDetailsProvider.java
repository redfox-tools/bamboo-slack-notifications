package tools.redfox.bamboo.notifications.slack.services;

import com.atlassian.bamboo.chains.ChainResultsSummary;
import com.atlassian.bamboo.jira.jiraissues.LinkedJiraIssue;
import com.atlassian.plugin.spring.scanner.annotation.component.BambooComponent;
import com.github.seratch.jslack.api.model.block.DividerBlock;
import com.github.seratch.jslack.api.model.block.LayoutBlock;
import org.springframework.beans.factory.annotation.Autowired;
import tools.redfox.bamboo.notifications.slack.utils.BlockUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@BambooComponent
public class JiraIssueDetailsProvider {
    private BlockUtils blockUtils;

    @Autowired
    public JiraIssueDetailsProvider(BlockUtils blockUtils) {
        this.blockUtils = blockUtils;
    }

    protected Set<LinkedJiraIssue> getJiraIssues(ChainResultsSummary resultsSummary) {
        Set<LinkedJiraIssue> issues;
        if (resultsSummary == null || (issues = resultsSummary.getJiraIssues()).size() == 0) {
            return null;
        }

        return issues;
    }

    public void attach(List<LayoutBlock> blocks, ChainResultsSummary resultsSummary) {
        Set<LinkedJiraIssue> issues = getJiraIssues(resultsSummary);
        if (issues == null) {
            return;
        }

        blocks.addAll(
                blockUtils.section(
                        "Jira issues",
                        issues.stream().map(blockUtils::markdownLink).collect(Collectors.toList())
                )
        );
        blocks.add(new DividerBlock());
    }
}
