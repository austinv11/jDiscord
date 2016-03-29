package me.itsghost.jdiscord.internal.request;

import java.util.List;
import java.util.Map;

import me.itsghost.jdiscord.Server;
import me.itsghost.jdiscord.internal.impl.*;
import me.itsghost.jdiscord.internal.request.poll.*;

import org.json.JSONObject;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFrame;

public class WebSocketClient extends WebSocketAdapter {
    public boolean loaded = false;
    protected Thread thread;
    private DiscordAPIImpl api;
    private ReadyPoll readyPoll;
    private WebSocket rm;

    public WebSocketClient(DiscordAPIImpl api, WebSocket rm) throws Exception{
        this.api = api;
        this.rm = rm;
        readyPoll = new ReadyPoll(api);
    }

    @Override
    public void onPingFrame(WebSocket socket, WebSocketFrame frame){

    }

    @Override
    public void onPongFrame(WebSocket socket, WebSocketFrame frame){

    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        this.loaded = true;
        api.log("Socket loaded");
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
        int code = serverCloseFrame.getCloseCode();
        String reason = serverCloseFrame.getCloseReason();
        api.log("Socket closed with " + code + " and we was at " + rm.getURI().getHost() + " for reason " + reason + " -" + closedByServer);
        if (code == 1000) {
            try {
                api.log("Logging in again");
                api.login();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (code == 4001) {
            System.out.println("\n");
            api.log("Uh... Some other client sent invalid data and timed everyone out!");
            try {
                api.getLoginTokens().process(api);
            } catch (Exception e) {
                api.log("Failed to reconnect: " + e.getCause());
                api.stop();
            } finally {
                System.out.println("\n");
            }
        }
        if (api.isDebugMode()) {
            System.out.println("------------------------------------");
            for (Thread thread : Thread.getAllStackTraces().keySet()) {
                System.out.println("------------------------------------");
                for (StackTraceElement element : thread.getStackTrace())
                    System.out.println(element.toString());
            }
        }

    }

    @Override
    public void onTextMessage(WebSocket websocket, String message) throws Exception {
        try {
            JSONObject obj = new JSONObject(message);
            if (api.isDebugMode())
            api.log(obj.toString());
            if (obj.getInt("op") == 7)
                return;
            JSONObject key = obj.getJSONObject("d");
            String type = obj.getString("t");

            Server a = key.isNull("guild_id") ? null : api.getServerById(key.getString("guild_id"));
            Server server = key.isNull("guild_id") ? null : (a != null ? a : api.getGroupById(key.getString("guild_id")).getServer());
            switch (type) {
                case "READY":
                    readyPoll.process(key, obj, server);
                    api.log("Successfully loaded user data!");
                    break;
                case "GUILD_MEMBER_ADD":
                    new AddUserPoll(api).process(key, obj, server);
                    break;
                case "GUILD_MEMBER_REMOVE":
                    new KickPoll(api).process(key, obj, server);
                    break;
                case "GUILD_BAN_ADD":
                    new BanPoll(api).process(key, obj, server);
                    break;
                case "GUILD_BAN_REMOVE":
                    //processBan(key, server);
                    //Unban?
                    break;
                case "MESSAGE_CREATE":
                    new MessagePoll(api).process(key, obj, server);
                    break;
                case "MESSAGE_UPDATE":
                    new MessagePoll(api).process(key, obj, server);
                    break;
                case "TYPING_START":
                    new TypingPoll(api).process(key, obj, server);
                    break;
                case "CHANNEL_CREATE":
                    new NewContactOrGroupPoll(api).process(key, obj, server);
                    break;
                case "PRESENCE_UPDATE":
                    new StatusPoll(api).process(key, obj, server);
                    break;
                case "USER_UPDATE":
                    new UpdateSettings(api).process(key, obj, server);
                    break;
                case "CHANNEL_DELETE":
                    new ChannelRemove(api).process(key, obj, server);
                    break;
                case "CHANNEL_UPDATE":
                    new ChannelUpdatePoll(api).process(key, obj, server);
                    break;
                case "GUILD_CREATE":
                    new GuildAdd(api).process(key, obj, server);
                    break;
                case "MESSAGE_DELETE":
                    new DeleteMessagePoll(api).process(key, obj, server);
                    break;
                case "GUILD_MEMBER_UPDATE":
                    new UserUpdatePoll(api).process(key, obj, server);
                    break;
                case "GUILD_ROLE_UPDATE":
                    new ServerUpdatePerm(api).process(key, obj, server);
                    break;
                case "VOICE_STATE_UPDATE":
                    try {
                        if (key.getString("user_id").equals(api.getSelfInfo().getId())) {
                            VoiceGroupImpl voice = api.getVoiceGroupById(key.getString("channel_id"));
                            voice.setSession(key.getString("session_id"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case "VOICE_SERVER_UPDATE":
                    try {
                        ((ServerImpl) server).setToken(key.getString("token"));
                        ((ServerImpl) server).setServer(key.getString("endpoint"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    api.log("Unknown type " + type + "\n >" + obj);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public boolean isOpen(){
       return rm.isOpen();
    }

    public void send(JSONObject obj) {
        this.send(obj.toString());
    }

    public void send(String send) {
        try {
            if (api.isDebugMode())
                System.out.print(send);
            rm.sendText(send);
        }catch (Exception e){
            try {
                api.stop();
                api.login();
                e.printStackTrace();
            } catch (Exception a) {

            }
        } finally {

        }
    }

    public void stop(){
        rm.sendClose(200, "jDiscord graceful shutdown");
        readyPoll.getThread().interrupt();
    }
}


