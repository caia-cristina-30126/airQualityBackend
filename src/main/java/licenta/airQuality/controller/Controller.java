package licenta.airQuality.controller;

import jakarta.validation.Valid;
import licenta.airQuality.dto.SensorDTO;
import licenta.airQuality.entities.AirQualityIndexWithType;
import licenta.airQuality.entities.Sensor;
import licenta.airQuality.generators.MeasurementsGenerator;
import licenta.airQuality.entities.Measurement;
import licenta.airQuality.service.SensorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import licenta.airQuality.service.FirebaseService;

import java.text.ParseException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping("/api")
public class Controller {
    /**
     * GET  /sensor             -> Get sensor by UUID
     * GET  /sensor/list        -> Get all sensors (an extension can be pagination)
     * POST /sensor             -> Save a sensor (sensor data, without UUID. It will be generated in BE and maybe retrieved in response)
     * PUT  /sensor             -> Update a sensor (must contain a UUID of a sensor)
     * ---
     * GET  /sensor/measurement/last        -> Get last measurement of a sensor (must contain the UUID of the sensor)
     * GET  /sensor/measurement/interval    -> Get all measurement of a sensor between startDate and endDate
     * POST /sensor/measurement             -> Save a measurement of a sensor
     */

    private final FirebaseService firebaseService;
    private final SensorService sensorService;

    private final MeasurementsGenerator measurementsGenerator;

    public Controller(FirebaseService firebaseService, SensorService sensorService, MeasurementsGenerator measurementsGenerator) {
        this.firebaseService = firebaseService;
        this.sensorService = sensorService;
        this.measurementsGenerator = measurementsGenerator;
    }

    @GetMapping("/home")
    public Response index() {

        return Response.builder()
                .message("Helloooo")
                .build();
    }

    //sensors collection
    @PostMapping("/createSensor") // admin
    public Response createSensor(@RequestBody @Validated SensorDTO sensor) throws ExecutionException, InterruptedException {

        final SensorDTO sensorDTO = sensorService.createSensor(sensor);

        return Response.builder()
                .message(String.format("Successfully with UUID: %s created!", sensorDTO.getUuid()))
                .build();
    }

    @GetMapping("/sensor/list") // pe harta
    public List<SensorDTO> getSensors() throws ExecutionException, InterruptedException {
        return sensorService.getAllSensors();
    }

    @GetMapping("/getSensorByUUID") // pagina separata
    public SensorDTO getSensorByUUID(@RequestHeader String UUID) throws ExecutionException, InterruptedException {
        return sensorService.getSensor(UUID);
    }

    @PutMapping("/updateSensor")
    public Response updateSensor( @RequestBody SensorDTO sensor) throws ExecutionException, InterruptedException {
        final SensorDTO sensorDTO = sensorService.updateSensor(sensor);

        return Response.builder()
                .message(String.format("Successfully with UUID: %s updated!", sensorDTO.getUuid()))
                .build();

    }

    @DeleteMapping("/deleteSensorByUUID") // admin
    public Response deleteSensorByUUID(@RequestHeader String UUID) throws ExecutionException, InterruptedException {
        String resultMessage = sensorService.deleteSensor(UUID);

        return Response.builder()
                .message(resultMessage)
                .build();
    }

    //measurements collection
    @PostMapping("/generateMeasurements")
    public Response generateMeasurements() {
        measurementsGenerator.generate();

        return Response.builder()
                .message("Success!")
                .build();
    }

    @PostMapping("/sensor/measurement")
    public String createMeasurementForSpecificSensor(@RequestHeader String sensorUUID, @RequestBody @Validated Measurement measurement) throws ExecutionException, InterruptedException {

        return firebaseService.createMeasurementForSpecificSensor(sensorUUID, measurement);
    }

    //by active field
    @GetMapping("/sensor/measurement/last") // done
    public Measurement getLastMeasurementByTypeIfActive(@RequestHeader String sensorUUID, @RequestHeader String measurementType) throws ExecutionException, InterruptedException {
        return firebaseService.getLastMeasurementByType(sensorUUID, measurementType);
    }

    @GetMapping("/sensor/measurement/interval")
    public List<Measurement> getMeasurementsBetweenDates(@RequestHeader String sensorUUID, @RequestHeader String measurementType, @RequestHeader Long startDate, @RequestHeader Long endDate) throws ExecutionException, InterruptedException, ParseException {
        return firebaseService.getMeasurementsBetweenDates(sensorUUID, measurementType, startDate, endDate);
    }

    @GetMapping("/sensor/measurement/lastWeek")
    public List<Measurement> getMeasurementsOfLastWeek(@RequestHeader String sensorUUID, @RequestHeader String measurementType) throws ExecutionException, InterruptedException {
        return firebaseService.getMeasurementsOfLastWeek(sensorUUID, measurementType);
    }

    @GetMapping("/aqi")
    public AirQualityIndexWithType aqi(@RequestHeader String sensorUUID) throws ExecutionException, InterruptedException {
      AirQualityIndexWithType aiq = firebaseService.airQualityIndex(sensorUUID);
      return aiq;
    }


}