package com.nsu.fit.pospelov;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

/**
 * Created by posiv on 23.11.16.
 */
public class Parser {
    private Options options;
    private CommandLineParser parser;

    public Parser(String[] args){
        options = new Options();
        parser = new DefaultParser();

        options.addOption("aparent",false,"ParentAddress");
        options.addOption("aport",false,"ParentPort");
        options.addOption("p",true,"Port");
        options.addOption("lose",true,"LosePercent");
        options.addOption("name",true,"NodeName");

        try{
            CommandLine cmd = parser.parse( options, args);
        }catch (Exception e){

        }
    }
}
