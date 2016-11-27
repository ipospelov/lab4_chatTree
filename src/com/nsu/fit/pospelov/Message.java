package com.nsu.fit.pospelov;

import java.net.DatagramPacket;
import java.util.UUID;

/**
 * Created by posiv on 27.11.16.
 */
public class Message {
    private DatagramPacket packet;
    private UUID id;
    private String type;

    public Message(String type) {
        this.type = type;
    }

    public void initDatagramPacket(){
        String result = "";
        byte buf[];
        id = java.util.UUID.randomUUID();
        switch (type){
            case "CONNECT":
                result = type + ":" + id.toString();
                buf = result.getBytes();
                packet = new DatagramPacket(buf, buf.length);
                break;
        }
    }

    public DatagramPacket getPacket() {
        return packet;
    }

    public UUID getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
