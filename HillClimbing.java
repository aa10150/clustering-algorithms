/**
 * Anshu Aramandla
 * 2/8/26
 */

import java.util.*;
import java.io.*;
import java.lang.Math;

// Vector class containing name, value, and components
class Vector {
    private String name;
    private ArrayList<Integer> components;
    private int vectorNum;

    public Vector() {
        components = new ArrayList<Integer>();
    }

    void setName(String name) {
        this.name = name;
    }
    void addComp(int component) {
        this.components.add(component);
    }
    void setNum(int num) {
        this.vectorNum = num;
    }
    String getName() {
        return name;
    }
    int getComp(int idx) {
        return components.get(idx);
    }
    int getNum() {
        return vectorNum;
    }
}

public class HillClimbing {
    public static void main(String[] args) {
        // initialize empty arraylist + open file to read
        ArrayList<String> lines = new ArrayList<String>();
        File inp = new File("input.txt");

        // read file and add each line to arraylist
        try (Scanner readfile = new Scanner(inp)) {
            while(readfile.hasNextLine()) {
                String line = readfile.nextLine().trim();
                lines.add(line);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        }

        // initialize array of Vector objects
        String[] line1 = lines.get(0).split(" ");
        int numComponents = Integer.parseInt(line1[0]);
        int numVectors = Integer.parseInt(line1[1]);
        Vector[] vectors = new Vector[numVectors];

        for (int i=0; i<numVectors; i++) {
            vectors[i] = new Vector();
            String[] vectorParse = lines.get(i+1).split(" ");
            String name = vectorParse[0];
            vectors[i].setNum(i);
            vectors[i].setName(name);
            for (int j=1; j<=numComponents; j++) {
                int component = Integer.parseInt(vectorParse[j]);
                vectors[i].addComp(component);
            }
        }

        // initialize other values
        double threshold = Double.parseDouble(line1[2]);
        int numClusters = Integer.parseInt(line1[3]);
        int numRestarts = Integer.parseInt(line1[4]);
        char outputVC = line1[5].charAt(0);

        // initialize 2d array containing distances between all points
        double[][] distances = new double[numVectors][numVectors];
        for (int i=0; i<numVectors; i++) {
            for (int j=0; j<numVectors; j++) {
                if (j==i) continue;
                // calculate distance between points
                int sum = 0;
                for (int k=0; k<numComponents; k++) {
                    int toBeSquared = vectors[i].getComp(k) - vectors[j].getComp(k);
                    sum += (toBeSquared*toBeSquared);
                }
                double distance = Math.sqrt(sum);
                // add distance to array
                distances[i][j] = distance;
                distances[j][i] = distance;
            }
        }

        ArrayList<ArrayList<Vector>> startState = randomState(numVectors, numClusters, vectors);
        hillClimb(startState, distances, threshold, numVectors, numClusters, vectors, true, numRestarts, outputVC);
    
    }

    // method: generate random starting state
    static ArrayList<ArrayList<Vector>> randomState(int numVectors, int numClusters, Vector[] vectors) {
        ArrayList<ArrayList<Vector>> randomState = new ArrayList<ArrayList<Vector>>();
        Random rand = new Random();
        boolean[] vectorsAdded = new boolean[numVectors];
        // pick numClusters vectors at random and assign them to clusters 1...numClusters
        for (int i=0; i<numClusters; i++) {
            ArrayList<Vector> cluster = new ArrayList<Vector>();
            int r = rand.nextInt(numVectors);
            if (!vectorsAdded[r]) {
                cluster.add(vectors[r]);
                randomState.add(cluster);
                vectorsAdded[r] = true;
            } else {
                i--;
            }
        }
        // go through remaining vectors and assign them to random clusters
        for (int i=0; i<(numVectors); i++) {
            if (!vectorsAdded[i]) {
                int r =  rand.nextInt(numClusters);
                randomState.get(r).add(vectors[i]);
            }
        }
        return randomState;
    }

    // stateToString method for printing out a state
    static String stateToString(ArrayList<ArrayList<Vector>> state, double[][] distances, double threshold) {
        String str = "";
        int value = 0;
        for (int c=0; c<state.size(); c++) {
            ArrayList<Vector> cluster = state.get(c);
            str = str + "{";
            for (int v=0; v<cluster.size(); v++) {
                Vector vector = cluster.get(v);
                str = str + vector.getName();
                if (v<cluster.size()-1) str = str + ",";
            }
            str = str + "}";
            if (c<state.size()-1) str = str + ",";
        }
        str = str + " Error=" + String.format("%.4f", stateError(state, distances, threshold));
        return str;
    }

    // method: find error of a cluster
    static double clusterError(ArrayList<Vector> cluster, double[][] distances, double threshold) {
        if (cluster.size()==1) return 0;
        // find diameter
        double diameter = 0;
        for (int i=0; i<cluster.size(); i++) {
            for (int j=0; j<cluster.size(); j++) {
                int iNum = cluster.get(i).getNum();
                int jNum = cluster.get(j).getNum();
                if (distances[iNum][jNum] > diameter) diameter = distances[iNum][jNum];
            }
        }
        return Math.max(0, diameter-threshold);
    }

    // method: find total error of a state
    static double stateError(ArrayList<ArrayList<Vector>> state, double[][] distances, double threshold) {
        double total = 0;
        for (ArrayList<Vector> cluster : state) {
            total += clusterError(cluster, distances, threshold);
        }
        return total;
    }

    static void hillClimb(ArrayList<ArrayList<Vector>> state, double[][] distances, double threshold,
        int numVectors, int numClusters, Vector[] vectors, boolean newStart, int startsLeft, char outputVC) {
            if (newStart) {
                System.out.println("\nRandomly chosen start state:");
                System.out.println(stateToString(state, distances, threshold));
            } else {
                if (outputVC == 'V') {
                    System.out.println("\nMove to");
                    System.out.println(stateToString(state, distances, threshold));
                }
            }
            
            if (outputVC == 'V') System.out.println("\nNeighbors:");
            // keep track of error amounts
            ArrayList<Double> errors = new ArrayList<Double>();
            ArrayList<ArrayList<ArrayList<Vector>>> neighbors = new ArrayList<ArrayList<ArrayList<Vector>>>();

            // calculate all possible neighbors
            // for each cluster in the state
            for (int c=0; c<state.size(); c++) {
                ArrayList<Vector> cluster = state.get(c);
                if (cluster.size()>1) {
                    // for each vector in the cluster
                    for (int v=0; v<cluster.size(); v++) {
                        Vector vector = cluster.get(v);                       
                        // iterate through each possible cluster the vector could be moved to
                        for (int i=0; i<state.size(); i++) {
                            if (i!=c) {
                                // copy current state
                                ArrayList<ArrayList<Vector>> newState = new ArrayList<>();
                                for (ArrayList<Vector> clusterToCopy : state) {
                                    ArrayList<Vector> newCluster = new ArrayList<>(clusterToCopy);
                                    newState.add(newCluster);
                                }
                                // add vector to cluster i
                                newState.get(i).add(cluster.get(v));
                                // remove vector from cluster c
                                newState.get(c).remove(cluster.get(v));
                                // print and add to list of neighbors
                                if (outputVC == 'V') {
                                    System.out.println(stateToString(newState, distances, threshold));
                                }
                                neighbors.add(newState);
                                errors.add(stateError(newState, distances, threshold));
                                // if solution found
                                if (stateError(newState, distances, threshold) == 0) {
                                    System.out.println("\nFound solution: " +
                                        stateToString(newState, distances, threshold));
                                    System.exit(0);
                                }
                            }
                        }
                    }
                }
            }

            // find lowest error out of the neighbors
            double min = Collections.min(errors);
            if (min >= stateError(state, distances, threshold) && startsLeft>0) {
                System.out.println("\nSearch failed");
                ArrayList<ArrayList<Vector>> randomStartState = randomState(numVectors, numClusters, vectors);
                hillClimb(randomStartState, distances, threshold, numVectors,
                    numClusters, vectors, true, startsLeft-1, outputVC);
            } else {
                int idx = errors.indexOf(min);
                hillClimb(neighbors.get(idx), distances, threshold, numVectors,
                    numClusters, vectors, false, startsLeft, outputVC);
            }
        }
}