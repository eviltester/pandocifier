package refactoring;

import org.junit.Ignore;
import org.junit.Test;
import uk.co.compendiumdev.pandocifier.BookTxtFile;
import uk.co.compendiumdev.pandocifier.LeanPubMarkdownLineProcessor;
import uk.co.compendiumdev.pandocifier.config.PandocifierConfig;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Properties;


/*
    Refactoring to a more 'strategic' implementation

    - inject config
    - externalise config
    - create Pandocifier object to allow usage from different `@Test` and `main` methods
    - use external property file to configure the pandocifier
 */
public class LeanPubPandocConfigExecuteObjectPropertiesTest {

    // for given a hardcoded path
    // read the Book.txt file
    // create a list of File names from Book.txt
    // create a folder called pandoced (if necessary)
    // create a new file in pandoced called leanpubpreview.md
    // write all the contents of the files from Book.txt into this file
    // output the command to generate the book to console


    @Ignore("Used to create the properties file")
    @Test public void createPropertiesFile() throws IOException {
        Properties hardcodedProperties = new Properties();
        hardcodedProperties.setProperty("InputFilePath","D:\\Users\\Alan\\Documents\\xp-dev\\eLearning\\tracksApiCaseStudy\\text\\Book.txt");
        hardcodedProperties.setProperty("PreviewFileName","leanpubpreview");
        hardcodedProperties.setProperty("TempFolderName","pandoced");
        hardcodedProperties.setProperty("PandocPath","c:\\users\\Alan\\AppData\\Local\\Pandoc\\pandoc.exe");

        FileOutputStream fos = new FileOutputStream(new File(System.getProperty("user.dir"),"pandocifier.properties"));
        hardcodedProperties.store(fos,"Initial Hard Coded Properties");
        fos.flush();
        fos.close();
    }


    @Test
    public void createPreviewMVP() throws IOException {

        PandocifierConfig config = new PandocifierConfigFileReader().fromPropertyFile(
                                        new File(System.getProperty("user.dir"),"pandocifier.properties"));
        Pandocifier pandocifier = new Pandocifier(config);
        pandocifier.createPDF();

    }


    private class Pandocifier {
        private final PandocifierConfig config;

        public Pandocifier(PandocifierConfig config) {
            this.config = config;
        }

        public void createPDF() throws IOException {
            BookTxtFile book_details = new BookTxtFile(config.getInputFilePath());
            book_details.readTheListOfContentFiles();

            // create a folder called pandoced (if necessary)
            File pandocfolder = new File(book_details.getParentFolder(), config.getTempFolderName());
            pandocfolder.mkdirs();

            // create a new file in pandoced called leanpubpreview.md
            File pandoced = new File(pandocfolder, config.getPreviewFileName() + ".md");
            if(pandoced.exists()){
                pandoced.delete();
            }
            pandoced.createNewFile();

            // write all the contents of the files from Book.txt into this file

            BufferedWriter leanpubpreview = Files.newBufferedWriter(pandoced.toPath(),
                    StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND);

            LeanPubMarkdownLineProcessor lineProcessor = new LeanPubMarkdownLineProcessor();

            for(String fileNameToWriteContents : book_details.contentFiles()){


                // I need to process some of the information as we work through so we will have to process the lines one by one
                List<String> lines  = Files.readAllLines(Paths.get(fileNameToWriteContents));

                String state="LINE_PROCESSING"; // LINE_PROCESSING, EXPECTED_SOURCE_INCLUDE, SKIP_LINE
                String lineCache="";

                for(String aLine : lines){

                    if(state.contentEquals("EXPECTED_SOURCE_INCLUDE")){
                        // if this line is not a "<<" source include then write the cache and clear it and go back to line processing
                        if(!lineProcessor.isLineAnExternalSourceInclude(aLine)){
                            leanpubpreview.write(lineCache);
                            state="LINE_PROCESSING";
                        }
                    }

                    if(lineProcessor.isLineAnExternalSourceInclude(aLine)){
                        lineProcessor.mergeTheExternalCodeFileAsACodeBlock(aLine, fileNameToWriteContents, leanpubpreview);
                        state="SKIP_LINE";
                    }

                    if(lineProcessor.isLineAnImage(aLine))
                        lineProcessor.copyImageReferencedToImagesFolder(aLine, fileNameToWriteContents, pandoced.getParent());

                    if(lineProcessor.isLineAPossibleSourceDeclaration(aLine)){
                        // remember it and cache the line so we can output it if the next line is not an external code import
                        state="EXPECTED_SOURCE_INCLUDE";
                        lineCache="";
                        lineCache = String.format("%s\n", aLine);
                    }

                    switch (state){
                        case "LINE_PROCESSING":
                            leanpubpreview.write(aLine);
                            leanpubpreview.newLine();
                            break;
                        case "SKIP_LINE":
                            state="LINE_PROCESSING";
                            break;
                    }
                }

                // write a blank line before adding the new file
                leanpubpreview.newLine();

            }
            leanpubpreview.close();


            // output the command to generate the book to console
            String inputFile =  config.getPreviewFileName() + ".md";
            String outputFile =  config.getPreviewFileName() + ".pdf";

            ProcessBuilder pb = new ProcessBuilder(config.getPandocPath(),
                    inputFile, "-f", "markdown", "-s", "-o", outputFile, "--toc" );
            pb.directory(pandocfolder);
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);

            // output the command
            for(String command : pb.command()){
                System.out.print(command + " ");
            }
            System.out.println("");

            Process p = pb.start();

            // open output folder
            Runtime.getRuntime().exec("explorer.exe "+ pandocfolder.getAbsolutePath());
        }
    }

    private class PandocifierConfigFileReader {
        public PandocifierConfig fromPropertyFile(File file) throws IOException {

            Properties fromFile = new Properties();

            fromFile.load(new FileInputStream(new File(System.getProperty("user.dir"),"pandocifier.properties")));

            PandocifierConfig config = new PandocifierConfig();
            config.setInputFilePath(fromFile.getProperty("InputFilePath"));
            config.setPreviewFileName(fromFile.getProperty("PreviewFileName"));
            config.setTempFolderName(fromFile.getProperty("TempFolderName"));
            config.setPandocPath(fromFile.getProperty("PandocPath"));
            return config;
        }
    }
}
