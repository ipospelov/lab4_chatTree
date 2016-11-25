package com.nsu.fit.pospelov;


import javafx.util.Pair;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

public class MessageHandler {
    private Node parentNode;
    private Set<Node> childNodes;
    private Map<UUID, String> sendedMessages;
    private Queue<Pair<DatagramPacket, String>> toSend;

    MessageHandler(Node parentNode, Set<Node> childNodes, Map<UUID, String> sendedMessages, Queue<Pair<DatagramPacket, String>> toSend){
        this.parentNode = parentNode;
        this.childNodes = childNodes;
        this.sendedMessages = sendedMessages;
        this.toSend = toSend;
    }

    private void sendMessage(){
        if(!toSend.isEmpty()){

        }

    }
    public void putMessageIntoQueue(String type) throws IOException {
        byte buf[];
        MessageGenerator messageGenerator = new MessageGenerator(type);
        buf = messageGenerator.getMessageToSend().getBytes();
        //System.out.println(new String(buf, "ASCII"));
        DatagramPacket toSend = new DatagramPacket(buf, buf.length, parentNode.getNodeAddress(), parentNode.getNodePort());
        this.toSend.add(new Pair<>(toSend,type));
    }
}
