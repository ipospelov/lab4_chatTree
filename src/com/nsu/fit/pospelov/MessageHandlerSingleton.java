package com.nsu.fit.pospelov;


import javafx.scene.chart.PieChart;
import javafx.util.Pair;

import java.io.IOException;
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


    void MessageHandlerInit(DatagramSocket socket, Node parentNode, Set<Node> childNodes, Map<UUID, Message> sendedMessages, Queue<Message> toSend){
        this.parentNode = parentNode;
        this.childNodes = childNodes;
        this.sendedMessages = sendedMessages;
        this.toSend = toSend;
        this.socket = socket;

    }

    private void sendMessage() throws IOException {
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
    public void putMessageIntoQueue(String type) throws IOException { //помещает message в очередь
        Message message = new Message(type);
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
