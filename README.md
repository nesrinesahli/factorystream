# FactoryStream

End-to-end real-time data platform for predictive maintenance on a simulated automotive production line.

FactoryStream streams sensor data (temperature, vibration, pressure) from ~50 factory machines through Kafka, stores it in an open Apache Iceberg lakehouse following the medallion architecture (Bronze → Silver → Gold), and computes industrial KPIs such as OEE, MTBF and MTTR. It also detects anomalies in real time and builds a feature dataset for predictive maintenance models.

The whole stack is 100% open source and runs locally with Docker Compose.

## Tech stack

- **Streaming:** Apache Kafka, Debezium (CDC)
- **Processing:** Apache Spark (PySpark), Java
- **Lakehouse:** Apache Iceberg, MinIO, Trino
- **Observability:** Elasticsearch, Kibana
- **Orchestration & quality:** Airflow, Great Expectations
- **Tooling:** Docker Compose, uv, GitHub Actions

## Status

🚧 Work in progress — building step by step.
