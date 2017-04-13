import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import uk.co.compendiumdev.pandocifier.config.PandocifierConfig;
import uk.co.compendiumdev.pandocifier.config.PandocifierConfigFileReader;
import uk.co.compendiumdev.pandocifier.main.PandocifierCLI;

import java.io.File;
import java.io.IOException;


public class LeanPubPandocPreviewTest {

    // for given a hardcoded path
    // read the Book.txt file
    // create a list of File names from Book.txt
    // create a folder called pandoced (if necessary)
    // create a new file in pandoced called leanpubpreview.md
    // write all the contents of the files from Book.txt into this file
    // output the command to generate the book to console

    @Ignore("Ignored because this is the MVP GUI for adhoc usage and interactive testing, not for running in the build")
    @Test
    public void createPreviewMVP() throws IOException {

        String args[] = { new File(System.getProperty("user.dir"),"pandocifier.properties").getAbsolutePath()};


        new PandocifierCLI().main(args);

        String propertyFileName = args[0];
        File propertyFile = new File(propertyFileName);
        PandocifierConfig config = new PandocifierConfigFileReader().fromPropertyFile(propertyFile);

        Assert.assertTrue(new File(config.getPreviewFileName()).exists());

    }

    @Ignore("Ignored because this is the MVP GUI for adhoc usage and interactive testing, not for running in the build")
    @Test
    public void createPreviewMVPHardPath() throws IOException {

        String args[] = { "d://users//Alan//Documents//github//pandocifier//pandocifier.properties"};

        new PandocifierCLI().main(args);

    }



}