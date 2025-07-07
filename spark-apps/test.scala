import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import java.util.Properties
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val spark = SparkSession.builder()
  .appName("Incremental Loading MySQL to HDFS")
  .master("spark://mysql-spark-master:7077")
  .config("spark.sql.adaptive.enabled", "true")
  .config("spark.sql.adaptive.coalescePartitions.enabled", "true")
  .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
  .getOrCreate()

import spark.implicits._

println("Reading data from MySQL...")
val product_df = spark.read
  .format("jdbc")
  .option("driver", "com.mysql.cj.jdbc.Driver")
  .option("url", "jdbc:mysql://mysql:3306/txn")
  .option("dbtable", "product")
  .option("user", "root")
  .option("password", "admin")
  .load()

println("Product data from MySQL:")
product_df.show()

println("Schema:")
product_df.printSchema()

    
    
