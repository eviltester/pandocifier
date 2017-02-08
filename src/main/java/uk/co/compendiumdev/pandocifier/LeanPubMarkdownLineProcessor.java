package uk.co.compendiumdev.pandocifier;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Created by Alan on 27/05/2016.
 */
public class LeanPubMarkdownLineProcessor {
    private String maxImagePath;    // the highest folder we will look for image files

    public void setMaxImagePath(String maxImagePath) {
        this.maxImagePath = maxImagePath;
    }

    public void mergeTheExternalCodeFileAsACodeBlock(String aLine, String foundInThisFile, BufferedWriter outputFile) {

        String thePath ="";
        try{
            String isFileLine = aLine.trim();

            // IS IT CODE?
            // <<[optional label](path)
            if(isFileLine.startsWith("<<")){
                // find the start of the path
                int startOfPath = isFileLine.indexOf("(");
                int endOfPath = isFileLine.indexOf(")");
                thePath = isFileLine.substring(startOfPath+1,endOfPath);

                Path rootOfTextFile = Paths.get(foundInThisFile).getParent();
                File theCodeFile = Paths.get(rootOfTextFile.toAbsolutePath().toString(), thePath).toFile();
                if(theCodeFile.exists()){
                    // read contents

                    outputFile.write("~~~~~~~~");
                    outputFile.newLine();

                    List<String> codelines  = Files.readAllLines(theCodeFile.toPath());
                    for(String codeLine : codelines){
                        outputFile.write(codeLine);
                        outputFile.newLine();
                    }

                    outputFile.write("~~~~~~~~");
                    outputFile.newLine();


                }else{
                    System.out.println(String.format("ERROR: Could not file code file %s", theCodeFile.getAbsolutePath()));
                }
            }



        }catch(Exception e){
            e.printStackTrace();
            System.out.println("ERROR Issues processing code line:");
            System.out.println(aLine);
            System.out.println("File:");
            System.out.println(foundInThisFile);
        }

    }

    public boolean isLineAnExternalSourceInclude(String aLine) {
        String isFileLine = aLine.trim();
        if(isFileLine.startsWith("<<")){
            return true;
        }
        return false;
    }

    public boolean isLineAPossibleSourceDeclaration(String aLine) {
        String isFileLine = aLine.trim();
        if(isFileLine.startsWith("{")){
            return true;
        }
        return false;
    }

    public void copyImageReferencedToImagesFolder(String aLine, String foundInThisFile, String outputPathName) {
        String theImagePath ="";
        try{
            String isFileLine = aLine.trim();

            // IS IT AN IMAGE?
            // rudimentary check does not support all image formats
            // http://pandoc.org/README.html#images
            // for now ![optional](path)
            // Used: where the image is in the same folder as the filename
            // Not tried with images in sub folder or in /images/ folder at Book.txt parent level
            if(isFileLine.startsWith("![")){
                // find the start of the path
                int startOfPath = isFileLine.indexOf("(");
                int endOfPath = isFileLine.indexOf(")");
                theImagePath = isFileLine.substring(startOfPath+1,endOfPath);

                Path rootOfTextFile = Paths.get(foundInThisFile).getParent();

                // traverse up the directories until found
                Boolean abortFind = false;
                Boolean foundFile = false;

                File maxImagePathFile = new File(maxImagePath);

                while(!abortFind && !foundFile){

                    File book_txt = Paths.get(rootOfTextFile.toAbsolutePath().toString(), "Book.txt").toFile();

                    // if we have reached the max path or we have found book.txt
                    if(book_txt.getParent().equals(maxImagePathFile.getParent()) || book_txt.exists()){
                        abortFind=true;
                        // do not look any higher than the folder with Book.txt in it
                    }

                    File theImageFile = Paths.get(rootOfTextFile.toAbsolutePath().toString(), theImagePath).toFile();
                    if(theImageFile.exists()){

                        foundFile=true;

                        // copy the file
                        System.out.println("Copy Image File:");
                        System.out.println(theImageFile.getAbsolutePath());


                        Path copyImageTo = Paths.get(outputPathName, theImagePath);

                        // make any subfolder paths if necessary
                        copyImageTo.getParent().toFile().mkdirs();

                        System.out.println(copyImageTo.toAbsolutePath());
                        Files.copy(theImageFile.toPath(), copyImageTo, StandardCopyOption.REPLACE_EXISTING);

                    }else{
                        System.out.println(String.format("ERROR: Could not file image file %s", theImageFile.getAbsolutePath()));
                        rootOfTextFile = rootOfTextFile.getParent(); // try next directory up in case we are in sub dirs
                    }

                }
            }

        }catch(Exception e){
            e.printStackTrace();
            System.out.println("ERROR Issues processing image line:");
            System.out.println(aLine);
            System.out.println("File:");
            System.out.println(foundInThisFile);
        }
    }

    public boolean isLineAnImage(String aLine) {

        if(aLine==null)
            return false;

        String isFileLine = aLine.trim();

        // IS IT AN IMAGE?
        if(isFileLine.startsWith("![")){
            return true;
        }

        return false;
    }


}
