package model;

public class MonthStatistic {
    private int hours;
    private int counterIdle = 0;
    private int counterReserved = 0;
    private int counterWorking = 0;
    private int counterRepair = 0;
    private double losses = 0;
    private double avgIdle = 0;
    private double avgReserved = 0;
    private int avgWorking = 0;
    private int avgRepair = 0;
    private final double WORKING_HOURS;

    public MonthStatistic(double WORKING_HOURS) {
        this.WORKING_HOURS = WORKING_HOURS;
    }
    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getCounterIdle() {
        return counterIdle;
    }

    public void addCounterIdle(int counterIdle) {
        this.counterIdle += counterIdle;
    }

    public int getCounterReserved() {
        return counterReserved;
    }

    public void addCounterReserved(int counterReserved) {
        this.counterReserved += counterReserved;
    }

    public int getCounterRunning() {
        return counterWorking;
    }

    public void addCounterRunning(int counterRunning) {
        this.counterWorking += counterRunning;
    }

    public int getCounterRepair() {
        return counterRepair;
    }

    public void addCounterRepair(int counterRepair) {
        this.counterRepair += counterRepair;
    }

    public double getLosses() {
        return losses;
    }

    public void addLosses(double losses) {
        this.losses += losses;
    }

    public void uppdateData(int counterIdle,
                            int counterReserved,
                            int counterWorking,
                            int counterRepair,
                            double losses) {
        this.counterIdle += counterIdle;
        this.counterWorking += counterWorking;
        this.counterRepair += counterRepair;
        this.losses += losses;
    }

    private double findAvg(int value) {
        return (value / (21 * WORKING_HOURS));
    }

    public double[] getAvgMonthStat() {
        double[] result = {findAvg(counterIdle),findAvg(counterReserved),findAvg(counterWorking),findAvg(counterRepair),this.losses};
        return result;
    }

    @Override
    public String toString() {
        return "MonthlyReport{" +
                ", counterIdle=" + counterIdle +
                ", counterReserved=" + counterReserved +
                ", counterWorking=" + counterWorking +
                ", counterRepair=" + counterRepair +
                ", losses=" + losses +
                ", avgIdle=" + findAvg(counterIdle) +
                ", avgReserved=" + findAvg(counterReserved) +
                ", avgWorking=" + findAvg(counterWorking) +
                ", avgRepair=" + findAvg(counterRepair) +
                '}';
    }
}
