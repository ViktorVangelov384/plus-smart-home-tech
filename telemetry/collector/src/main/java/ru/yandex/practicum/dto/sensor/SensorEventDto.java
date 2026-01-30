package ru.yandex.practicum.dto.sensor;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.enums.SensorEventType;

import java.time.Instant;
import java.util.Objects;

@Getter
@Setter
@ToString
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ClimateSensorEventDto.class, name = "CLIMATE_SENSOR_EVENT"),
        @JsonSubTypes.Type(value = LightSensorEventDto.class, name = "LIGHT_SENSOR_EVENT"),
        @JsonSubTypes.Type(value = MotionSensorEventDto.class, name = "MOTION_SENSOR_EVENT"),
        @JsonSubTypes.Type(value = SwitchSensorEventDto.class, name = "SWITCH_SENSOR_EVENT"),
        @JsonSubTypes.Type(value = TemperatureSensorEventDto.class, name = "TEMPERATURE_SENSOR_EVENT")
})
public abstract class SensorEventDto {

    @NotBlank(message = "Идентификатор сенсора обязателен")
    private String id;

    @NotBlank(message = "Идентификатор хаба обязателен")
    private String hubId;

    private Instant timestamp = Instant.now();

    @NotNull(message = "Тип события обязателен")
    public abstract SensorEventType getType();

    public boolean belongsToHub(String hubId) {
        return this.hubId != null && this.hubId.equals(hubId);
    }

    public boolean isFromSensor(String sensorId) {
        return this.id != null && this.id.equals(sensorId);
    }

    public boolean isStale(long maxAgeSeconds) {
        if (timestamp == null) {
            return true;
        }
        Instant threshold = Instant.now().minusSeconds(maxAgeSeconds);
        return timestamp.isBefore(threshold);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SensorEventDto that = (SensorEventDto) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(hubId, that.hubId) &&
                Objects.equals(timestamp, that.timestamp) &&
                getType() == that.getType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, hubId, timestamp, getType());
    }
}
