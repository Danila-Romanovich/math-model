import model.GenRandNum;
import model.Machine;
import model.Event;


import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

public class MathModel {
    // параметры модели
    private final int NUM_MACHINES; // Количество рабочих мест (пк)
    private final double MTBF; // Среднее время работы до отказа (в часах)
    private final double MTTR; // Среднее время ремонта (в часах)
    private final double STANDART_DEVIATION; // стандартное отклонение для нормального распределения
    private final int SALARY_TECHNICIANS; // зарплата наладчика (в час)
    private final int COAST_MACHINE; // цена ПК за шт.
    private final double LOSSES; // убытки за простой 1го места (в час)
    private final int WORKING_HOURS; // длительность рабочего дня (в часах)
    private final int SIMULATION_TIME; // Общее время симуляции (в годах)
    // входные данные
    private final int NUM_TECHNICIANS; // Количество наладчиков
    private final int SPARE_MACHINES; // кол-во резервных ПК
    // инициализация компонентов
    private PriorityQueue<Event> eventQueue = new PriorityQueue<>();  // Очередь событий
    Queue<Machine> repairQueue = new LinkedList<>(); // очередь на ремонот
    Queue<Machine> reservedQueue = new LinkedList<>(); // очередь в резерве



    public MathModel(int NUM_MACHINES, double MTBF, double MTTR, double STANDART_DEVIATION, int SALARY_TECHNICIANS, int COAST_MACHINE, double LOSSES, int WORKING_HOURS, int NUM_TECHNICIANS, int SPARE_MACHINES, int SIMULATION_TIME) {
        this.NUM_MACHINES = NUM_MACHINES;
        this.MTBF = MTBF;
        this.MTTR = MTTR;
        this.STANDART_DEVIATION = STANDART_DEVIATION;
        this.SALARY_TECHNICIANS = SALARY_TECHNICIANS;
        this.COAST_MACHINE = COAST_MACHINE;
        this.LOSSES = LOSSES;
        this.WORKING_HOURS = WORKING_HOURS;
        this.SIMULATION_TIME = SIMULATION_TIME;
        this.NUM_TECHNICIANS = NUM_TECHNICIANS;
        this.SPARE_MACHINES = SPARE_MACHINES;
    }


    public void runSimulation() {
        Machine[] machines = initMachines();
        Technician[] technicians = initTechnicians();


        int workingMachines = this.NUM_MACHINES + this.SPARE_MACHINES;  // Количество работающих машин

        int busyTechnicians = 0;  // Количество занятых наладчиков

        double currentTime = 0;  // Текущее время симуляции
        double simulationHours = convertYearsToHours();
        int counterMonth = 0;
        int idleMachines = 0;
        int reservedMachines = 0;

        while (currentTime <= simulationHours) {
            Event event = eventQueue.peek();  // Извлекаем событие из очереди
            currentTime++;

            if (currentTime >= event.time) { // Если это время события
                currentTime = event.time;

                if (event.type == 0) { // Если это событие отказа

                    event.machine.setStatus(false);  // Машина перестает работать
                    System.out.println(event.machine.getId() + " ПК сломался");
                    repairQueue.add(event.machine); // добавляем в очередь на ремонт
                    event.machine.setProcessing(2); // устанавливаем статус "в очереди на ремонт"
                    System.out.println(event.machine.getId() + " ПК в очереди на ремонт");


                    if (!reservedQueue.isEmpty()) { // если есть резервные ПК то отправляем в отдел разработки
                        Machine machine = reservedQueue.poll();
                        machine.setProcessing(1);
                        System.out.println(machine.getId() + " ПК взят с резерва");
                        eventQueue.add(new Event(( machine.getFailureTime() + currentTime), 0, machine)); // добавляем событие отказа
                    }

                    workingMachines--;  // Уменьшаем количество работающих машин

                    if (workingMachines < NUM_MACHINES) { // счётчик простаивающих машин
                        idleMachines++;
                    }

                    if (busyTechnicians < NUM_TECHNICIANS) {  // Если есть свободные наладчики
                        busyTechnicians++;  // Увеличиваем количество занятых наладчиков

                        Machine machine = repairQueue.poll();
                        machine.setProcessing(3);
                        System.out.println(machine.getId() + " ПК в ремонте");

                        double repairTime = event.machine.getRepairTime();  // Рассчитываем время окончания ремонта
                        eventQueue.add(new Event((repairTime + currentTime), 1, machine));  // Добавляем событие окончания ремонта
                    }
                    eventQueue.remove(event);


                } else if (event.type == 1) {// Если это событие ремонта

                    event.machine.setStatus(true); // Машина снова работает
                    System.out.println(event.machine.getId() + " ПК починили");
                    event.machine.setFailureTime(GenRandNum.generateExponential(MTBF)); // Генерируем новое время до отказа
                    event.machine.setRepairTime(GenRandNum.generateNormal(MTTR, STANDART_DEVIATION)); // Генерируем новое время на ремонт


                    if (workingMachines < NUM_MACHINES) {
                        event.machine.setProcessing(1);
                        System.out.println(event.machine.getId() + " ПК переехал с ремонта в отдел разработки");
                        eventQueue.add(new Event((event.machine.getFailureTime() + currentTime), 0, event.machine)); // добавляем событие отказа
                        idleMachines++; // счётчик простаивающих машин
                    } else {
                        event.machine.setProcessing(0); // отправляем в резерв
                        reservedQueue.add(event.machine);
                        System.out.println(event.machine.getId() + " ПК добавлен в резерв");
                    }
                    eventQueue.remove(event); // убераем из очереди событий
                    workingMachines++;  // Увеличиваем количество работающих машин
                    busyTechnicians--;

                    if (workingMachines >= NUM_MACHINES && idleMachines > 0) {
                        idleMachines--;
                    }

                    if (!repairQueue.isEmpty()) { // если есть очередь на ремонт
                        busyTechnicians++;  // Увеличиваем количество занятых наладчиков
                        Machine machine = repairQueue.poll();
                        machine.setProcessing(3);
                        System.out.println(machine.getId() + " ПК в ремонте");

                        double repairTime = machine.getRepairTime();  // Рассчитываем время окончания ремонта
                        eventQueue.add(new Event((repairTime + currentTime), 1, machine));  // Добавляем событие окончания ремонта
                    }

                }

            }

        }
        System.out.println("");


    }

    private Machine[] initMachines() {
        Machine[] machines = new Machine[NUM_MACHINES + SPARE_MACHINES];
        for (int i = 0; i < machines.length; i++) {
            machines[i] = new Machine(i + 1, GenRandNum.generateExponential(MTBF), GenRandNum.generateNormal(MTTR, STANDART_DEVIATION));
            if (i < NUM_MACHINES) {
                eventQueue.add(new Event(machines[i].getFailureTime(), 0, machines[i]));  // Добавляем событие отказа
            } else {
                machines[i].setProcessing(0);
                reservedQueue.add(machines[i]); // добавляем машину в очередь в резерве
            }

        }
        return machines;
    }

    private Technician[] initTechnicians() {
        Technician[] technicians = new Technician[NUM_TECHNICIANS];
        for (int i = 0; i < technicians.length; i++) {
            technicians[i] = new Technician();
        }
        return technicians;
    }

    private double convertYearsToHours() {
        return (SIMULATION_TIME * 12 * 21 * WORKING_HOURS);
    }

    private double convertHoursToYears(double hours) {
        return (hours /(12 * 21 * WORKING_HOURS));
    }


}

class Technician {
    public boolean status; // занят или не занят
    Technician() {
        status = true;
    }
}
