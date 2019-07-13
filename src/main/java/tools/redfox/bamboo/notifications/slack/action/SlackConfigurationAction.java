package tools.redfox.bamboo.notifications.slack.action;

import com.atlassian.bamboo.configuration.AdministrationConfigurationAccessor;
import com.atlassian.bamboo.deployments.projects.service.DeploymentProjectService;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.security.BambooPermissionManager;
import com.atlassian.bamboo.util.UrlBuilder;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.bamboo.ww2.aware.permissions.GlobalAdminSecurityAware;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.xsrf.XsrfTokenAccessor;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class SlackConfigurationAction extends BambooActionSupport implements GlobalAdminSecurityAware {
    public static final String SLACK_BOT_OAUTH_TOKEN = "slackOAuthBot";
    public static final String PLUGIN_STORAGE_KEY = "tools.redfox.bamboo.notifications.slack";

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
            if (request.getParameter(SLACK_BOT_OAUTH_TOKEN).isEmpty()) {
                addFieldError(SLACK_BOT_OAUTH_TOKEN, getText("tools.redfox.bamboo.notifications.sentry.global.organisation.error"));
            } else {
                pluginSettings.put(PLUGIN_STORAGE_KEY + ".bot.oauth", request.getParameter(SLACK_BOT_OAUTH_TOKEN));
            }
        }
    }

    @Override
    public String execute() throws Exception {
        HttpServletRequest request = ServletActionContext.getRequest();

        if (request.getMethod().equals("POST")) {
            return "reload";
        }

        Map<String, Object> context = ServletActionContext.getValueStack(request).getContext();
        context.put(SLACK_BOT_OAUTH_TOKEN, pluginSettings.get(PLUGIN_STORAGE_KEY + ".bot.oauth"));
        context.put("saved", request.getParameterMap().containsKey("saved") && getFieldErrors().size() == 0);
        context.put("cancelUrl", getBambooUrl().withBaseUrlFromRequest("/admin/administer.action"));

        return super.execute();
    }

    public String getToken() {
        return xsrfTokenAccessor.getXsrfToken(ServletActionContext.getRequest(), ServletActionContext.getResponse(), true);
    }

    public String getEnableAppUrl() {
        return "***REMOVED***";
    }
}
