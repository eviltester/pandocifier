package com.compendiumdev.pandocifier;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alan on 26/05/2016.
 */
public class BookTxtFile {
    private final String filepath;
    private List<String> bookContentFiles;

    public BookTxtFile(String book_txt_filepath) {
        this.filepath = book_txt_filepath;
        bookContentFiles = new ArrayList<String>();
    }

    public String getBookTxtFileName() {
        return filepath;
    }

    public void addFileToBookContents(String absolutePath) {
        bookContentFiles.add(absolutePath);
    }

    public File getParentFolder(){
        return new File(filepath).getParentFile();
    }

    public List<String> contentFiles() {
        return bookContentFiles;
    }

    public void readTheListOfContentFiles() throws IOException {
        BookFileReader bookFileReader = new BookFileReader();
        bookFileReader.readFileInto(this);
    }
}
