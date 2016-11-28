package com.nsu.fit.pospelov;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

import static java.lang.Thread.sleep;

public class MessageHandlerSingleton {
    private Node parentNode;
    private Set<Node> childNodes;
    private Map<UUID,Message> sendedMessages;
    private Map<UUID,Message> receivedMessages;
    private volatile Deque<Message> toSend; //тип сообщения в первом аргументе
    private DatagramSocket socket;

    private byte buf[];

    MessageHandlerSingleton(){
    }

    private static class MessageHandlerHolder{
        private final static MessageHandlerSingleton instance = new MessageHandlerSingleton();
    }

    public static MessageHandlerSingleton getInstance(){
        return MessageHandlerHolder.instance;
    }


    void MessageHandlerInit(DatagramSocket socket, Node parentNode, Set<Node> childNodes,
                            Map<UUID, Message> sendedMessages, Map<UUID, Message> receivedMessages,
                            Deque<Message> toSend){
        toSend = new LinkedBlockingDeque(10);
        sendedMessages = new HashMap<>();
        childNodes = new HashSet<>();
        buf = new byte[1024];
        this.parentNode = parentNode;
        this.childNodes = childNodes;
        this.sendedMessages = sendedMessages;
        this.receivedMessages = receivedMessages;
        this.toSend = toSend;
        this.socket = socket;

    }

    public void receiveMessage() throws Exception {
        Arrays.fill( buf, (byte) 0 );
        DatagramPacket packet = new DatagramPacket(buf,buf.length);
        socket.receive(packet);
        Message message = parseMessage(packet);
        //System.out.println("yes");
        receivedMessages.put(message.getId(),message);
    }

    public Message parseMessage(DatagramPacket packet) throws Exception {
        Message message;
        String[] splittedMessage;
        String s = new String(packet.getData(), "ASCII");
        String usersMessage;
        splittedMessage = s.split(":");
        message = new Message(null,splittedMessage[0], splittedMessage[2]); //usersMes, type, nodeName
        //System.out.println(message.getType());
        switch (message.getType()){
            case "CONNECT":
                message.setId(java.util.UUID.fromString(splittedMessage[1]));
                childNodes.add(new Node(message.getNodeName(),packet.getAddress(),packet.getPort()));
                //System.out.println(childNodes.size());
                break;
            case "USERS":
                message.setUsersMessage(splittedMessage[3]);
                message.setId(java.util.UUID.fromString(splittedMessage[1]));
                System.out.println(message.getNodeName() +":"+message.getUsersMessage());
        }
        return message;
    }

    public void sendMessage() throws Exception {
        Message message;
        DatagramPacket packet;

        if(!toSend.isEmpty()){
            message = toSend.getFirst();
            packet = message.getPacket();
            if(message.getType().equals("CONNECT")) {
                packet.setPort(parentNode.getNodePort());
                packet.setAddress(parentNode.getNodeAddress());
                socket.send(packet);
                sendedMessages.put(message.getId(),message);

            }
            if(message.getType().equals("USERS")) {
                if(parentNode != null) {
                    packet.setPort(parentNode.getNodePort());
                    packet.setAddress(parentNode.getNodeAddress());
                    socket.send(packet);
                    sendedMessages.put(message.getId(), message);
                }
                for(Node node: childNodes){
                    packet.setPort(node.getNodePort());
                    packet.setAddress(node.getNodeAddress());
                    socket.send(packet);
                    sendedMessages.put(message.getId(),message);
                }

            }
            toSend.pollFirst();

        }
    }



    public void putMessageIntoDeque(String type, String data, String name) throws IOException { //помещает message в очередь
        Message message = new Message(data, type, name);
        message.initDatagramPacket();
        toSend.push(message);
    }
}
