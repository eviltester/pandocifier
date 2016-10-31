package uk.co.compendiumdev.pandocifier.config;

public class PandocifierConfig {
    private String inputFilePath;
    private String previewFileName;
    private String tempFolderName;
    private String pandocPath;

    public void setInputFilePath(String inputFile) {
        this.inputFilePath = inputFile;
    }

    public String getInputFilePath() {
        return inputFilePath;
    }

    public void setPreviewFileName(String previewFileName) {
        this.previewFileName = previewFileName;
    }

    public void setTempFolderName(String tempFolderName) {
        this.tempFolderName = tempFolderName;
    }

    public String getTempFolderName() {
        return tempFolderName;
    }

    public String getPreviewFileName() {
        return previewFileName;
    }

    public void setPandocPath(String pandocPath) {
        this.pandocPath = pandocPath;
    }

    public String getPandocPath() {
        return pandocPath;
    }
}
