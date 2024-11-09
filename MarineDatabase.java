import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class MarineDatabase {
    
    private static final String URL = "jdbc:mysql://localhost:3307/MarineDB";
    private static final String USER = "root";  
    private static final String PASSWORD = "";  

    private static Connection connection;

    public static void main(String[] args) {
        connection = getConnection();

        if (connection != null) {
            createTables(connection);
            SwingUtilities.invokeLater(() -> new MarineDatabase().createAndShowGUI());
        }
    }

    private static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            notifyUser("Error connecting to database: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private static void createTables(Connection connection) {
        try (Statement statement = connection.createStatement()) {
            String createSpeciesTable = "CREATE TABLE IF NOT EXISTS Species (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "name VARCHAR(50), " +
                    "status VARCHAR(50), " +
                    "habitat VARCHAR(50)" +
                    ");";

            String createEnvironmentTable = "CREATE TABLE IF NOT EXISTS Environment (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "region VARCHAR(50), " +
                    "temperature DOUBLE, " +
                    "salinity DOUBLE" +
                    ");";

            String createConditionTable = "CREATE TABLE IF NOT EXISTS Conditions (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "condition_name VARCHAR(50), " +
                    "severity VARCHAR(50), " +
                    "affected_species INT, " +
                    "FOREIGN KEY (affected_species) REFERENCES Species(id)" +
                    ");";

            statement.execute(createSpeciesTable);
            statement.execute(createEnvironmentTable);
            statement.execute(createConditionTable);
            notifyUser("Tables created successfully.", "Database", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            notifyUser("Error creating tables: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Marine Environment Database System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setLayout(new GridLayout(0, 1));

        JButton addSpeciesButton = new JButton("Add Species Data");
        JButton addEnvironmentButton = new JButton("Add Environment Data");
        JButton addConditionButton = new JButton("Add Condition Data");
        JButton displaySpeciesButton = new JButton("Display All Species");
        JButton displayEnvironmentButton = new JButton("Display Environment Data");
        JButton displayConditionButton = new JButton("Display Condition Data");
        JButton exitButton = new JButton("Exit");

        // Add action listeners
        addSpeciesButton.addActionListener(e -> addSpeciesData());
        addEnvironmentButton.addActionListener(e -> addEnvironmentData());
        addConditionButton.addActionListener(e -> addConditionData());
        displaySpeciesButton.addActionListener(e -> displaySpeciesData());
        displayEnvironmentButton.addActionListener(e -> displayEnvironmentData());
        displayConditionButton.addActionListener(e -> displayConditionData());
        exitButton.addActionListener(e -> {
            closeConnection(connection);
            System.exit(0);
        });

        frame.add(addSpeciesButton);
        frame.add(addEnvironmentButton);
        frame.add(addConditionButton);
        frame.add(displaySpeciesButton);
        frame.add(displayEnvironmentButton);
        frame.add(displayConditionButton);
        frame.add(exitButton);

        frame.setVisible(true);
    }

    private void addSpeciesData() {
        String name = JOptionPane.showInputDialog("Enter species name:");
        String status = JOptionPane.showInputDialog("Enter status (e.g., Endangered, Threatened):");
        String habitat = JOptionPane.showInputDialog("Enter habitat:");

        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO Species (name, status, habitat) VALUES (?, ?, ?)")) {
            ps.setString(1, name);
            ps.setString(2, status);
            ps.setString(3, habitat);
            ps.executeUpdate();
            notifyUser("Species data added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            notifyUser("Error adding species data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addEnvironmentData() {
        String region = JOptionPane.showInputDialog("Enter region name:");
        double temperature = Double.parseDouble(JOptionPane.showInputDialog("Enter temperature:"));
        double salinity = Double.parseDouble(JOptionPane.showInputDialog("Enter salinity:"));

        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO Environment (region, temperature, salinity) VALUES (?, ?, ?)")) {
            ps.setString(1, region);
            ps.setDouble(2, temperature);
            ps.setDouble(3, salinity);
            ps.executeUpdate();
            notifyUser("Environment data added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            notifyUser("Error adding environment data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addConditionData() {
        String conditionName = JOptionPane.showInputDialog("Enter condition name:");
        String severity = JOptionPane.showInputDialog("Enter severity (e.g., Low, Medium, High):");

        int speciesId = 0;
        while (true) {
            try {
                speciesId = Integer.parseInt(JOptionPane.showInputDialog("Enter affected species ID:"));
                break; // Exit loop if input is valid
            } catch (NumberFormatException e) {
                notifyUser("Please enter a valid integer for species ID.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO Conditions (condition_name, severity, affected_species) VALUES (?, ?, ?)")) {
            ps.setString(1, conditionName);
            ps.setString(2, severity);
            ps.setInt(3, speciesId);
            ps.executeUpdate();
            notifyUser("Condition data added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            notifyUser("Error adding condition data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displaySpeciesData() {
        StringBuilder data = new StringBuilder();
        try (Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery("SELECT * FROM Species");
            while (rs.next()) {
                data.append("ID: ").append(rs.getInt("id"))
                    .append(", Name: ").append(rs.getString("name"))
                    .append(", Status: ").append(rs.getString("status"))
                    .append(", Habitat: ").append(rs.getString("habitat"))
                    .append("\n");
            }
            notifyUser(data.length() > 0 ? data.toString() : "No species data available.", "Species Data", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            notifyUser("Error displaying species data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayEnvironmentData() {
        StringBuilder data = new StringBuilder();
        try (Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery("SELECT * FROM Environment");
            while (rs.next()) {
                data.append("ID: ").append(rs.getInt("id"))
                    .append(", Region: ").append(rs.getString("region"))
                    .append(", Temperature: ").append(rs.getDouble("temperature"))
                    .append(", Salinity: ").append(rs.getDouble("salinity"))
                    .append("\n");
            }
            notifyUser(data.length() > 0 ? data.toString() : "No environment data available.", "Environment Data", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            notifyUser("Error displaying environment data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayConditionData() {
        StringBuilder data = new StringBuilder();
        try (Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery("SELECT * FROM Conditions");
            while (rs.next()) {
                data.append("ID: ").append(rs.getInt("id"))
                    .append(", Condition: ").append(rs.getString("condition_name"))
                    .append(", Severity: ").append(rs.getString("severity"))
                    .append(", Affected Species ID: ").append(rs.getInt("affected_species"))
                    .append("\n");
            }
            notifyUser(data.length() > 0 ? data.toString() : "No condition data available.", "Condition Data", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            notifyUser("Error displaying condition data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void closeConnection(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            notifyUser("Error closing connection: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Notification module
    private static void notifyUser(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(null, message, title, messageType);
    }
}