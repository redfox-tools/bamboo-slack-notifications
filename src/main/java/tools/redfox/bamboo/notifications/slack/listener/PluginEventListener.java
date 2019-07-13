package tools.redfox.bamboo.notifications.slack.listener;

import com.atlassian.event.api.EventListener;
import com.atlassian.plugin.event.events.PluginDisablingEvent;
import kong.unirest.Unirest;

public class PluginEventListener {
    @EventListener
    public void onPluginDisabling(PluginDisablingEvent event) {
        Unirest.shutDown();
    }
}
