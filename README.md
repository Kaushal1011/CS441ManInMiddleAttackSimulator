# CS441ManInMiddleAttackSimulator


# Random Walk with Man-in-the-Middle Attack Simulator

## Overview
This project simulates random walks on graphs and incorporates a Man-in-the-Middle (MitM) attack simulator 
targeting the nodes within the graph. The graphs have been produced by [Dr Mark Grechanik's]() [NetGameSim]() repository. The project aims to shed light on the 
potential vulnerabilities and robustness of networks against MitM attacks. Utilizing Apache Spark and GraphX, 
the simulator provides insights into how successful or failed attacks 
transpire based on the network's structure and nodes' properties.

## Requirements
- Apache Spark
- GraphX library for Spark
- Typesafe Config library

## Setup

1. **Clone the repository**:
    ```bash
    git clone
   
2. **Build the project**:
    ```bash
    sbt clean compile assembly
    ```
    This will create a jar file in the `target/scala-2.12` directory.
3. **Run Tests**:
    ```bash
    sbt test
    ```
    This will run the tests in the `src/test/scala` directory.

## Usage
1. **Configuration**:
    - Use the `ConfigFactory` to load configurations regarding the simulation settings such as walk length, initial nodes percentage, and simulation iterations.

2. **Execution**:
    ```bash
    # Using spark-submit
    spark-submit --class Main [path_to_jar] <nodeFileOG> <nodeFilePath> <edgeFilePath> <outputFilePath>
    ```

   Arguments:
    - `nodeFileOG`: Original node file path.
    - `nodeFilePath`: Path to the file containing node data.
    - `edgeFilePath`: Path to the file containing edge data.
    - `outputFilePath`: Path where the output stats will be saved.

example run instructions:

```bash
spark-submit --class Main ./target/scala-2.12/CS441HW2MitM-assembly-0.1.0-SNAPSHOT.jar ./input/NodesOut.txt ./input/NodesPerturbedOut.txt ./input/EdgesPerturbedOut.txt ./output/sparkS
```

3. **Output**:
    - The results include the number of successful, failed, misidentified, and uneventful attacks.
    - These stats are saved to the specified output file for further analysis.


## Random Walk Algorithm

1. **Initialization**:
    - A list of `initialNodes` is defined, which represents the starting points of the random walk.
    - For these starting vertices, if they have neighbors, a random neighbor is selected as the initial "next step". For all other vertices or those without neighbors, `Long.MaxValue` is assigned, indicating that they're not currently active in the walk.

![algoInit](./assets/graphInit.png)

2. **Pregel API**:
    - The random walk is executed using the Pregel API, which is designed to perform iterative graph computations. The structure of the Pregel call has three main components:

        - **Vertex Program**: This defines what each vertex does when it receives a message:
            - If the vertex receives a valid message (`newValue`), it updates its value to the received message (which is the ID of the next vertex in the walk). If no valid message is received, the vertex retains its previous value (`oldValue`).

        - **Send Message**: This defines the condition under which messages are sent between vertices and what those messages contain:
            - For each edge triplet (source vertex, edge, destination vertex):
                - If the source vertex has a valid value and its value matches the destination vertex's ID (indicating that the random walk is at this destination vertex), then:
                    - The neighbors of the destination vertex are retrieved.
                    - A random neighbor is chosen from these neighbors.
                    - A message containing the ID of this random neighbor is sent to the destination vertex, indicating where the walk should go next.

        - **Merge Message**: If a vertex receives multiple messages (which shouldn't generally happen in this random walk setup), this function defines how to merge those messages:
            - A random message is chosen among the received messages.

![pregelWalk](./assets/pregel.png)

3. **Iterations**:
    - The above Pregel process is executed iteratively. At each iteration:
        - Active vertices (those participating in the walk) "decide" their next step by choosing a random neighbor.
        - They then "move" to that neighbor in the next iteration.
    - The iterations continue until either a predefined maximum number of iterations (`5` in this code) is reached or no active vertices/messages remain.

In essence, the random walk algorithm starts at certain nodes (`initialNodes`). In each iteration of the Pregel computation, the walk "moves" to a random neighboring vertex. The Pregel API facilitates this iterative process, allowing vertices to communicate their next step and update accordingly.

---

## Man-in-the-Middle (MitM) Attack Algorithm

### Overview

The algorithm simulates the decisions and outcomes of a MitM attack on a node, comparing it against the original graph. The decisions of whether to attack and the outcomes are influenced by a dynamic probability which depends on the history of successful and failed attacks.

### Steps

1. **Configuration Loading**:
   - The necessary configurations are loaded from a file, giving parameters like similarity thresholds and different probabilities for the attack decision.

2. **Attack Function (`attack`)**:
   - Given two nodes (current and original) and historical attack outcomes, this function decides whether to execute an attack and the outcome of the attack.
   - Steps:
      1. Compute the similarity rank between the current node and the node in the original graph.
      2. Check if the similarity exceeds a given threshold. If it does:
         - Determine the attack probability based on the history of attacks:
            - If both successful and failed attacks are zero, use `initCase` probability.
            - If successful attacks equal failed attacks, use `sucEqFail` probability.
            - If successful attacks are greater than failed attacks, use `sucGtFail` probability.
            - Otherwise, use `sucLtFail` probability.
         - Randomly decide if an attack should be launched based on the calculated probability.
         - If an attack is launched:
            - If the node IDs match and the data is valuable, the attack is deemed **successful**.
            - If the node IDs don't match, but the original node data is valuable, the attack is **failed**.
            - If the node IDs don't match and the original node data isn't valuable, the attack is **uneventful**.
            - If the node IDs match but the data isn't valuable, the attack is **misidentified**.

3. **Simulate Attacks on the Original Graph (`attackingOriginalGraph`)**:
   - Given a node and the original graph, this function tries to simulate an attack on each node of the original graph until a successful attack is made.
   - Steps:
      1. Set the initial state: No attack has been made and initialize counters for different attack outcomes.
      2. For each node in the original graph:
         - If an attack has already been made, continue to the next node.
         - Otherwise, use the `attack` function to decide and execute an attack.
         - If an attack is made, propagate the updated state to the next iteration.
      3. Return the final counts of successful, failed, misidentified, and uneventful attacks.

---

This Man-in-the-Middle attack simulation provides insights into the decisions and outcomes of potential attacks on a node by comparing it to an original graph and considering historical attack outcomes.

![mitm](./assets/mitmattack.png)

## Visualization

The repo is accompanied by a submodule capabale of visualising the random walks made during a run. The project produces a logs file in the log directory that log file can be processed by 
the LogViz module accompanied by the project. It will take input the log file and produce log file for each iteration such that it can 
be uploaded and visualised by the [Visualiser](https://random-walk-visualiser-for-cs-441.vercel.app/). Example of the visualisation is shown below.

For initial graph (red nodes are initial nodes, blue are the ones that were walked).

![initial](./assets/initial.png)

This is how the walk progresses 

![walk](./assets/walker.gif)


Some examples of the log files which can be visualised are placed in [Example Vis Logs](./exampleVisLogs) directory. These can be uploaded to
[Visualiser](https://random-walk-visualiser-for-cs-441.vercel.app/) to see the visualisation live.

## Features

1. **Graph Construction**:
   - Parses nodes and edges from input files to create a graph representation.
   - Allows dynamic linking of nodes and their respective edges for flexibility.

2. **Random Walk**:
   - Simulates random walks on the graph, starting from a subset of nodes.
   - The walks are directed based on the neighbors of the current node.

3. **Man-in-the-Middle Attack**:
   - For each node visited during the random walk, the simulator attempts a MitM attack.
   - Tracks successful, failed, misidentified, and uneventful attacks for analysis.

4. **Configurable Parameters**:
   - Enables configuration of the walk length, initial node percentages, and number of iterations for simulation.
   - Provides flexibility for research experiments.

5. **Logging & Output**:
   - Offers comprehensive logging for tracking the flow and results of the simulations.
   - Saves summarized statistics to an output file.
