package tools.redfox.bamboo.notifications.slack.utils;

import com.atlassian.bamboo.jira.jiraissues.JiraIssueDetails;
import com.atlassian.bamboo.jira.jiraissues.LinkedJiraIssue;
import com.atlassian.plugin.spring.scanner.annotation.component.BambooComponent;
import com.github.seratch.jslack.api.model.block.ContextBlock;
import com.github.seratch.jslack.api.model.block.ContextBlockElement;
import com.github.seratch.jslack.api.model.block.LayoutBlock;
import com.github.seratch.jslack.api.model.block.SectionBlock;
import com.github.seratch.jslack.api.model.block.composition.MarkdownTextObject;
import com.github.seratch.jslack.api.model.block.composition.TextObject;
import com.github.seratch.jslack.api.model.block.element.BlockElement;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@BambooComponent
public class BlockUtils {
    @Autowired
    private UrlProvider urlProvider;

    public ContextBlock context(List<ContextBlockElement> elements) {
        return ContextBlock.builder().elements(elements).build();
    }

    public ContextBlock context(ContextBlockElement element) {
        return context(
                new LinkedList<ContextBlockElement>() {{
                    add(element);
                }}
        );
    }

    public ContextBlock context(String element) {
        return context(MarkdownTextObject.builder().text(element).build());
    }

    public MarkdownTextObject markdownLink(String url, String name) {
        return markdownLink(url, name, null);
    }

    public MarkdownTextObject markdownLink(String url, String name, String comment) {
        return MarkdownTextObject.builder().text(
                MessageFormat.format("<{0}|{1}>{2}", url, name, comment != null ? " - " + comment : "")
        ).build();
    }

    public MarkdownTextObject markdownLink(LinkedJiraIssue issue) {
        String comment = null;
        if (issue.getJiraIssueDetails() != null) {
            comment = MessageFormat.format(
                    "{0} - {1}",
                    issue.getJiraIssueDetails().getType().getTypeDescription(),
                    issue.getJiraIssueDetails().getSummary()
            );
        }
        return markdownLink(urlProvider.jiraIssue(issue.getIssueKey()), issue.getIssueKey(), comment);
    }

    public MarkdownTextObject markdownLink(JiraIssueDetails issue) {
        String comment = MessageFormat.format(
                "{0} - {1}",
                issue.getType().getTypeDescription(),
                issue.getSummary()
        );

        return markdownLink(urlProvider.jiraIssue(issue.getIssueKey()), issue.getIssueKey(), comment);
    }

    public MarkdownTextObject markdownLink(Object o) {
        return o instanceof JiraIssueDetails ? markdownLink((JiraIssueDetails)o) : markdownLink((LinkedJiraIssue)o);
    }

    public SectionBlock header(String headline) {
        return SectionBlock
                .builder()
                .text(MarkdownTextObject
                        .builder()
                        .text(MessageFormat.format("*{0}*", headline))
                        .build()
                )
                .build();
    }

    public List<LayoutBlock> section(String header, List<ContextBlockElement> elements) {
        return new LinkedList<LayoutBlock>() {{
            add(header(header));
            add(context(elements));
        }};
    }

    public SectionBlock section(TextObject text, BlockElement accessory) {
        return SectionBlock
                .builder()
                .text(text)
                .accessory(accessory)
                .build();
    }

    public List<LayoutBlock> section(String header, List<ContextBlockElement> elements, MarkdownTextObject footer) {
        List<LayoutBlock> blocks = section(header, elements);
        blocks.add(context(footer));
        return blocks;
    }

    public List<ContextBlockElement> elements(List<String> elements) {
        return elements.stream().map(e -> MarkdownTextObject.builder().text(e).build()).collect(Collectors.toList());
    }

    public ContextBlockElement element(String element) {
        return MarkdownTextObject.builder().text(element).build();
    }

    public void setUrlProvider(UrlProvider urlProvider) {
        this.urlProvider = urlProvider;
    }
}
