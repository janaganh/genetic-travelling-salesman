drunken-adventure
=================

A Genetic Algorithm implementation for Travelling sales man problem using structure information of genes.


here is how to run the code:
1. Extract the tsp.zip to folder
2. Use any IDE to open the project 
3. right click class file Tsp.java and run.

the program runs for 26 cities. You can for 42 and 48 cities, but before that you have to do some minor coding changes. for example to run for 42 cities:
1. In the Tsp class ,go loadData method (line no: 126), change the "cities26.txt" to "cities42.txt" and like wise for 48 cities .
2.change costant CITY_NUM to reflect the number of cities, for example for 26 cities it is 26, for 42 its 42 and likewise.

the program writes a log into file called graph.log, this can be used produce live graphs for each generation of population.
for example it can be used with LiveGraph:http://www.live-graph.org/