package espas.ch;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class FileDownloadHandler {
    private JFrame frame;
    private List<String> preselectedDirectories;
    private WatchService watchService;
    private Path monitoredPath;
    private MainFrame mainFrame;

    public FileDownloadHandler() {
        preselectedDirectories = loadDirectories(); // Load directories from a file
        monitoredPath = Paths.get("H:/temp");

        frame = new JFrame("File Download Handler");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLayout(new FlowLayout());
        frame.setAlwaysOnTop(true);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null); // Center the frame on the screen

        mainFrame = new MainFrame(preselectedDirectories, this);
        mainFrame.setVisible(true);

        startMonitoring();
    }

    public List<String> loadDirectories() {
        List<String> directories = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("directories.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                directories.add(line);
            }
        } catch (IOException e) {
            // Handle the exception or use default directories
            directories.add("H:/temp2");
            directories.add("H:/temp3");
            directories.add("H:/temp4");
        }
        return directories;
    }

    public void saveDirectories(List<String> directories) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("directories.txt"))) {
            for (String dir : directories) {
                writer.write(dir);
                writer.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error saving directories: " + e.getMessage());
        }
    }

    private void startMonitoring() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            monitoredPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

            new Thread(() -> {
                while (true) {
                    try {
                        WatchKey key = watchService.take();
                        for (WatchEvent<?> event : key.pollEvents()) {
                            if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                                Path filePath = monitoredPath.resolve((Path) event.context());
                                mainFrame.handleNewDownload(filePath.toFile());
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
        SwingUtilities.invokeLater(FileDownloadHandler::new);
    }
}