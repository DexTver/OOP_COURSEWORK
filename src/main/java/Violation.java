import java.sql.*;

class Violation {
    private String licensePlate;
    private String date;
    private String type;

    public Violation(String licensePlate, String date, String type, Connection conn) throws Exception {
        // Проверка существования номера машины
        if (!carExists(licensePlate, conn)) {
            throw new Exception("Автомобиль с таким номерным знаком не найден.");
        }
        // Проверка корректности даты
        if (!isValidDate(date)) {
            throw new Exception("Некорректная дата нарушения.");
        }

        this.licensePlate = licensePlate;
        this.date = date;
        this.type = type;
    }

    private boolean carExists(String licensePlate, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM Cars WHERE licensePlate = ?");
        pstmt.setString(1, licensePlate);
        ResultSet rs = pstmt.executeQuery();
        rs.next();
        return rs.getInt(1) > 0;
    }

    private boolean isValidDate(String date) {
        return date.matches("^\\d{2}\\.\\d{2}\\.\\d{4}$");
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public String getDate() {
        return date;
    }

    public String getType() {
        return type;
    }
}