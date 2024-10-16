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
    private JTextField directoryInputField;
    private JLabel selectedDirectoryLabel;
    private List<String> preselectedDirectories; // Declare list of preselected directories
    private WatchService watchService; // Declare WatchService as an instance variable
    private Path monitoredPath; // Current monitored path
    private JToggleButton monitorToggle; // Toggle button for monitoring

    public FileDownloadHandler() {
        // Initialize the list of preselected directories
        preselectedDirectories = new ArrayList<>();
        preselectedDirectories.add("H:/_1-semester/319");
        preselectedDirectories.add("H:/_1-semester/162");
        preselectedDirectories.add("H:/temp");

        // Set default monitored path
        monitoredPath = Paths.get("H:/temp");

        // Create the main frame
        frame = new JFrame("File Download Handler");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 150);
        frame.setLayout(new FlowLayout());

        frame.setAlwaysOnTop(true);
        frame.setResizable(false);

        // Center the frame on the screen
        frame.setLocationRelativeTo(null);

        // Create components
        monitorToggle = new JToggleButton("Start Monitoring");
        JButton addButton = new JButton("+");
        JButton removeButton = new JButton("-");
        JButton changePathButton = new JButton("Change Path");

        directoryInputField = new JTextField(20);
        selectedDirectoryLabel = new JLabel("No directory selected.");

        // Add action listeners to buttons
        monitorToggle.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (monitorToggle.isSelected()) {
                    monitorToggle.setText("Stop Monitoring");
                    startMonitoring(); // Start monitoring when toggled on
                } else {
                    monitorToggle.setText("Start Monitoring");
                    stopMonitoring(); // Stop monitoring when toggled off
                }
            }
        });

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String newPath = directoryInputField.getText().trim();
                if (!newPath.isEmpty() && !preselectedDirectories.contains(newPath)) {
                    preselectedDirectories.add(newPath);
                    directoryInputField.setText(""); // Clear input field
                    JOptionPane.showMessageDialog(frame, "Directory added: " + newPath);
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid or duplicate directory path.");
                }
            }
        });

        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String pathToRemove = directoryInputField.getText().trim();
                if (preselectedDirectories.remove(pathToRemove)) {
                    directoryInputField.setText(""); // Clear input field
                    JOptionPane.showMessageDialog(frame, "Directory removed: " + pathToRemove);
                } else {
                    JOptionPane.showMessageDialog(frame, "Directory not found.");
                }
            }
        });

        changePathButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String newPath = JOptionPane.showInputDialog(frame, "Enter new path to monitor:", monitoredPath.toString());
                if (newPath != null && !newPath.isEmpty()) {
                    monitoredPath = Paths.get(newPath);
                    JOptionPane.showMessageDialog(frame, "Monitoring path changed to: " + monitoredPath);
                } else {
                    JOptionPane.showMessageDialog(frame, "Please enter a valid path.");
                }
            }
        });

        // Add components to the frame
        frame.add(monitorToggle);
        frame.add(selectedDirectoryLabel);
        frame.add(new JLabel("Enter directory path:"));
        frame.add(directoryInputField);
        frame.add(addButton);
        frame.add(removeButton);
        frame.add(changePathButton);

        frame.setVisible(true);
    }

    private void startMonitoring() {
        try {
            watchService = FileSystems.getDefault().newWatchService(); // Initialize the WatchService
            monitoredPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE); // Listen for new files

            new Thread(() -> {
                while (monitorToggle.isSelected()) {
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

    private void stopMonitoring() {
        try {
            watchService.close(); // Stop the WatchService
            watchService = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleNewDownload(File downloadedFile) {
        // Display a selection dialog for the user to choose a directory
        String selectedDirectory = (String) JOptionPane.showInputDialog(
                frame,
                "Select a directory:",
                "Directory Selection",
                JOptionPane.QUESTION_MESSAGE,
                null,
                preselectedDirectories.toArray(), // Use the class-level variable
                preselectedDirectories.get(0) // Default selection
        );

        // Check if a directory was selected
        if (selectedDirectory != null) {
            selectedDirectoryLabel.setText("Selected Directory: " + selectedDirectory);
            // Prompt for a new file name (without changing the extension)
            String newName = JOptionPane.showInputDialog(frame, "Enter new file name (without extension):", downloadedFile.getName());
            if (newName != null && !newName.isEmpty()) {
                // Preserve the original file extension
                String fileExtension = getFileExtension(downloadedFile);
                String newFileName = newName + "." + fileExtension;
                // Move the file (or implement renaming logic)
                File newFile = new File(selectedDirectory, newFileName);
                boolean success = downloadedFile.renameTo(newFile);
                if (success) {
                    JOptionPane.showMessageDialog(frame, "File renamed to: " + newFileName);
                } else {
                    JOptionPane.showMessageDialog(frame, "Failed to rename file.");
                }
            }
            // Debug info
            System.out.println("Downloaded file: " + downloadedFile.getAbsolutePath());
            System.out.println("Will move to: " + selectedDirectory);
        }
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < name.length() - 1) {
            return name.substring(dotIndex + 1);
        }
        return ""; // Return empty string if no extension
    }

    public static void main(String[] args) {
        // Run the GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(FileDownloadHandler::new);
    }
}
