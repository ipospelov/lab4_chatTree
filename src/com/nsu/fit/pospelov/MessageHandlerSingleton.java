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
    private Deque<Message> toSend; //тип сообщения в первом аргументе
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
        buf = new byte[1024];
        this.parentNode = parentNode;
        this.childNodes = childNodes;
        this.sendedMessages = sendedMessages;
        this.receivedMessages = receivedMessages;
        this.toSend = toSend;
        this.socket = socket;

    }

    public void receiveMessage() throws Exception {
        socket.setSoTimeout(500);
        DatagramPacket packet = new DatagramPacket(buf,buf.length);
        socket.receive(packet);
        System.out.println("Smth");
        Message message = parseMessage(packet);
        receivedMessages.put(message.getId(),message);
    }

    public Message parseMessage(DatagramPacket packet) throws Exception {
        Message message;
        String[] splittedMessage;
        String s = new String(packet.getData(), "ASCII");
        splittedMessage = s.split(":");
        message = new Message(null,splittedMessage[0], splittedMessage[2]); //usersMes, type, nodeName

        switch (message.getType()){
            case "CONNECT":
                message.setId(java.util.UUID.fromString(splittedMessage[1]));
                childNodes.add(new Node(message.getNodeName(),packet.getAddress(),packet.getPort()));
                System.out.println(s);
                break;
        }
        return message;
    }

/*    public void handleMessage(Message message){
        switch (message.getType()){
            case "USERS":
                System.out.println(message.getUsersMessage());
                break;
            case "CONNECT":
                //Node node = new Node()
                //childNodes.add()
                break;
        }
        *//*if(message.getType().equals("USERS")){
            System.out.println(message.getUsersMessage());
        }else if*//*
    }*/

    public void sendMessage() throws Exception {
        Message message;
        DatagramPacket packet;
        //System.out.println(toSend.size());
        //System.exit(1);
        if(!toSend.isEmpty()){
            message = toSend.getFirst();
            packet = message.getPacket();
            if(message.getType().equals("CONNECT")) {
                packet.setPort(parentNode.getNodePort());
                packet.setAddress(parentNode.getNodeAddress());
            }
            sendedMessages.put(message.getId(),message);
            toSend.pollFirst();
            //toSend.remove();
        }
    }



    public void putMessageIntoDeque(String type, String data, String name) throws IOException { //помещает message в очередь
        Message message = new Message(data, type, name);
        message.initDatagramPacket();
        //System.out.println("!" + toSend.size());

        toSend.push(message);
        //toSend.add(message);
        //byte buf[];
        //MessageGenerator messageGenerator = new MessageGenerator(type);
        //buf = messageGenerator.getMessageToSend().getBytes();
        //System.out.println(new String(buf, "ASCII"));
        //DatagramPacket toSend = new DatagramPacket(buf, buf.length, parentNode.getNodeAddress(), parentNode.getNodePort());
        //this.toSend.add(new Message(toSend,messageGenerator.getId(),messageGenerator.getType()));
    }
}
