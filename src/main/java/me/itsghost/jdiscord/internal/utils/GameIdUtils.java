package me.itsghost.jdiscord.internal.utils;

import me.itsghost.jdiscord.internal.httprequestbuilders.PacketBuilder;
import me.itsghost.jdiscord.internal.httprequestbuilders.RequestType;

import org.json.JSONArray;
import org.json.JSONObject;

public class GameIdUtils {

    private  static String GAMEARRAY;

    public static String getGameFromId(String id) {
        if (id.matches("^-?\\d+$")) {
            JSONArray array = new JSONArray(getGameArray());
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);

                if (item.getInt("id") == Integer.valueOf(id))
                    return item.getString("name");
            }
        }
        return id;
    }

    private static String getGameArray(){
        if (GAMEARRAY != null)
             return GAMEARRAY;

        PacketBuilder pb = new PacketBuilder(null);
        pb.setSendLoginHeaders(false);
        pb.setType(RequestType.GET);
        pb.setUrl("http://pastebin.com/raw.php?i=e94CkJpk");
        GAMEARRAY = pb.makeRequest();

        return GAMEARRAY;
    }
}
