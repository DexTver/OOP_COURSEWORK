import java.sql.*;

class Car {
    private String vin;
    private final String driverLicenseNumber;
    private String licensePlate;
    private final String color;
    private final String model;
    private final String inspectionExpiryDate;
    private final String insuranceExpiryDate;

    public Car(String vin, String driverLicenseNumber, String licensePlate, String color, String model, String inspectionExpiryDate, String insuranceExpiryDate, Connection conn) throws Exception {
        if (!isValidVIN(vin)) {
            throw new Exception("Некорректный VIN номер.");
        }
        if (!isUniqueVIN(vin, conn)) {
            throw new Exception("VIN номер уже существует.");
        }
        if (driveNotExists(driverLicenseNumber, conn)) {
            throw new Exception("Водитель с таким номером ВУ не найден.");
        }
        if (isInvalidLicensePlate(licensePlate)) {
            throw new Exception("Некорректный формат номерного знака.");
        }
        if (isLicensePlateExist(licensePlate, conn)) {
            throw new Exception("Номерной знак уже существует.");
        }
        if (isInvalidDate(inspectionExpiryDate)) {
            throw new Exception("Некорректная дата окончания техосмотра.");
        }
        if (isInvalidDate(insuranceExpiryDate)) {
            throw new Exception("Некорректная дата окончания страховки.");
        }

        this.vin = vin;
        this.driverLicenseNumber = driverLicenseNumber;
        this.licensePlate = licensePlate;
        this.color = color;
        this.model = model;
        this.inspectionExpiryDate = inspectionExpiryDate;
        this.insuranceExpiryDate = insuranceExpiryDate;
    }

    public Car(String driverLicenseNumber, String licensePlate, String color, String model, String inspectionExpiryDate, String insuranceExpiryDate, Connection conn) throws Exception {
        if (driveNotExists(driverLicenseNumber, conn)) {
            throw new Exception("Водитель с таким номером ВУ не найден.");
        }
        if (isInvalidLicensePlate(licensePlate)) {
            throw new Exception("Некорректный формат номерного знака.");
        }
        if (isLicensePlateExist(licensePlate, conn)) {
            throw new Exception("Номерной знак уже существует.");
        }
        if (isInvalidDate(inspectionExpiryDate)) {
            throw new Exception("Некорректная дата окончания техосмотра.");
        }
        if (isInvalidDate(insuranceExpiryDate)) {
            throw new Exception("Некорректная дата окончания страховки.");
        }

        this.driverLicenseNumber = driverLicenseNumber;
        this.licensePlate = licensePlate;
        this.color = color;
        this.model = model;
        this.inspectionExpiryDate = inspectionExpiryDate;
        this.insuranceExpiryDate = insuranceExpiryDate;
    }

    public Car(String driverLicenseNumber, String color, String model, String inspectionExpiryDate, String insuranceExpiryDate, Connection conn) throws Exception {
        if (driveNotExists(driverLicenseNumber, conn)) {
            throw new Exception("Водитель с таким номером ВУ не найден.");
        }
        if (isInvalidDate(inspectionExpiryDate)) {
            throw new Exception("Некорректная дата окончания техосмотра.");
        }
        if (isInvalidDate(insuranceExpiryDate)) {
            throw new Exception("Некорректная дата окончания страховки.");
        }

        this.driverLicenseNumber = driverLicenseNumber;
        this.color = color;
        this.model = model;
        this.inspectionExpiryDate = inspectionExpiryDate;
        this.insuranceExpiryDate = insuranceExpiryDate;
    }

    private boolean driveNotExists(String licenseNumber, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM Drivers WHERE driverLicenseNumber = ?");
        pstmt.setString(1, licenseNumber);
        ResultSet rs = pstmt.executeQuery();
        rs.next();
        return rs.getInt(1) <= 0;
    }

    private boolean isInvalidLicensePlate(String plate) {
        return !plate.matches("^[АВЕКМНОРСТУХ]\\d{3}[АВЕКМНОРСТУХ]{2}\\d{2,3}$");
    }

    private boolean isLicensePlateExist(String plate, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM Cars WHERE licensePlate = ?");
        pstmt.setString(1, plate);
        ResultSet rs = pstmt.executeQuery();
        rs.next();
        return rs.getInt(1) != 0;
    }

    private boolean isValidVIN(String vin) {
        return vin.matches("[A-HJ-NPR-Z0-9]{17}");
    }

    private boolean isUniqueVIN(String vin, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM Cars WHERE vin = ?");
        pstmt.setString(1, vin);
        ResultSet rs = pstmt.executeQuery();
        rs.next();
        return rs.getInt(1) == 0;
    }

    private boolean isInvalidDate(String date) {
        return !date.matches("^\\d{2}\\.\\d{2}\\.\\d{4}$");
    }

    public String getVIN() {
        return vin;
    }

    public String getDriverLicenseNumber() {
        return driverLicenseNumber;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public String getColor() {
        return color;
    }

    public String getModel() {
        return model;
    }

    public String getInspectionExpiryDate() {
        return inspectionExpiryDate;
    }

    public String getInsuranceExpiryDate() {
        return insuranceExpiryDate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }
}
