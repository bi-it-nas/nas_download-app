package espas.ch;

import java.io.File;

public class FileHandler {
    public static boolean renameFile(File oldFile, File newFile) {
        return oldFile.renameTo(newFile);
    }
}