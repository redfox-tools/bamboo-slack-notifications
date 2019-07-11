package tools.redfox.bamboo.notifications.slack.utils;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.application.jira.JiraApplicationType;
import com.atlassian.bamboo.commit.CommitContext;
import com.atlassian.bamboo.configuration.AdministrationConfigurationAccessor;
import com.atlassian.bamboo.utils.BambooUrl;
import com.atlassian.bamboo.vcs.configuration.PlanRepositoryDefinition;
import com.atlassian.bamboo.vcs.viewer.runtime.VcsRepositoryViewer;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.spring.scanner.annotation.component.BambooComponent;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.UrlMode;
import org.springframework.beans.factory.annotation.Autowired;

@BambooComponent
public class UrlProvider {
    private final BambooUrl bambooUrl;
    private final ApplicationLinkService applicationLinkService;
    private PluginAccessor pluginAccessor;

    @Autowired
    public UrlProvider(@Autowired AdministrationConfigurationAccessor administrationConfigurationAccessor, @ComponentImport ApplicationLinkService applicationLinkService, @ComponentImport PluginAccessor pluginAccessor) {
        this.bambooUrl = new BambooUrl(administrationConfigurationAccessor);
        this.applicationLinkService = applicationLinkService;
        this.pluginAccessor = pluginAccessor;
    }

    public String commit(CommitContext commitContext, PlanRepositoryDefinition repositoryData) {
        VcsRepositoryViewer module = (VcsRepositoryViewer) pluginAccessor.getPluginModule(repositoryData.getViewerConfiguration().getPluginKey()).getModule();
        return module.getWebRepositoryUrlForRevision(commitContext.getChangeSetId(), repositoryData);
    }

    public String jiraIssue(String issueKey) {
        return baseJiraUrl() + "/browse/" + issueKey;
    }

    public String userProfile(String userName) {
        return bambooUrl.getBaseUrl(UrlMode.ABSOLUTE) + "/browse/user/" + userName;
    }

    public String buildResult(String buildResultKey) {
        return bambooUrl.getBaseUrl(UrlMode.ABSOLUTE) + "/browse/" + buildResultKey;
    }

    public String projectPage(String projectKey) {
        return bambooUrl.getBaseUrl(UrlMode.ABSOLUTE) + "/browse/" + projectKey;
    }

    private String baseJiraUrl() {
        return applicationLinkService.getPrimaryApplicationLink(JiraApplicationType.class).getDisplayUrl().toString();
    }
}
