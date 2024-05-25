package model;

public class Event implements Comparable<Event> { //Comparable интерфейс с сортировкой при добавлении новых элементов.
    public double time;  // Время события
    public int type;     // Тип события: 0 - отказ, 1 - ремонт
    public Machine machine; // Машина, к которой относится событие

    public Event(double time, int type, Machine machine) {
        this.time = time;
        this.type = type;
        this.machine = machine;
    }

    // Метод сравнения событий по времени для сортировки в очереди
    @Override
    public int compareTo(Event other) {
        return Double.compare(this.time, other.time);
    }
}
