# CS441ManInMiddleAttackSimulator


# Random Walk with Man-in-the-Middle Attack Simulator

## Overview
This project simulates random walks on graphs and incorporates a Man-in-the-Middle (MitM) attack simulator 
targeting the nodes within the graph. The graphs have been produced by [Dr Mark Grechanik's](https://github.com/0x1DOCD00D) [NetGameSim](https://github.com/0x1DOCD00D/NetGameSim) repository. The project aims to shed light on the 
potential vulnerabilities and robustness of networks against MitM attacks. Utilizing Apache Spark and GraphX, the simulator provides insights into how successful or failed attacks 
transpire based on the network's structure and nodes' properties.

## [Algorithms Overview](./ALGORITHMS.md)

The linked file contains a detailed overview of the algorithms used in this project for:

1. Random Walk
2. MitM Attack

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
