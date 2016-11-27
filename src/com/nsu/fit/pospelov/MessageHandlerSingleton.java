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

    MessageHandlerSingleton(){
    }

    private static class MessageHandlerHolder{
        private final static MessageHandlerSingleton instance = new MessageHandlerSingleton();
    }

    public static MessageHandlerSingleton getInstance(){
        return MessageHandlerSingleton.getInstance();
    }


    void MessageHandlerInit(DatagramSocket socket, Node parentNode, Set<Node> childNodes, Map<UUID, Message> sendedMessages, Map<UUID, Message> receivedMessages, Queue<Message> toSend){
        this.parentNode = parentNode;
        this.childNodes = childNodes;
        this.sendedMessages = sendedMessages;
        this.receivedMessages = receivedMessages;
        this.toSend = toSend;
        this.socket = socket;

    }

    public void receiveMessage() throws Exception {
        byte buf[] = new byte[1024];
        socket.setSoTimeout(500);
        DatagramPacket packet = new DatagramPacket(buf,buf.length);
        socket.receive(packet);
        Message message = parseMessage(packet);
        receivedMessages.put(message.getId(),message);
    }

    public Message parseMessage(DatagramPacket packet) throws UnsupportedEncodingException {
        Message message;
        String[] splittedMessage;
        String s = new String(packet.getData(), "ASCII");
        splittedMessage = s.split(":");
        message = new Message(splittedMessage[0], null);
        switch (message.getType()){
            case "CONNECT":
                message.setId(java.util.UUID.fromString(splittedMessage[1]));
                break;
        }
        return message;
    }

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
            //добавить в отправленные
            sendedMessages.put(message.getId(),message);
            socket.send(packet);
        }
    }



    public void putMessageIntoQueue(String type, String data) throws IOException { //помещает message в очередь
        Message message = new Message(type, data);
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
