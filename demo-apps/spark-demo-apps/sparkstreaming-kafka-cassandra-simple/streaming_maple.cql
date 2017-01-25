CREATE KEYSPACE streamingsample WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3 };

USE streamingsample;

CREATE TABLE streaming_sample(
streammsg text,
PRIMARY KEY (streammsg)
);
