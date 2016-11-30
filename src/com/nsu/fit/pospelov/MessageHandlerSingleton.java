package com.nsu.fit.pospelov;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
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
    private Map<UUID, Node> messageLastSenders; //знать, от кого пришло
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
        receivedMessages = new HashMap<>();
        messageLastSenders = new HashMap<>();
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
        receivedMessages.put(message.getId(),message);
        messageLastSenders.put(message.getId(),new Node(packet.getAddress(),packet.getPort()));
        //System.out.println(message.getId());
    }

    public Message parseMessage(DatagramPacket packet) throws Exception {
        Message message;
        String[] splittedMessage;
        String s = new String(packet.getData(), "ASCII");
        splittedMessage = s.split(":", -1);
        message = new Message(null,splittedMessage[0].split("\0")[0], splittedMessage[2].split("\0")[0]); //usersMes, type, nodeName
        switch (message.getType()){
            case "CONNECT":
                message.setId(java.util.UUID.fromString(splittedMessage[1].split("\0")[0]));
                childNodes.add(new Node(message.getOwnerNodeName(),packet.getAddress(),packet.getPort()));
                break;
            case "USERS":
                message.setUsersMessage(splittedMessage[3].split("\0")[0]);
                message.setId(java.util.UUID.fromString(splittedMessage[1].split("\0")[0]));
                System.out.println(message.getOwnerNodeName() +": "+message.getUsersMessage());
                putMessageIntoDeque(message);
        }

        return message;
    }

    public void sendMessage() throws Exception {
        Message message;
        DatagramPacket packet;

        if(!toSend.isEmpty()){
            message = toSend.getFirst();
            packet = message.getPacket();
            /*System.out.println("/");
            System.out.println(message.getId() );
            System.out.println(receivedMessages.containsKey(message.getId()));
            for ( UUID key : receivedMessages.keySet() ) {
                System.out.println( key.toString().equals(message.getId().toString()) );
                System.out.println(key.compareTo(message.getId()));
            }
            System.out.println("/");*/

            //System.exit(1);
            if(message.getType().equals("CONNECT")) {
                packet.setPort(parentNode.getNodePort());
                packet.setAddress(parentNode.getNodeAddress());
                socket.send(packet);
                sendedMessages.put(message.getId(),message);

            }
            if(message.getType().equals("USERS")) {
                if(receivedMessages.containsKey(message.getId())){
                    if(parentNode != null && !equalsSenderReceiver(message,parentNode)) {
                    //if(parentNode != null && ) parentNode.getNodePort(){
                        packet.setPort(parentNode.getNodePort());
                        packet.setAddress(parentNode.getNodeAddress());
                        socket.send(packet);
                        sendedMessages.put(message.getId(), message);
                    }
                    for(Node node: childNodes){
                        //if( !node.getNodeName().equals(message.getOwnerNodeName()) ) {
                        if( !equalsSenderReceiver(message,node)) {
                            packet.setPort(node.getNodePort());
                            packet.setAddress(node.getNodeAddress());
                            socket.send(packet);
                            sendedMessages.put(message.getId(), message);
                        }
                    }
                }else{
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
                        sendedMessages.put(message.getId(), message);
                    }
                }

            }
            toSend.pollFirst();

        }
    }

    private boolean checkChildAttachment(Message message){
        for(Node node: childNodes){
            if(node.getNodeName().equals(message.getOwnerNodeName()))
                return true;
        }
        return false;
    }

    private boolean equalsSenderReceiver(Message message, Node sender){
        if(sender.getNodePort() == messageLastSenders.get(message.getId()).getNodePort()
                && sender.getNodeAddress().equals(messageLastSenders.get(message.getId()).getNodeAddress()))
            return true;
        return false;
    }


    public void putMessageIntoDeque(String type, String data, String name) throws IOException { //помещает message в очередь
        Message message = new Message(data, type, name);
        message.initDatagramPacket();
        toSend.push(message);
    }

    public void putMessageIntoDeque(Message message){
        //System.out.println("!!!"+message.getId());
        message.initDatagramPacket();
        toSend.push(message);
    }
}
