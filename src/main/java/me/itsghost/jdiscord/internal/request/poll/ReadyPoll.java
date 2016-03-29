package me.itsghost.jdiscord.internal.request.poll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import me.itsghost.jdiscord.OnlineStatus;
import me.itsghost.jdiscord.Role;
import me.itsghost.jdiscord.SelfData;
import me.itsghost.jdiscord.Server;
import me.itsghost.jdiscord.events.APILoadedEvent;
import me.itsghost.jdiscord.internal.impl.AccountManagerImpl;
import me.itsghost.jdiscord.internal.impl.DiscordAPIImpl;
import me.itsghost.jdiscord.internal.impl.GroupImpl;
import me.itsghost.jdiscord.internal.impl.ServerImpl;
import me.itsghost.jdiscord.internal.impl.UserImpl;
import me.itsghost.jdiscord.internal.impl.VoiceGroupImpl;
import me.itsghost.jdiscord.talkable.GroupUser;

import org.json.JSONArray;
import org.json.JSONObject;

public class ReadyPoll implements Poll {
    @Getter
    private Thread thread;
    private DiscordAPIImpl api;

    public ReadyPoll(DiscordAPIImpl api) {
        this.api = api;
    }

    /*
        What the fuck happened here?
     */

    @Override
    public void process(JSONObject content, JSONObject rawRequest, Server server) {
        if (api.isLoaded())
            return; //we reconnected

        api.setMutedChannels(content.getJSONObject("user_settings").getJSONArray("muted_channels"));

        setupLocalData(content.getJSONObject("user"));

        thread = new Thread(() -> {
            while (!api.getRequestManager().getSocketClient().isOpen()) {
                try {
                    ((AccountManagerImpl)api.getAccountManager()).updateStatus();
                    api.getRequestManager().getSocketClient().send(new JSONObject().put("op", 1).put("d", System.currentTimeMillis()).toString());
                    System.out.println("Sent ping");
                    Thread.sleep(content.getLong("heartbeat_interval"));
                } catch (Exception e) {
                    api.stop();
                }
            }
        });

        thread.start();

        setupServers(content);
        setupContacts(content);

        api.getEventManager().executeEvent(new APILoadedEvent());

        api.setLoaded(true);
    }

    public void setupLocalData(JSONObject userDataJson){
        SelfData data = new SelfData();

        data.setUsername(userDataJson.getString("username"));
        data.setEmail(userDataJson.isNull("email") ? "Guest" : userDataJson.getString("email"));
        data.setId(userDataJson.getString("id"));
        data.setAvatar(UserUtils.getAvatarURLFromJSON(data.getId(), userDataJson));
        data.setAvatarId((userDataJson.isNull("avatar") ? "" : userDataJson.getString("avatar")));

        api.setSelfInfo(data);
    }

    public void setupContacts(JSONObject key) {
        JSONArray array = key.getJSONArray("private_channels");
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.getJSONObject(i);
            JSONObject contact = item.getJSONObject("recipient");

            String id = contact.getString("id");

            if (item.getString("id").equals(api.getSelfInfo().getId()))
                api.setAs(contact.getString("id"));

            UserImpl userImpl = new UserImpl(contact.getString("username"), id, item.getString("id"), api);
            userImpl.setAvatar(UserUtils.getAvatarURLFromJSON(id, contact));
            userImpl.setAvatarId(contact.isNull("avatar") ? "" : userImpl.getId());

            api.getAvailableDms().add(userImpl);
        }
    }

    public List<GroupUser> getGroupUsersFromJson(JSONObject obj, List<Role> roles) {
        JSONArray members = obj.getJSONArray("members");
        List<GroupUser> guList = new ArrayList<>();

        for (int i = 0; i < members.length(); i++) {
            JSONObject item = members.getJSONObject(i);
            JSONObject user = item.getJSONObject("user");

            String username = user.getString("username");
            String id = user.getString("id");
            String dis = String.valueOf(user.get("discriminator")); //Sometimes returns an int or string... just cast the obj to string
            String avatarId = (user.isNull("avatar") ? "" : user.getString("avatar"));
            UserImpl userImpl;

            if (api.isUserKnown(id)) {
                userImpl = (UserImpl) api.getUserById(id);
            } else {
                userImpl = new UserImpl(username, id, id, api);
                userImpl.setAvatar(UserUtils.getAvatarURLFromJSON(id, user));
                userImpl.setAvatarId(avatarId);
            }

            List<Role> rolesA = new ArrayList<>();

            if (item.getJSONArray("roles").length() > 0)
                for (Role roleV : roles)
                    if (roleV.getId().equals(item.getJSONArray("roles").opt(0)) || (roleV.getName().equals("@everyone")))
                        rolesA.add(roleV);

            GroupUser gu = new GroupUser(userImpl, dis);
            gu.setRoles(rolesA);
            guList.add(gu);
        }
        return guList;
    }


    public List<Role> getRoles(JSONArray rolesArray){
        List<Role> roles = new ArrayList<>();
        for (int i = 0; i < rolesArray.length(); i++) {
            JSONObject roleObj = rolesArray.getJSONObject(i);
            Map<String, Integer> perms = new HashMap<>();
            perms.put("allow", roleObj.getInt("permissions"));
            roles.add(new Role(roleObj.getString("name"),
                    roleObj.getString("id"),
                    roleObj.isNull("color") ? null : String.valueOf(roleObj.get("color")),
                    perms));
        }
        return roles;
    }

    public void setupServers(JSONObject key) {
        JSONArray guilds = key.getJSONArray("guilds");
        for (int i = 0; i < guilds.length(); i++) {
            JSONObject item = guilds.getJSONObject(i);

            ServerImpl server = new ServerImpl(item.getString("id"), api);
            server.setName(item.getString("name"));
            server.setLocation(item.getString("region"));
            server.setCreatorId(item.getString("owner_id"));
            server.setAvatar(item.isNull("icon") ? "" : "https://cdn.discordapp.com/icons/" + server.getId() + "/" + item.getString("icon") + ".jpg");
            server.setRoleMeta(item.getJSONArray("roles"));
            server.setRoles(getRoles(item.getJSONArray("roles")));

            List<GroupUser> users = getGroupUsersFromJson(item, server.getRoles());
            users = updateOnlineStatus(users, item.getJSONArray("presences"));

            server.getConnectedClients().addAll(users);

            JSONArray channels = item.getJSONArray("channels");
            for (int ia = 0; ia < channels.length(); ia++) {
                JSONObject channel = channels.getJSONObject(ia);
                if (!channel.getString("type").equals("text"))
                    addVoiceGroup(channel, server);
                else
                    addTextChannel(channel, server);
            }
            api.getAvailableServers().add(server);
        }
    }

    public void addVoiceGroup(JSONObject channelmeta, Server server){
        server.getVoiceGroups().add(new VoiceGroupImpl(channelmeta.getString("id"), channelmeta.getString("name"), server, api));
    }

    public void addTextChannel(JSONObject channel, Server server){
        GroupImpl group = new GroupImpl(channel.getString("id"),
                channel.getString("id"),
                server,
                api);
        group.setName(channel.getString("name"));
        group.setTopic(channel.isNull("topic") ? "" : channel.getString("topic"));
        group.setPosition(channel.getInt("position"));
        server.getGroups().add(group);
    }

    public void stop() {
        if (thread != null)
            thread.interrupt();
    }


    //TODO: Java 8 this fucker or use collections
    public List<GroupUser> updateOnlineStatus(List<GroupUser> users, JSONArray presences){
        for (int i = 0; i < presences.length(); i++) {
            JSONObject item = presences.getJSONObject(i);
            for (GroupUser gUser : users){
                UserImpl user = (UserImpl)gUser.getUser();
                if (user.equals(item.getJSONObject("user").getString("id"))){
                    OnlineStatus status = OnlineStatus.fromName(item.getString("status"));
                    user.setGame(UserUtils.parseGame(item));
                    user.setOnlineStatus(status);
                }
            }
        }
        return users;
    }
}
