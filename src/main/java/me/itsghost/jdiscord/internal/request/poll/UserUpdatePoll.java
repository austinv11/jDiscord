package me.itsghost.jdiscord.internal.request.poll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.itsghost.jdiscord.Role;
import me.itsghost.jdiscord.Server;
import me.itsghost.jdiscord.internal.impl.DiscordAPIImpl;
import me.itsghost.jdiscord.internal.impl.ServerImpl;
import me.itsghost.jdiscord.internal.impl.UserImpl;
import me.itsghost.jdiscord.talkable.GroupUser;

import org.json.JSONArray;
import org.json.JSONObject;

public class UserUpdatePoll implements Poll {
    private DiscordAPIImpl api;

    public UserUpdatePoll(DiscordAPIImpl api) {
        this.api = api;
    }

    //TODO: Again, JAVA FUCKING 8

    @Override
    public void process(JSONObject content, JSONObject rawRequest, Server server){
        if (content.isNull("user"))
            return; //proxy url nolonger valid

        JSONObject user = content.getJSONObject("user");
        JSONArray rolesArray = content.getJSONArray("roles");
        GroupUser gUser = server.getGroupUserById(user.getString("id"));

        ((UserImpl)gUser.getUser()).setUsername(user.getString("username"));
        ((UserImpl)gUser.getUser()).setAvatarId(user.isNull("avatar") ? "Failed to grab" : user.getString("avatar"));
        ((UserImpl)gUser.getUser()).setAvatar(UserUtils.getAvatarURLFromJSON(api.getSelfInfo().getId(), user));


        List<Role> roles = new ArrayList<>();

        for (int i = 0; i < rolesArray.length(); i++) {
            String id = rolesArray.getString(i);
            for (Role role : getRoles(((ServerImpl)server).getRoleMeta()))
                if (role.getId().equals(id))
                    roles.add(role);
        }

        gUser.setRoles(roles);
    }


    public static List<Role> getRoles(JSONArray rolesArray){
        List<Role> roles = new ArrayList<>();
        for (int i = 0; i < rolesArray.length(); i++) {
            JSONObject roleObj = rolesArray.getJSONObject(i);


            Map<String, Integer> perms = new HashMap<>();
            perms.put("allow", roleObj.getInt("allow"));
            perms.put("deny", roleObj.getInt("deny"));

            roles.add(new Role(roleObj.getString("name"),
                    roleObj.getString("id"),
                    roleObj.isNull("color") ? null : "#" + String.valueOf(roleObj.getInt("color")),
                    perms));
        }
        return roles;
    }
}
