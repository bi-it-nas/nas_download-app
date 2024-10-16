package espas.ch; // Make sure this matches your folder structure

import java.io.File;

public class FileHandler {
    public static boolean renameFile(File originalFile, File newFile) {
        return originalFile.renameTo(newFile);
    }

    // Additional file operations can be added here
}
