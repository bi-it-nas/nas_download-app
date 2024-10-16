package espas.ch; // Make sure this matches your folder structure

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MainFrame {
    private JFrame frame;
    private JTextField directoryInputField;
    private JLabel selectedDirectoryLabel;
    private List<String> preselectedDirectories;

    public MainFrame() {
        // Initialize preselected directories
        preselectedDirectories = new ArrayList<>();
        preselectedDirectories.add("H:/temp2");
        preselectedDirectories.add("H:/temp3");
        preselectedDirectories.add("H:/temp4");

        // Create the main frame
        frame = new JFrame("File Download Handler");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLayout(new FlowLayout());
        frame.setLocationRelativeTo(null); // Center the window

        // Create components
        JButton plusButton = new JButton("+");
        JButton minusButton = new JButton("-");
        JButton swapButton = new JButton("↔");

        directoryInputField = new JTextField(20);
        selectedDirectoryLabel = new JLabel("No directory selected.");
        selectedDirectoryLabel.setForeground(Color.RED); // For highlighting

        // Set button actions
        plusButton.addActionListener(e -> showDirectoryInputDialog("Add Directory:"));
        minusButton.addActionListener(e -> showDirectoryDropdown());
        swapButton.addActionListener(e -> showDirectoryInputDialog("Change Monitoring Path:"));

        // Add components to the frame
        frame.add(plusButton);
        frame.add(minusButton);
        frame.add(swapButton);
        frame.add(selectedDirectoryLabel);
        frame.add(directoryInputField);

        // Focus on the first button to allow keyboard navigation
        plusButton.requestFocusInWindow();

        // Set button colors and styles
        styleButtons(plusButton, minusButton, swapButton);

        frame.setVisible(true);
    }

    private void showDirectoryInputDialog(String title) {
        String currentPath = (title.equals("Change Monitoring Path:")) ? "Current Path: " + selectedDirectoryLabel.getText() : "";
        String newPath = JOptionPane.showInputDialog(frame, title, currentPath);

        if (newPath != null && !newPath.trim().isEmpty()) {
            if (Files.isDirectory(Paths.get(newPath))) {
                if (title.equals("Add Directory:")) {
                    preselectedDirectories.add(newPath);
                    JOptionPane.showMessageDialog(frame, "Directory added: " + newPath);
                } else if (title.equals("Change Monitoring Path:")) {
                    selectedDirectoryLabel.setText("Monitoring Path: " + newPath);
                    // Implement changing monitored directory logic if needed
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid directory path. Please enter a valid path.");
            }
        }
    }

    private void showDirectoryDropdown() {
        String selectedDirectory = (String) JOptionPane.showInputDialog(
                frame,
                "Select a directory to remove:",
                "Directory Removal",
                JOptionPane.QUESTION_MESSAGE,
                null,
                preselectedDirectories.toArray(),
                preselectedDirectories.get(0)
        );

        if (selectedDirectory != null) {
            int response = JOptionPane.showConfirmDialog(frame, "Are you sure you want to remove " + selectedDirectory + "?", "Confirm Removal", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                preselectedDirectories.remove(selectedDirectory);
                JOptionPane.showMessageDialog(frame, "Directory removed: " + selectedDirectory);
            }
        }
    }

    private void styleButtons(JButton... buttons) {
        for (JButton button : buttons) {
            button.setFont(new Font("San Francisco", Font.PLAIN, 16));
            button.setBackground(Color.LIGHT_GRAY);
            button.setFocusPainted(false);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}
