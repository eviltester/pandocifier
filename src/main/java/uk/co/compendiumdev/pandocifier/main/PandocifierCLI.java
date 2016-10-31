package uk.co.compendiumdev.pandocifier.main;


import uk.co.compendiumdev.pandocifier.Pandocifier;
import uk.co.compendiumdev.pandocifier.config.PandocifierConfig;
import uk.co.compendiumdev.pandocifier.config.PandocifierConfigFileReader;

import java.io.File;
import java.io.IOException;

public class PandocifierCLI {

    public static void main(String[] args) {

        PandocifierConfig config = null;

        String propertyFileName;
        File propertyFile;

        if(args.length ==0){
            propertyFileName = "pandocifier.properties";
            propertyFile = new File(System.getProperty("user.dir"),propertyFileName);

            System.out.println("Using Default properties path " + propertyFile.getAbsolutePath());

        }else{
            // assume [0] is the filename or path
            // if we check it as a file and it does not exist then assume it is a filename in user.dir
            propertyFileName = args[0];

            propertyFile = new File(propertyFileName);

            if(!propertyFile.exists()){
                System.out.println("Could not find properties file " + propertyFile.getAbsolutePath());
                System.out.println("Trying in working directory");
                propertyFile = new File(System.getProperty("user.dir"),propertyFileName);
            }
        }

        if(!propertyFile.exists()){
            System.err.println("Could not find property file: " + propertyFile.getAbsolutePath());
            System.exit(1);
        }


        try {

            config = new PandocifierConfigFileReader().fromPropertyFile(propertyFile);
            Pandocifier pandocifier = new Pandocifier(config);
            pandocifier.createPDF();

        } catch (IOException e) {
            System.err.println("Error running pandocifier : " + e.getMessage());
            e.printStackTrace();
        }


    }
}
