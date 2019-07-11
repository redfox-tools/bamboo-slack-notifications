package tools.redfox.bamboo.notifications.slack.listener;

import com.atlassian.bamboo.chains.ChainResultsSummary;
import com.atlassian.bamboo.chains.ChainStageResult;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.resultsummary.BuildResultsSummary;
import com.atlassian.bamboo.resultsummary.ResultsSummaryManager;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.variable.VariableContext;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.model.block.DividerBlock;
import com.github.seratch.jslack.api.model.block.LayoutBlock;
import org.jetbrains.annotations.NotNull;
import tools.redfox.bamboo.notifications.slack.services.CommitDetailsProvider;
import tools.redfox.bamboo.notifications.slack.services.JiraIssueDetailsProvider;
import tools.redfox.bamboo.notifications.slack.slack.SlackService;
import tools.redfox.bamboo.notifications.slack.utils.BlockUtils;
import tools.redfox.bamboo.notifications.slack.utils.EntityUtils;
import tools.redfox.bamboo.notifications.slack.utils.UrlProvider;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

abstract public class AbstractActionListener {
    private ResultsSummaryManager resultsSummaryManager;
    private SlackService slack;
    private UrlProvider urlProvider;
    private EntityUtils entityUtils;
    private BlockUtils blockUtils;
    private JiraIssueDetailsProvider jiraIssueDetailsProvider;
    private CommitDetailsProvider commitDetailsProvider;

    public enum Notification {
        STARTED,
        JOB,
        FINISHED
    }

    public void sendNotification(BuildContext buildContext, ChainResultsSummary resultsSummary, Notification notification) {
        try {
            List<LayoutBlock> blocks = new LinkedList<>();

            blocks.add(blockUtils.header(getHeadline(buildContext, notification)));
            blocks.add(blockUtils.context(getAuthor(buildContext)));
            blocks.add(new DividerBlock());

            for (ChainStageResult result : ((ChainResultsSummary) resultsSummaryManager.getResultsSummary(buildContext.getPlanResultKey())).getStageResults()) {
                for (BuildResultsSummary jobResult : result.getBuildResults()) {
                    blocks.add(blockUtils.context(getJobSummary(result, jobResult)));
                }
            }
            blocks.add(new DividerBlock());

            commitDetailsProvider.attach(blocks, buildContext);
            jiraIssueDetailsProvider.attach(blocks, resultsSummary);

            VariableContext variables = buildContext.getVariableContext();
            String messageTs = slack.send("general", blocks, variables.getResultVariables().get("custom.bamboo.slack.build.message"));
            variables.addResultVariable("custom.bamboo.slack.build.message", messageTs);
        } catch (IOException | SlackApiException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    private String getJobSummary(ChainStageResult result, BuildResultsSummary jobResult) {
        return entityUtils.jobResult(result.getName(), jobResult);
    }

    @NotNull
    private String getAuthor(BuildContext buildContext) {
        return entityUtils.triggerReason(buildContext.getTriggerReason());
    }

    @NotNull
    private String getHeadline(BuildContext buildContext, Notification notification) {
        String headline = new HashMap<Notification, String>() {{
            put(Notification.STARTED, "Starting build #<{0}|{1}> of <{2}|{3}>");
            put(Notification.JOB, "Building <{2}|{3}> (#<{0}|{1}>)");
            put(Notification.FINISHED, "Build #<{0}|{1}> of <{2}|{3}> completed. ");
        }}.getOrDefault(notification, "Unknown event");

        return MessageFormat.format(
                headline,
                urlProvider.buildResult(buildContext.getBuildResultKey()),
                buildContext.getBuildNumber(),
                urlProvider.projectPage(buildContext.getProjectName()),
                buildContext.getProjectName()
        );
    }

    public void setResultsSummaryManager(ResultsSummaryManager resultsSummaryManager) {
        this.resultsSummaryManager = resultsSummaryManager;
    }

    public void setSlack(SlackService slack) {
        this.slack = slack;
    }

    public void setUrlProvider(UrlProvider urlProvider) {
        this.urlProvider = urlProvider;
    }

    public void setEntityUtils(EntityUtils entityUtils) {
        this.entityUtils = entityUtils;
    }

    public void setBlockUtils(BlockUtils blockUtils) {
        this.blockUtils = blockUtils;
    }

    public void setJiraIssueDetailsProvider(JiraIssueDetailsProvider jiraIssueDetailsProvider) {
        this.jiraIssueDetailsProvider = jiraIssueDetailsProvider;
    }

    public void setCommitDetailsProvider(CommitDetailsProvider commitDetailsProvider) {
        this.commitDetailsProvider = commitDetailsProvider;
    }
}
