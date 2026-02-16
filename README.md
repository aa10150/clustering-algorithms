## State Space Search Algorithms: Iterative Deepening and Hill Climbing

The input.txt file should contain the following contents:

Line 1: The following, separated by whitespace:
- Dimension of the vector space (number of components of each vector)
- Number of points (vectors) in the dataset
- Diameter threshold
- Number of clusters
- An additional numerical parameter: For Iterative Deepening, the target value. For Hill Climbing, the maximum number of random restarts to allow before declaring failure.
- "V" for verbose output or "C" for compact output

Lines 2 to end: Each line specifies one vector in the dataset with the following, separated by whitespace:
- The name of the vector
- The components of the vector
- (Iterative Deepening only) the value of the vector

### Example Inputs

#### Iterative Deepening:

2 4 1 2 8 V 

a 0 0 4

b 1 0 1

c 2 0 2

d 2 1 3

#### Hill Climbing:

4 12 3 4 25 V

a 1 1 10 2

b 1 2 2 10

c 1 1 10 2

d 1 10 1 1

e 1 11 2 1

f 2 1 2 11

g 2 2 1 11

h 2 2 11 2

i 2 10 2 1

j 10 1 1 1

k 10 2 1 1


l 11 1 1 1

