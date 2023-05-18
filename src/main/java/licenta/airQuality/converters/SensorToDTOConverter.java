package licenta.airQuality.converters;

import com.google.cloud.Timestamp;
import licenta.airQuality.dto.GeoPointDTO;
import licenta.airQuality.dto.SensorDTO;
import licenta.airQuality.entities.Sensor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static java.time.Instant.ofEpochSecond;
import static java.time.LocalDate.ofInstant;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class SensorToDTOConverter implements Converter<Sensor, SensorDTO> {
    @Override
    public void convert(final Sensor source, final SensorDTO target) {
        if (isNull(source) || isNull(target)) {
            throw new IllegalArgumentException("Source or target sensor is null!");
        }

        convertInternal(source, target);
    }

    @Override
    public SensorDTO convert(final Sensor source) {
        if (isNull(source)) {
            throw new IllegalArgumentException("Source sensor is null!");
        }

        final SensorDTO target = new SensorDTO();
        convertInternal(source, target);

        return target;
    }

    private void convertInternal(final Sensor source, final SensorDTO target) {
        target.setUuid(source.getUuid());
        target.setActive(source.getActive());
        target.setName(source.getName());

        if(nonNull(source.getLocation())) {
            final GeoPointDTO location = new GeoPointDTO(source.getLocation().getLatitude(), source.getLocation().getLongitude());
            target.setLocation(location);
        }

        if(nonNull(source.getCreationDate())) {
            final LocalDate creationDate = convertTimestamp(source.getCreationDate());
            target.setCreationDate(creationDate);
        }
    }

    public static LocalDate convertTimestamp(Timestamp timestamp) {
        final Instant instant = ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
        return ofInstant(instant, ZoneId.systemDefault());
    }
}
