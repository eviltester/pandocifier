import org.junit.Test;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;


public class LeanPubPandocPreviewTest {

    // for given a hardcoded path
    // read the Book.txt file
    // create a list of File names from Book.txt
    // create a folder called pandoced (if necessary)
    // create a new file in pandoced called leanpubpreview.md
    // write all the contents of the files from Book.txt into this file
    // output the command to generate the book to console

    // note: this won't handle files with images at the moment

    @Test
    public void createPreviewMVP() throws IOException {

        // for given a hardcoded path
        String book_txt="";


        // read the Book.txt file
        File book_txt_file = new File(book_txt);

        if(!book_txt_file.exists()){
            throw new FileNotFoundException("ERROR: Could not find file:" + book_txt_file.getAbsolutePath());
        }



        // create a list of File names from Book.txt
        // experiment with the Java 1.8 readAllLines method
        List<String> lines = Files.readAllLines(Paths.get(book_txt));

        File book_txt_parent_folder = book_txt_file.getParentFile();

        List<String> filesToCollate = new ArrayList<String>();

        for(String aLine : lines){

            String filePath;

            if(aLine!=null){            // ignore any nulls
                filePath = aLine;

                filePath = filePath.trim();
                if(filePath.length()>0){   // ignore any empty lines

                    if(!filePath.startsWith("#")){    // ignore any comments

                        File fileToAdd = new File(book_txt_parent_folder, filePath);
                        System.out.println(fileToAdd.getAbsolutePath());

                        if(fileToAdd.exists()){
                            filesToCollate.add(fileToAdd.getAbsolutePath());
                        }else{
                            System.out.println("ERROR: File Does Not Exist. Did not add to file collation list");
                        }
                    }
                }
            }
        }

        // create a folder called pandoced (if necessary)
        File pandoced = new File(book_txt_parent_folder, "pandoced");
        pandoced.mkdirs();

        // create a new file in pandoced called leanpubpreview.md
        pandoced = new File(pandoced, "leanpubpreview.md");
        if(pandoced.exists()){
            pandoced.delete();
        }
        pandoced.createNewFile();

        // write all the contents of the files from Book.txt into this file

        BufferedWriter leanpubpreview = Files.newBufferedWriter(pandoced.toPath(),
                                                                StandardOpenOption.WRITE,
                                                                StandardOpenOption.APPEND);

        for(String fileNameToWriteContents : filesToCollate){

            BufferedReader reader = Files.newBufferedReader(
                                                Paths.get(fileNameToWriteContents));

            int BUFFER_SIZE = 1024 * 4;
            char[] buffer = new char[BUFFER_SIZE];
            int n = 0;
            while (-1 != (n = reader.read(buffer))) {
                leanpubpreview.write(buffer, 0, n);
            }

            reader.close();

            // write a blank line before adding the new file
            leanpubpreview.newLine();

        }
        leanpubpreview.close();


        // find all the image files and copy them in
        List<String> imagePaths = new ArrayList<String>();

        for(String fileNameToWriteContents : filesToCollate){

            lines = Files.readAllLines(Paths.get(fileNameToWriteContents));

            for(String aLine : lines){
                String theImagePath ="";
                try{
                    String isFileLine = aLine.trim();
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

                        Path rootOfTextFile = Paths.get(fileNameToWriteContents).getParent();
                        File theImageFile = Paths.get(rootOfTextFile.toAbsolutePath().toString(), theImagePath).toFile();
                        if(theImageFile.exists()){
                            // copy the file
                            System.out.println("Copy Image File:");
                            System.out.println(theImageFile.getAbsolutePath());

                            Path copyImageTo = Paths.get(pandoced.getParent(), theImagePath);
                            System.out.println(copyImageTo.toAbsolutePath());
                            Files.copy(theImageFile.toPath(), copyImageTo, StandardCopyOption.REPLACE_EXISTING);

                        }else{
                            System.out.println(String.format("ERROR: Could not file image file %s", theImageFile.getAbsolutePath()));
                        }
                    }



                }catch(Exception e){
                    e.printStackTrace();
                    System.out.println("ERROR Issues processing image line:");
                    System.out.println(aLine);
                    System.out.println("File:");
                    System.out.println(fileNameToWriteContents);
                }

            }


        }
        leanpubpreview.close();


        // output the command to generate the book to console
        System.out.println("pandoc leanpubpreview.md -f markdown -s -o leanpubpreview.pdf --toc");
    }
}
