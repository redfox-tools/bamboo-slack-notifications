package tools.redfox.bamboo.notifications.slack.utils;

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
        return MarkdownTextObject.builder().text(
                MessageFormat.format("<{0}|{1}>", url, name)
        ).build();
    }

    public MarkdownTextObject markdownLink(LinkedJiraIssue issue) {
        return markdownLink(urlProvider.jiraIssue(issue.getIssueKey()), issue.getIssueKey());
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
