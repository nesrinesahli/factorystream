import os

from pyspark.sql import SparkSession

SPARK_VERSION = "4.0.1"
ICEBERG_VERSION = "1.10.1"

PACKAGES = [
    f"org.apache.spark:spark-sql-kafka-0-10_2.13:{SPARK_VERSION}",
    f"org.apache.iceberg:iceberg-spark-runtime-4.0_2.13:{ICEBERG_VERSION}",
    f"org.apache.iceberg:iceberg-aws-bundle:{ICEBERG_VERSION}",
]


def build_spark(app_name: str) -> SparkSession:
    """Local Spark session wired to the Iceberg REST catalog (as `lake`) and MinIO."""
    catalog_uri = os.getenv("ICEBERG_CATALOG_URI", "http://localhost:8181")
    s3_endpoint = os.getenv("S3_ENDPOINT", "http://localhost:9000")

    spark = (
        SparkSession.builder.appName(app_name)
        .master("local[2]")
        .config("spark.jars.packages", ",".join(PACKAGES))
        .config("spark.sql.extensions", "org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions")
        .config("spark.sql.session.timeZone", "UTC")
        # Iceberg catalog named "lake", backed by the REST catalog
        .config("spark.sql.catalog.lake", "org.apache.iceberg.spark.SparkCatalog")
        .config("spark.sql.catalog.lake.type", "rest")
        .config("spark.sql.catalog.lake.uri", catalog_uri)
        # Data files are read and written directly in MinIO
        .config("spark.sql.catalog.lake.io-impl", "org.apache.iceberg.aws.s3.S3FileIO")
        .config("spark.sql.catalog.lake.s3.endpoint", s3_endpoint)
        .config("spark.sql.catalog.lake.s3.path-style-access", "true")
        .config("spark.sql.catalog.lake.s3.access-key-id", os.environ["MINIO_ROOT_USER"])
        .config("spark.sql.catalog.lake.s3.secret-access-key", os.environ["MINIO_ROOT_PASSWORD"])
        .config("spark.sql.catalog.lake.client.region", "us-east-1")
        .getOrCreate()
    )
    spark.sparkContext.setLogLevel("WARN")
    return spark


if __name__ == "__main__":
    # Smoke test: read the table Trino created in Step 3
    spark = build_spark("smoke-test")
    spark.sql("SHOW NAMESPACES IN lake").show()
    # Trino writes Parquet encodings that Spark's vectorized reader can't decode
    readings = spark.read.option("vectorization-enabled", "false").table("lake.demo.readings")
    readings.orderBy("event_time").show()
    spark.stop()
