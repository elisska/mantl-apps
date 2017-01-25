File Aggregator
===============

### Description
*Merge small text files into large files.*

File Aggregator is a Spark job application which allows to merge small text files in order to resolve [Hadoop small files problem](http://blog.cloudera.com/blog/2009/02/the-small-files-problem/) . Input file contents are being copied into output files and delimited by a linebreak(default). 

### Assembly
Package with Maven.  

```
mvn package
```

### Configure
Application can be configured from command line providing parameters

* **-i** - is a required property, specify it to point out directory URI small files to be read and aggregated into combined files, example:
```
-i hdfs://localhost/user/examples/files
```
* **-o** - is a required property, specify it to point out directory URI to upload combined files to, **NOTE:** directory should not exist, it will be created as part of the job, example:
```
-o hdfs://localhost/user/examples/files-out
```
* -m - is a optional property, specify it to point out spark master URI, by default will be governed by --master option of spark-submit command which would be required in case not providing it to File Aggregator application, example: 
```
-m spark://quickstart.cloudera:7077
```
* -n - is a optional property, Application display name, default: File Aggregator , example:
```
-n "File Agg"
```

* -f - is an optional property, Max size of single output file in Mb, default: 128 , example:
```
-f 64
```
* -b - is an optional property, HDFS file block size in Mb, default: governed by dfs.blocksize Hadoop option , example:
```
-b 64
```
* -d - is an optional property, delimiter to separate content from small input files in combined files, default: "linebreak" , example:
```
-d %
```
* -r - is an optional property, enables recursive file reads in nested input directories, default: true , example:
```
-r false
```
* *--help* - can be used to view usage options from command line.

### Run
Run application by submitting it to Spark via command line, providing mandatory parameters, example:

``` 
spark-submit --class "com.cisco.mantl.aggregator.AggDriver" --master local jars/file-aggregator-with-dependencies.jar -i /inDir -o /outDir -n "File Agg" -f 64 -b 64 -r false
```
