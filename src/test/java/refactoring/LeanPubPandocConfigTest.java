package refactoring;

import uk.co.compendiumdev.pandocifier.BookTxtFile;
import uk.co.compendiumdev.pandocifier.LeanPubMarkdownLineProcessor;
import org.junit.Before;
import org.junit.Test;
import uk.co.compendiumdev.pandocifier.config.PandocifierConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;


/*
    Refactoring to a more 'strategic' implementation

    - inject config
    - externalise config
    -


 */
public class LeanPubPandocConfigTest {

    // for given a hardcoded path
    // read the Book.txt file
    // create a list of File names from Book.txt
    // create a folder called pandoced (if necessary)
    // create a new file in pandoced called leanpubpreview.md
    // write all the contents of the files from Book.txt into this file
    // output the command to generate the book to console


    PandocifierConfig config;

    @Before
    public void injectableConfiguration(){

        //String book_txt = "D:\\Users\\Alan\\Documents\\xp-dev\\buggyGames\\testingBuggyGamesManuscript\\trunk\\manuscript\\Book.txt";
        config = new PandocifierConfig();
        config.setInputFilePath("D:\\Users\\Alan\\Documents\\xp-dev\\eLearning\\tracksApiCaseStudy\\text\\Book.txt");
        config.setPreviewFileName("leanpubpreview");
        config.setTempFolderName("pandoced");
    }

    @Test
    public void createPreviewMVP() throws IOException {

        BookTxtFile book_details = new BookTxtFile(config.getInputFilePath());
        book_details.readTheListOfContentFiles();

        // create a folder called pandoced (if necessary)
        File pandoced = new File(book_details.getParentFolder(), config.getTempFolderName());
        pandoced.mkdirs();

        // create a new file in pandoced called leanpubpreview.md
        pandoced = new File(pandoced, config.getPreviewFileName() + ".md");
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
        System.out.println(
                String.format("pandoc %1$s.md -f markdown -s -o %1$s.pdf --toc", config.getPreviewFileName()));
    }


}
