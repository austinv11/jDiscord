package me.itsghost.jdiscord.internal.request.poll;

import me.itsghost.jdiscord.internal.utils.GameIdUtils;

import org.json.JSONObject;

/**
 * Created by Ghost on 09/01/2016.
 */
public class UserUtils {

    /*
        //Ugly class, make the other ones look better-ish.
     */
    public static String getAvatarFromJSON(JSONObject obj) {
       return null;
    }

    public static String getAvatarURLFromJSON(String id,JSONObject obj) {
        return ("https://cdn.discordapp.com/avatars/" + id + "/" + (obj.isNull("avatar") ? "" : obj.getString("avatar")) + ".jpg");
    }

    public static String parseGame(JSONObject item){
        String game = item.isNull("game_id") ? "ready to play" : GameIdUtils.getGameFromId(item.get("game_id").toString());
        try {
            game = item.isNull("game") ? game : item.getJSONObject("game").getString("name");
        }catch (Exception e){
            //api.log("Illegal client found!");
        }
        return game;
    }

}
