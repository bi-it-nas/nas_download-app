package espas.ch; // Ensure this matches your folder structure

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class MainFrame extends JFrame {
    private List<String> preselectedDirectories;
    private JTextField directoryInputField;

    public MainFrame(List<String> preselectedDirectories) {
        this.preselectedDirectories = preselectedDirectories;

        // Set up the main frame
        setTitle("File Organizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());
        setSize(400, 200);
        setLocationRelativeTo(null); // Center the frame on launch
        setAlwaysOnTop(true); // Keep the window on top
        setResizable(false);

        // Create buttons
        JButton addButton = createButton("+");
        JButton removeButton = createButton("-");
        JButton swapButton = createButton("⏷"); // Placeholder for the swap icon

        // Add button actions
        addButton.addActionListener(e -> showAddDirectoryInput());
        removeButton.addActionListener(e -> showRemoveDirectoryInput());
        swapButton.addActionListener(e -> showSwapDirectoryInput());

        // Add buttons to the frame
        add(addButton);
        add(removeButton);
        add(swapButton);

        // Center buttons
        for (Component component : getContentPane().getComponents()) {
            component.setPreferredSize(new Dimension(80, 40)); // Set preferred button size
        }

        pack(); // Resize the frame to fit buttons
        setVisible(true); // Show the frame
    }

    private JButton createButton(String label) {
        JButton button = new JButton(label);
        button.setPreferredSize(new Dimension(80, 40));
        button.setFocusPainted(false); // Remove focus border
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2)); // Set a simple border
        button.setBackground(new Color(240, 240, 240)); // Light background color
        button.setForeground(Color.BLACK); // Button text color
        button.setFont(new Font("San Francisco", Font.PLAIN, 16)); // Set font
        button.setOpaque(true); // Make sure the background color shows
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10) // Add padding to simulate rounding
        ));
        return button;
    }

    private void showAddDirectoryInput() {
        if (directoryInputField == null) {
            directoryInputField = new JTextField(20);
        }
        int result = JOptionPane.showConfirmDialog(this, directoryInputField, "Enter new directory path:", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String newPath = directoryInputField.getText().trim();
            if (isValidPath(newPath)) {
                preselectedDirectories.add(newPath);
                directoryInputField.setText(""); // Clear input field
                JOptionPane.showMessageDialog(this, "Directory added: " + newPath);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid path, please try again.");
                showAddDirectoryInput(); // Show input again
            }
        }
    }

    private void showRemoveDirectoryInput() {
        String[] options = preselectedDirectories.toArray(new String[0]);
        String selected = (String) JOptionPane.showInputDialog(this, "Select a directory to remove:", "Remove Directory", JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (selected != null) {
            preselectedDirectories.remove(selected);
            JOptionPane.showMessageDialog(this, "Directory removed: " + selected);
        }
    }

    private void showSwapDirectoryInput() {
        String newPath = (String) JOptionPane.showInputDialog(this, "Enter new path to monitor:", "Swap Directory", JOptionPane.PLAIN_MESSAGE, null, null, "");
        if (newPath != null && isValidPath(newPath)) {
            // Implement swapping logic here
            JOptionPane.showMessageDialog(this, "Swap path set to: " + newPath);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid path.");
        }
    }

    private boolean isValidPath(String path) {
        return Files.exists(Paths.get(path)); // Check if the path exists
    }

    public void handleNewDownload(File downloadedFile) {
        // Display a selection dialog for the user to choose a directory
        String selectedDirectory = (String) JOptionPane.showInputDialog(
                this,
                "Select a directory to move the file:",
                "Directory Selection",
                JOptionPane.QUESTION_MESSAGE,
                null,
                preselectedDirectories.toArray(), // Use the class-level variable
                preselectedDirectories.get(0) // Default selection
        );

        // Check if a directory was selected
        if (selectedDirectory != null) {
            // Prompt for a new file name (without changing the extension)
            String newName = JOptionPane.showInputDialog(this, "Enter new file name (without extension):", downloadedFile.getName());
            if (newName != null && !newName.isEmpty()) {
                // Preserve the original file extension
                String fileExtension = getFileExtension(downloadedFile);
                String newFileName = newName + "." + fileExtension;
                // Move the file (or implement renaming logic)
                File newFile = new File(selectedDirectory, newFileName);
                boolean success = FileHandler.renameFile(downloadedFile, newFile);
                if (success) {
                    JOptionPane.showMessageDialog(this, "File moved to: " + newFile.getAbsolutePath());
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to move file.");
                }
            }
        }
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndex = name.lastIndexOf('.');
        return (lastIndex == -1) ? "" : name.substring(lastIndex + 1); // Get file extension
    }
}
