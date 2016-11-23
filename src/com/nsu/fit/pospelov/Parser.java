package com.nsu.fit.pospelov;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import java.net.InetAddress;

public class Parser {
    private Options options;
    private CommandLineParser parser;

    Parser(String[] args){
        options = new Options();
        parser = new DefaultParser();

        options.addOption("p",true,"Port");
        options.addOption("lose",true,"LosePercent");
        options.addOption("name",true,"NodeName");
        options.addOption("paddress",true,"ParentAddress");
        options.addOption("pport",true,"ParentPort");

        try{
            Client client;
            CommandLine cmd = parser.parse( options, args);
            int losePercent = Integer.parseInt(cmd.getOptionValue("lose"));
            int nodePort = Integer.parseInt(cmd.getOptionValue("p"));

            if(cmd.hasOption("paddress") && cmd.hasOption("pport")){
                InetAddress aparentAddress = InetAddress.getByName(cmd.getOptionValue("paddress"));
                int pparentPort = Integer.parseInt(cmd.getOptionValue("pport"));
                client = new Client(cmd.getOptionValue("name"), losePercent, nodePort, aparentAddress, pparentPort);
            }else{
                client = new Client(cmd.getOptionValue("name"), losePercent, nodePort);
            }

        }catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
