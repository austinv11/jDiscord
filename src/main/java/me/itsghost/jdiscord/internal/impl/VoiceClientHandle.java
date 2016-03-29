package me.itsghost.jdiscord.internal.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import me.itsghost.jdiscord.DiscordAPI;
//import me.itsghost.networking.builders.ByteReader;
//import me.itsghost.networking.udp.UDPConnection;
//import org.java_websocket.WebSocket;

/**
 * Created by Ghost on 23/12/2015.
 */
@SuppressWarnings("unused")
public class VoiceClientHandle {
    public DiscordAPI api;
    public ServerImpl server;
    public VoiceGroupImpl voiceGroup;
    public VoiceClientHandle(DiscordAPI api, ServerImpl server, VoiceGroupImpl a) throws Exception{
       // super(1243);
        this.api = api;
        this.server = server;
        this.voiceGroup = a;
    }


    public OutputStream getEncodedSound(String url) throws Exception{
        File file = new File("out.stream.opus");
        if (file.exists())
            file.delete();
        Process pros = Runtime.getRuntime().exec("ffmpeg -i " + url  + " -acodec libopus -b:a 240000 -vbr on -compression_level 10 out.stream.opus");
        while (pros.isAlive()) {}
        System.out.println(pros.exitValue());
        return new FileOutputStream(file);
    }

	private int seq = 0;
    private int timestamp = 0;

    public void play(){

    }
}
