package com.nsu.fit.pospelov;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by posiv on 23.11.16.
 */
public class Node {
    private InetAddress nodeAddress;
    private int nodePort;
    private String nodeName;
    private int nodeLosePercent;

    public Node(String nodeName, InetAddress nodeAddress, int nodePort) throws UnknownHostException {
        this.nodeAddress = InetAddress.getLocalHost();
        this.nodePort = nodePort;
        this.nodeName = nodeName;
        this.nodeLosePercent = nodeLosePercent;
    }

    public Node(InetAddress nodeAddress, int nodePort) {
        this.nodeAddress = nodeAddress;
        this.nodePort = nodePort;
    }

    public Node(String nodeName, int nodeLosePercent ,int nodePort) {
        this.nodeName = nodeName;
        this.nodeLosePercent = nodeLosePercent;
        this.nodePort = nodePort;
    }


    public int getNodePort() {
        return nodePort;
    }

    public void setNodePort(int nodePort) {
        this.nodePort = nodePort;
    }

    public String getNodeName() {
        return nodeName;
    }

    public InetAddress getNodeAddress() {
        return nodeAddress;
    }

    public void setNodeAddress(InetAddress nodeAddress) {
        this.nodeAddress = nodeAddress;
    }
}
