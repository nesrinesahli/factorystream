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

## Getting started

Requirements: Docker, Java 21 (JDK), Maven.

```bash
# Set credentials (edit the values)
cp .env.example .env

# Start Kafka, MinIO, the Iceberg REST catalog and Trino
docker compose up -d

# Build and run the IoT simulator
cd producer-java
mvn package
java -jar target/producer-java-0.1.0.jar
```

| Service | URL | Role |
|---|---|---|
| Kafka | `localhost:9092` | Event streaming |
| MinIO console | http://localhost:9001 | S3-compatible object storage |
| Iceberg REST catalog | http://localhost:8181 | Table metadata catalog |
| Trino | http://localhost:8080 | SQL query engine |

## IoT producer (`producer-java/`)

Simulates a fleet of factory machines (presses, welding robots, paint booths) spread over 3 production lines. Each machine sends one JSON reading per tick to the Kafka topic `sensor.raw`, keyed by `machine_id` so each machine's events stay ordered within a partition.

Readings follow a Gaussian distribution around per-type baselines, with injected anomalies:

- **Drift**: temperature and vibration rise gradually, then the machine breaks down (`E01`)
- **Spike**: one-off vibration peak
- **Breakdown**: sudden stop (`E02`), with `cadence = 0` until repaired

Example event:

```json
{"event_id":"6cd436a4-...","machine_id":"M001","line_id":"L1","machine_type":"PRESS","event_time":1792945994250,"temperature":72.4,"vibration":2.1,"pressure":150.3,"cadence":20,"error_code":null}
```

Configuration (environment variables):

| Variable | Default | Description |
|---|---|---|
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka brokers |
| `TOPIC` | `sensor.raw` | Target topic |
| `MACHINES` | `50` | Number of simulated machines |
| `TICKS_PER_SECOND` | `1` | Readings per machine per second |
| `ANOMALY_RATE` | `0.002` | Probability per tick that an anomaly starts |
| `SEED` | random | Fixed seed for reproducible runs |

Sustains **1,000 events/s** locally (`TICKS_PER_SECOND=20`) with an idempotent producer (`acks=all`).

Known limitation: anomaly durations are counted in ticks, so they shorten as `TICKS_PER_SECOND` increases.

## Lakehouse (MinIO + Iceberg + Trino)

Tables are stored as Parquet files in the MinIO bucket `warehouse`, managed by Apache Iceberg through a REST catalog, and queried with Trino.

```bash
docker exec -it trino trino
```

```sql
-- Query a table as it was at an earlier point in time (Iceberg time travel)
SELECT count(*) FROM iceberg.demo.readings
FOR TIMESTAMP AS OF TIMESTAMP '2026-10-26 18:08:20 UTC';

-- Inspect the table's snapshot history
SELECT snapshot_id, committed_at, operation
FROM iceberg.demo."readings$snapshots";
```

## Status

🚧 Work in progress — building step by step.
