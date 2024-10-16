package espas.ch;

import javax.swing.*;
import java.awt.FlowLayout;
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
    private Thread watchThread;

    public FileDownloadHandler() {
        preselectedDirectories = loadDirectories(); // Load directories from a file
        monitoredPath = loadMonitoredPath(); // Load monitored path from a file

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
                directories.add(line.toLowerCase());
            }
        } catch (IOException e) {
            // Handle the exception or use default directories
            directories.add("c:/temp");
            directories.add("c:/temp3");
            directories.add("c:/temp2");
        }
        return directories;
    }

    public void saveDirectories(List<String> directories) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("directories.txt"))) {
            for (String dir : directories) {
                writer.write(dir.toLowerCase());
                writer.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error saving directories: " + e.getMessage());
        }
    }

    public Path getMonitoredPath() {
        return monitoredPath;
    }

    public void setMonitoredPath(Path monitoredPath) {
        this.monitoredPath = monitoredPath;
        saveMonitoredPath(monitoredPath.toString());
        startMonitoring();
    }

    public void saveMonitoredPath(String path) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("monitored_path.txt"))) {
            writer.write(path);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error saving monitored path: " + e.getMessage());
        }
    }

    private Path loadMonitoredPath() {
        try (BufferedReader reader = new BufferedReader(new FileReader("monitored_path.txt"))) {
            String path = reader.readLine();
            if (path != null) {
                return Paths.get(path);
            }
        } catch (IOException e) {
            // Handle the exception or use a default path
        }
        return Paths.get("c:/temp");
    }

    private void startMonitoring() {
        if (watchThread != null && watchThread.isAlive()) {
            watchThread.interrupt();
        }

        try {
            if (watchService != null) {
                watchService.close();
            }
            watchService = FileSystems.getDefault().newWatchService();
            monitoredPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

            watchThread = new Thread(() -> {
                while (true) {
                    try {
                        WatchKey key = watchService.take();
                        for (WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent.Kind<?> kind = event.kind();
                            if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                                Path newPath = monitoredPath.resolve((Path) event.context());
                                System.out.println("New file detected: " + newPath);
                                mainFrame.handleNewDownload(newPath.toFile());
                            }
                        }
                        key.reset();
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            });
            watchThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FileDownloadHandler::new);
    }
}