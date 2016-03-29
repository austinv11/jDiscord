package me.itsghost.jdiscord.internal.request.poll;

import me.itsghost.jdiscord.OnlineStatus;
import me.itsghost.jdiscord.Server;
import me.itsghost.jdiscord.events.UserNameChangedEvent;
import me.itsghost.jdiscord.events.UserOnlineStatusChangedEvent;
import me.itsghost.jdiscord.internal.impl.DiscordAPIImpl;
import me.itsghost.jdiscord.internal.impl.UserImpl;
import me.itsghost.jdiscord.talkable.Group;
import me.itsghost.jdiscord.talkable.GroupUser;

import org.json.JSONObject;

public class StatusPoll implements Poll {
    private DiscordAPIImpl api;

    public StatusPoll(DiscordAPIImpl api) {
        this.api = api;
    }

    @Override
    public void process(JSONObject content, JSONObject rawRequest, Server server) {
        try {
            JSONObject userA = content.getJSONObject("user");
            String id = content.getString("guild_id");
            String authorId = userA.getString("id");

            Group a = api.getGroupById(id);

            if (a == null) {
                api.log("I think I came online or offline... ignoring.");
                return;
            }

            GroupUser gUser = a.getServer().getGroupUserById(authorId);
            UserImpl user = (UserImpl)gUser.getUser();

            OnlineStatus status = OnlineStatus.fromName(content.getString("status"));
            String game = UserUtils.parseGame(content);

            user.setGame(game);
            user.setOnlineStatus(status);

            if (!(userA.isNull("username"))){
                user.setUsername(userA.getString("username"));
                UserNameChangedEvent event = new UserNameChangedEvent(a, gUser);
                api.getEventManager().executeEvent(event);
            }

            UserOnlineStatusChangedEvent event = new UserOnlineStatusChangedEvent(user, status, game);
            api.getEventManager().executeEvent(event);

        }catch(Exception e){
            api.log("Failed to process message:\n >" + content);
            System.out.println("Server: " + server);
            e.printStackTrace();
        }
    }
}
