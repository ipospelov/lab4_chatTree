package com.nsu.fit.pospelov;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Thread.sleep;

public class MessageHandlerSingleton {
    private Node parentNode;
    private Set<Node> childNodes;
    private volatile Map<UUID,Message> sendedMessages;
    private Map<UUID,Message> receivedMessages;
    private Map<UUID, Node> messageLastSenders;// = createLRUMap(15); //знать, от кого пришло
    private volatile Deque<Message> toSend; //тип сообщения в первом аргументе
    private DatagramSocket socket;
    private Node clientNode;
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
                            Deque<Message> toSend, Node clientNode){
        this.clientNode = clientNode;
        toSend = new LinkedBlockingDeque(20);
        this.sendedMessages = sendedMessages;
        this.receivedMessages = receivedMessages;
        //messageLastSenders = createLRUMap(10);
        //sendedMessages = new HashMap<>();
        childNodes = new HashSet<>();
        //receivedMessages = new HashMap<>();
        messageLastSenders = new HashMap<>();
        buf = new byte[1024];
        this.parentNode = parentNode;
        this.childNodes = childNodes;
        //this.sendedMessages = sendedMessages;
        //this.receivedMessages = receivedMessages;
        this.toSend = toSend;
        this.socket = socket;

    }

    public void receiveMessage() throws Exception {
        Arrays.fill( buf, (byte) 0 );
        Random rand = new Random();
        DatagramPacket packet = new DatagramPacket(buf,buf.length);
        socket.receive(packet);
        if( ((0 + (int)(Math.random() * ((100) + 1))) < Client.losePercent)){
            System.out.println("loosing packet");
            return;
        }
        Message message = parseMessage(packet);

        if(message == null)
            return;

        messageLastSenders.put(message.getId(),new Node(packet.getAddress(),packet.getPort()));
        //System.out.println(message.getId());
    }

    public Message parseMessage(DatagramPacket packet) throws Exception {
        Message message;
        String[] splittedMessage;
        String s = new String(packet.getData(), "ASCII");
        splittedMessage = s.split(":", -1);
        message = new Message(null,splittedMessage[0].split("\0")[0], splittedMessage[2].split("\0")[0]); //usersMes, type, nodeName
        message.setId(java.util.UUID.fromString(splittedMessage[1].split("\0")[0]));

        if(receivedMessages.containsKey(message.getId())  && !message.getType().equals("ACK")){
            System.out.println("ACK loosed. Twice users message receiving");
            putAckIntoDeque(message.getId(),clientNode.getNodeName(),packet.getPort(),packet.getAddress());
            return null;
        }

        switch (message.getType()){
            case "CONNECT":
                receivedMessages.put(message.getId(),message);
                putAckIntoDeque(message.getId(),clientNode.getNodeName(),packet.getPort(),packet.getAddress());
                childNodes.add(new Node(message.getOwnerNodeName(),packet.getAddress(),packet.getPort()));
                break;
            case "USERS":
                receivedMessages.put(message.getId(),message);
                putAckIntoDeque(message.getId(),clientNode.getNodeName(),packet.getPort(),packet.getAddress());
                message.setUsersMessage(splittedMessage[3].split("\0")[0]);
                System.out.println(message.getOwnerNodeName() +": "+message.getUsersMessage());
                putMessageIntoDeque(message);
                break;
            case "DISCONNECT":
                message.setNewParentPort(Integer.parseInt(splittedMessage[3].split("\0")[0]));
                String ipadr = splittedMessage[4].split("\0")[0];
                ipadr = ipadr.substring(1,ipadr.length());
                message.setNewParentNodeAddress(InetAddress.getByName(ipadr));
                if(parentNode != null){
                    if(packet.getPort() == parentNode.getNodePort()
                            &&packet.getAddress().equals(parentNode.getNodeAddress())) {

                        if (message.getNewParentPort() == clientNode.getNodePort()
                                && message.getNewParentNodeAddress().equals(clientNode.getNodeAddress())) {
                            parentNode = null;
                        }
                        if (message.getNewParentPort() != clientNode.getNodePort()
                                && !message.getNewParentNodeAddress().equals(clientNode.getNodeAddress())) {
                            parentNode = new Node(message.getNewParentNodeAddress(), message.getNewParentPort());
                            putMessageIntoDeque("CONNECT", null, clientNode.getNodeName());
                        }
                    }else{
                        removeDeadChild(packet);
                    }

                }else{
                    removeDeadChild(packet);
                }
                System.out.println(message.getOwnerNodeName() + " disconnected");
                break;
            case "ACK":
                sendedMessages.remove(message.getId());
                break;
        }

        return message;
    }

    public void sendMessage() throws Exception {
        Message message;
        DatagramPacket packet;
        synchronized (sendedMessages) {
            if (!toSend.isEmpty()) {
                message = toSend.getFirst();

                packet = message.getPacket();
                switch (message.getType()) {
                    case "CONNECT":
                        SetNSend(parentNode, message, packet);
                        sendedMessages.put(message.getId(), message);
                        break;
                    case "USERS":
                        if (receivedMessages.containsKey(message.getId())) {
                            if (parentNode != null && !equalsSenderReceiver(message, parentNode)) {
                                SetNSend(parentNode, message, packet);
                                sendedMessages.put(message.getId(), message);
                            }
                            for (Node node : childNodes) {
                                if (!equalsSenderReceiver(message, node)) {
                                    SetNSend(node, message, packet);
                                    sendedMessages.put(message.getId(), message);
                                }
                            }
                        } else {
                            if (parentNode != null) {
                                SetNSend(parentNode, message, packet);
                                sendedMessages.put(message.getId(), message);
                            }
                            for (Node node : childNodes) {
                                SetNSend(node, message, packet);
                                sendedMessages.put(message.getId(), message);
                            }
                        }
                        break;
                    case "DISCONNECT":
                        if (parentNode != null)
                            SetNSend(parentNode, message, packet);
                        for (Node node : childNodes) {
                            SetNSend(node, message, packet);
                        }
                        break;
                    case "ACK":
                        socket.send(packet);
                        break;
                }

                toSend.removeFirst();

            }
        }
    }
    private void SetNSend(Node node, Message message, DatagramPacket packet) throws IOException {
        packet.setPort(node.getNodePort());
        packet.setAddress(node.getNodeAddress());
        socket.send(packet);
    }


    private boolean equalsSenderReceiver(Message message, Node sender){
        if(sender.getNodePort() == messageLastSenders.get(message.getId()).getNodePort()
                && sender.getNodeAddress().equals(messageLastSenders.get(message.getId()).getNodeAddress()))
            return true;
        return false;
    }

    private void removeDeadChild(DatagramPacket packet){
        for(Node node : childNodes){
            if(packet.getPort()==node.getNodePort() && packet.getAddress().equals(node.getNodeAddress())){
                childNodes.remove(node);
                break;
            }
        }
    }


    public void putMessageIntoDeque(String type, String data, String name) throws IOException { //помещает message в очередь
        Message message = new Message(data, type, name);

        if(type.equals("DISCONNECT")){
            if(parentNode!=null) {
                message.setNewParentPort(parentNode.getNodePort());
                message.setNewParentNodeAddress(parentNode.getNodeAddress());
            }else{
                if(childNodes.size() > 0) {
                    message.setNewParentPort(childNodes.iterator().next().getNodePort());
                    message.setNewParentNodeAddress(childNodes.iterator().next().getNodeAddress());
                    System.out.println(message.getNewParentPort());
                    System.out.println(message.getNewParentNodeAddress());
                }else {
                    message.setNewParentPort(-1);
                    message.setNewParentNodeAddress(InetAddress.getLocalHost());
                }
            }
        }
        message.initDatagramPacket();
        toSend.push(message);
    }

    public void putAckIntoDeque(UUID id, String ownerName, int port, InetAddress address){
        Message message = new Message(null,"ACK",ownerName);
        message.initAckDatagramPacket(id,port,address);
        toSend.push(message);
    }

    public void putMessageIntoDeque(Message message){
        message.initDatagramPacket();
        toSend.push(message);
    }

    public static  <K, V> Map<K, V> createLRUMap(final int maxEntries) {
        return new LinkedHashMap<K, V>(maxEntries*10/7, 0.7f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > maxEntries;
            }
        };
    }
}
