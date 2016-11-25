package com.nsu.fit.pospelov;

import javafx.util.Pair;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

public class Client {

    private class MessageParser {

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
    }

    private Node parentNode;
    private Set<Node> childNodes;
    private Node node;
    private DatagramSocket socket;

    private Map<UUID,String> receivedMessages;                 //хранилище принятых
    private Map<UUID,String> sendedMessages;                   //хранилище отправленных, чтобы не получать подтверждения по несколько раз
    private Queue<Pair<DatagramPacket, String>> toSend;                     //очередь сообщений на отправку

    private MessageHandler messageHandler;                     //сущность, отвечающая за формирование сообщений, отправку, запись в контейнеры



    Client(String nodeName, int losePercent, int port) throws Exception {
        node = new Node(nodeName, losePercent, port);
        socket = new DatagramSocket(port);
        messageHandler = new MessageHandler(parentNode,childNodes,sendedMessages,toSend);
        //getMesage();
    }

    Client(String nodeName, int losePercent, int port, InetAddress parentAddress, int parentPort) throws Exception {
        node = new Node(nodeName, losePercent, port);
        socket = new DatagramSocket(port);
        parentNode = new Node(parentAddress, parentPort);
        messageHandler = new MessageHandler(parentNode,childNodes, sendedMessages, toSend);
        messageHandler.putMessageIntoQueue("CONNECT");
        //sendMessage("CONNECT");
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
