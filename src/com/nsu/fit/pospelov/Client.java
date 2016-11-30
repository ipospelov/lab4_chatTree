package com.nsu.fit.pospelov;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

public class Client {

    private class InputStreamReader extends Thread{
        public void run(){
            Scanner in = new Scanner(System.in);
            String message;
            while (true) {
                message = in.nextLine();
                try {
                    messageHandlerSingleton.putMessageIntoDeque("USERS",message,clientName);
                } catch (IOException e) {
                    System.out.println("Putting into Deque error");
                    e.printStackTrace();
                }
            }
        }
    }

    private class MessageSender extends Thread{
        public void run(){
            while (true){

                try {

                    messageHandlerSingleton.sendMessage();
                    sleep(500);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
    }

    private class MessageReader extends Thread{
        public void run(){
            while (true) {
                try {
                    messageHandlerSingleton.receiveMessage();
                } catch (Exception e) {
                    //e.printStackTrace();

                    continue;
                }
            }
        }
    }

    private InputStreamReader inputStreamReader;
    private MessageSender messageSender;
    private MessageReader messageReader;

    private Node parentNode;
    private Set<Node> childNodes;
    private Node node;
    private DatagramSocket socket;
    private String clientName;

    private Map<UUID,Message> receivedMessages;                 //хранилище принятых
    private Map<UUID,Message> sendedMessages;                   //хранилище отправленных, чтобы не получать подтверждения по несколько раз
    private Deque<Message> toSend;                     //очередь сообщений на отправку

    private MessageHandlerSingleton messageHandlerSingleton;                     //сущность, отвечающая за формирование сообщений, отправку, запись в контейнеры



    Client(String nodeName, int losePercent, int port) throws Exception {

        clientName = nodeName;
        inputStreamReader = new InputStreamReader();
        messageReader = new MessageReader();
        messageSender = new MessageSender();
        node = new Node(nodeName, losePercent, port);
        socket = new DatagramSocket(port);
        //System.out.println(port);
        messageHandlerSingleton = MessageHandlerSingleton.getInstance();
        messageHandlerSingleton.MessageHandlerInit(socket, parentNode,childNodes, sendedMessages, receivedMessages, toSend);

        messageReader.start();
        inputStreamReader.start();
        messageSender.start();
    }

    Client(String nodeName, int losePercent, int port, InetAddress parentAddress, int parentPort) throws Exception {
        clientName = nodeName;
        inputStreamReader = new InputStreamReader();
        messageSender = new MessageSender();
        messageReader = new MessageReader();
        node = new Node(nodeName, losePercent, port);
        socket = new DatagramSocket(port);

        parentNode = new Node(parentAddress, parentPort);
        messageHandlerSingleton = MessageHandlerSingleton.getInstance();
        messageHandlerSingleton.MessageHandlerInit(socket, parentNode,childNodes, sendedMessages, receivedMessages, toSend);
        messageHandlerSingleton.putMessageIntoDeque("CONNECT", null, nodeName);

        messageSender.start();
        inputStreamReader.start();
        messageReader.start();

    }

}
