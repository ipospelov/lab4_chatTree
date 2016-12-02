package com.nsu.fit.pospelov;

import sun.misc.Signal;

import java.io.IOException;
import java.net.*;
import java.util.*;

import static com.nsu.fit.pospelov.MessageHandlerSingleton.createLRUMap;
import static java.lang.Thread.sleep;

public class Client {

    private class InputStreamReader extends Thread{
        public void run(){
            Scanner in = new Scanner(System.in);
            String message;
            while (true) {
                message = in.nextLine();
                try {
                    if(!message.equals(""))
                        messageHandlerSingleton.putMessageIntoDeque("USERS",message,clientName);
                } catch (IOException e) {
                    System.out.println("Putting into Deque error");
                    e.printStackTrace();
                }
            }
        }
    }

    public class DisconnectSender extends Thread{
        public void run(){
            try {
                messageHandlerSingleton.putMessageIntoDeque("DISCONNECT", null, clientName);
                sleep(2500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class MessageSender extends Thread{
        public void run(){
            int count = 0;
            while (true){
                count++;
                try {
                    sleep(500);
                    messageHandlerSingleton.sendMessage();

                    synchronized (sendedMessages) {
                        //System.out.println(sendedMessages.size());
                        if (count == 5 && sendedMessages.size() > 0) {
                            messageHandlerSingleton.dublicateSendedMessages();
                            /*for (UUID key : sendedMessages.keySet()) {
                                System.out.println(sendedMessages.get(key));
                                messageHandlerSingleton.putMessageIntoDeque(sendedMessages.get(key));
                            }*/
                            count = 0;
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
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
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    e.printStackTrace();

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

    private volatile Map<UUID,Message> receivedMessages;                 //хранилище принятых
    private volatile Map<UUID,Message> sendedMessages;
    private volatile Deque<Message> toSend;                     //очередь сообщений на отправку
    private MessageHandlerSingleton messageHandlerSingleton;                     //ие сообщений, отправку, запись в контейнеры
    private ChatSignalHandler signalHandler;

    public static int losePercent;


    Client(String nodeName, int losePercent, int port) throws Exception {
        sendedMessages = createLRUMap(15);
        receivedMessages = createLRUMap(15);
        this.losePercent = losePercent;
        clientName = nodeName;
        inputStreamReader = new InputStreamReader();
        messageReader = new MessageReader();
        messageSender = new MessageSender();
        node = new Node(nodeName, losePercent, port);
        node.setNodeAddress(getLocalAddress());
        socket = new DatagramSocket(port);
        messageHandlerSingleton = MessageHandlerSingleton.getInstance();
        messageHandlerSingleton.MessageHandlerInit(socket, parentNode,childNodes, sendedMessages, receivedMessages, toSend, node);

        messageReader.start();
        inputStreamReader.start();
        messageSender.start();

        setSignalHandler(nodeName);
    }


    Client(String nodeName, int losePercent, int port, InetAddress parentAddress, int parentPort) throws Exception {
        sendedMessages = createLRUMap(15);
        receivedMessages = createLRUMap(15);
        this.losePercent = losePercent;
        clientName = nodeName;
        inputStreamReader = new InputStreamReader();
        messageSender = new MessageSender();
        messageReader = new MessageReader();
        node = new Node(nodeName, losePercent, port);

        node.setNodeAddress(getLocalAddress());
        socket = new DatagramSocket(port);

        parentNode = new Node(parentAddress, parentPort);
        messageHandlerSingleton = MessageHandlerSingleton.getInstance();
        messageHandlerSingleton.MessageHandlerInit(socket, parentNode,childNodes, sendedMessages, receivedMessages, toSend, node);
        messageHandlerSingleton.putMessageIntoDeque("CONNECT", null, nodeName);

        messageSender.start();
        inputStreamReader.start();
        messageReader.start();

        setSignalHandler(nodeName);

    }

    private void setSignalHandler(String nodeName){
        signalHandler = new ChatSignalHandler(){
            @Override
            public void handle(Signal sig) {
                try {
                    //inputStreamReader.interrupt();
                    //messageSender.interrupt();
                    //messageReader.interrupt();
                    messageHandlerSingleton.putMessageIntoDeque("DISCONNECT", null, nodeName);
                    messageHandlerSingleton.sendMessage();
                    //messageHandlerSingleton.waitingForDisconnectAck();
                } catch (Exception e) {
                    System.out.println("Disconnecting error:" + e);
                }

            }
        };
        ChatSignalHandler.install("TERM", signalHandler);
        ChatSignalHandler.install("INT", signalHandler);
        ChatSignalHandler.install("ABRT", signalHandler);
    }

    private static InetAddress getLocalAddress(){
        try {
            Enumeration<NetworkInterface> b = NetworkInterface.getNetworkInterfaces();
            while( b.hasMoreElements()){
                for ( InterfaceAddress f : b.nextElement().getInterfaceAddresses())
                    if ( f.getAddress().isSiteLocalAddress())
                        return f.getAddress();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

}
