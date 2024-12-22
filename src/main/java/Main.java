import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class Main {
    private static JTable table;
    private static String currentMode = "Violations";
    private static Connection conn; // Постоянное подключение к БД

    public static void main(String[] args) {
        JFrame frame = new JFrame("GAI System");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenuBar menuBar = new JMenuBar();

        // Меню Файл
        JMenu fileMenu = new JMenu("Файл");
        JMenuItem connectDB = new JMenuItem("Подключить БД");
        JMenuItem createPdfReport = new JMenuItem("Создать отчёт PDF");
        JMenuItem createHtmlReport = new JMenuItem("Создать отчёт HTML");
        fileMenu.add(connectDB);
        fileMenu.add(createPdfReport);
        fileMenu.add(createHtmlReport);
        menuBar.add(fileMenu);

        // Меню Вид
        JMenu viewMenu = new JMenu("Вид");
        JMenuItem viewDrivers = new JMenuItem("Водители");
        JMenuItem viewCars = new JMenuItem("Машины");
        JMenuItem viewViolations = new JMenuItem("Нарушения");
        viewMenu.add(viewDrivers);
        viewMenu.add(viewCars);
        viewMenu.add(viewViolations);
        menuBar.add(viewMenu);

        // Меню Добавить
        JMenu addMenu = new JMenu("Добавить");
        JMenuItem addDriver = new JMenuItem("Добавить водителя");
        JMenuItem addCar = new JMenuItem("Добавить машину");
        JMenuItem addViolation = new JMenuItem("Добавить нарушение");
        addMenu.add(addDriver);
        addMenu.add(addCar);
        addMenu.add(addViolation);
        menuBar.add(addMenu);

        // Меню Изменить
        JMenu editMenu = new JMenu("Изменить");
        JMenuItem editRecord = new JMenuItem("Редактировать запись");
        JMenuItem deleteRecord = new JMenuItem("Удалить записи");
        editMenu.add(editRecord);
        editMenu.add(deleteRecord);
        menuBar.add(editMenu);

        frame.setJMenuBar(menuBar);

        // Определение таблиц
        String[] violationColumns = {"ФИО водителя", "Номер машины", "Тип нарушения", "Дата нарушения"};
        String[] carColumns = {"Номер машины", "ФИО владельца", "Модель", "Цвет", "VIN", "Дата окончания страховки", "Дата окончания техосмотра"};
        String[] driverColumns = {"ФИО", "Номер ВУ", "Пол", "Дата рождения", "Дата окончания действия ВУ"};

        table = new JTable(new DefaultTableModel(new Object[][]{}, violationColumns));
        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Панель поиска
        JPanel searchPanel = new JPanel();
        JComboBox<String> searchBox = new JComboBox<>(violationColumns);
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Поиск");
        searchPanel.add(searchBox);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        frame.add(searchPanel, BorderLayout.SOUTH);

        // Обработчики переключения режимов
        viewCars.addActionListener(e -> {
            currentMode = "Cars";
            table.setModel(new DefaultTableModel(new Object[][]{}, carColumns));
            searchBox.setModel(new DefaultComboBoxModel<>(carColumns));
            loadDatabase();
        });
        viewViolations.addActionListener(e -> {
            currentMode = "Violations";
            table.setModel(new DefaultTableModel(new Object[][]{}, violationColumns));
            searchBox.setModel(new DefaultComboBoxModel<>(violationColumns));
            loadDatabase();
        });
        viewDrivers.addActionListener(e -> {
            currentMode = "Drivers";
            table.setModel(new DefaultTableModel(new Object[][]{}, driverColumns));
            searchBox.setModel(new DefaultComboBoxModel<>(driverColumns));
            loadDatabase();
        });

        // Подключение к БД
        connectDB.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser(".");
            fileChooser.setDialogTitle("Выбрать файл базы данных");
            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                connectToDatabase(file);
                loadDatabase();
            }
        });

        frame.setVisible(true);
    }

    private static void connectToDatabase(File file) {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static void loadDatabase() {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs;
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.setRowCount(0);

            if (currentMode.equals("Drivers")) {
                rs = stmt.executeQuery("SELECT d.name, d.driverLicenseNumber, d.gender, d.birthDate, d.licenseExpiryDate FROM Drivers d");
                while (rs.next()) {
                    model.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)});
                }
            } else if (currentMode.equals("Cars")) {
                rs = stmt.executeQuery("SELECT c.licensePlate, d.name, c.model, c.color, c.vin, c.insuranceExpiryDate, c.inspectionExpiryDate FROM Cars c JOIN Drivers d ON c.driverLicenseNumber = d.driverLicenseNumber");
                while (rs.next()) {
                    model.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7)});
                }
            } else if (currentMode.equals("Violations")) {
                rs = stmt.executeQuery("SELECT d.name, c.licensePlate, v.violationType, v.date FROM Violations v JOIN Cars c ON v.vin = c.vin JOIN Drivers d ON c.driverLicenseNumber = d.driverLicenseNumber");
                while (rs.next()) {
                    model.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)});
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
