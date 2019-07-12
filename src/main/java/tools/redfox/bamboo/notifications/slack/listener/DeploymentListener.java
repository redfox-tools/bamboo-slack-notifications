package tools.redfox.bamboo.notifications.slack.listener;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.bamboo.deployments.environments.Environment;
import com.atlassian.bamboo.deployments.execution.events.DeploymentEvent;
import com.atlassian.bamboo.deployments.execution.events.DeploymentFinishedEvent;
import com.atlassian.bamboo.deployments.execution.events.DeploymentStartedEvent;
import com.atlassian.bamboo.deployments.projects.DeploymentProject;
import com.atlassian.bamboo.deployments.projects.service.DeploymentProjectService;
import com.atlassian.bamboo.deployments.results.DeploymentResult;
import com.atlassian.bamboo.deployments.results.service.DeploymentResultService;
import com.atlassian.bamboo.deployments.versions.DeploymentVersion;
import com.atlassian.bamboo.deployments.versions.service.DeploymentVersionService;
import com.atlassian.bamboo.event.HibernateEventListenerAspect;
import com.atlassian.bamboo.resultsummary.ResultsSummary;
import com.atlassian.bamboo.resultsummary.ResultsSummaryManager;
import com.atlassian.bamboo.trigger.TriggerDefinition;
import com.atlassian.event.api.EventListener;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.model.block.ContextBlock;
import com.github.seratch.jslack.api.model.block.DividerBlock;
import com.github.seratch.jslack.api.model.block.LayoutBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import tools.redfox.bamboo.notifications.slack.persistence.model.SlackNotification;
import tools.redfox.bamboo.notifications.slack.slack.SlackService;
import tools.redfox.bamboo.notifications.slack.utils.BlockUtils;
import tools.redfox.bamboo.notifications.slack.utils.EmojiResolver;
import tools.redfox.bamboo.notifications.slack.utils.EntityUtils;
import tools.redfox.bamboo.notifications.slack.utils.UrlProvider;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

public class DeploymentListener {
    @ComponentImport
    private final DeploymentVersionService deploymentVersionService;
    @ComponentImport
    private final ResultsSummaryManager resultsSummaryManager;
    @ComponentImport
    private DeploymentProjectService deploymentProjectService;
    @ComponentImport
    private ActiveObjects ao;

    private SlackService slack;
    private UrlProvider urlProvider;
    private BlockUtils blockUtils;
    private EntityUtils entityUtils;
    private final DeploymentResultService deploymentResultService;

    private static Logger logger = LoggerFactory.getLogger(DeploymentListener.class);

    @Autowired
    public DeploymentListener(
            @ComponentImport DeploymentResultService deploymentResultService,
            @ComponentImport DeploymentVersionService deploymentVersionService,
            @ComponentImport ResultsSummaryManager resultsSummaryManager,
            @ComponentImport DeploymentProjectService deploymentProjectService,
            @ComponentImport ActiveObjects ao,
            SlackService slack,
            UrlProvider urlProvider,
            BlockUtils blockUtils,
            EntityUtils entityUtils
    ) {
        this.deploymentResultService = deploymentResultService;
        this.deploymentVersionService = deploymentVersionService;
        this.resultsSummaryManager = resultsSummaryManager;
        this.deploymentProjectService = deploymentProjectService;
        this.slack = slack;
        this.ao = ao;
        this.urlProvider = urlProvider;
        this.blockUtils = blockUtils;
        this.entityUtils = entityUtils;
    }

    @EventListener
    @HibernateEventListenerAspect
    public void onDeploymentStartedEvent(DeploymentStartedEvent event) {
        handleDeployementEvent(event);
    }

    @EventListener
    @HibernateEventListenerAspect
    public void onDeploymentFinishedEvent(DeploymentFinishedEvent event) {
        handleDeployementEvent(event);
    }

    private void handleDeployementEvent(DeploymentEvent event) {
        DeploymentResult deploymentResult = deploymentResultService.getDeploymentResult(event.getDeploymentResultId());
        DeploymentVersion version = deploymentResult.getDeploymentVersion();
        ResultsSummary buildResult = resultsSummaryManager.getResultsSummary(
                Objects.requireNonNull(deploymentVersionService.getRelatedPlanResultKeys(version.getId()).stream().findFirst().orElse(null))
        );
        DeploymentProject deploymentProject = deploymentProjectService.getDeploymentProjectForVersion(version.getId());
        Map<Long, DeploymentResult> deploymentResults = new HashMap<Long, DeploymentResult>() {{
            for (DeploymentResult result : deploymentResultService.getDeploymentResultsForDeploymentVersion(version.getId())) {
                put(result.getEnvironment().getId(), result);
            }
        }};

        String masterEnvironmentId = getParentEnvironmentId(deploymentResult.getEnvironment());
        DeploymentResult masterDeployment = null;
        if (Long.valueOf(masterEnvironmentId) != deploymentResult.getEnvironment().getId()) {
            masterDeployment = deploymentResultService.getDeploymentResultsForDeploymentVersionAndEnvironment(version.getId(), Long.valueOf(masterEnvironmentId)).stream().findFirst().orElse(null);
        }
        if (masterDeployment == null) {
            masterDeployment = deploymentResult;
        }

        List<LayoutBlock> blocks = new LinkedList<>();
        blocks.add(blockUtils.header(getHeadline(deploymentResult, buildResult, event)));
        blocks.add(blockUtils.context(entityUtils.triggerReason(masterDeployment.getTriggerReason())));
        blocks.add(new DividerBlock());

        blocks.addAll(
                new LinkedList<ContextBlock>() {{
                    for (Environment environment : deploymentProject.getEnvironments().stream().filter(e -> filterEnvironment(e, masterEnvironmentId)).collect(Collectors.toList())) {
                        DeploymentResult result = deploymentResults.getOrDefault(environment.getId(), null);
                        add(blockUtils.context(
                                MessageFormat.format(
                                        "{0} *{1}* ({2})",
                                        EmojiResolver.emoji(result),
                                        entityUtils.environment(environment),
                                        entityUtils.deployment(result)
                                )
                        ));
                    }
                }}
        );

        ao.executeInTransaction(
                new TransactionCallback<Object>() {
                    @Override
                    public Object doInTransaction() {
                        SlackNotification notification;
                        try {
                            notification = ao.find(
                                    SlackNotification.class,
                                    "ENTITY_KEY = ? AND REL_ENTITY_TYPE = ? ",
                                    version.getId(),
                                    version.getClass().getSimpleName()
                            )[0];
                        } catch (Exception e) {
                            notification = ao.create(SlackNotification.class);
                            notification.setEntityKey(version.getId());
                            notification.setRelEntityType(version.getClass().getSimpleName());
                        }

                        try {
                            notification.setMessageTs(slack.send("general", blocks, notification.getMessageTs()));
                            notification.save();
                            ao.flushAll();
                        } catch (IOException | SlackApiException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                }
        );


//        if (!notifications.getSortedNotificationRules().stream().anyMatch(n -> n.getRecipientType().equals("tools.redfox.bamboo.slack-notifications:slackNotification"))) {
//            logger.info("Ignore deployment as environment '{}' is not configured with Sentry Notification", result.getEnvironment().getName());
//            return;
//        }
    }

    private boolean filterEnvironment(Environment environment, String masterEnvironmentId) {
        return masterEnvironmentId.equals(String.valueOf(environment.getId()))
                || masterEnvironmentId.equals(getParentEnvironmentId(environment));
    }

    private String getParentEnvironmentId(Environment environment) {
        return environment
                .getTriggerDefinitions()
                .stream()
                .filter(TriggerDefinition::isEnabled)
                .map(t -> t.getConfiguration().getOrDefault("deployment.trigger.afterSuccessfulDeployment.triggeringEnvironmentId", "-1"))
                .filter(t -> !t.equals("-1"))
                .findFirst()
                .orElse(String.valueOf(environment.getId()));
    }

    private String getHeadline(DeploymentResult result, ResultsSummary buildResult, DeploymentEvent event) {
        String headline;
        if (event instanceof DeploymentStartedEvent) {
            headline = "Deploying version <{2}|{3}> of <{0}|{1}>";
        } else {
            headline = "Version <{2}|{3}> of <{0}|{1}> deployed";
        }
        return MessageFormat.format(
                headline,
                urlProvider.projectPage(buildResult.getImmutablePlan().getProject().getName()),
                buildResult.getImmutablePlan().getProject().getName(),
                urlProvider.version(result.getDeploymentVersion().getId()),
                result.getDeploymentVersionName()
        );
    }

}
