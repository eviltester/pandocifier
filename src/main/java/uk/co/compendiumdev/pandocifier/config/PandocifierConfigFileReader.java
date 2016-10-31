package uk.co.compendiumdev.pandocifier.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


public class PandocifierConfigFileReader {

    public PandocifierConfig fromPropertyFile(File file) throws IOException {

        Properties fromFile = new Properties();

        fromFile.load(new FileInputStream(file));

        PandocifierConfig config = new PandocifierConfig();
        config.setInputFilePath(fromFile.getProperty("InputFilePath"));
        config.setPreviewFileName(fromFile.getProperty("PreviewFileName"));
        config.setTempFolderName(fromFile.getProperty("TempFolderName"));
        config.setPandocPath(fromFile.getProperty("PandocPath"));
        return config;
    }
}
