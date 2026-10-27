import os

from pyspark.sql import functions as F

from spark_session import build_spark

KAFKA_BOOTSTRAP_SERVERS = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")
TOPIC = os.getenv("TOPIC", "sensor.raw")
TABLE = os.getenv("TABLE", "lake.bronze.sensor_events")
CHECKPOINT_DIR = os.getenv("CHECKPOINT_DIR", "checkpoints/bronze_sensor")


def main() -> None:
    spark = build_spark("bronze-sensor-events")

    spark.sql("CREATE NAMESPACE IF NOT EXISTS lake.bronze")
    spark.sql(f"""
        CREATE TABLE IF NOT EXISTS {TABLE} (
            kafka_key        STRING,
            kafka_value      STRING,
            kafka_topic      STRING,
            kafka_partition  INT,
            kafka_offset     BIGINT,
            kafka_timestamp  TIMESTAMP,
            ingested_at      TIMESTAMP
        )
        USING iceberg
        PARTITIONED BY (hours(kafka_timestamp))
    """)

    raw = (
        spark.readStream.format("kafka")
        .option("kafka.bootstrap.servers", KAFKA_BOOTSTRAP_SERVERS)
        .option("subscribe", TOPIC)
        .option("startingOffsets", "earliest")
        .load()
    )

    bronze = raw.select(
        F.col("key").cast("string").alias("kafka_key"),
        F.col("value").cast("string").alias("kafka_value"),
        F.col("topic").alias("kafka_topic"),
        F.col("partition").alias("kafka_partition"),
        F.col("offset").alias("kafka_offset"),
        F.col("timestamp").alias("kafka_timestamp"),
        F.current_timestamp().alias("ingested_at"),
    )

    query = (
        bronze.writeStream.format("iceberg")
        .outputMode("append")
        .option("checkpointLocation", CHECKPOINT_DIR)
        .trigger(processingTime="10 seconds")
        .toTable(TABLE)
    )
    query.awaitTermination()


if __name__ == "__main__":
    main()
