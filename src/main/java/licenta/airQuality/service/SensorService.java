package licenta.airQuality.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import io.netty.util.internal.StringUtil;
import licenta.airQuality.converters.Converter;
import licenta.airQuality.dto.GeoPointDTO;
import licenta.airQuality.dto.SensorDTO;
import licenta.airQuality.entities.Sensor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static java.util.Objects.isNull;

@Slf4j
@Service
public class SensorService {
    private static final String SENSOR_COLLECTION = "sensors";

    private final Converter<SensorDTO, Sensor> entityConverter;
    private final Converter<Sensor, SensorDTO> dtoConverter;

    public SensorService(Converter<SensorDTO, Sensor> entityConverter, Converter<Sensor, SensorDTO> dtoConverter) {
        this.entityConverter = entityConverter;
        this.dtoConverter = dtoConverter;

    }

    public SensorDTO createSensor(SensorDTO sensorDTO) throws ExecutionException, InterruptedException {
        final ZonedDateTime zonedDateTime = ZonedDateTime.now();
        final UUID uuid = UUID.nameUUIDFromBytes(zonedDateTime.toString().getBytes());
        sensorDTO.setUuid(uuid.toString());
        sensorDTO.setCreationDate(LocalDate.now());

        final Sensor sensor = entityConverter.convert(sensorDTO);
        sensor.setCreationDate(Timestamp.now());

        ApiFuture<WriteResult> sensorDocument = getFirestore().collection(SENSOR_COLLECTION)
                .document(sensor.getUuid())
                .create(sensor);

        final String timestamp = sensorDocument.get().getUpdateTime().toString();
        final String logMessage = String.format("[%s] Sensor with uuid: %s was successfully created!", timestamp, sensor.getUuid());
        log.info(logMessage);

        return dtoConverter.convert(sensor);
    }

    public SensorDTO getSensor(String sensorUUID) {
        if(StringUtil.isNullOrEmpty(sensorUUID)) {
            throw new IllegalArgumentException("Sensor UUID is null or empty!");
        }
        ApiFuture<DocumentSnapshot>  apiFuture = getFirestore().collection(SENSOR_COLLECTION)
                .document(sensorUUID)
                .get();
        DocumentSnapshot sensorDocumentSnapshot;
        try {
            sensorDocumentSnapshot = apiFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        final Sensor sensor;
        if (sensorDocumentSnapshot.exists()) {
            sensor = sensorDocumentSnapshot.toObject(Sensor.class);
            return dtoConverter.convert(sensor);
        } else {
            log.warn("Sensor not found!");
            return null;
        }
    }
    public List<SensorDTO> getAllSensors() {
        ApiFuture<QuerySnapshot> queryApiFuture=  getFirestore().collection(SENSOR_COLLECTION)
                .get();
        QuerySnapshot querySnapshot;
        try {
            querySnapshot = queryApiFuture.get();

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        List<SensorDTO> resultList = new ArrayList<>();
        for (QueryDocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
            Sensor sensor = documentSnapshot.toObject(Sensor.class);
            SensorDTO sensorDTO = dtoConverter.convert(sensor);
            resultList.add(sensorDTO);
        }

        log.info("Number of sensors: " + String.valueOf(resultList.size()));
        return resultList;
    }

    public SensorDTO updateSensor(SensorDTO sensorDTO) throws ExecutionException, InterruptedException {
        final Sensor newSensor = entityConverter.convert(sensorDTO);

        final DocumentReference documentReference = getFirestore()
                .collection(SENSOR_COLLECTION)
                .document(sensorDTO.getUuid());

        final ApiFuture<DocumentSnapshot> apiFuture = documentReference.get();
        if(!apiFuture.get().exists()) {
            log.warn(String.format("Sensor with UUID: %s not found!", newSensor.getUuid()));
            return null;
        }

        final Sensor oldSensor = documentReference.get().get().toObject(Sensor.class);
        if(isNull(oldSensor)) {
            log.warn(String.format("Sensor with UUID: %s not found!", newSensor.getUuid()));
            return null;
        }

        newSensor.setCreationDate(oldSensor.getCreationDate());
        newSensor.setLocation(oldSensor.getLocation());
       // newSensor.setLocation(oldSensor.getLocation());
        if (newSensor.getName() == null) {
            newSensor.setName(oldSensor.getName());
        }
        if (newSensor.getActive() == null) {
            newSensor.setActive(oldSensor.getActive());
        }
        if(newSensor.getMeasurementsType() == null) {
            newSensor.setMeasurementsType(oldSensor.getMeasurementsType());
        }

        //if (isNull(newSensor.getLocation().getLatitude()) || isNull(newSensor.getLocation().getLongitude())) {
       //     GeoPoint geoPoint = new GeoPoint(oldSensor.getLocation().getLatitude(), oldSensor.getLocation().getLongitude());
       //     newSensor.setLocation(geoPoint);
      //  }
        final ApiFuture<WriteResult> writeResultApiFuture = documentReference.set(newSensor);

        final String timestamp = writeResultApiFuture.get().getUpdateTime().toString();
        final String logMessage = String.format("[%s] Sensor with uuid: %s was successfully updated!", timestamp, newSensor.getUuid());
        log.info(logMessage);

        return dtoConverter.convert(newSensor);
    }



    public String deleteSensor(final String sensorUUID) throws ExecutionException, InterruptedException {
        if(StringUtil.isNullOrEmpty(sensorUUID)) {
            throw new IllegalArgumentException("Sensor UUID is null or empty!");
        }

        ApiFuture<WriteResult>   apiFuture =  getFirestore()
                .collection(SENSOR_COLLECTION)
                .document(sensorUUID)
                .delete();

        final String result = String.format("[%s] Document with %s has been deleted", apiFuture.get().getUpdateTime(), sensorUUID);
        log.info(result);
        return result;

//        return apiFuture.get().getUpdateTime().toString();

//        DocumentSnapshot sensorDocumentSnapshot;
//        try {
//            sensorDocumentSnapshot = apiFuture.get();
//        } catch (InterruptedException | ExecutionException e) {
//            throw new RuntimeException(e);
//        }

//        if (sensorDocumentSnapshot.exists()) {
//            getFirestore().collection(SENSOR_COLLECTION)
//                    .document(sensorUUID).delete();
//            return "Document with " + sensorUUID + " has been deleted";
//        } else {
//            return "Document with " + sensorUUID + " does not exists.";
//        }
    }

    public Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }
}
