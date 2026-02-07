package ru.practicum.hub.emulators;

import com.google.protobuf.Timestamp;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

public abstract class AbstractSensorEmulator implements SensorEventEmulator {

    protected final int generateInRange(int min, int max) {
        validateRange(min, max);
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    protected final Timestamp currentTimestamp() {
        Instant now = Instant.now();
        return Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond())
                .setNanos(now.getNano())
                .build();
    }

    protected final boolean generateRandomBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    private void validateRange(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min > max: " + min + " > " + max);
        }
    }
}
