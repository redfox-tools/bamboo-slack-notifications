package tools.redfox.bamboo.notifications.slack.action;

import com.atlassian.bamboo.configuration.AdministrationConfigurationAccessor;
import com.atlassian.bamboo.deployments.projects.service.DeploymentProjectService;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.security.BambooPermissionManager;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.bamboo.ww2.aware.permissions.GlobalAdminSecurityAware;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.xsrf.XsrfTokenAccessor;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import tools.redfox.bamboo.notifications.slack.services.JiraClient;
import tools.redfox.bamboo.notifications.slack.services.JiraIssueDetailsProvider;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class SlackConfigurationAction extends BambooActionSupport implements GlobalAdminSecurityAware {
    public static final String SLACK_BOT_OAUTH_TOKEN = "slackOAuthBot";
    public static final String SLACK_BOT_JIRA_URL = "slackJiraURL";
    public static final String SLACK_BOT_JIRA_USERNAME = "slackJiraUsername";
    public static final String SLACK_BOT_JIRA_PASSWORD = "slackJiraPassword";
    public static final String PLUGIN_STORAGE_KEY = "tools.redfox.bamboo.notifications.slack.";

    private final XsrfTokenAccessor xsrfTokenAccessor;
    private final PluginSettings pluginSettings;

    @Autowired
    public SlackConfigurationAction(@ComponentImport BambooPermissionManager bambooPermissionManager,
                                    @ComponentImport DeploymentProjectService deploymentProjectService,
                                    @ComponentImport AdministrationConfigurationAccessor administrationConfigurationAccessor,
                                    @ComponentImport PlanManager planManager,
                                    @ComponentImport XsrfTokenAccessor xsrfTokenAccessor,
                                    @ComponentImport PluginSettingsFactory pluginSettingsFactory) {
        this.xsrfTokenAccessor = xsrfTokenAccessor;
        this.pluginSettings = pluginSettingsFactory.createGlobalSettings();
        setBambooPermissionManager(bambooPermissionManager);
        setDeploymentProjectService(deploymentProjectService);
        setAdministrationConfigurationAccessor(administrationConfigurationAccessor);
        setPlanManager(planManager);
    }

    @Override
    public void validate() {
        HttpServletRequest request = ServletActionContext.getRequest();
        if (request.getMethod().equals("POST")) {
            handleField(request, SLACK_BOT_OAUTH_TOKEN, getText("tools.redfox.bamboo.notifications.slack.slack.error.oauth"));

            if (!request.getParameter(SLACK_BOT_JIRA_PASSWORD).isEmpty()) {
                pluginSettings.put(PLUGIN_STORAGE_KEY + SLACK_BOT_JIRA_PASSWORD, request.getParameter(SLACK_BOT_JIRA_PASSWORD));
            }

            if (!request.getParameter(SLACK_BOT_JIRA_URL).isEmpty() || !request.getParameter(SLACK_BOT_JIRA_USERNAME).isEmpty()) {
                handleField(request, SLACK_BOT_JIRA_URL, getText("tools.redfox.bamboo.notifications.slack.jira.error.url"));
                handleField(request, SLACK_BOT_JIRA_USERNAME, getText("tools.redfox.bamboo.notifications.slack.jira.error.username"));
                String stored = (String) pluginSettings.get(PLUGIN_STORAGE_KEY + SLACK_BOT_JIRA_PASSWORD);

                if (request.getParameter(SLACK_BOT_JIRA_PASSWORD).isEmpty() && (stored == null || stored.isEmpty())) {
                    handleField(request, SLACK_BOT_JIRA_PASSWORD, getText("tools.redfox.bamboo.notifications.slack.jira.error.password"));
                }
            }
        }

        Map<String, Object> context = ServletActionContext.getValueStack(request).getContext();
        context.putAll(request.getParameterMap());
    }

    @Override
    public String execute() throws Exception {
        HttpServletRequest request = ServletActionContext.getRequest();

        if (request.getMethod().equals("POST")) {
            return "reload";
        }

        setContext(request);
        return super.execute();
    }


    public String getToken() {
        return xsrfTokenAccessor.getXsrfToken(ServletActionContext.getRequest(), ServletActionContext.getResponse(), true);
    }

    public String getEnableAppUrl() {
        return "https://slack.com/oauth/authorize?client_id=688687762519.678094921075&scope=read&redirect_uri=https://mp.ngrok.io/bamboo/admin/slack/authorize.action";
    }

    private void handleField(HttpServletRequest request, String name, String error) {
        String param = request.getParameter(name);
        if (param == null || param.isEmpty()) {
            addFieldError(name, error);
        } else {
            pluginSettings.put(PLUGIN_STORAGE_KEY + name, param);
        }
    }

    protected void setContext(HttpServletRequest request) {
        Map<String, Object> context = ServletActionContext.getValueStack(request).getContext();
        context.put(SLACK_BOT_OAUTH_TOKEN, pluginSettings.get(PLUGIN_STORAGE_KEY + SLACK_BOT_OAUTH_TOKEN));
        context.put(SLACK_BOT_JIRA_URL, pluginSettings.get(PLUGIN_STORAGE_KEY + SLACK_BOT_JIRA_URL));
        context.put(SLACK_BOT_JIRA_USERNAME, pluginSettings.get(PLUGIN_STORAGE_KEY + SLACK_BOT_JIRA_USERNAME));
        context.put(SLACK_BOT_JIRA_PASSWORD, pluginSettings.get(PLUGIN_STORAGE_KEY + SLACK_BOT_JIRA_PASSWORD));
        context.put("saved", request.getParameterMap().containsKey("saved") && getFieldErrors().size() == 0);
        context.put("cancelUrl", getBambooUrl().withBaseUrlFromRequest("/admin/administer.action"));
    }
}
