import model.GenRandNum;
import model.Machine;
import model.Event;
import model.MonthStatistic;


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
    // инициализация компонентов
    private PriorityQueue<Event> eventQueue = new PriorityQueue<>();  // Очередь событий
    Queue<Machine> repairQueue = new LinkedList<>(); // очередь на ремонот
    Queue<Machine> reservedQueue = new LinkedList<>(); // очередь в резерве


    public MathModel(int NUM_MACHINES, double MTBF, double MTTR, double STANDART_DEVIATION, int SALARY_TECHNICIANS, int COAST_MACHINE, double LOSSES, int WORKING_HOURS, int SIMULATION_TIME) {
        this.NUM_MACHINES = NUM_MACHINES;
        this.MTBF = MTBF;
        this.MTTR = MTTR;
        this.STANDART_DEVIATION = STANDART_DEVIATION;
        this.SALARY_TECHNICIANS = SALARY_TECHNICIANS;
        this.COAST_MACHINE = COAST_MACHINE;
        this.LOSSES = LOSSES;
        this.WORKING_HOURS = WORKING_HOURS;
        this.SIMULATION_TIME = SIMULATION_TIME;
    }


    public void runSimulation(int numTechnicans, int spareMachines) {
        Machine[] machines = initMachines(spareMachines);
        MonthStatistic[] monthStatistics = initMonthStatistics();

        int workingMachines = this.NUM_MACHINES + spareMachines;  // Количество работающих машин

        int busyTechnicians = 0;  // Количество занятых наладчиков

        int currentTime = 1;  // Текущее время симуляции
        double simulationHours = convertYearsToHours();
        int counterMonth = 0;
        int idleMachines = 0;
        int reservedMachines = 0;

        int counterIdle = 0;
        int counterReserved = 0;
        int counterWorking = 0;
        int counterRepair = 0;
        double losses = 0;

        while (currentTime <= simulationHours) {
            Event event = eventQueue.peek();  // Извлекаем событие из очереди
            double eventTime = currentTime;

            if (eventTime % event.time < 1) { // Если это время события
                eventTime = event.time;

                if (event.type == 0) { // Если это событие отказа

                    event.machine.setStatus(false);  // Машина перестает работать
                    System.out.println(event.machine.getId() + " ПК сломался"  + ",    часы: " + eventTime);
                    repairQueue.add(event.machine); // добавляем в очередь на ремонт
                    event.machine.setProcessing(2); // устанавливаем статус "в очереди на ремонт"
                    System.out.println(event.machine.getId() + " ПК в очереди на ремонт"  + ",    часы: " + eventTime);


                    if (!reservedQueue.isEmpty()) { // если есть резервные ПК то отправляем в отдел разработки
                        Machine machine = reservedQueue.poll();
                        machine.setProcessing(1);
                        System.out.println(machine.getId() + " ПК взят с резерва"  + ",    часы: " + eventTime);
                        eventQueue.add(new Event((machine.getFailureTime() + eventTime), 0, machine)); // добавляем событие отказа
                    }

                    workingMachines--;  // Уменьшаем количество работающих машин

                    if (workingMachines < NUM_MACHINES) { // счётчик простаивающих машин
                        idleMachines++;
                    }

                    if (busyTechnicians < numTechnicans) {  // Если есть свободные наладчики
                        busyTechnicians++;  // Увеличиваем количество занятых наладчиков

                        Machine machine = repairQueue.poll();
                        machine.setProcessing(3);
                        System.out.println(machine.getId() + " ПК в ремонте"  + ",    часы: " + eventTime);

                        double repairTime = event.machine.getRepairTime();  // Рассчитываем время окончания ремонта
                        eventQueue.add(new Event((repairTime + eventTime), 1, machine));  // Добавляем событие окончания ремонта
                    }
                    eventQueue.remove(event);


                } else if (event.type == 1) {// Если это событие ремонта

                    event.machine.setStatus(true); // Машина снова работает
                    System.out.println(event.machine.getId() + " ПК починили"  + ",    часы: " + eventTime);
                    event.machine.setFailureTime(GenRandNum.generateExponential(MTBF)); // Генерируем новое время до отказа
                    event.machine.setRepairTime(GenRandNum.generateNormal(MTTR, STANDART_DEVIATION)); // Генерируем новое время на ремонт


                    if (workingMachines < NUM_MACHINES) {
                        event.machine.setProcessing(1);
                        System.out.println(event.machine.getId() + " ПК переехал с ремонта в отдел разработки"  + ",    часы: " + currentTime);
                        eventQueue.add(new Event((event.machine.getFailureTime() + eventTime), 0, event.machine)); // добавляем событие отказа
                        idleMachines++; // счётчик простаивающих машин
                    } else {
                        event.machine.setProcessing(0); // отправляем в резерв
                        reservedQueue.add(event.machine);
                        System.out.println(event.machine.getId() + " ПК добавлен в резерв"  + ",    часы: " + eventTime);
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
                        System.out.println(machine.getId() + " ПК в ремонте"  + ",    часы: " + currentTime);

                        double repairTime = machine.getRepairTime();  // Рассчитываем время окончания ремонта
                        eventQueue.add(new Event((repairTime + currentTime), 1, machine));  // Добавляем событие окончания ремонта
                    }
                }
            } else {
                currentTime++;

                if (counterMonth < SIMULATION_TIME * 12) {
                    monthStatistics[counterMonth].uppdateData(counterIdle,counterReserved,counterWorking,counterRepair,losses);
                }

                // подсчёт номера месяца
                if (currentTime % (21 * WORKING_HOURS ) < 1 && currentTime > 1) {
                    if (counterMonth > 1) {
                        if (monthStatistics[counterMonth].getHours() != currentTime) {
                            monthStatistics[counterMonth].setHours(currentTime);
                            counterMonth++;
                        }
                    } else {
                        monthStatistics[counterMonth].setHours(currentTime);
                        counterMonth++;
                    }
                    System.out.println("");
                    System.out.println("Месяц: " + counterMonth + ",    часы: " + currentTime);
                    System.out.println("");
                }

            }
        }
        for (int i=0; i<monthStatistics.length;i++) {
            System.out.println(monthStatistics[i].toString());
        }
    }


    private Machine[] initMachines(int spareMachines) {
        Machine[] machines = new Machine[NUM_MACHINES + spareMachines];
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

    private MonthStatistic[] initMonthStatistics() {
        MonthStatistic[] monthStatistics = new MonthStatistic[SIMULATION_TIME * 12];
        for (int i = 0; i < monthStatistics.length; i++) {
            monthStatistics[i] = new MonthStatistic();
        }
        return monthStatistics;
    }

    private double convertYearsToHours() {
        return (SIMULATION_TIME * 12 * 21 * WORKING_HOURS);
    }

    private double convertHoursToYears(double hours) {
        return (hours / (12 * 21 * WORKING_HOURS));
    }
}