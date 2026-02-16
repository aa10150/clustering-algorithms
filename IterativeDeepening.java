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
    private int value;
    private ArrayList<Integer> components;
    private int vectorNum;

    public Vector() {
        components = new ArrayList<Integer>();
    }

    void setName(String name) {
        this.name = name;
    }
    void setValue(int value) {
        this.value = value;
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
    int getValue() {
        return value;
    }
    int getComp(int idx) {
        return components.get(idx);
    }
    int getNum() {
        return vectorNum;
    }
}

enum DFSResultType {
    SOLUTION,
    MORESTATES,
    NOMORESTATES
}

// return type for DFS method including state and
// type of result (solution found/cutoff by depth limit/no successor states)
class DFSResult {
    DFSResultType type;
    ArrayList<ArrayList<Vector>> state;

    DFSResult(DFSResultType type, ArrayList<ArrayList<Vector>> state) {
        this.type = type;
        this.state = state;
    }
}

public class IterativeDeepening {
    public static void main(String str[]) {
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
            for (int j=0; j<numComponents; j++) {
                int component = Integer.parseInt(vectorParse[j+1]);
                vectors[i].addComp(component);
            }
            int value = Integer.parseInt(vectorParse[numComponents+1]);
            vectors[i].setValue(value);
        }

        // initialize other values
        double threshold = Double.parseDouble(line1[2]);
        int numClusters = Integer.parseInt(line1[3]);
        int target = Integer.parseInt(line1[4]);
        char outputVC = line1[5].charAt(0);
        
        // initialize 2d array containing all pairs within diameter threshold of each other
        boolean[][] validPairs = new boolean[numVectors][numVectors];
        for (int i=0; i<numVectors; i++) {
            for (int j=0; j<numVectors; j++) {
                if (j==i) continue;
                // calculate distance between points
                int tempsum = 0;
                for (int k=0; k<numComponents; k++) {
                    int toBeSquared = vectors[i].getComp(k) - vectors[j].getComp(k);
                    tempsum += (toBeSquared*toBeSquared);
                }
                double distance = Math.sqrt(tempsum);
                // add pair if within threshold
                if (distance <= threshold) {
                    validPairs[i][j] = true;
                    validPairs[j][i] = true;
                }
            }
        }

        // iterative deepening
        for (int depth=1; depth<numVectors+1; depth++) {
            if (outputVC == 'V') System.out.printf("Depth=%d\n", depth);
            boolean moreStates = false; // track in case there are no solutions
            // call DFS to current depth on each vector
            for (int i=0; i<numVectors; i++) {
                ArrayList<ArrayList<Vector>> initialState = new ArrayList<ArrayList<Vector>>();
                ArrayList<Vector> initialCluster = new ArrayList<Vector>();
                initialCluster.add(vectors[i]);
                initialState.add(initialCluster);
                DFSResult result = DFS(i, vectors, numClusters, target,
                    validPairs, depth, initialState, outputVC);
                // check result type                
                if (result.type == DFSResultType.SOLUTION) {
                    System.out.println("Found solution: " + stateToString(result.state));
                    System.exit(0);
                } else if (result.type == DFSResultType.MORESTATES) {
                    moreStates = true;
                }
            }
            if (outputVC == 'V') System.out.println();
            if (!moreStates) {
                System.out.println("No solution found.");
                break;
            }
        }
    }

    // stateToString method for printing out a state
    static String stateToString(ArrayList<ArrayList<Vector>> state) {
        String str = "";
        int value = 0;
        for (int c=0; c<state.size(); c++) {
            ArrayList<Vector> cluster = state.get(c);
            str = str + "{";
            for (int v=0; v<cluster.size(); v++) {
                Vector vector = cluster.get(v);
                str = str + vector.getName();
                if (v<cluster.size()-1) str = str + ",";
                value += vector.getValue();
            }
            str = str + "}";
            if (c<state.size()-1) str = str + ",";
        }
        str = str + " Value=" + value;
        return str;
    }

    // DFS method to use in iterative deepening
    static DFSResult DFS(int vector, Vector[] vectors, int numClusters, int target,
        boolean[][] validPairs, int depth, ArrayList<ArrayList<Vector>> state, char outputVC) {

        // check if state is a solution
        int sum = 0;
        for (ArrayList<Vector> cluster : state) {
            for (Vector v : cluster) sum += v.getValue();
        }
        if (outputVC == 'V') System.out.printf("%s\n", stateToString(state));
        if (sum >= target) return new DFSResult(DFSResultType.SOLUTION, state);;

        if (depth == 1) return new DFSResult(DFSResultType.MORESTATES, null);
        boolean moreStates = false;

        int numVectors = vectors.length;
        // iterate through each vector that comes after current vector
        for (int i=vector+1; i<numVectors; i++) {
            boolean added = false;
            // check for each cluster if vector is valid to add
            for (int c=0; c<state.size(); c++) {
                ArrayList<Vector> cluster = state.get(c);
                boolean valid = true;
                // check that each pair in the cluster would be within threshold
                for (int j=0; j<cluster.size(); j++) {
                    Vector v = cluster.get(j);
                    int vNum = v.getNum();
                    if (!validPairs[i][vNum]) {
                        valid = false;
                    }                      
                }
                // if valid add to cluster
                if (valid) {
                    // copy current state
                    ArrayList<ArrayList<Vector>> newState = new ArrayList<>();
                    for (ArrayList<Vector> clusterToCopy : state) {
                        ArrayList<Vector> newCluster = new ArrayList<>(clusterToCopy);
                        newState.add(newCluster);
                    }
                    newState.get(c).add(vectors[i]);
                    // recursive call
                    DFSResult next = DFS(i, vectors, numClusters, target,
                        validPairs, depth-1, newState, outputVC);
                    // check return type
                    if (next.type == DFSResultType.SOLUTION) {
                        return next;
                    } else if (next.type == DFSResultType.MORESTATES) {
                        moreStates = true;
                    }
                }
            }
            // add vector in new cluster if possible
            if (state.size() < numClusters) {
                // copy current state
                ArrayList<ArrayList<Vector>> newState = new ArrayList<>();
                for (ArrayList<Vector> clusterToCopy : state) {
                    ArrayList<Vector> newCluster = new ArrayList<>(clusterToCopy);
                    newState.add(newCluster);
                }
                ArrayList<Vector> newCluster = new ArrayList<Vector>();
                newCluster.add(vectors[i]);
                newState.add(newCluster);
                // recursive call
                DFSResult next = DFS(i, vectors, numClusters, target,
                    validPairs, depth-1, newState, outputVC);
                // check return type
                if (next.type == DFSResultType.SOLUTION) {
                    return next;
                } else if (next.type == DFSResultType.MORESTATES) {
                    moreStates = true;
                }
            }
        }
        if (moreStates)
            return new DFSResult(DFSResultType.MORESTATES, null);
        else
            return new DFSResult(DFSResultType.NOMORESTATES, null);
    }

}