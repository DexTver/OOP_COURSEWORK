// Абстрактный класс Person представляет базовую информацию о человеке
abstract class Person {
    protected String name;           // Имя
    protected String gender;         // Пол
    protected String birthDate;      // Дата рождения

    // Конструктор для инициализации базовых полей
    public Person(String name, String gender, String birthDate) {
        this.name = name;
        this.gender = gender;
        this.birthDate = birthDate;
    }
}