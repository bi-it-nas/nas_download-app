package espas.ch;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class MainFrame extends JFrame {
    private List<String> preselectedDirectories;
    private JButton directoryButton;
    private FileDownloadHandler fileDownloadHandler;
    private boolean isDialogOpen = false;
    private JTextField directoryInputField = new JTextField(20);
    private JLabel errorLabel = new JLabel();
    private JPanel mainPanel;
    private CardLayout cardLayout;

    public MainFrame(List<String> preselectedDirectories, FileDownloadHandler fileDownloadHandler) {
        this.preselectedDirectories = preselectedDirectories.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        this.fileDownloadHandler = fileDownloadHandler;

        setTitle("File Organizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(400, 200);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        setAutoRequestFocus(true);
        setResizable(false);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Create components
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JLabel directoryLabel = new JLabel("Monitoring:");
        directoryButton = new JButton(fileDownloadHandler.getMonitoredPath().toString());
        directoryButton.setPreferredSize(new Dimension(200, 30));
        directoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchToTextField();
            }
        });

        JButton addButton = createButton("+");
        JButton removeButton = createButton("-");

        // Add action listeners
        addButton.addActionListener(e -> showAddDirectoryInput());
        removeButton.addActionListener(e -> showRemoveDirectoryInput());

        // Add components to button panel
        buttonPanel.add(directoryLabel);
        buttonPanel.add(directoryButton);
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);

        // Create text field panel
        JPanel textFieldPanel = new JPanel(new FlowLayout());
        JTextField textField = new JTextField(directoryButton.getText(), 20);
        JButton confirmButton = createButton("Enter");

        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateMonitoredPath(textField.getText().trim());
                switchToButton();
            }
        });

        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateMonitoredPath(textField.getText().trim());
                switchToButton();
            }
        });

        textFieldPanel.add(textField);
        textFieldPanel.add(confirmButton);

        // Add panels to main panel
        mainPanel.add(buttonPanel, "buttonPanel");
        mainPanel.add(textFieldPanel, "textFieldPanel");

        add(mainPanel, BorderLayout.CENTER);
        add(errorLabel, BorderLayout.SOUTH);

        cardLayout.show(mainPanel, "buttonPanel");

        pack();
        setVisible(true);
    }

    private void switchToTextField() {
        cardLayout.show(mainPanel, "textFieldPanel");
    }

    private void switchToButton() {
        directoryButton.setText(fileDownloadHandler.getMonitoredPath().toString());
        cardLayout.show(mainPanel, "buttonPanel");
    }

    private void updateMonitoredPath(String newPath) {
        if (isValidPath(newPath)) {
            fileDownloadHandler.setMonitoredPath(Paths.get(newPath));
            fileDownloadHandler.saveMonitoredPath(newPath); // Save the new path
            errorLabel.setText("");
        } else {
            errorLabel.setText("Invalid path");
        }
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