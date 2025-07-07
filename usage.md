# Spark HDFS MySQL Docker Setup - Complete Workflow

## 1. Start the Services

First, make sure you're in the project directory and start all services:

```bash
cd /home/krishtal/Desktop/Dev/incremental_loading
docker-compose up -d
```

Wait for all services to be healthy (about 2-3 minutes):

```bash
docker-compose ps
```

## 2. Verify Services are Running

Check that all containers are running:

```bash
docker ps
```

You should see:

- mysql-container (MySQL database)
- mysql-namenode (Hadoop NameNode)
- mysql-datanode (Hadoop DataNode)
- mysql-spark-master (Spark Master)
- spark-worker-1 (Spark Worker 1)
- spark-worker-2 (Spark Worker 2)
- spark-history-server (Spark History Server)

## 3. Access Web UIs

- **Hadoop NameNode UI**: http://localhost:9870
- **Spark Master UI**: http://localhost:8080
- **Spark Worker 1 UI**: http://localhost:8081
- **Spark Worker 2 UI**: http://localhost:8082
- **Spark History Server UI**: http://localhost:18081

## 4. Setup MySQL Database (Optional)

Connect to MySQL and create a sample database:

```bash
docker exec -it mysql-container mysql -u root -p
```

In MySQL prompt (password: admin):

```sql
CREATE DATABASE my_database;
USE my_database;

CREATE TABLE employees (
    id INT PRIMARY KEY,
    name VARCHAR(100),
    age INT,
    department VARCHAR(50)
);

INSERT INTO employees VALUES
(1, 'John Doe', 25, 'Engineering'),
(2, 'Jane Smith', 30, 'Marketing'),
(3, 'Bob Johnson', 35, 'Sales'),
(4, 'Alice Brown', 28, 'HR');

EXIT;
```

## 5. Download MySQL Connector (Required for MySQL integration)

The MySQL connector JAR is automatically downloaded to the correct location when you first run the setup. If you need to manually download it:

```bash
docker exec -it mysql-spark-master wget -O /spark/jars/mysql-connector-j-8.0.33.jar https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar
```

## 6. Submit Spark Jobs

### Method 1: Using the submission script (Recommended)

```bash
# Make script executable (first time only)
chmod +x submit-spark-job.sh

# Submit a simple test job
./submit-spark-job.sh simple-test.py

# Submit the MySQL-HDFS integration job
./submit-spark-job.sh spark-mysql-hdfs-example.py
```

### Method 2: Direct spark-submit command

```bash
docker exec -it mysql-spark-master /spark/bin/spark-submit \
 --master spark://mysql-spark-master:7077 \
 --deploy-mode client \
 --driver-memory 1g \
 --executor-memory 1g \
 --executor-cores 1 \
 --num-executors 1 \
 --conf spark.sql.adaptive.enabled=true \
 --conf spark.sql.adaptive.coalescePartitions.enabled=true \
 --jars /spark/jars/mysql-connector-j-8.0.33.jar \
 /opt/spark-apps/simple-test.py
```

## 7. Monitor Jobs

- Check Spark Master UI at http://localhost:8080 for job status and cluster health
- Check Hadoop NameNode UI at http://localhost:9870 for HDFS files and cluster status
- View running and completed applications in the Spark UI
- Monitor resource usage across workers

## 8. Access HDFS

List files in HDFS root:

```bash
docker exec -it mysql-namenode hdfs dfs -ls /
```

View Spark output in HDFS:

```bash
docker exec -it mysql-namenode hdfs dfs -ls /test-output/
docker exec -it mysql-namenode hdfs dfs -ls /spark-output/
```

Read a file from HDFS:

```bash
docker exec -it mysql-namenode hdfs dfs -cat /test-output/part-00000-*.parquet
```

## 9. Interactive Spark Shell

Start PySpark shell with MySQL connector:

```bash
docker exec -it mysql-spark-master /spark/bin/pyspark \
 --master spark://mysql-spark-master:7077 \
 --jars /spark/jars/mysql-connector-j-8.0.33.jar
```

## 10. Development Workflow

1. **Write your Spark application** in the `spark-apps/` directory
2. **Test locally** using the simple test first: `./submit-spark-job.sh simple-test.py`
3. **Submit your job**: `./submit-spark-job.sh your-app.py`
4. **Monitor progress** via Spark UI (http://localhost:8080)
5. **Check results** in HDFS via NameNode UI (http://localhost:9870)

## 11. Shutdown

Stop all services:

```bash
docker-compose down
```

Stop and remove all data:

```bash
docker-compose down -v
```

## Troubleshooting

### Error: "spark-submit: executable file not found"

**Solution**: The script now uses the full path `/spark/bin/spark-submit`. If you see this error, update your commands to use the full path.

### Error: "container is not running"

**Solution**: Check container names and status:

```bash
docker ps
docker-compose ps
```

### If containers are not starting:

```bash
docker-compose down
docker-compose up -d
docker-compose logs [service-name]
```

### If Spark jobs fail:

1. Check if MySQL connector JAR is present: `docker exec -it mysql-spark-master ls -la /spark/jars/`
2. Verify all containers are running: `docker ps`
3. Check Spark Master UI for error details: http://localhost:8080
4. View container logs: `docker-compose logs mysql-spark-master`

### If HDFS is not accessible:

```bash
docker exec -it mysql-namenode hdfs namenode -format
docker-compose restart mysql-namenode mysql-datanode
```

### If MySQL connection fails:

1. Ensure MySQL container is running: `docker ps | grep mysql`
2. Test MySQL connection: `docker exec -it mysql-container mysql -u root -p`
3. Check if database exists: `SHOW DATABASES;` in MySQL prompt

## Example Applications

The repository includes:

- `simple-test.py`: Basic Spark + HDFS test
- `spark-mysql-hdfs-example.py`: Complete MySQL to HDFS integration example

Both applications demonstrate different aspects of the stack and can be used as templates for your own applications.
