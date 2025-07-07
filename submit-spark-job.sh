#!/bin/bash

SCALA_FILE=${1:-"test.scala"}

echo "=== Submitting Spark Scala Job ==="
echo "Scala file: $SCALA_FILE"
echo "=================================="

if [ ! -f "$SCALA_FILE" ]; then
    echo "Error: Scala file '$SCALA_FILE' not found!"
    exit 1
fi

echo "Copying Scala file to Spark master container..."
docker cp "$SCALA_FILE" mysql-spark-master:/opt/spark-apps/

echo "Running Spark Scala application..."
docker exec mysql-spark-master bash -c "
    export JAVA_HOME=/usr/lib/jvm/java-8-openjdk
    export SPARK_HOME=/spark
    cd /opt/spark-apps
    
    /spark/bin/spark-shell \
    --master spark://mysql-spark-master:7077 \
    --packages com.mysql:mysql-connector-j:8.0.33 \
    --conf spark.sql.warehouse.dir=/opt/spark-data \
    -i $SCALA_FILE
"

echo "=== Job submission completed ==="