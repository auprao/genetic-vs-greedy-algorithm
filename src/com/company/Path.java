package com.company;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Path {
    int[] chromosome;

    public Path(int citiesNumber) {
        List<Integer> consecutives = new ArrayList<>(citiesNumber);
        for (int i = 1; i < citiesNumber; i++) {
            consecutives.add(i);
        }
        Collections.shuffle(consecutives);
        chromosome = new int[citiesNumber];
        if (chromosome.length > 0){
            chromosome[0] = 0;
        }
        for (int i = 1; i < citiesNumber; i++) {
            chromosome[i] = consecutives.get(i-1);
        }
    }

    public Path(int[] chromosome) {
        this.chromosome = chromosome;
    }

    public double distance(double[][] distances) {
        double distance = 0;
        for (int i = 0; i < distances.length - 1; i++) {
            distance += distances[ chromosome[i] ][ chromosome[i + 1] ];
        }
        distance += distances[ chromosome[distances.length - 1] ][ chromosome[0] ];
        return distance;
    }

    public void mutate(double mutationChance) {
        for (int i = 1; i < Main.citiesNumber - 1; i++) {
            if (Math.random() < mutationChance) {
                int temp = chromosome[i];
                chromosome[i] = chromosome[i + 1];
                chromosome[i + 1] = temp;
            }
        }
    }

    public Path getGreedyPath(){
        Path greedyPath = new Path(Main.citiesNumber);

        List<Integer> visited = new ArrayList<>();
        List<Integer> unvisited = new ArrayList<>();
        for (int i = 0; i < Main.citiesNumber; i++) {
            unvisited.add(i);
        }
        greedyPath.chromosome[0] = 0;
        visited.add(0);
        unvisited.remove((Integer) 0);
        int last = Main.citiesNumber;

        for (int i = 1; i < chromosome.length; i++) {
            int current = visited.get(visited.size() - 1);
            int closestCity = unvisited.get(0);

            /*int closestIndex = i;
            for (int j = 1; j < Main.citiesNumber - 1; j++) {
                if ((! (visited.contains(j))) && i!=j){
                    if (Main.calcDistances(Main.cities.get(i),Main.cities.get(closestIndex)) > Main.calcDistances(Main.cities.get(i),Main.cities.get(j))){
                    closestIndex = j;
                    }
                }
            }*/

            for (int j = 1; j < unvisited.size(); j++) {
                //if (Main.calcDistances(Main.cities.get(current),Main.cities.get(closestCity)) > Main.calcDistances(Main.cities.get(current),Main.cities.get(j))){
                if (Main.distances[current][closestCity] > Main.distances[current][unvisited.get(j)]){
                    closestCity = unvisited.get(j);
                }
            }

            /*
            System.out.println("curr" + current);
            System.out.println("closest" + closestCity);
*/

            if (Main.mode.equals("user")) {

                Line line = new Line(Main.cities.get(current).getCenterX(), Main.cities.get(current).getCenterY(),
                        Main.cities.get(closestCity).getCenterX(), Main.cities.get(closestCity).getCenterY());
                line.setStroke(Color.CRIMSON);
                line.setStrokeWidth(2);
                Main.lines.add(line);

            }



            greedyPath.chromosome[i] = closestCity;
            visited.add(closestCity);
            unvisited.remove((Integer) closestCity);
            //System.out.println(unvisited);
            last = closestCity;
        }

        if (Main.mode.equals("user")) {

            Line line = new Line(Main.cities.get(last).getCenterX(), Main.cities.get(last).getCenterY(),
                    Main.cities.get(0).getCenterX(), Main.cities.get(0).getCenterY());
            line.setStroke(Color.RED);
            line.setStrokeWidth(2);
            Main.lines.add(line);

        }

        return greedyPath;
    }

    public static void drawGeneticPath(Path path){

        // only called if in user mode

        int[] chromosome = path.chromosome;

        for (int i = 1; i < chromosome.length; i++) {

            Line line = new Line(Main.cities.get(chromosome[i-1]).getCenterX(), Main.cities.get(chromosome[i-1]).getCenterY(),
                    Main.cities.get(chromosome[i]).getCenterX(), Main.cities.get(chromosome[i]).getCenterY());
            line.setStroke(Color.BLUEVIOLET);
            line.setStrokeWidth(4);


            Main.lines.add(line);

        }

        Line line = new Line(Main.cities.get(chromosome[chromosome.length-1]).getCenterX(), Main.cities.get(chromosome[chromosome.length-1]).getCenterY(),
                Main.cities.get(0).getCenterX(), Main.cities.get(0).getCenterY());
        line.setStroke(Color.BLUE);
        line.setStrokeWidth(4);


        Main.lines.add(line);
    }

//    Cycle crossover (CX)
//    In F1, every city maintains the position it had in at least one of its parents.
//    P1: J B F C A D H G I E
//    P2: F A G D H C E B J I
//    Choose the first city from P1 and copy it to F1. Check the corresponding city in P2 (here, city F) and
//    copy it to F1, in the same position it occurs in P1. Repeat.
//    F1: J B F * A * H G I E
//    When you encounter a city that is already in F1 the cycle is complete. Now fill in the remaining cities
//    from P2.
//    F1: J B F D A C H G I E
    public static Path crossover(Path p1, Path p2) {
        int length = p1.chromosome.length;
        int[] chromosome = new int[length];
       // System.out.print("p1 = " + Arrays.toString(p1.chromosome) + ", p2 = " + Arrays.toString(p2.chromosome) );
        chromosome[0] = 0;

        //from p1
        int i = 1;
        while (true) {
            chromosome[i] = p1.chromosome[i];
            int searched = p2.chromosome[i];

            //check if searched is in the chromosome
            boolean found = false;
            for (int k = 1; k < length; k++) {
                if (chromosome[k] == searched) {
                    found = true;
                    break;
                }
            }
            if (found) {
                break;
            }

            //take from p1 to chromosome
            for (int j = 1; j < length; j++) {
                if (p1.chromosome[j] == searched) {
                    i = j;
                    break;
                }
            }
        }

        //the rest from p2
        for (int j = 1; j < length; j++) {
            if (chromosome[j] == 0){
                chromosome[j] = p2.chromosome[j];
            }
        }
        //System.out.println(", child = "+ Arrays.toString(chromosome));
        return new Path(chromosome);
    }

    public int getFitness() {
        int sum = 0;

        for (int i = 1; i < chromosome.length; i++) {
            sum += Main.distances[chromosome[i-1]][chromosome[i]];
        }
        sum += Main.distances[chromosome[Main.citiesNumber-1]][chromosome[0]];

        return sum;
    }

    @Override
    public String toString() {
        return Arrays.toString(chromosome);
    }


    public Path shuffle(int[] table) {

        List<Integer> intList = new ArrayList<>();

        for (int i = 1; i < table.length; i++) {
            intList.add(table[i]);
        }

        Collections.shuffle(intList);

        for (int i = 1; i < table.length; i++) {
            table[i] = intList.get(i - 1);
        }

        return new Path(table);
    }
}
