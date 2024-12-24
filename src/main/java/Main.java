import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;

public class Main {
    private static JTable table;
    private static String currentMode = "Violations";
    private static Connection conn = null;
    private static JMenu dbPathMenu;

    public static void main(String[] args) {
        JFrame frame = new JFrame("GAI System");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("Файл");
        JMenuItem createDB = new JMenuItem("Создать БД");
        JMenuItem connectDB = new JMenuItem("Подключить БД");
        fileMenu.add(createDB);
        fileMenu.add(connectDB);
        menuBar.add(fileMenu);

        JMenu addMenu = new JMenu("Добавить");
        JMenuItem addDriver = new JMenuItem("Водителя");
        JMenuItem addCar = new JMenuItem("Машину");
        JMenuItem addViolation = new JMenuItem("Нарушение");
        addMenu.add(addDriver);
        addMenu.add(addCar);
        addMenu.add(addViolation);
        menuBar.add(addMenu);

        JMenu editMenu = new JMenu("Изменить");
        JMenuItem editRecord = new JMenuItem("Редактировать");
        JMenuItem deleteRecord = new JMenuItem("Удалить");
        editMenu.add(editRecord);
        editMenu.add(deleteRecord);
        menuBar.add(editMenu);

        JMenu viewMenu = new JMenu("Вид");
        JMenuItem viewDrivers = new JMenuItem("Водители");
        JMenuItem viewCars = new JMenuItem("Машины");
        JMenuItem viewViolations = new JMenuItem("Нарушения");
        viewMenu.add(viewDrivers);
        viewMenu.add(viewCars);
        viewMenu.add(viewViolations);
        menuBar.add(viewMenu);

        dbPathMenu = new JMenu("Не подключена БД");
        dbPathMenu.setEnabled(false);
        menuBar.add(dbPathMenu);

        frame.setJMenuBar(menuBar);

        String[] violationColumns = {"ФИО водителя", "Номер машины", "Тип нарушения", "Дата нарушения"};
        String[] carColumns = {"Номер машины", "ФИО владельца", "Модель", "Цвет", "VIN", "Дата окончания страховки", "Дата окончания техосмотра"};
        String[] driverColumns = {"ФИО", "Номер ВУ", "Пол", "Дата рождения", "Дата окончания действия ВУ"};

        table = new JTable(new DefaultTableModel(new Object[][]{}, violationColumns));
        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel searchPanel = new JPanel();
        JComboBox<String> searchBox = new JComboBox<>(violationColumns);
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Поиск");
        searchPanel.add(searchBox);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        frame.add(searchPanel, BorderLayout.SOUTH);

        viewCars.addActionListener(e ->
                switchView("Cars", carColumns, searchBox));

        viewViolations.addActionListener(e ->
                switchView("Violations", violationColumns, searchBox));

        viewDrivers.addActionListener(e ->
                switchView("Drivers", driverColumns, searchBox));

        createDB.addActionListener(e -> {
            createDatabase(frame);
            loadDatabase();
        });

        connectDB.addActionListener(e -> {
            connectToDatabase(frame);
            loadDatabase();
        });

        addDriver.addActionListener(e -> {
            if (conn != null) {
                addDriverDialog(frame);
                loadDatabase();
            }
        });

        addCar.addActionListener(e -> {
            if (conn != null) {
                addCarDialog(frame);
                loadDatabase();
            }
        });

        addViolation.addActionListener(e -> {
            if (conn != null) {
                addViolationDialog(frame);
                loadDatabase();
            }
        });

        editRecord.addActionListener(e -> {
            if (conn != null) {
                editSelectedRecords(frame);
                loadDatabase();
            }
        });

        deleteRecord.addActionListener(e -> {
            if (conn != null) {
                deleteSelectedRecords(frame);
                loadDatabase();
            }
        });

        frame.setVisible(true);
    }

    public static void createDatabase(JFrame frame) {
        JFileChooser fileChooser = new JFileChooser(".");
        fileChooser.setDialogTitle("Создать базу данных");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("SQLite Database (*.db)", "db"));

        int userSelection = fileChooser.showSaveDialog(frame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().endsWith(".db")) {
                    file = new File(file.getAbsolutePath() + ".db");
                }
                if (file.exists()) {
                    int openConfirm = JOptionPane.showConfirmDialog(frame, "Файл с таким именем уже существует. Открыть его?", "Предупреждение", JOptionPane.YES_NO_OPTION);
                    if (openConfirm == JOptionPane.YES_OPTION) {
                        conn = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
                        dbPathMenu.setText(file.getAbsolutePath());
                        JOptionPane.showMessageDialog(frame, "Подключена база данных.", "Успех", JOptionPane.INFORMATION_MESSAGE);
                    }
                    return;
                }

                conn = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());

                Statement stmt = conn.createStatement();

                stmt.execute("CREATE TABLE Drivers ("
                        + "driverLicenseNumber TEXT PRIMARY KEY,"
                        + "name TEXT,"
                        + "gender TEXT,"
                        + "birthDate TEXT,"
                        + "licenseExpiryDate TEXT)");

                stmt.execute("CREATE TABLE Cars ("
                        + "licensePlate TEXT PRIMARY KEY,"
                        + "driverLicenseNumber TEXT,"
                        + "vin TEXT,"
                        + "color TEXT,"
                        + "model TEXT,"
                        + "inspectionExpiryDate TEXT,"
                        + "insuranceExpiryDate TEXT,"
                        + "FOREIGN KEY(driverLicenseNumber) REFERENCES Drivers(driverLicenseNumber) ON DELETE CASCADE)");

                stmt.execute("CREATE TABLE Violations ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "licensePlate TEXT,"
                        + "date TEXT,"
                        + "violationType TEXT,"
                        + "FOREIGN KEY(licensePlate) REFERENCES Cars(licensePlate) ON DELETE CASCADE)");

                dbPathMenu.setText(file.getAbsolutePath());

                JOptionPane.showMessageDialog(frame, "База данных успешно создана и подключена.", "Успех", JOptionPane.INFORMATION_MESSAGE);

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Ошибка при создании базы данных.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void connectToDatabase(JFrame frame) {
        JFileChooser fileChooser = new JFileChooser(".");
        fileChooser.setDialogTitle("Выбрать файл базы данных");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("SQLite Database (*.db)", "db"));

        if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File dbFile = fileChooser.getSelectedFile();
            if (!dbFile.getName().endsWith(".db")) {
                JOptionPane.showMessageDialog(frame, "Выбранный файл не является базой данных.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
                dbPathMenu.setText(dbFile.getAbsolutePath());
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Ошибка при подключении к базе данных.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void loadDatabase() {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs;
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.setRowCount(0);

            switch (currentMode) {
                case "Drivers":
                    rs = stmt.executeQuery("SELECT d.name, d.driverLicenseNumber, d.gender, d.birthDate, d.licenseExpiryDate FROM Drivers d");
                    while (rs.next()) {
                        model.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)});
                    }
                    break;
                case "Cars":
                    rs = stmt.executeQuery("SELECT c.licensePlate, d.name, c.model, c.color, c.vin, c.insuranceExpiryDate, c.inspectionExpiryDate FROM Cars c JOIN Drivers d ON c.driverLicenseNumber = d.driverLicenseNumber");
                    while (rs.next()) {
                        model.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7)});
                    }
                    break;
                case "Violations":
                    rs = stmt.executeQuery("SELECT d.name, c.licensePlate, v.violationType, v.date FROM Violations v JOIN Cars c ON v.licensePlate = c.licensePlate JOIN Drivers d ON c.driverLicenseNumber = d.driverLicenseNumber");
                    while (rs.next()) {
                        model.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)});
                    }
                    break;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static void addDriverDialog(JFrame frame) {
        JTextField nameField = new JTextField();
        JTextField genderField = new JTextField();
        JTextField birthDateField = new JTextField();
        JTextField licenseNumberField = new JTextField();
        JTextField expiryDateField = new JTextField();

        Object[] message = {
                "ФИО:", nameField,
                "Пол:", genderField,
                "Дата рождения:", birthDateField,
                "Номер ВУ:", licenseNumberField,
                "Срок действия ВУ:", expiryDateField
        };

        while (true) {
            int option = JOptionPane.showConfirmDialog(frame, message, "Добавить водителя", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try {
                    Driver driver = new Driver(
                            nameField.getText(), genderField.getText(), birthDateField.getText(), licenseNumberField.getText(), expiryDateField.getText(), conn);
                    PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Drivers VALUES (?, ?, ?, ?, ?)");
                    pstmt.setString(1, driver.getDriverLicenseNumber());
                    pstmt.setString(2, driver.getName());
                    pstmt.setString(3, driver.getGender());
                    pstmt.setString(4, driver.getBirthDate());
                    pstmt.setString(5, driver.getLicenseExpiryDate());
                    pstmt.executeUpdate();
                    break;
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage(), "Ошибка", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                break;
            }
        }
    }

    public static void addCarDialog(JFrame frame) {
        JTextField vinField = new JTextField();
        JTextField licenseNumberField = new JTextField();
        JTextField licensePlateField = new JTextField();
        JTextField colorField = new JTextField();
        JTextField modelField = new JTextField();
        JTextField inspectionField = new JTextField();
        JTextField insuranceField = new JTextField();

        Object[] message = {
                "Номерной знак:", licensePlateField,
                "Номер ВУ владельца:", licenseNumberField,
                "Модель:", modelField,
                "Цвет:", colorField,
                "VIN:", vinField,
                "Дата окончания страховки:", insuranceField,
                "Дата окончания техосмотра:", inspectionField
        };

        while (true) {
            int option = JOptionPane.showConfirmDialog(frame, message, "Добавить машину", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try {
                    Car car = new Car(
                            vinField.getText(),
                            licenseNumberField.getText(),
                            licensePlateField.getText(),
                            colorField.getText(),
                            modelField.getText(),
                            inspectionField.getText(),
                            insuranceField.getText(),
                            conn
                    );
                    PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Cars VALUES (?, ?, ?, ?, ?, ?, ?)");
                    pstmt.setString(1, car.getLicensePlate());
                    pstmt.setString(2, car.getDriverLicenseNumber());
                    pstmt.setString(3, car.getVIN());
                    pstmt.setString(4, car.getColor());
                    pstmt.setString(5, car.getModel());
                    pstmt.setString(6, car.getInspectionExpiryDate());
                    pstmt.setString(7, car.getInsuranceExpiryDate());
                    pstmt.executeUpdate();
                    break;
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage(), "Ошибка", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                break;
            }
        }
    }

    public static void addViolationDialog(JFrame frame) {
        JTextField licensePlateField = new JTextField();
        JTextField dateField = new JTextField();
        JTextField typeField = new JTextField();

        Object[] message = {
                "Номер машины:", licensePlateField,
                "Тип нарушения:", typeField,
                "Дата нарушения:", dateField
        };

        while (true) {
            int option = JOptionPane.showConfirmDialog(frame, message, "Добавить нарушение", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try {
                    Violation violation = new Violation(
                            licensePlateField.getText(),
                            dateField.getText(),
                            typeField.getText(),
                            conn
                    );
                    PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Violations (licensePlate, date, violationType) VALUES (?, ?, ?)");
                    pstmt.setString(1, violation.getLicensePlate());
                    pstmt.setString(2, violation.getDate());
                    pstmt.setString(3, violation.getType());
                    pstmt.executeUpdate();
                    break;
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage(), "Ошибка", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                break;
            }
        }
    }

    public static void editSelectedRecords(JFrame frame) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1 || table.getSelectedRowCount() > 1) {
            JOptionPane.showMessageDialog(frame, "Выберите одну запись для редактирования.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        switch (currentMode) {
            case "Drivers":
                editDriverDialog(frame, selectedRow);
                break;
            case "Cars":
                editCarDialog(frame, selectedRow);
                break;
            case "Violations":
                editViolationDialog(frame, selectedRow);
                break;
        }
    }

    private static void editDriverDialog(JFrame frame, int selectedRow) {
        try {
            String licenseNumber = table.getValueAt(selectedRow, 1).toString();

            JTextField nameField = new JTextField(table.getValueAt(selectedRow, 0).toString());
            JTextField genderField = new JTextField(table.getValueAt(selectedRow, 2).toString());
            JTextField birthDateField = new JTextField(table.getValueAt(selectedRow, 3).toString());
            JTextField expiryDateField = new JTextField(table.getValueAt(selectedRow, 4).toString());

            Object[] message = {
                    "Номер ВУ: " + licenseNumber,
                    "ФИО:", nameField,
                    "Пол:", genderField,
                    "Дата рождения:", birthDateField,
                    "Срок действия ВУ:", expiryDateField
            };

            int option = JOptionPane.showConfirmDialog(frame, message, "Редактировать водителя", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                Driver driver = new Driver(
                        nameField.getText(),
                        genderField.getText(),
                        birthDateField.getText(),
                        expiryDateField.getText());

                PreparedStatement pstmt = conn.prepareStatement("UPDATE Drivers SET name = ?, gender = ?, birthDate = ?, licenseExpiryDate = ? WHERE driverLicenseNumber = ?");
                pstmt.setString(1, driver.getName());
                pstmt.setString(2, driver.getGender());
                pstmt.setString(3, driver.getBirthDate());
                pstmt.setString(4, driver.getLicenseExpiryDate());
                pstmt.setString(5, licenseNumber);
                pstmt.executeUpdate();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void editCarDialog(JFrame frame, int selectedRow) {
        try {
            String vin = table.getValueAt(selectedRow, 4).toString();
            String oldLicensePlate = table.getValueAt(selectedRow, 0).toString();

            PreparedStatement pstmt = conn.prepareStatement("SELECT driverLicenseNumber FROM Cars WHERE vin = ?");
            pstmt.setString(1, vin);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            String driverLicenseNumber = rs.getString(1);

            JTextField licensePlateField = new JTextField(table.getValueAt(selectedRow, 0).toString());
            JTextField licenseNumberField = new JTextField(driverLicenseNumber);
            JTextField modelField = new JTextField(table.getValueAt(selectedRow, 2).toString());
            JTextField colorField = new JTextField(table.getValueAt(selectedRow, 3).toString());
            JTextField insuranceField = new JTextField(table.getValueAt(selectedRow, 5).toString());
            JTextField inspectionField = new JTextField(table.getValueAt(selectedRow, 6).toString());

            Object[] message = {
                    "VIN: " + vin,
                    "Номер машины:", licensePlateField,
                    "Номер ВУ владельца:", licenseNumberField,
                    "Модель:", modelField,
                    "Цвет:", colorField,
                    "Дата окончания страховки:", insuranceField,
                    "Дата окончания техосмотра:", inspectionField
            };

            int option = JOptionPane.showConfirmDialog(frame, message, "Редактировать машину", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                Car car;
                if (oldLicensePlate.equals(licensePlateField.getText())) {
                    car = new Car(
                            licenseNumberField.getText(),
                            colorField.getText(),
                            modelField.getText(),
                            insuranceField.getText(),
                            inspectionField.getText(),
                            conn);
                    car.setLicensePlate(oldLicensePlate);
                } else {
                    car = new Car(
                            licenseNumberField.getText(),
                            licensePlateField.getText(),
                            colorField.getText(),
                            modelField.getText(),
                            insuranceField.getText(),
                            inspectionField.getText(),
                            conn);
                }

                pstmt = conn.prepareStatement("UPDATE Cars SET licensePlate = ?, driverLicenseNumber = ?, model = ?, color = ?, insuranceExpiryDate = ?, inspectionExpiryDate = ? WHERE vin = ?");
                pstmt.setString(1, car.getLicensePlate());
                pstmt.setString(2, car.getDriverLicenseNumber());
                pstmt.setString(3, car.getModel());
                pstmt.setString(4, car.getColor());
                pstmt.setString(5, car.getInsuranceExpiryDate());
                pstmt.setString(6, car.getInspectionExpiryDate());
                pstmt.setString(7, vin);
                pstmt.executeUpdate();

                if (!oldLicensePlate.equals(car.getLicensePlate())) {
                    pstmt = conn.prepareStatement("UPDATE Violations SET licensePlate = ? WHERE licensePlate = ?");
                    pstmt.setString(1, car.getLicensePlate());
                    pstmt.setString(2, oldLicensePlate);
                    pstmt.executeUpdate();
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void editViolationDialog(JFrame frame, int selectedRow) {
        try {
            String oldLicensePlate = table.getValueAt(selectedRow, 1).toString();
            String oldDate = table.getValueAt(selectedRow, 3).toString();

            JTextField licensePlateField = new JTextField(table.getValueAt(selectedRow, 1).toString());
            JTextField dateField = new JTextField(table.getValueAt(selectedRow, 3).toString());
            JTextField typeField = new JTextField(table.getValueAt(selectedRow, 2).toString());

            Object[] message = {
                    "Номер машины:", licensePlateField,
                    "Дата нарушения:", dateField,
                    "Тип нарушения:", typeField
            };

            int option = JOptionPane.showConfirmDialog(frame, message, "Редактировать нарушение", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                Violation violation = new Violation(
                        licensePlateField.getText(),
                        dateField.getText(),
                        typeField.getText(),
                        conn);

                PreparedStatement pstmt = conn.prepareStatement("UPDATE Violations SET licensePlate = ?, date = ?, violationType = ? WHERE licensePlate = ? AND date = ?");
                pstmt.setString(1, violation.getLicensePlate());
                pstmt.setString(2, violation.getDate());
                pstmt.setString(3, violation.getType());
                pstmt.setString(4, oldLicensePlate);
                pstmt.setString(5, oldDate);
                pstmt.executeUpdate();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void deleteSelectedRecords(JFrame frame) {
        int[] selectedRows = table.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(frame, "Выберите записи для удаления.", "Предупреждение", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(frame, "Вы уверены, что хотите удалить выделенные записи?", "Подтверждение удаления", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            for (int row : selectedRows) {
                String value;
                PreparedStatement pstmt;
                switch (currentMode) {
                    case "Drivers":
                        value = table.getValueAt(row, 1).toString();

                        pstmt = conn.prepareStatement("DELETE FROM Violations WHERE licensePlate IN (SELECT licensePlate FROM Cars WHERE driverLicenseNumber = ?)");
                        pstmt.setString(1, value);
                        pstmt.executeUpdate();

                        pstmt = conn.prepareStatement("DELETE FROM Cars WHERE driverLicenseNumber = ?");
                        pstmt.setString(1, value);
                        pstmt.executeUpdate();

                        pstmt = conn.prepareStatement("DELETE FROM Drivers WHERE driverLicenseNumber = ?");
                        pstmt.setString(1, value);
                        pstmt.executeUpdate();

                        break;
                    case "Cars":
                        value = table.getValueAt(row, 0).toString();

                        pstmt = conn.prepareStatement("DELETE FROM Violations WHERE licensePlate = ?");
                        pstmt.setString(1, value);
                        pstmt.executeUpdate();

                        pstmt = conn.prepareStatement("DELETE FROM Cars WHERE licensePlate = ?");
                        pstmt.setString(1, value);
                        pstmt.executeUpdate();

                        break;
                    case "Violations":
                        String licensePlate = table.getValueAt(row, 1).toString();
                        String date = table.getValueAt(row, 3).toString();
                        System.out.println("licensePlate: " + licensePlate + ", date: " + date);

                        pstmt = conn.prepareStatement("DELETE FROM Violations WHERE licensePlate = ? AND date = ?");
                        pstmt.setString(1, licensePlate);
                        pstmt.setString(2, date);
                        pstmt.executeUpdate();
                        break;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Ошибка при удалении записей.", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void switchView(String mode, String[] columns, JComboBox<String> searchBox) {
        currentMode = mode;
        table.setModel(new DefaultTableModel(new Object[][]{}, columns));
        searchBox.setModel(new DefaultComboBoxModel<>(columns));
        if (conn != null) {
            loadDatabase();
        }
    }
}
