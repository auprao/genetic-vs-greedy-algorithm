package com.company;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main extends Application {

    static String mode = "printer";
    // modes: "printer", "user"

    static double mutationChance = 0.02;
    static int citiesNumber;
    static List<Circle> cities = new ArrayList<>(5);
    static int[][] distances;
    int count = 0;
    static int populationSize = 300;
    static Path[] population = new Path[populationSize];
    double[] fitness = new double[populationSize];

    //note: dont want this for printer mode
    static AnchorPane root = new AnchorPane();
    static List<Line> lines = new ArrayList<>(600);

    private final int iterations = 2000 ;

    public static void main(String[] args) {
	    launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {


        if (mode.equals("user")) {
            Scene scene = new Scene(root, 600, 600);

            Button buttonGA = new Button("Genetic");
            buttonGA.setTranslateX(50);
            buttonGA.setTranslateY(480);
            buttonGA.setOnAction(event -> {
                //System.out.println(sumAllDistances());
                initGA();
            });

            Button buttonGR = new Button("Greedy");
            buttonGR.setTranslateX(50);
            buttonGR.setTranslateY(450);
            buttonGR.setOnAction(event -> {
                //System.out.println(sumAllDistances());
                initGreedy();
            });
            root.getChildren().addAll(buttonGA,buttonGR);

            primaryStage.setScene(scene);
            primaryStage.show();

        }

/*        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(1), event -> {
            time++;
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.setAutoReverse(false);
        timeline.play();*/

        else if (mode.equals("printer")) {

            long ga, gr = 0;

            for (int i = 0; i < 1000; i++) {

                for (int j = 0; j < 15; j++) {
                    createRandomCity();
                }
                ga =  System.nanoTime();
                initGA();
                ga = System.nanoTime() - ga;
                System.out.print(ga/1000 + ";");

                gr =  System.nanoTime();
                initGreedy();
                gr = System.nanoTime() - gr;
                System.out.print(gr/1000 + ";");

                System.out.println("");
                deleteCities();
            }
        }

        //getPopulationZero();

        //Path.crossover(new Path(8), new Path(8));
    }

    private void deleteCities() {
        cities.clear();
        count = 0;
    }

    private void createRandomCity() {
        double x = 10 + Math.random()*580;
        double y = 10 + Math.random()*580;

        Circle circle = new Circle(x,y, 15);
        circle.setId(count + "");

        if (mode.equals("user")) {
            circle.setFill(Color.ORANGE);

            StackPane stack = new StackPane();

            Text text = new Text(count + "");
            stack.getChildren().addAll(circle, text);
            stack.setTranslateX(x-15);
            stack.setTranslateY(y-15);

            root.getChildren().add(stack);
        }

        cities.add(circle);
        count++;
    }

    private void createCity(double x, double y) {

        Circle circle = new Circle(x,y, 15);
        circle.setId(count + "");

        if (mode.equals("user")) {
            circle.setFill(Color.ORANGE);

            StackPane stack = new StackPane();

            Text text = new Text(count + "");
            stack.getChildren().addAll(circle, text);
            stack.setTranslateX(x-15);
            stack.setTranslateY(y-15);

            root.getChildren().add(stack);
        }

        cities.add(circle);
        count++;
    }

    private void sumAllDistances() {
        citiesNumber = cities.size();
        distances = new int[citiesNumber][citiesNumber];
        double sum = 0;
        for (int row = 0; row < citiesNumber; row++) {
            for (int column = 0; column < row; column++) {
                double distance = calcDistances(cities.get(row), cities.get(column));
                distances[row][column] = (int) distance;
                distances[column][row] = (int) distance;
                sum = sum + distance;
            }
        }
    }

    public static double calcDistances(Circle c, Circle d) {
        return Math.sqrt( (c.getCenterX()-d.getCenterX())*(c.getCenterX()-d.getCenterX())
                + (c.getCenterY()-d.getCenterY())*(c.getCenterY()-d.getCenterY()));
    }

    private static void getPopulationZero() {
        for (int i = 0; i < populationSize; i++) {
            Path p = new Path(citiesNumber);
            for (int j = 0; j < citiesNumber; j++) {
                p.chromosome[j] = j;
            }
            p = p.shuffle(p.chromosome);
            population[i] = p;
            //System.out.print(Arrays.toString(p.chromosome));
        }
    }

    private static void getGreedyPopulationZero() {
        for (int i = 0; i < populationSize; i++) {
            Path p = new Path(citiesNumber);
            for (int j = 0; j < citiesNumber; j++) {
                p.chromosome[j] = j;
            }
            p.getGreedyPath();
            population[i] = p;
        }
    }

    private static int getRandomInt(int max){
        return (int) (Math.random()*max);
    }

    private void initGreedy() {
        sumAllDistances();
        //System.out.println(path);

        if (mode.equals("user")) {
            getPopulationZero();
            for (int i = 0; i < lines.size(); i++) {
                root.getChildren().remove(lines.get(i));
            }
            lines.clear();
        }

        Path path = new Path(citiesNumber);
        //System.out.println(path.getGreedyPath() + ", " + path.getGreedyPath().getFitness());
        System.out.print(path.getGreedyPath().getFitness() + ";");

        if (mode.equals("user")) {
            for (int i = 0; i < lines.size(); i++) {
                root.getChildren().add(lines.get(i));
            }
        }
    }

    private void initGA(){
        sumAllDistances();

       // Path path = new Path(citiesNumber);

        getPopulationZero();
        calcFitness();
        //printPopulation(0);
        for (int i = 1; i < iterations; i++) {
            getNewGeneration();
            for (int m = 0; m < populationSize; m++) {
                population[m].mutate(mutationChance);
            }
            calcFitness();
            //printPopulation(i);
        }
            printPopulation(iterations-1);

        if (mode.equals("user")) {
            for (int j = 0; j < lines.size(); j++) {
                root.getChildren().remove(lines.get(j));
            }
            lines.clear();
        }

        int min = 0;
        for (int i = 0; i < populationSize; i++) {
            if (fitness[i] < fitness[min]) {
                min = i;
            }
        }


        if (mode.equals("user")) {

            Path.drawGeneticPath(population[min]);

            for (int j = 0; j < lines.size(); j++) {
                root.getChildren().add(lines.get(j));
            }
        }




    }

    private void printPopulation(int n) {
        int min = 0;
        for (int i = 0; i < populationSize; i++) {
            if (fitness[i] < fitness[min]) {
                min = i;
            }
        }

        //System.out.println(n+1 + "it = " + fitness[min]);
        System.out.print(((int) fitness[min]) + ";");

    }

    private Path getShortestOfIteration() {
        int min = 0;
        for (int i = 0; i < populationSize; i++) {
            if (fitness[i] < fitness[min]) {
                min = i;
            }
        }
        return population[min];
    }

    private void calcFitness() {
        for (int i = 0; i < populationSize; i++) {
            int sum = 0;

            for (int j = 1; j < citiesNumber; j++) {
               sum += distances[population[i].chromosome[j-1]][population[i].chromosome[j]];
            }
            sum += distances[population[i].chromosome[citiesNumber-1]][population[i].chromosome[0]];

            fitness[i] = sum;
        }
    }

    private Path getParentByRouletteWheelSelection() {
        double total = 0;
        for (int i = 0; i < populationSize; i++) {
            total += Math.sqrt(Math.sqrt(10000000/fitness[i]));
        }
        double sum = 0;
        double random = Math.random() * total;
        for (int i = 0; i < populationSize; i++) {
            sum += Math.sqrt(Math.sqrt(10000000/fitness[i]));
            if (sum >= random) {
                return population[i];
            }
        }
        return population[populationSize - 1];
    }

    private void getNewGeneration() {
        Path[] newGeneration = new Path[populationSize];

        for (int i = 0; i < populationSize; i++) {
            Path p1 = getParentByRouletteWheelSelection();
            Path p2 = getParentByRouletteWheelSelection();
            Path child = Path.crossover(p1, p2);
            newGeneration[i] = child;
        }

        population = newGeneration;
    }
}
