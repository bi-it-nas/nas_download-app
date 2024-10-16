package espas.ch;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class FileDownloadHandler {
    private JFrame frame;
    private List<String> preselectedDirectories; // Declare list of preselected directories
    private WatchService watchService; // Declare WatchService as an instance variable
    private Path monitoredPath; // Current monitored path
    private MainFrame mainFrame;

    public FileDownloadHandler() {
        // Initialize the list of preselected directories
        preselectedDirectories = new ArrayList<>();
        preselectedDirectories.add("H:/temp2");
        preselectedDirectories.add("H:/temp3");
        preselectedDirectories.add("H:/temp4");

        // Set default monitored path
        monitoredPath = Paths.get("H:/temp");

        // Create the main frame
        frame = new JFrame("File Download Handler");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLayout(new FlowLayout());
        frame.setAlwaysOnTop(true);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null); // Center the frame on the screen

        // Initialize and show main frame
        mainFrame = new MainFrame(preselectedDirectories);
        mainFrame.setVisible(true);

        startMonitoring(); // Start monitoring on creation
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
                                mainFrame.handleNewDownload(filePath.toFile()); // Pass the file to the main frame
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

    public static void main(String[] args) {
        // Run the GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(FileDownloadHandler::new);
    }
}
