package licenta.airQuality.generators;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.GeoPoint;
import licenta.airQuality.constants.Location;
import licenta.airQuality.constants.MeasurementUnit;

import licenta.airQuality.dto.GeoPointDTO;
import licenta.airQuality.dto.SensorDTO;
import licenta.airQuality.entities.Measurement;
import licenta.airQuality.entities.Sensor;
import licenta.airQuality.service.FirebaseService;
import licenta.airQuality.service.SensorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class MeasurementsGenerator {
    final static long MILLIS_IN_DAY = 60 * 60 * 24 * 1000;
    final static Double RANGE_MIN_CELSIUS = -10.0;
    final static Double RANGE_MAX_CELSIUS = 40.0;

    final static Double RANGE_MIN_HUMIDITY = 20.0;
    final static Double RANGE_MAX_HUMIDITY = 70.0;

    final static Double RANGE_MIN_NO2 = 0.0;
    final static Double RANGE_MAX_NO2 = 180.85;

    final static Double RANGE_MIN_PM10 = 0.0;
    final static Double RANGE_MAX_PM10 = 150.0;

    final static Double RANGE_MIN_O3 = 0.0;
    final static Double RANGE_MAX_O3 = 150.0;
    private final FirebaseService firebaseService;

    private final SensorService sensorService;

    public MeasurementsGenerator(FirebaseService firebaseService, SensorService sensorService) {
        this.firebaseService = firebaseService;
        this.sensorService = sensorService;
    }

    public void generate() {
        log.info("Start generating measurements");

        final GeoPointDTO geoPoint = new GeoPointDTO(Location.ZORILOR_CJ_RO.getLatitude(), Location.ZORILOR_CJ_RO.getLongitude());
        final String uuid = UUID.randomUUID().toString();
        final List<String> measurementsType = new ArrayList<>();
        measurementsType.add("NO2");
        measurementsType.add("O2");
        measurementsType.add("temp");
        final SensorDTO sensor = new SensorDTO(true, uuid, geoPoint, "ZORILOR_CJ_RO", LocalDate.now(), measurementsType);
        try {
            sensorService.createSensor(sensor);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        //temp, humidity, nitrogen dioxide, particulate matter 10, pm2.5
        final Instant now = Instant.now();
        final String tempType = "temp";
        final String humidityType = "humidity";


        final String O3Type= "O3";
        // final Double offset = 1.1;
        for(int i = 0; i< 10; i++) {
            Instant temporaryInstant = now.minusMillis(i * MILLIS_IN_DAY);
            final Random r = new Random(temporaryInstant.toEpochMilli());
            final Timestamp timestamp = Timestamp.ofTimeSecondsAndNanos(temporaryInstant.getEpochSecond(), temporaryInstant.getNano());
            double tempValue = r.nextDouble(RANGE_MIN_CELSIUS, RANGE_MAX_CELSIUS);
            double humidityValue = r.nextDouble(RANGE_MIN_HUMIDITY, RANGE_MAX_HUMIDITY);
            double O3Value = r.nextDouble(RANGE_MIN_O3, RANGE_MAX_O3);
            final Measurement measurementTemp = new Measurement(MeasurementUnit.CELSIUS_DEGREE, tempValue, timestamp, tempType);
            final Measurement measurementHumidity = new Measurement(MeasurementUnit.PERCENT, humidityValue, timestamp, humidityType);
            final Measurement measurementO3   = new Measurement(MeasurementUnit.PARTS_PER_BILLION, O3Value, timestamp, O3Type);
            try {
                firebaseService.createMeasurementForSpecificSensor(sensor.getUuid(), measurementTemp);
                firebaseService.createMeasurementForSpecificSensor(sensor.getUuid(), measurementHumidity);
                firebaseService.createMeasurementForSpecificSensor(sensor.getUuid(), measurementO3);
                log.info("Measurement successfully saved" );
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        log.info("Finish generating measurements");
    }
}
