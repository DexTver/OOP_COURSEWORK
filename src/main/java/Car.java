// Класс Car хранит информацию об автомобиле
class Car {
    private String vin;                     // VIN номер
    private String driverLicenseNumber;     // Номер водительского удостоверения владельца
    private String licensePlate;            // Номерной знак
    private String color;                   // Цвет
    private String model;                   // Модель
    private String inspectionExpiryDate;    // Дата окончания техосмотра
    private String insuranceExpiryDate;     // Дата окончания страховки

    // Конструктор для инициализации автомобиля
    public Car(String vin, String driverLicenseNumber, String licensePlate, String color, String model, String inspectionExpiryDate, String insuranceExpiryDate) {
        this.vin = vin;
        this.driverLicenseNumber = driverLicenseNumber;
        this.licensePlate = licensePlate;
        this.color = color;
        this.model = model;
        this.inspectionExpiryDate = inspectionExpiryDate;
        this.insuranceExpiryDate = insuranceExpiryDate;
    }
}