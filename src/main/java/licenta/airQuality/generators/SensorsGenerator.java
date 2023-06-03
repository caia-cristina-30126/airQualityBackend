package licenta.airQuality.generators;

import licenta.airQuality.dto.GeoPointDTO;
import licenta.airQuality.dto.SensorDTO;
import licenta.airQuality.service.SensorService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


@Service
public class SensorsGenerator {

    private final SensorService sensorService;

    public SensorsGenerator(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    public SensorDTO generate(final Double locationLat,
                              final Double locationLong,
                              final String sensorName,
                              final Boolean isActive) {

        final GeoPointDTO geoPoint = new GeoPointDTO(locationLat, locationLong);
        final List<String> measurementsType = new ArrayList<>();
        measurementsType.add("NO2");
        measurementsType.add("O2");
        measurementsType.add("temp");
        final SensorDTO sensor = new SensorDTO(isActive, null, geoPoint, sensorName, LocalDate.now(), measurementsType);

        try {
            sensorService.createSensor(sensor);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return sensor;
    }

}

