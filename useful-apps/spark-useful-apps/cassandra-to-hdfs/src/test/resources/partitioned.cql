CREATE TABLE partitionedtable(
    id varchar,
    name varchar,
    clkey varchar,
    data varchar,
    PRIMARY KEY((id, name), clkey));

INSERT INTO partitionedtable(id, name, clkey, data) values('id01','name01','clkey0111','data0111');
INSERT INTO partitionedtable(id, name, clkey, data) values('id01','name01','clkey0112','data0112');

INSERT INTO partitionedtable(id, name, clkey, data) values('id01','name02','clkey012','data012');
