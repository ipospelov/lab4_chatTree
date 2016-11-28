package com.nsu.fit.pospelov;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

public class MessageHandlerSingleton {
    private Node parentNode;
    private Set<Node> childNodes;
    private Map<UUID,Message> sendedMessages;
    private Map<UUID,Message> receivedMessages;
    private Queue<Message> toSend; //тип сообщения в первом аргументе
    private DatagramSocket socket;

    private byte buf[];

    MessageHandlerSingleton(){
    }

    private static class MessageHandlerHolder{
        private final static MessageHandlerSingleton instance = new MessageHandlerSingleton();
    }

    public static MessageHandlerSingleton getInstance(){
        return MessageHandlerSingleton.getInstance();
    }


    void MessageHandlerInit(DatagramSocket socket, Node parentNode, Set<Node> childNodes,
                            Map<UUID, Message> sendedMessages, Map<UUID, Message> receivedMessages,
                            Queue<Message> toSend){
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

    public void sendMessage() throws IOException {
        Message message;
        DatagramPacket packet;
        if(!toSend.isEmpty()){
            message = toSend.peek();
            packet = message.getPacket();
            if(message.getType().equals("CONNECT")) {
                packet.setPort(parentNode.getNodePort());
                packet.setAddress(parentNode.getNodeAddress());
            }
            socket.send(packet);
            sendedMessages.put(message.getId(),message);
            toSend.remove();
        }
    }



    public void putMessageIntoQueue(String type, String data, String name) throws IOException { //помещает message в очередь
        Message message = new Message(type, data, name);
        message.initDatagramPacket();
        toSend.add(message);
        //byte buf[];
        //MessageGenerator messageGenerator = new MessageGenerator(type);
        //buf = messageGenerator.getMessageToSend().getBytes();
        //System.out.println(new String(buf, "ASCII"));
        //DatagramPacket toSend = new DatagramPacket(buf, buf.length, parentNode.getNodeAddress(), parentNode.getNodePort());
        //this.toSend.add(new Message(toSend,messageGenerator.getId(),messageGenerator.getType()));
    }
}
