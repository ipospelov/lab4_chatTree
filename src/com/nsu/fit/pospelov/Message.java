package com.nsu.fit.pospelov;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.UUID;

/**
 * Created by posiv on 27.11.16.
 */
public class Message {
    private DatagramPacket packet;
    private UUID id;
    private String type;
    private String usersMessage;
    private String ownerNodeName;
    private int newParentPort;
    private InetAddress newParentNodeAddress;

    public Message(String usersMessage, String type, String name) {
        this.type = type;
        this.usersMessage = usersMessage;
        ownerNodeName =name;

    }

    public void initDatagramPacket(){
        String result;
        byte buf[];
        if(id == null) {
            id = java.util.UUID.randomUUID();
        }
        switch (type){
            case "CONNECT":
                result = type + ":" + id.toString() + ":" + ownerNodeName;
                buf = result.getBytes();
                packet = new DatagramPacket(buf, buf.length);
                break;
            case "USERS":
                result = type + ":" + id.toString() + ":" + ownerNodeName + ":" + usersMessage;
                buf = result.getBytes();
                packet = new DatagramPacket(buf, buf.length);
                break;
            case "DISCONNECT":
                result = type + ":" + id.toString() + ":" + ownerNodeName + ":" + newParentPort + ":" + newParentNodeAddress;
                buf = result.getBytes();
                packet = new DatagramPacket(buf, buf.length);
                break;
            /*case "ACK":
                result = type + ":" + id.toString();
                buf = result.getBytes();
                packet = new DatagramPacket(buf, buf.length);
                break;*/
        }
    }
    public void initAckDatagramPacket(UUID id){
        String result = type + ":" + id.toString();
        byte buf[] = buf = result.getBytes();
        packet = new DatagramPacket(buf, buf.length);
    }

    public int getNewParentPort() {
        return newParentPort;
    }

    public InetAddress getNewParentNodeAddress() {
        return newParentNodeAddress;
    }

    public void setNewParentPort(int newParentPort) {

        this.newParentPort = newParentPort;
    }

    public void setNewParentNodeAddress(InetAddress newParentNodeAddress) {
        this.newParentNodeAddress = newParentNodeAddress;
    }

    public DatagramPacket getPacket() {
        return packet;
    }

    public UUID getId() {
        return id;
    }

    public String getOwnerNodeName(){ return ownerNodeName; }

    public String getType() {
        return type;
    }

    public String getUsersMessage() {return usersMessage;}

    public void setId(UUID id) {this.id = id;}

    public void setType(String type) {this.type = type;}

    public void setUsersMessage(String usersMessage) {this.usersMessage = usersMessage;}
}
