package com.nsu.fit.pospelov;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class Client {
    private Node parentNode;
    private Set<Node> childNodes;
    private Node node;
    private DatagramSocket socket;

    Client(String nodeName, int losePercent, int port) throws Exception {
        node = new Node(nodeName, losePercent, port);
        socket = new DatagramSocket(port);
        getMesage();
    }

    Client(String nodeName, int losePercent, int port, InetAddress parentAddress, int parentPort) throws Exception {
        node = new Node(nodeName, losePercent, port);
        socket = new DatagramSocket(port);
        parentNode = new Node(parentAddress, parentPort);
        sendMessage("CONNECT");
    }


    private void getMesage() throws IOException {
        byte buff[] = new byte[1024];
        String message;
        DatagramPacket rcv = new DatagramPacket(buff,1024);
        socket.receive(rcv);
        message = new String(rcv.getData(), StandardCharsets.UTF_8);
        System.out.println(message);
    }

    private void sendMessage(String type) throws IOException {
        byte buf[];
        Message message = new Message(type);
        buf = message.getMessageToSend().getBytes();
        DatagramPacket toSend = new DatagramPacket(buf, buf.length, parentNode.getNodeAddress(), parentNode.getNodePort());
        socket.send(toSend);
    }



    private void putIntoTree(InetAddress rootAddress, int rootPort){

    }
}
