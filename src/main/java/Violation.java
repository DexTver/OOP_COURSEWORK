// Класс Violation хранит данные о нарушении
class Violation {
    private int id;               // Уникальный идентификатор нарушения
    private String vin;           // VIN автомобиля, совершившего нарушение
    private String date;          // Дата нарушения
    private String violationType; // Тип нарушения

    // Конструктор для инициализации нарушения
    public Violation(int id, String vin, String date, String violationType) {
        this.id = id;
        this.vin = vin;
        this.date = date;
        this.violationType = violationType;
    }
}