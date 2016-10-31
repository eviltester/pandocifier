package uk.co.compendiumdev.pandocifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * Created by Alan on 26/05/2016.
 */
public class BookFileReader {
    public void readFileInto(BookTxtFile book_details) throws IOException {
        // read the Book.txt file
        File book_txt_file = new File(book_details.getBookTxtFileName());

        if(!book_txt_file.exists()){
            throw new FileNotFoundException("ERROR: Could not find file:" + book_txt_file.getAbsolutePath());
        }


        // create a list of File names from Book.txt
        // experiment with the Java 1.8 readAllLines method
        List<String> lines = Files.readAllLines(book_txt_file.toPath());

        File book_txt_parent_folder = book_txt_file.getParentFile();

        //List<String> filesToCollate = new ArrayList<String>();

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
                            //filesToCollate.add(fileToAdd.getAbsolutePath());
                            book_details.addFileToBookContents(fileToAdd.getAbsolutePath());
                        }else{
                            System.out.println("ERROR: File Does Not Exist. Did not add to file collation list");
                        }
                    }
                }
            }
        }
    }
}
