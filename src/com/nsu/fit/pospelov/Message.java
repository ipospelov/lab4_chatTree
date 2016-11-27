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
    private String usersMessage;

    public Message(String s, String type) {
        this.type = type;
        this.usersMessage = s;

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

    public void setId(UUID id) {this.id = id;}

    public void setType(String type) {this.type = type;}

    public void setUsersMessage(String usersMessage) {this.usersMessage = usersMessage;}
}
