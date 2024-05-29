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


    public Object[] runSimulation(int numTechnicans, int spareMachines) {
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
        int counterReserved = spareMachines;
        int counterWorking = 0;
        int counterRepair = 0;
        double losses = 0;

        while (currentTime < simulationHours) {
            Event event = eventQueue.peek();  // Извлекаем событие из очереди
            double eventTime = currentTime;

            if (eventTime % event.time < 1) { // Если это время события
                eventTime = event.time;

                if (event.type == 0) { // Если это событие отказа

                    event.machine.setStatus(false);  // Машина перестает работать
//                    System.out.println(event.machine.getId() + " ПК сломался"  + ",    часы: " + eventTime);
                    repairQueue.add(event.machine); // добавляем в очередь на ремонт
                    event.machine.setProcessing(2); // устанавливаем статус "в очереди на ремонт"
//                    System.out.println(event.machine.getId() + " ПК в очереди на ремонт"  + ",    часы: " + eventTime);
                    counterRepair++;

                    if (!reservedQueue.isEmpty()) { // если есть резервные ПК то отправляем в отдел разработки
                        Machine machine = reservedQueue.poll();
                        machine.setProcessing(1);
//                        System.out.println(machine.getId() + " ПК взят с резерва"  + ",    часы: " + eventTime);
                        eventQueue.add(new Event((machine.getFailureTime() + eventTime), 0, machine)); // добавляем событие отказа
                        if (counterReserved > 0) {
                            counterReserved--;
                        }
                    }

                    workingMachines--;  // Уменьшаем количество работающих машин

                    if (workingMachines < NUM_MACHINES) { // счётчик простаивающих машин
                        counterIdle++;
                    }

                    if (busyTechnicians < numTechnicans) {  // Если есть свободные наладчики
                        busyTechnicians++;  // Увеличиваем количество занятых наладчиков

                        Machine machine = repairQueue.poll();
                        machine.setProcessing(3);
//                        System.out.println(machine.getId() + " ПК в ремонте"  + ",    часы: " + eventTime);

                        double repairTime = event.machine.getRepairTime();  // Рассчитываем время окончания ремонта
                        eventQueue.add(new Event((repairTime + eventTime), 1, machine));  // Добавляем событие окончания ремонта
                    }
                    eventQueue.remove(event);


                } else if (event.type == 1) {// Если это событие ремонта

                    event.machine.setStatus(true); // Машина снова работает
//                    System.out.println(event.machine.getId() + " ПК починили"  + ",    часы: " + eventTime);
                    event.machine.setFailureTime(GenRandNum.generateExponential(MTBF)); // Генерируем новое время до отказа
                    event.machine.setRepairTime(GenRandNum.generateNormal(MTTR, STANDART_DEVIATION)); // Генерируем новое время на ремонт
                    counterRepair--;


                    if (workingMachines < NUM_MACHINES) {
                        event.machine.setProcessing(1);
//                        System.out.println(event.machine.getId() + " ПК переехал с ремонта в отдел разработки"  + ",    часы: " + currentTime);
                        eventQueue.add(new Event((event.machine.getFailureTime() + eventTime), 0, event.machine)); // добавляем событие отказа


                    } else {
                        event.machine.setProcessing(0); // отправляем в резерв
                        reservedQueue.add(event.machine);
//                        System.out.println(event.machine.getId() + " ПК добавлен в резерв"  + ",    часы: " + eventTime);
                        counterReserved++;
                    }
                    eventQueue.remove(event); // убераем из очереди событий
                    workingMachines++;  // Увеличиваем количество работающих машин
                    busyTechnicians--;

                    if (workingMachines >= NUM_MACHINES && counterIdle > 0) {
                        counterIdle--;
                    }

                    if (!repairQueue.isEmpty()) { // если есть очередь на ремонт
                        busyTechnicians++;  // Увеличиваем количество занятых наладчиков
                        Machine machine = repairQueue.poll();
                        machine.setProcessing(3);
//                        System.out.println(machine.getId() + " ПК в ремонте"  + ",    часы: " + currentTime);

                        double repairTime = machine.getRepairTime();  // Рассчитываем время окончания ремонта
                        eventQueue.add(new Event((repairTime + currentTime), 1, machine));  // Добавляем событие окончания ремонта
                    }
                }
            } else {
                currentTime++;

                if (counterMonth < SIMULATION_TIME * 12) {
                    monthStatistics[counterMonth].uppdateData(counterIdle,counterReserved,workingMachines,counterRepair,(LOSSES * counterIdle));
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
//                    System.out.println("");
//                    System.out.println("Месяц: " + counterMonth + ",    часы: " + currentTime);
//                    System.out.println("");
                }

            }
        }
//        for (int i=0; i<monthStatistics.length;i++) {
//            System.out.println(monthStatistics[i].toString());
//        }
        Object[] obj = new Object[2];
        double[][] arrStat = new double[monthStatistics.length][];
        for (int i = 0; i < monthStatistics.length; i++) {
            arrStat[i] = monthStatistics[i].getAvgMonthStat();
        }
        obj[1] = finalStatistick(monthStatistics, spareMachines, numTechnicans);
        return obj;
    }

    public void optimization() {
        double matrix[][] = new double[10][10];
        double minLosses = 0;
        int technicans = 0;
        int machines = 0;
        for (int i = 0; i < 10; i++) { // по строкам (кол.во наладчиков)
            for (int j = 0; j < 10; j++) { // по столбцам (кол.во резервных ПК)
                matrix[i][j] = ((double[])runSimulation(i+1,j)[1])[4] ;
                if (i == 0 && j == 0) {
                    minLosses = matrix[i][j];
                    technicans = i+1;
                    machines = j;
                } else if (matrix[i][j] < minLosses) {
                    minLosses = matrix[i][j];
                    technicans = i+1;
                    machines = j;
                }

            }
        }
        for (int i = 0; i < 10; i++) { // по строкам (кол.во наладчиков)
            for (int j = 0; j < 10; j++) { // по столбцам (кол.во резервных ПК)
                System.out.print(" " + matrix[i][j] + " ");
            }
            System.out.println("");
        }
        System.out.println("Минимальные потери " + minLosses + "$ при найме " + technicans + " наладчиков и покупке " + machines + " едениц оборудования");
    }


    private Machine[] initMachines(int spareMachines) {
        Machine[] machines = new Machine[NUM_MACHINES + spareMachines];
        for (int i = 0; i < machines.length; i++) {
            machines[i] = new Machine(i + 1, GenRandNum.generateExponential(MTBF), GenRandNum.generateNormal(MTTR, STANDART_DEVIATION));
            if (i < NUM_MACHINES) {
                eventQueue.add(new Event(machines[i].getFailureTime(), 0, machines[i]));  // Добавляем событие отказа
                System.out.println(machines[i].getFailureTime());
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
            monthStatistics[i] = new MonthStatistic(WORKING_HOURS);
        }
        return monthStatistics;
    }

    private double convertYearsToHours() {
        return (SIMULATION_TIME * 12 * 21 * WORKING_HOURS);
    }

    private double convertHoursToYears(double hours) {
        return (hours / (12 * 21 * WORKING_HOURS));
    }

    public double[] finalStatistick(MonthStatistic[] monthStatistics, int spareMachines, int numTechnicans) {
        double monthLosses = 0;
        for (int i = 0; i < monthStatistics.length; i++) {
            monthLosses += monthStatistics[i].getLosses();
        }
        double avgMonthLosses = monthLosses / monthStatistics.length;
        double lossesOnTechnicans = numTechnicans * SALARY_TECHNICIANS * convertYearsToHours();
        double lossesOnSpareMachines = spareMachines * COAST_MACHINE;
        double totalLosses = monthLosses + lossesOnTechnicans + lossesOnSpareMachines;
        double avgTotalLosses = totalLosses / monthStatistics.length;
        System.out.println("Средние потери в месяц: " + avgTotalLosses + "\n" +
                "Затраты на зарплату наладчикам: " + lossesOnTechnicans + "\n" +
                "Затраты на покупку резервных ПК: " + lossesOnSpareMachines + "\n" +
                "Средние потери в месяц из за простоя: " + avgMonthLosses + "\n" +
                "Общие потери: " + totalLosses);
        double[] result = {avgTotalLosses, lossesOnTechnicans, lossesOnSpareMachines, avgMonthLosses, totalLosses};
        return result;
    }
}