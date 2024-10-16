package espas.ch;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class MainFrame extends JFrame {
    private List<String> preselectedDirectories;
    private JTextField directoryInputField;
    private FileDownloadHandler fileDownloadHandler;
    private boolean isDialogOpen = false;

    public MainFrame(List<String> preselectedDirectories, FileDownloadHandler fileDownloadHandler) {
        this.preselectedDirectories = preselectedDirectories.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        this.fileDownloadHandler = fileDownloadHandler;

        setTitle("File Organizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());
        setSize(400, 200);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        setAutoRequestFocus(true);
        setResizable(false);

        // Create components
        JLabel directoryLabel = new JLabel("Monitored Directory:");
        directoryInputField = new JTextField(20);
        JButton addButton = createButton("+");
        JButton removeButton = createButton("-");

        // Add action listeners
        addButton.addActionListener(e -> showAddDirectoryInput());
        removeButton.addActionListener(e -> showRemoveDirectoryInput());

        // Add components to frame
        add(directoryLabel);
        add(directoryInputField);
        add(addButton);
        add(removeButton);

        // Focus on the input field
        directoryInputField.requestFocusInWindow();

        pack();
        setVisible(true);
    }

    private JButton createButton(String label) {
        JButton button = new JButton(label);
        button.setPreferredSize(new Dimension(80, 40));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        button.setBackground(new Color(240, 240, 240));
        button.setForeground(Color.BLACK);
        button.setFont(new Font("San Francisco", Font.PLAIN, 16));
        button.setOpaque(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        return button;
    }

    private void showAddDirectoryInput() {
        int result = JOptionPane.showConfirmDialog(this, directoryInputField, "Enter new directory path:", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String newPath = directoryInputField.getText().trim().toLowerCase();
            if (isValidPath(newPath) && !preselectedDirectories.contains(newPath)) {
                preselectedDirectories.add(newPath);
                directoryInputField.setText("");
                fileDownloadHandler.saveDirectories(preselectedDirectories); // Save changes
                JOptionPane.showMessageDialog(this, "Directory added: " + newPath);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid or duplicate path, please try again.");
                SwingUtilities.invokeLater(this::showAddDirectoryInput); // Reopen the dialog
            }
        }
    }

    private void showRemoveDirectoryInput() {
        String[] options = preselectedDirectories.toArray(new String[0]);
        String selected = (String) JOptionPane.showInputDialog(this, "Select a directory to remove:", "Remove Directory", JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (selected != null) {
            preselectedDirectories.remove(selected);
            fileDownloadHandler.saveDirectories(preselectedDirectories); // Save changes
            JOptionPane.showMessageDialog(this, "Directory removed: " + selected);
        }
    }

    private boolean isValidPath(String path) {
        return Files.exists(Paths.get(path));
    }

    public void handleNewDownload(File downloadedFile) {
        if (isDialogOpen) {
            return;
        }
        isDialogOpen = true;

        SwingUtilities.invokeLater(() -> {
            String selectedDirectory = (String) JOptionPane.showInputDialog(
                    this,
                    "Select a directory to move the file:",
                    "Directory Selection",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    preselectedDirectories.toArray(),
                    preselectedDirectories.get(0)
            );

            if (selectedDirectory != null) {
                String newName = JOptionPane.showInputDialog(this, "Enter new file name (without extension):", downloadedFile.getName());
                if (newName != null && !newName.isEmpty()) {
                    String fileExtension = getFileExtension(downloadedFile);
                    String newFileName = newName + "." + fileExtension;
                    File newFile = new File(selectedDirectory, newFileName);
                    boolean success = FileHandler.renameFile(downloadedFile, newFile);
                    if (success) {
                        JOptionPane.showMessageDialog(this, "File moved to: " + newFile.getAbsolutePath());
                    }
                }
            }
            isDialogOpen = false;
        });
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndex = name.lastIndexOf('.');
        return (lastIndex == -1) ? "" : name.substring(lastIndex + 1);
    }
}