package com.nsu.fit.pospelov;

import java.io.IOException;
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
                System.out.println(message);
                try {
                    messageHandlerSingleton.putMessageIntoQueue("USERS",message,clientName);
                } catch (IOException e) {
                    System.out.println("Putting into queue error");
                    e.printStackTrace();
                }
            }
        }
    }

    private class MessageNetworkInterraction extends Thread{ //посылает и принимает
        public void run(){
            while (true){

                try {

                    messageHandlerSingleton.sendMessage();
                    //sleep(500);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*private class MessageParser {

        private String[] splitedMessage;
        public MessageParser(String message, InetAddress address, int port) {

            splitedMessage = message.split(":");
            parseMessage();
            switch (splitedMessage[0]){
                case "CONNECT":
                    childNodes.add(new Node(address,port));
                    break;
            }
        }

        private void parseMessage() {

        }
    }*/
    private InputStreamReader inputStreamReader;

    private Node parentNode;
    private Set<Node> childNodes;
    private Node node;
    private DatagramSocket socket;
    private String clientName;

    private Map<UUID,Message> receivedMessages;                 //хранилище принятых
    private Map<UUID,Message> sendedMessages;                   //хранилище отправленных, чтобы не получать подтверждения по несколько раз
    private Queue<Message> toSend;                     //очередь сообщений на отправку

    private MessageHandlerSingleton messageHandlerSingleton;                     //сущность, отвечающая за формирование сообщений, отправку, запись в контейнеры



    Client(String nodeName, int losePercent, int port) throws Exception {
        clientName = nodeName;
        inputStreamReader = new InputStreamReader();
        node = new Node(nodeName, losePercent, port);
        socket = new DatagramSocket(port);
        messageHandlerSingleton = MessageHandlerSingleton.getInstance();
        messageHandlerSingleton.MessageHandlerInit(socket, parentNode,childNodes, sendedMessages, receivedMessages, toSend);

        inputStreamReader.start();
    }

    Client(String nodeName, int losePercent, int port, InetAddress parentAddress, int parentPort) throws Exception {
        clientName = nodeName;
        inputStreamReader = new InputStreamReader();
        node = new Node(nodeName, losePercent, port);
        socket = new DatagramSocket(port);
        parentNode = new Node(parentAddress, parentPort);
        messageHandlerSingleton = MessageHandlerSingleton.getInstance();
        messageHandlerSingleton.MessageHandlerInit(socket, parentNode,childNodes, sendedMessages, receivedMessages, toSend);
        messageHandlerSingleton.putMessageIntoQueue("CONNECT", null,nodeName);

        inputStreamReader.start();
    }


/*    private void getMesage() throws IOException {
        byte buff[] = new byte[1024];
        MessageParser messageParser;
        String message;
        DatagramPacket rcv = new DatagramPacket(buff,1024);
        socket.receive(rcv);
        messageParser = new MessageParser(new String(rcv.getData(), "ASCII"), rcv.getAddress(), rcv.getPort());
        //message = new String(rcv.getData(), "ASCII");
        //System.out.println(message);
    }*/




    private void putIntoTree(InetAddress rootAddress, int rootPort){

    }
}
