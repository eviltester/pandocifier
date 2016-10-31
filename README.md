# pandocifier

Local leanpub format book.txt preview using pandoc

~~~~~~~~
// for given a path to a Book.txt
// read the Book.txt file
// create a list of File names from Book.txt
// create a folder called pandoced (if necessary)
// create a new file in pandoced called preview.md
// write all the contents of the files from Book.txt into this file
// output the command to generate the book to console
~~~~~~~~

## Usage Instructions

`java -jar book-preview-pandoc-1.0.jar pandoc.properties`

Create a property file e.g. `pandoc.properties` with the format:

~~~~~~~~
TempFolderName=pandoced
PandocPath=c\:\\pathTo\\Pandoc\\pandoc.exe
InputFilePath=D\:\\pathTo\\Book.txt
PreviewFileName=preview
~~~~~~~~

Change the path names to match the install location for pandoc `PandocPath` and the path to your `Book.txt` in leanpub format

You can change TempFolderName to whatever you want - it is the name of the folder that images are copied in to and the `preview.md` and `preview.pdf` files are generated into.

You could change the `preview` name as well if you want to.

Application has minimal error handling.

## Build instructions

`mvn clean package`

## Associated blog posts:

- http://blog.javafortesters.com/2016/04/an-example-of-creating-tool-using-test.html