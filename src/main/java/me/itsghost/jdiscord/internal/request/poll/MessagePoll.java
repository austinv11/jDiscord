package me.itsghost.jdiscord.internal.request.poll;

import me.itsghost.jdiscord.Server;
import me.itsghost.jdiscord.events.UserChatEvent;
import me.itsghost.jdiscord.internal.impl.DiscordAPIImpl;
import me.itsghost.jdiscord.internal.impl.MessageImpl;
import me.itsghost.jdiscord.internal.utils.TimestampUtils;
import me.itsghost.jdiscord.talkable.Group;
import me.itsghost.jdiscord.talkable.GroupUser;
import me.itsghost.jdiscord.talkable.User;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONObject;

public class MessagePoll implements Poll {
    private DiscordAPIImpl api;

    public MessagePoll(DiscordAPIImpl api) {
        this.api = api;
    }

    @Override
    public void process(JSONObject content, JSONObject rawRequest, Server server) {
        try {
            if (content.isNull("author"))
                return; //image update event?

            String id = content.getString("channel_id");
            String authorId = content.getJSONObject("author").getString("id");

            Group group = api.getGroupById(id);
            User user = api.getUserById(authorId);

            group = (group == null) ? api.getGroupById(authorId) : group;
            user = (user == null) ? api.getBlankUser() : user;

            String msgContent = (content.isNull("proxy_url") ? StringEscapeUtils.unescapeJson(content.getString("content")) : content.getJSONObject("embeds").getString("url"));
            String msgId = content.getString("id");

            MessageImpl msg = new MessageImpl(msgContent, msgId, id, api);
            msg.setSender(user);
            msg.setMentions(content.getJSONArray("mentions"));
            msg.setTimestamp(TimestampUtils.parse(content.optString("timestamp", null)));
            msg.setEditedTimestamp(TimestampUtils.parse(content.optString("edited_timestamp", null)));

            if (msg.getEditedTimestamp() != null)
                msg.setEdited(true);


            GroupUser gUser = (group.getServer() == null) ? new GroupUser(user,  user.getId()) : group.getServer().getGroupUserById(authorId);

            api.getEventManager().executeEvent(new UserChatEvent(group, gUser, msg));
        }catch(Exception e){
e.printStackTrace();
        }
    }
}
