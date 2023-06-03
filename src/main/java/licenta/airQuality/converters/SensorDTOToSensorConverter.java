package licenta.airQuality.converters;

import com.google.cloud.firestore.GeoPoint;
import licenta.airQuality.dto.SensorDTO;
import licenta.airQuality.entities.Sensor;
import org.springframework.stereotype.Service;

import static java.util.Objects.isNull;

@Service
public class SensorDTOToSensorConverter implements Converter<SensorDTO, Sensor> {

    @Override
    public void convert(SensorDTO source, Sensor target) {
        if (isNull(source) || isNull(target)) {
            throw new IllegalArgumentException("Source or target sensor is null!");
        }

        convertInternal(source, target);
    }

    @Override
    public Sensor convert(SensorDTO source) {
        if (isNull(source)) {
            throw new IllegalArgumentException("Source sensor is null!");
        }

        final Sensor target = new Sensor();
        convertInternal(source, target);

        return target;
    }

    private void convertInternal(final SensorDTO source, final Sensor target) {
        target.setUuid(source.getUuid());
        target.setActive(source.getActive());
        target.setName(source.getName());
        target.setMeasurementsType(source.getMeasurementsType());

        final GeoPoint location = new GeoPoint(source.getLocation().getLatitude(), source.getLocation().getLongitude());
        target.setLocation(location);
    }

}
