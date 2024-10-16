package espas.ch; // Make sure this matches your folder structure

import javax.swing.*;
import java.awt.*;
import java.io.File; // Importing the File class
import java.io.IOException;
import java.nio.file.*;

public class FileDownloadHandler {
    private WatchService watchService; // Declare WatchService as an instance variable
    private Path monitoredPath; // Current monitored path

    public FileDownloadHandler() {
        // Default monitored path (change as needed)
        monitoredPath = Paths.get("H:/temp");
        startMonitoring();
    }

    private void startMonitoring() {
        try {
            watchService = FileSystems.getDefault().newWatchService(); // Initialize the WatchService
            monitoredPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE); // Listen for new files

            new Thread(() -> {
                while (true) {
                    try {
                        WatchKey key = watchService.take(); // Wait for a key
                        for (WatchEvent<?> event : key.pollEvents()) {
                            if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                                Path filePath = monitoredPath.resolve((Path) event.context());
                                handleNewDownload(filePath.toFile()); // Handle the new download
                            }
                        }
                        key.reset();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleNewDownload(File downloadedFile) {
        // Logic for handling new downloads
        System.out.println("New file detected: " + downloadedFile.getAbsolutePath());
        // You can further process the file as needed
    }

    public static void main(String[] args) {
        // Run the file handler to monitor files
        new FileDownloadHandler();
    }
}
