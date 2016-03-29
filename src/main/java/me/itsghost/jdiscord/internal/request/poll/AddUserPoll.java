package me.itsghost.jdiscord.internal.request.poll;

import java.util.ArrayList;

import me.itsghost.jdiscord.Server;
import me.itsghost.jdiscord.events.UserJoinedChat;
import me.itsghost.jdiscord.internal.impl.DiscordAPIImpl;
import me.itsghost.jdiscord.internal.impl.UserImpl;
import me.itsghost.jdiscord.talkable.GroupUser;

import org.json.JSONObject;

public class AddUserPoll implements Poll {
    private DiscordAPIImpl api;

    public AddUserPoll(DiscordAPIImpl api) {
        this.api = api;
    }

    @Override
    public void process(JSONObject content, JSONObject rawRequest, Server server) {
        JSONObject user = content.getJSONObject("user");

        UserImpl userImpl = new UserImpl(user.getString("username"), user.getString("id"), user.getString("id"), api);
        userImpl.setAvatar(user.isNull("avatar") ? "" : "https://cdn.discordapp.com/avatars/" + server.getId() + "/" + user.getString("avatar") + ".jpg");
        userImpl.setAvatarId(user.isNull("avatar") ? "" : server.getId());
        userImpl.setGame("NULL");

        GroupUser gUser = new GroupUser(userImpl, user.getString("discriminator"));
        gUser.setRoles(new ArrayList<>());

        server.getConnectedClients().add(gUser);

        api.getEventManager().executeEvent(new UserJoinedChat(server, gUser));
    }
}
