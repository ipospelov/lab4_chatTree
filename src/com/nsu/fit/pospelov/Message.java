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
    private String nodeName;

    public Message(String usersMessage, String type, String name) {
        this.type = type;
        this.usersMessage = usersMessage;
        nodeName =name;

    }

    public void initDatagramPacket(){
        String result = "";
        byte buf[];
        id = java.util.UUID.randomUUID();
        switch (type){
            case "CONNECT":
                result = type + ":" + id.toString() + ":" + nodeName;
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

    public String getNodeName(){ return nodeName; }

    public String getType() {
        return type;
    }

    public String getUsersMessage() {return usersMessage;}

    public void setId(UUID id) {this.id = id;}

    public void setType(String type) {this.type = type;}

    public void setUsersMessage(String usersMessage) {this.usersMessage = usersMessage;}
}
