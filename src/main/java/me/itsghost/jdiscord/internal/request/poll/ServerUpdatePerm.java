package me.itsghost.jdiscord.internal.request.poll;

import me.itsghost.jdiscord.Server;
import me.itsghost.jdiscord.internal.impl.DiscordAPIImpl;
import me.itsghost.jdiscord.talkable.Group;

import org.json.JSONObject;


public class ServerUpdatePerm  implements Poll {
    private DiscordAPIImpl api;

    public ServerUpdatePerm(DiscordAPIImpl api) {
        this.api = api;
    }

    @SuppressWarnings("unused")
	@Override
    public void process(JSONObject content, JSONObject rawRequest, Server server) {
        try {
            Group group = api.getGroupById(content.getString("channel_id"));
            if (!(content.isNull("role"))) {
                System.out.println(content);
            }

        }catch(Exception e){
            api.log("Failed to process message:\n >" + content);
        }
    }
}
