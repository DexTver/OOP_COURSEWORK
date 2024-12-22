// Класс Driver расширяет Person и добавляет поля для водителя
class Driver extends Person {
    private String driverLicenseNumber;   // Номер водительского удостоверения
    private String licenseExpiryDate;     // Дата окончания действия удостоверения

    // Конструктор для инициализации водителя
    public Driver(String name, String gender, String birthDate, String driverLicenseNumber, String licenseExpiryDate) {
        super(name, gender, birthDate);
        this.driverLicenseNumber = driverLicenseNumber;
        this.licenseExpiryDate = licenseExpiryDate;
    }
}