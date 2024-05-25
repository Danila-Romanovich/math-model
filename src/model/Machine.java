package model;

public class Machine {

    private int id;                // номер ПК
    private boolean status;      // Статус машины: исправна или нет
    private int processing;   // 0 - в резерве, 1 - работает, 2 - в очереди на ремонт, 3 - ремонтируется;
    private double failureTime;     // Время до отказа
    private double repairTime;      // Время ремонта

    public Machine(int id, double failureTime, double repairTime) {
        this.id = id;
        this.status = true;  // Изначально машина работает
        this.processing = 1;
        this.failureTime = failureTime;
        this.repairTime = repairTime;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public double getFailureTime() {
        return failureTime;
    }

    public void setFailureTime(double failureTime) {
        this.failureTime = failureTime;
    }

    public double getRepairTime() {
        return repairTime;
    }

    public void setRepairTime(double repairTime) {
        this.repairTime = repairTime;
    }

    public int getProcessing() {
        return processing;
    }

    public void setProcessing(int processing) {
        this.processing = processing;
    }

    public int getId() {
        return id;
    }
}
