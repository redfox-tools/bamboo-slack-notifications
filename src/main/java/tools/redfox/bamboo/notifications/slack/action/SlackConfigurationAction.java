package tools.redfox.bamboo.notifications.slack.action;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.application.jira.JiraApplicationType;
import com.atlassian.bamboo.configuration.AdministrationConfigurationAccessor;
import com.atlassian.bamboo.deployments.projects.service.DeploymentProjectService;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.security.BambooPermissionManager;
import com.atlassian.bamboo.user.BambooUserManager;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.bamboo.ww2.aware.permissions.GlobalAdminSecurityAware;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.xsrf.XsrfTokenAccessor;
import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.response.channels.ChannelsListResponse;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SlackConfigurationAction extends BambooActionSupport implements GlobalAdminSecurityAware {
    public static final String SLACK_BOT_OAUTH_TOKEN = "slackOAuthBot";
    public static final String SLACK_BOT_JIRA_BAMBOO_USERNAME = "slackJiraBambooUsername";
    public static final String PLUGIN_STORAGE_KEY = "tools.redfox.bamboo.notifications.slack.";

    private final XsrfTokenAccessor xsrfTokenAccessor;
    private final PluginSettings pluginSettings;
    private final BambooUserManager bambooUserManager;
    private ApplicationLinkService applicationLinkService;

    @Autowired
    public SlackConfigurationAction(@ComponentImport BambooPermissionManager bambooPermissionManager,
                                    @ComponentImport DeploymentProjectService deploymentProjectService,
                                    @ComponentImport AdministrationConfigurationAccessor administrationConfigurationAccessor,
                                    @ComponentImport XsrfTokenAccessor xsrfTokenAccessor,
                                    @ComponentImport PluginSettingsFactory pluginSettingsFactory,
                                    @ComponentImport BambooUserManager bambooUserManager,
                                    @ComponentImport ApplicationLinkService applicationLinkService) {
        this.bambooPermissionManager = bambooPermissionManager;
        this.deploymentProjectService = deploymentProjectService;
        this.administrationConfigurationAccessor = administrationConfigurationAccessor;
        this.xsrfTokenAccessor = xsrfTokenAccessor;
        this.pluginSettings = pluginSettingsFactory.createGlobalSettings();
        this.bambooUserManager = bambooUserManager;
        this.applicationLinkService = applicationLinkService;
        setBambooPermissionManager(bambooPermissionManager);
        setDeploymentProjectService(deploymentProjectService);
        setAdministrationConfigurationAccessor(administrationConfigurationAccessor);
        setPlanManager(planManager);
    }

    @Override
    public void validate() {
        HttpServletRequest request = ServletActionContext.getRequest();
        if (request.getMethod().equals("POST")) {
            validateField(request, SLACK_BOT_OAUTH_TOKEN, getText("tools.redfox.bamboo.notifications.slack.slack.error.oauth"));

            String token = request.getParameter(SLACK_BOT_OAUTH_TOKEN);

            Slack slack = Slack.getInstance();
            ChannelsListResponse channelsResponse = null;
            try {
                channelsResponse = slack.methods().channelsList(req -> req.token(token));
                if (!channelsResponse.isOk()) {
                    addFieldError(SLACK_BOT_OAUTH_TOKEN, "Unable to validate slack token");
                }
            } catch (SlackApiException | IOException e) {
            }

            if (applicationLinkService.getPrimaryApplicationLink(JiraApplicationType.class) != null) {
                validateField(request, SLACK_BOT_JIRA_BAMBOO_USERNAME, getText("tools.redfox.bamboo.notifications.slack.slack.error.oauth"));
            }
        }

        Map<String, Object> context = ServletActionContext.getValueStack(request).getContext();
        context.putAll(request.getParameterMap());
    }

    @Override
    public String execute() throws Exception {
        HttpServletRequest request = ServletActionContext.getRequest();

        if (request.getMethod().equals("POST") && getFieldErrors().size() == 0) {
            pluginSettings.put(PLUGIN_STORAGE_KEY + SLACK_BOT_OAUTH_TOKEN, request.getParameter(SLACK_BOT_OAUTH_TOKEN));
            pluginSettings.put(PLUGIN_STORAGE_KEY + SLACK_BOT_JIRA_BAMBOO_USERNAME, request.getParameter(SLACK_BOT_JIRA_BAMBOO_USERNAME));
            return "reload";
        }

        setContext(request);
        return super.execute();
    }

    public String getToken() {
        return xsrfTokenAccessor.getXsrfToken(ServletActionContext.getRequest(), ServletActionContext.getResponse(), true);
    }

    private void validateField(HttpServletRequest request, String name, String error) {
        String param = request.getParameter(name);
        if (param == null || param.isEmpty()) {
            addFieldError(name, error);
        }
    }

    protected void setContext(HttpServletRequest request) {
        Map<String, Object> context = ServletActionContext.getValueStack(request).getContext();
        context.put(SLACK_BOT_OAUTH_TOKEN, pluginSettings.get(PLUGIN_STORAGE_KEY + SLACK_BOT_OAUTH_TOKEN));
        context.put(SLACK_BOT_JIRA_BAMBOO_USERNAME, pluginSettings.get(PLUGIN_STORAGE_KEY + SLACK_BOT_JIRA_BAMBOO_USERNAME));

        List<String> users = new LinkedList<>();
        users.add("");
        for (String name : bambooUserManager.getUserNames()) {
            users.add(name);
        }

        context.put("users", users);
        context.put("jira", applicationLinkService.getPrimaryApplicationLink(JiraApplicationType.class));
        context.put("saved", request.getParameterMap().containsKey("saved") && getFieldErrors().size() == 0);
        context.put("cancelUrl", getBambooUrl().withBaseUrlFromRequest("/admin/administer.action"));
    }
}
