package dbbackuptool;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author estherwang
 */
public class DBBackupTool extends Thread {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/test";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "n10270275";
    private static final String BACKUP_FILE = "backup.txt";

    private final JButton backupButton;
    private final JTextArea logTextArea;
    private final JTextField sqlFileTextField;

    public DBBackupTool(JButton backupButton, JTextField sqlFileTextField, JTextArea logTextArea) {
        this.backupButton = backupButton;
        this.logTextArea = logTextArea;
        this.sqlFileTextField = sqlFileTextField;
    }

    @Override
    public void run() {
        String sqlFilePath = sqlFileTextField.getText().trim();
        
        if (sqlFilePath.isEmpty()) {
            logTextArea.append("Please provide a SQL file path.\n");
            backupButton.setEnabled(true);
            return;
        }

        File sqlFile = new File(sqlFilePath);
        
        if (!sqlFile.exists() || !sqlFile.isFile()) {
            logTextArea.append("Invalid SQL file path: " + sqlFilePath + "\n");
            backupButton.setEnabled(true);
            return;
        }

        backupButton.setEnabled(false);
        logTextArea.append("Backup started.\n");
        
        if (backupDatabase(sqlFile)) {
            logTextArea.append("Backup completed.\n");
        } else {
            logTextArea.append("Backup failed.\n");
            System.err.println("Backup failed.");
        }
        
        backupButton.setEnabled(true);
    }

    public synchronized boolean backupDatabase(File sqlFile) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             Statement statement = connection.createStatement();
             BufferedWriter writer = new BufferedWriter(new FileWriter(BACKUP_FILE))) {

            StringBuilder queryBuilder = new StringBuilder();
            
            try (BufferedReader reader = new BufferedReader(new FileReader(sqlFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    queryBuilder.append(line).append("\n");
                }
            } catch (IOException e) {
                System.err.println("Error reading SQL file: " + e.getMessage());
                return false;
            }
            
            String backupFilePath = new File(BACKUP_FILE).getAbsolutePath();
            String queries = queryBuilder.toString();
            
            String grantQuery = "GRANT SELECT, INSERT, UPDATE, DELETE ON testBackup.* TO CURRENT_USER;";
            statement.execute(grantQuery);
            

            if (!queries.isEmpty()) {
                writer.write(queries);
                writer.newLine();
                writer.write("Database backup created successfully.");
                writer.flush();
                
                String[] sqlQueries = queries.split(";");
                for (int i = 0; i < sqlQueries.length - 1; i++) {
                    statement.execute(sqlQueries[i]);
                }

                logTextArea.append("Backup file location: " + backupFilePath + "\n");

                statement.close();
                connection.close();
                writer.close();
                
                return true;
            } else {
                writer.write("Error during backup: SQL execution failed.");
                writer.newLine();
                System.err.println("Error during writer backup");
                
                statement.close();
                connection.close();
                writer.close();
                
                return false;
            }
        } catch (SQLException | IOException e) {
            System.err.println("Error during backup: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            createAndShowGUI();
        });
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Database Backup Tool");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());

        JLabel sqlFileLabel = new JLabel("SQL File:");
        JTextField sqlFileTextField = new JTextField();
        sqlFileTextField.setColumns(20);
        JButton backupButton = new JButton("Backup");
        JTextArea logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logTextArea);

        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(sqlFileLabel);
        inputPanel.add(sqlFileTextField);
        inputPanel.add(backupButton);

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        frame.getContentPane().add(panel);
        frame.pack();
        frame.setSize(600, 300);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        ButtonPressed bp = new ButtonPressed(backupButton, sqlFileTextField, logTextArea);
        backupButton.addActionListener(bp);
    }
}

class ButtonPressed implements ActionListener{
    JButton backupButton;
    JTextArea logTextArea;
    JTextField sqlFileTextField;
    
    ButtonPressed(JButton backupButton, JTextField sqlFileTextField, JTextArea logTextArea){
        this.backupButton = backupButton;
        this.logTextArea = logTextArea;
        this.sqlFileTextField = sqlFileTextField;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        DBBackupTool backupTool = new DBBackupTool(backupButton, sqlFileTextField, logTextArea);
                backupTool.start();
    }    
}
