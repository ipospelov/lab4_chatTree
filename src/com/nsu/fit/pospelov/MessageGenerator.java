package com.nsu.fit.pospelov;

/**
 * Created by posiv on 24.11.16.
 */
public class MessageGenerator {
    private String type;
    public MessageGenerator(String type) {
        this.type = type;
    }

    public String getMessageToSend(){
        String result = "";
        String UUID = java.util.UUID.randomUUID().toString();
        switch (type){
            case "CONNECT":
                result = type + ":" + UUID;
                break;
        }

        return result;
    }
}
