package me.itsghost.jdiscord.internal.request.poll;

import me.itsghost.jdiscord.Server;
import me.itsghost.jdiscord.events.ChannelDeletedEvent;
import me.itsghost.jdiscord.internal.impl.DiscordAPIImpl;
import me.itsghost.jdiscord.internal.impl.GroupImpl;
import me.itsghost.jdiscord.internal.impl.UserImpl;

import org.json.JSONObject;

public class NewContactOrGroupPoll implements Poll {
    private DiscordAPIImpl api;

    public NewContactOrGroupPoll(DiscordAPIImpl api) {
        this.api = api;
    }

    @Override
    public void process(JSONObject content, JSONObject rawRequest, Server server) {
        if (content.getBoolean("is_private")) {
            String cid = content.getString("id");
            JSONObject recp = content.getJSONObject("recipient");

            String id = recp.getString("id");
            String username = recp.getString("username");

            UserImpl userImpl = new UserImpl(username, id, cid, api);
            userImpl.setAvatar(UserUtils.getAvatarURLFromJSON(id, recp));
            userImpl.setAvatarId(recp.getString("avatar"));

            api.updateContact(userImpl);
        }else{
            GroupImpl group = new GroupImpl(content.getString("id"), content.getString("id"), server, api);
            group.setName(content.getString("name"));
            group.setPosition(content.getInt("position"));
            group.setTopic(content.isNull("topic") ? "" : content.getString("topic"));
            server.getGroups().add(group);
            api.getEventManager().executeEvent(new ChannelDeletedEvent(group));
        }
    }
}
