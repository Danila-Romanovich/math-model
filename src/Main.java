public class Main {
    public static void main(String[] args) {
        MathModel model = new MathModel(100, 1000,24, 8, 10, 150000, 150, 8,7);
        model.runSimulation(1,2);
        model.optimization();

    }
}