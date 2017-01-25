## HOW TO

* write test:
```
$ spark-submit --class com.cisco.dfsio.test.Runner hdfs:///user/$USER/demo-applications/scripts/spark-test-dfsio-with-dependencies.jar --file test.txt --nFiles 10 --fSize 20000000 -m write --log testHdfsIO.log
```

* read test:
```
$ spark-submit --class com.cisco.dfsio.test.Runner hdfs:///user/$USER/demo-applications/scripts/spark-test-dfsio-with-dependencies.jar --file test.txt --nFiles 10 --fSize 20000000 -m read --log testHdfsIO.log
```
