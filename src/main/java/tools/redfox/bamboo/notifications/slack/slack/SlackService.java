package tools.redfox.bamboo.notifications.slack.slack;

import com.atlassian.plugin.spring.scanner.annotation.component.BambooComponent;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.RequestConfigurator;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.methods.request.chat.ChatUpdateRequest;
import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;
import com.github.seratch.jslack.api.methods.response.chat.ChatUpdateResponse;
import com.github.seratch.jslack.api.methods.response.conversations.ConversationsListResponse;
import com.github.seratch.jslack.api.model.Conversation;
import com.github.seratch.jslack.api.model.ConversationType;
import com.github.seratch.jslack.api.model.block.LayoutBlock;
import org.springframework.beans.factory.annotation.Autowired;
import tools.redfox.bamboo.notifications.slack.action.SlackConfigurationAction;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@BambooComponent
public class SlackService {
    private static final Slack slack = Slack.getInstance();
    private List<Conversation> channels;
    private PluginSettingsFactory pluginSettingsFactory;

    @Autowired
    public SlackService(@ComponentImport PluginSettingsFactory pluginSettingsFactory) {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    public List<Conversation> getChannels() throws IOException, SlackApiException {
        if (channels == null) {
            ConversationsListResponse conversationsResponse = slack.methods().conversationsList(
                    req -> req
                            .token(getToken())
                            .types(Arrays.asList(ConversationType.PUBLIC_CHANNEL, ConversationType.PRIVATE_CHANNEL))

            );
            assert conversationsResponse.isOk();
            channels = conversationsResponse.getChannels();
        }
        return channels;
    }

    public Conversation getChannel(String name) {
        try {
            return getChannels()
                    .stream()
                    .filter(c -> c.getName().equals(name))
                    .findFirst()
                    .get();
        } catch (IOException | SlackApiException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getChannelId(String name) {
        return getChannel(name).getId();
    }

    public ChatPostMessageResponse send(RequestConfigurator<ChatPostMessageRequest.ChatPostMessageRequestBuilder> req) throws IOException, SlackApiException {
        return slack.methods().chatPostMessage(
                req.configure(ChatPostMessageRequest.builder()).token(getToken()).build()
        );
    }

    public ChatUpdateResponse update(RequestConfigurator<ChatUpdateRequest.ChatUpdateRequestBuilder> req, String messageTs) throws IOException, SlackApiException {
        return slack.methods().chatUpdate(
                req.configure(ChatUpdateRequest.builder())
                        .token(getToken())
                        .ts(messageTs)
                        .build()
        );
    }

    public String send(String channel, List<LayoutBlock> blocks, String textVersion, String notification) throws IOException, SlackApiException {
        if (notification == null) {
            return send(
                    req -> req
                            .channel(getChannelId(channel))
                            .blocks(blocks)
                            .text(textVersion)
            ).getTs();
        } else {
            return update(
                    req -> req
                            .channel(getChannelId(channel))
                            .blocks(blocks)
                            .text(textVersion),
                    notification
            ).getTs();
        }
    }

    public String getToken() {
        return (String) pluginSettingsFactory.createGlobalSettings().get(SlackConfigurationAction.PLUGIN_STORAGE_KEY + SlackConfigurationAction.SLACK_BOT_OAUTH_TOKEN);
    }

    public boolean isConfigured() {
        String token = getToken();
        return (token != null && !token.equals(""));
    }
}
