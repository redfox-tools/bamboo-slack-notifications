package tools.redfox.bamboo.notifications.slack.services;

import com.atlassian.bamboo.commit.CommitContext;
import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.plan.PlanHelper;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.vcs.configuration.PlanRepositoryDefinition;
import com.atlassian.plugin.spring.scanner.annotation.component.BambooComponent;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.github.seratch.jslack.api.model.block.DividerBlock;
import com.github.seratch.jslack.api.model.block.LayoutBlock;
import org.springframework.beans.factory.annotation.Autowired;
import tools.redfox.bamboo.notifications.slack.utils.BlockUtils;
import tools.redfox.bamboo.notifications.slack.utils.EntityUtils;
import tools.redfox.bamboo.notifications.slack.utils.UrlProvider;

import java.util.List;
import java.util.stream.Collectors;

@BambooComponent
public class CommitDetailsProvider {
    private EntityUtils entityUtils;
    private BlockUtils blockUtils;
    private UrlProvider urlProvider;
    private PlanManager planManager;

    @Autowired
    public CommitDetailsProvider(EntityUtils entityUtils, BlockUtils blockUtils, UrlProvider urlProvider, @ComponentImport PlanManager planManager) {
        this.entityUtils = entityUtils;
        this.blockUtils = blockUtils;
        this.urlProvider = urlProvider;
        this.planManager = planManager;
    }

    public void attach(List<LayoutBlock> blocks, BuildContext buildContext) {
        List<CommitContext> commits = buildContext.getBuildChanges().getChanges();
        if (commits.size() > 0) {
            Plan plan = planManager.getPlanByKey(buildContext.getPlanKey());
            List<PlanRepositoryDefinition> repositories = PlanHelper.getPlanRepositoryDefinitions(plan);
            if (repositories.size() == 0) {
                return;
            }

            blocks.addAll(
                    blockUtils.section(
                            "Commits",
                            commits.stream().map(c -> entityUtils.commit(c, repositories.get(0))).map(blockUtils::element).collect(Collectors.toList()),
                            blockUtils.markdownLink(urlProvider.buildResult(buildContext.getBuildResultKey()), "more")
                    )
            );
            blocks.add(new DividerBlock());
        }
    }
}
