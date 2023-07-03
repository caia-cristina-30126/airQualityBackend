package licenta.airQuality.controller;

import com.google.cloud.Timestamp;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import licenta.airQuality.auth.TokenValidationFirebase;
import licenta.airQuality.dto.SensorDTO;
import licenta.airQuality.entities.AirQualityIndexWithType;
import licenta.airQuality.entities.User;
import licenta.airQuality.generators.MeasurementsGenerator;
import licenta.airQuality.entities.Measurement;
import licenta.airQuality.service.SensorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import licenta.airQuality.service.FirebaseService;

import java.text.ParseException;
import java.util.ArrayList;
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
    public Response index(@RequestHeader String idToken) {
        try {
            FirebaseToken firebaseToken = TokenValidationFirebase.tokenValidation(idToken);
            String userRole = (String) firebaseToken.getClaims().get("role");
            log.info("userRole" + userRole);
            String userId = firebaseToken.getUid();
            log.info("userID" + userId);
            return Response.builder()
                    .message("Helloooo")
                    .build();
        } catch (FirebaseAuthException e) {

            return Response.builder()
                    .message("Error: " + e.getMessage())
                    .build();
        }
    }

    // users collection
    @PostMapping("/createUser")
    public Response createUser(@RequestBody @Validated User user) throws ExecutionException, InterruptedException, FirebaseAuthException {
        firebaseService.createUser(user);
        return Response.builder()
                .message(String.format("Successfully with email: %s created!", user.getEmail()))
                .build();
    }

    @GetMapping("/getUser")
    public User getUserByEmail(@RequestHeader String email) throws ExecutionException, InterruptedException {
        return firebaseService.getUserByEmail(email);
    }

    @PutMapping("/updateUser")
    public Response updateUser(@RequestBody User user) throws ExecutionException, InterruptedException {
        firebaseService.updateUser(user);
        return Response.builder()
                .message(String.format("Successfully user with email: %s updated!", user.getEmail()))
                .build();

    }
    //sensors collection
    @PostMapping("/createSensor") // admin
    public Response createSensor(@RequestBody @Validated SensorDTO sensor, @RequestHeader String idToken) throws ExecutionException, InterruptedException {
        try {
            TokenValidationFirebase.tokenValidation(idToken);
            final SensorDTO sensorDTO = sensorService.createSensor(sensor);
            return Response.builder()
                    .message(String.format("Successfully with UUID: %s created!", sensorDTO.getUuid()))
                    .build();
        } catch (FirebaseAuthException e) {
            return Response.builder()
                    .message("Error: " + e.getMessage())
                    .build();
        }
    }

    @GetMapping("/sensor/list") // pe harta
    public List<SensorDTO> getSensors(@RequestHeader String idToken)  {
        try {
            TokenValidationFirebase.tokenValidation(idToken);
            return sensorService.getAllSensors();
        } catch (FirebaseAuthException e) {
            return null;
        }
    }

    @GetMapping("/getSensorByUUID") // pagina separata
    public SensorDTO getSensorByUUID(@RequestHeader String UUID) {
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

    @PostMapping("/sensor/createMeasurement") // use this!!
    public String createMeasurementForSpecificSensor(@RequestHeader String sensorUUID, @RequestBody @Validated Measurement measurement) throws ExecutionException, InterruptedException {
        measurement.setInstantTime(Timestamp.now());
        return firebaseService.createMeasurementForSpecificSensor(sensorUUID, measurement);
    }

    //by active field
    @GetMapping("/sensor/measurement/last") // done
    public Measurement getLastMeasurementByTypeIfActive(@RequestHeader String sensorUUID, @RequestHeader String measurementType) throws ExecutionException, InterruptedException {
        return firebaseService.getLastMeasurementByType(sensorUUID, measurementType);
    }
    @GetMapping("/sensor/measurement/latestTimestamp") // done
    public List<Measurement> getLastMeasurementOfLastHour(@RequestHeader String sensorUUID) throws ExecutionException, InterruptedException {
     return firebaseService.getLastMeasurementOfLastHour(sensorUUID);
    }


    @GetMapping("/sensor/measurement/lastHoursMeasurements")
    public List<Measurement> getMeasurementsOfLastWeek(@RequestHeader String sensorUUID, @RequestHeader String measurementType) throws ExecutionException, InterruptedException {
        return firebaseService.getLastMeasurements(sensorUUID, measurementType);
    }

    @GetMapping("/aqi")
    public AirQualityIndexWithType aqi(@RequestHeader String sensorUUID) throws ExecutionException, InterruptedException {
      AirQualityIndexWithType aiq = firebaseService.airQualityIndex(sensorUUID);
      return aiq;
    }


}