package com.factorystream.producer;

public record Config(
        String bootstrapServers,
        String topic,
        int machines,
        double ticksPerSecond,
        double anomalyRate,
        Long seed
) {
    public Config {
        if (machines <= 0) {
            throw new IllegalArgumentException("MACHINES must be > 0, got " + machines);
        }
        if (ticksPerSecond <= 0) {
            throw new IllegalArgumentException("TICKS_PER_SECOND must be > 0, got " + ticksPerSecond);
        }
        // Machine rolls up to 3.2 x anomalyRate, which must stay a valid probability
        if (anomalyRate < 0 || anomalyRate > 0.3) {
            throw new IllegalArgumentException("ANOMALY_RATE must be between 0 and 0.3, got " + anomalyRate);
        }
    }

    public static Config fromEnv() {
        String seed = env("SEED", null);
        return new Config(
                env("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"),
                env("TOPIC", "sensor.raw"),
                Integer.parseInt(env("MACHINES", "50")),
                Double.parseDouble(env("TICKS_PER_SECOND", "1")),
                Double.parseDouble(env("ANOMALY_RATE", "0.002")),
                seed == null ? null : Long.parseLong(seed)
        );
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
