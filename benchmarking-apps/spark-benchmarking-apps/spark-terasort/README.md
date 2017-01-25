#TeraSort benchmark for Spark

This is a Spark-based TeraSort application.
The original application is here: [https://github.com/ehiggs/spark-terasort](https://github.com/ehiggs/spark-terasort).
Thanks to [Ewan Higgs](https://github.com/ehiggs).

# Building

`mvn install`

# Running

`cd` to your your spark install.

## Generate data

    ./bin/spark-submit --class com.github.ehiggs.spark.terasort.TeraGen 
    path/to/spark-terasort/target/spark-terasort-1.0-SNAPSHOT-jar-with-dependencies.jar 
    1g file://$HOME/data/terasort_in 

## Sort the data
    ./bin/spark-submit --class com.github.ehiggs.spark.terasort.TeraSort
    path/to/spark-terasort/target/spark-terasort-1.0-SNAPSHOT-jar-with-dependencies.jar 
    file://$HOME/data/terasort_in file://$HOME/data/terasort_out

## Validate the data
    ./bin/spark-submit --class com.github.ehiggs.spark.terasort.TeraValidate
    path/to/spark-terasort/target/spark-terasort-1.0-SNAPSHOT-jar-with-dependencies.jar 
    file://$HOME/data/terasort_out file://$HOME/data/terasort_validate

