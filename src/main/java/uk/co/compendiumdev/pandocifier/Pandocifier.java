package uk.co.compendiumdev.pandocifier;


import uk.co.compendiumdev.pandocifier.config.PandocifierConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class Pandocifier {

    // TODO: need to handle X> I> W> A> etc. and make >
    // TODO: when processing above make sure no headings present in the quotes e.g. `> ## This breaks pandoc latex creation`

    private final PandocifierConfig config;

    public Pandocifier(PandocifierConfig config) {
        this.config = config;
    }

    public void createPDF() throws IOException {
        BookTxtFile book_details = new BookTxtFile(config.getInputFilePath());
        book_details.readTheListOfContentFiles();

        // create a folder called pandoced (if necessary)
        // is the tempFolderName an actual path that exists?
        File pandocfolder = new File(config.getTempFolderName());
        if(pandocfolder.exists() && pandocfolder.isDirectory()){
            // consider it an actual temp folder
        }else{
            // assume it is a name relative to the book.txt file
            pandocfolder = new File(book_details.getParentFolder(), config.getTempFolderName());
            pandocfolder.mkdirs();
        }



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
        lineProcessor.setMaxImagePath(config.getInputFilePath());

        for(String fileNameToWriteContents : book_details.contentFiles()){


            // I need to process some of the information as we work through so we will have to process the lines one by one
            List<String> lines  = Files.readAllLines(Paths.get(fileNameToWriteContents));

            String state="LINE_PROCESSING"; // LINE_PROCESSING, EXPECTED_SOURCE_INCLUDE, SKIP_LINE
            String lineCache="";

            for(String aLine : lines){

                if(state.contentEquals("EXPECTED_SOURCE_INCLUDE")){
                    // if this line is not a "<<" source include then write the cache and clear it and go back to line processing
                    if(!lineProcessor.isLineAnExternalSourceInclude(aLine) && !lineProcessor.isLineACodeBlock(aLine)){
                        leanpubpreview.write(lineCache);
                        state="LINE_PROCESSING";
                    }
                }

                if(lineProcessor.isLineAnExternalSourceInclude(aLine)){
                    lineProcessor.mergeTheExternalCodeFileAsACodeBlock(aLine, fileNameToWriteContents, leanpubpreview);
                    state="SKIP_LINE";
                }

                if(lineProcessor.isLineACodeBlock(aLine)){
                    // if line is a code block then we want the block, but not the header in the cache
                    lineCache="";
                    state="LINE_PROCESSING";
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
