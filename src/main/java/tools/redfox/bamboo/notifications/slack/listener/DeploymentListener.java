package tools.redfox.bamboo.notifications.slack.listener;

import com.atlassian.bamboo.build.JiraIssueResultsManager;
import com.atlassian.bamboo.deployments.environments.service.EnvironmentService;
import com.atlassian.bamboo.deployments.execution.events.DeploymentEvent;
import com.atlassian.bamboo.deployments.execution.events.DeploymentFinishedEvent;
import com.atlassian.bamboo.deployments.execution.events.DeploymentStartedEvent;
import com.atlassian.bamboo.deployments.results.DeploymentResult;
import com.atlassian.bamboo.deployments.results.service.DeploymentResultService;
import com.atlassian.bamboo.deployments.versions.DeploymentVersion;
import com.atlassian.bamboo.deployments.versions.service.DeploymentVersionService;
import com.atlassian.bamboo.jira.jiraissues.LinkedJiraIssue;
import com.atlassian.bamboo.notification.NotificationSet;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.atlassian.bamboo.resultsummary.ResultsSummary;
import com.atlassian.bamboo.resultsummary.ResultsSummaryManager;
import com.atlassian.event.api.EventListener;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;
import com.github.seratch.jslack.api.model.block.*;
import com.github.seratch.jslack.api.model.block.composition.MarkdownTextObject;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import tools.redfox.bamboo.notifications.slack.slack.SlackService;
import tools.redfox.bamboo.notifications.slack.utils.BlockUtils;
import tools.redfox.bamboo.notifications.slack.utils.EntityUtils;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;


public class DeploymentListener {
    private EnvironmentService environmentService;
    private DeploymentVersionService deploymentVersionService;
    private ResultsSummaryManager resultsSummaryManager;
    private JiraIssueResultsManager jiraIssueResultsManager;
    private SlackService slack;
    private BlockUtils blockUtils;
    private EntityUtils entityUtils;
    private final DeploymentResultService deploymentResultService;

    private static Logger logger = LoggerFactory.getLogger(DeploymentListener.class);

    @Autowired
    public DeploymentListener(
            @ComponentImport DeploymentResultService deploymentResultService,
            @ComponentImport EnvironmentService environmentService,
            @ComponentImport DeploymentVersionService deploymentVersionService,
            @ComponentImport ResultsSummaryManager resultsSummaryManager,
            @ComponentImport JiraIssueResultsManager jiraIssueResultsManager,
            SlackService slack,
            BlockUtils blockUtils,
            EntityUtils entityUtils
    ) {
        this.deploymentResultService = deploymentResultService;
        this.environmentService = environmentService;
        this.deploymentVersionService = deploymentVersionService;
        this.resultsSummaryManager = resultsSummaryManager;
        this.jiraIssueResultsManager = jiraIssueResultsManager;
        this.slack = slack;
        this.blockUtils = blockUtils;
        this.entityUtils = entityUtils;
    }

    @EventListener
    public void onDeploymentStartedEvent(DeploymentStartedEvent event) {
        handleDeployementEvent(event);
    }

    @EventListener
    public void onDeploymentFinishedEvent(DeploymentFinishedEvent event) {
        handleDeployementEvent(event);
    }

    private void handleDeployementEvent(DeploymentEvent event) {
        @Nullable DeploymentResult deploymentResult = deploymentResultService.getDeploymentResult(event.getDeploymentResultId());
        @Nullable DeploymentVersion version = deploymentResult.getDeploymentVersion();
        PlanResultKey planKey = deploymentVersionService.getRelatedPlanResultKeys(version.getId()).stream().findFirst().get();
        @Nullable ResultsSummary buildResult = resultsSummaryManager.getResultsSummary(planKey);
        NotificationSet notifications = environmentService.getNotificationSet(deploymentResult.getEnvironment().getId());

        try {
            List<LayoutBlock> blocks = new LinkedList<>();
            blocks.add(blockUtils.header(getHeadline(deploymentResult, buildResult, event)));
            blocks.add(blockUtils.context(entityUtils.triggerReason(deploymentResult.getTriggerReason())));
            blocks.add(new DividerBlock());

            List<LinkedJiraIssue> issues = jiraIssueResultsManager.findJiraIssuesForBuildResults(new LinkedList<ResultsSummary>() {{
                add(buildResult);
            }});

            if (issues.size() > 0) {
                blocks.add(ContextBlock.builder().elements(new LinkedList<ContextBlockElement>() {{
                    for (LinkedJiraIssue issue : issues) {
                        add(MarkdownTextObject.builder().text(issue.getIssueKey()).build());
                    }
                }}).build());
            }

            blocks.add(ContextBlock.builder().elements(new LinkedList<ContextBlockElement>() {{
                add(MarkdownTextObject.builder().text("<https://example.com|See more>").build());
            }}).build());

//            result.getEnvironment().g
        } catch (Exception e) {
            e.printStackTrace();
        }

//        if (!notifications.getSortedNotificationRules().stream().anyMatch(n -> n.getRecipientType().equals("tools.redfox.bamboo.slack-notifications:slackNotification"))) {
//            logger.info("Ignore deployment as environment '{}' is not configured with Sentry Notification", result.getEnvironment().getName());
//            return;
//        }
    }

    private String getHeadline(DeploymentResult result, ResultsSummary buildResult, DeploymentEvent event) {
        String headline;
        if (event instanceof DeploymentStartedEvent) {
            headline = "Deploying version {1} of {0} to {2}";
        } else {
            headline = "Version {1} of {0} deployed to {2} completed";
        }
        return MessageFormat.format(
                headline,
                buildResult.getImmutablePlan().getProject().getName(),
                result.getDeploymentVersionName(),
                result.getEnvironment().getKey()
        );
    }

    private String getAuthor(DeploymentResult result, DeploymentEvent event) {
        return result.getTriggerReason().getNameForSentence();
    }
}
