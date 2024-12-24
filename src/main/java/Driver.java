import java.sql.*;

class Driver {
    private String name;
    private String gender;
    private String birthDate;
    private String driverLicenseNumber;
    private String licenseExpiryDate;

    public Driver(String name, String gender, String birthDate, String driverLicenseNumber, String licenseExpiryDate, Connection conn) throws Exception {
        if (!isValidName(name)) {
            throw new Exception("Некорректное имя.");
        }
        if (!isValidGender(gender)) {
            throw new Exception("Пол должен быть 'мужской' или 'женский'.");
        }
        if (!isValidDate(birthDate)) {
            throw new Exception("Некорректная дата рождения (формат: dd.mm.yyyy).\nПример: 01.01.2000");
        }
        if (!isValidDate(licenseExpiryDate)) {
            throw new Exception("Некорректная дата окончания действия ВУ (формат: dd.mm.yyyy).\nПример: 01.01.2030");
        }
        if (!isValidLicenseNumber(driverLicenseNumber)) {
            throw new Exception("Номер ВУ должен содержать 10 цифр.");
        }
        if (!isUniqueLicenseNumber(driverLicenseNumber, conn)) {
            throw new Exception("Номер ВУ уже существует.");
        }

        this.name = name;
        this.gender = gender;
        this.birthDate = birthDate;
        this.driverLicenseNumber = driverLicenseNumber;
        this.licenseExpiryDate = licenseExpiryDate;
    }

    public Driver(String name, String gender, String birthDate, String licenseExpiryDate) throws Exception {
        if (!isValidName(name)) {
            throw new Exception("Некорректное имя.");
        }
        if (!isValidGender(gender)) {
            throw new Exception("Пол должен быть 'мужской' или 'женский'.");
        }
        if (!isValidDate(birthDate)) {
            throw new Exception("Некорректная дата рождения (формат: dd.mm.yyyy).\nПример: 01.01.2000");
        }
        if (!isValidDate(licenseExpiryDate)) {
            throw new Exception("Некорректная дата окончания действия ВУ (формат: dd.mm.yyyy).\nПример: 01.01.2030");
        }

        this.name = name;
        this.gender = gender;
        this.birthDate = birthDate;
        this.licenseExpiryDate = licenseExpiryDate;
    }

    private boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty();
    }

    private boolean isValidGender(String gender) {
        return gender.equalsIgnoreCase("мужской") || gender.equalsIgnoreCase("женский");
    }

    private boolean isValidDate(String date) {
        return date.matches("^\\d{2}\\.\\d{2}\\.\\d{4}$");
    }

    private boolean isValidLicenseNumber(String licenseNumber) {
        return licenseNumber.matches("\\d{10}");
    }

    private boolean isUniqueLicenseNumber(String licenseNumber, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM Drivers WHERE driverLicenseNumber = ?");
        pstmt.setString(1, licenseNumber);
        ResultSet rs = pstmt.executeQuery();
        rs.next();
        return rs.getInt(1) == 0;
    }

    public String getName() {
        return name;
    }

    public String getGender() {
        return gender;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getDriverLicenseNumber() {
        return driverLicenseNumber;
    }

    public String getLicenseExpiryDate() {
        return licenseExpiryDate;
    }
}
