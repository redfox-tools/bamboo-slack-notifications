package tools.redfox.bamboo.notifications.slack.slack;

import com.atlassian.plugin.spring.scanner.annotation.component.BambooComponent;
import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.RequestConfigurator;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.methods.request.chat.ChatUpdateRequest;
import com.github.seratch.jslack.api.methods.response.channels.ChannelsListResponse;
import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;
import com.github.seratch.jslack.api.methods.response.chat.ChatUpdateResponse;
import com.github.seratch.jslack.api.model.Channel;
import com.github.seratch.jslack.api.model.block.ContextBlock;
import com.github.seratch.jslack.api.model.block.LayoutBlock;
import com.github.seratch.jslack.api.model.block.SectionBlock;
import com.github.seratch.jslack.api.model.block.composition.MarkdownTextObject;

import java.io.IOException;
import java.util.List;

@BambooComponent
public class SlackService {
    private static final Slack slack = Slack.getInstance();
    private final String token;
    private List<Channel> channels;

    public SlackService() {
        this.token = "***REMOVED***";
    }

    public List<Channel> getChannels() throws IOException, SlackApiException {
        if (channels == null) {
            ChannelsListResponse channelsResponse = slack.methods().channelsList(req -> req.token(token));
            assert channelsResponse.isOk();
            channels = channelsResponse.getChannels();
        }
        return channels;
    }

    public Channel getChannel(String name) {
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
                req.configure(ChatPostMessageRequest.builder()).token(token).build()
        );
    }

    public ChatUpdateResponse update(RequestConfigurator<ChatUpdateRequest.ChatUpdateRequestBuilder> req, String messageTs) throws IOException, SlackApiException {
        return slack.methods().chatUpdate(
                req.configure(ChatUpdateRequest.builder())
                        .token(token)
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
}
