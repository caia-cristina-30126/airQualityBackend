package licenta.airQuality.service;

import com.google.api.core.ApiFuture;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.GenericTypeIndicator;
import io.netty.util.internal.StringUtil;
import licenta.airQuality.auth.TokenValidationFirebase;
import licenta.airQuality.dto.SensorDTO;
import licenta.airQuality.entities.AirQualityIndexWithType;
import licenta.airQuality.entities.Measurement;
import licenta.airQuality.entities.Sensor;
import licenta.airQuality.entities.User;
import licenta.airQuality.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.text.ParseException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static com.google.firebase.cloud.FirestoreClient.getFirestore;
import static java.util.Objects.isNull;


@Slf4j
@Service
public class FirebaseService {
    private static final String DASH = "-";
    private final String COLLECTION_MEASUREMENTS_NAME = "measurements";

    private CollectionReference sensorsCollectionRef() {
        return getFirestore().collection("sensors");
    }

    private DocumentReference sensorDocumentRef(final String documentId) {
        return sensorsCollectionRef().document(documentId);
    }

    private ApiFuture<DocumentSnapshot> getDocumentSnapshot(String sensorUUID) {
        return  sensorDocumentRef(sensorUUID).get();
    }
    DocumentSnapshot sensorDocumentSnapshot(String sensorUUID) throws ExecutionException, InterruptedException {
       return getDocumentSnapshot(sensorUUID).get();
    }

    // users collection
    public User createUser(User user) throws FirebaseAuthException {
        final ZonedDateTime zonedDateTime = ZonedDateTime.now();
        final UUID uuid = UUID.nameUUIDFromBytes(zonedDateTime.toString().getBytes());
        user.setUuid(uuid.toString());
        getFirestore().collection("users")
                .document(user.getUuid())
                .create(user);

        return user;
    }

    public User getUserByUUID(String userUUID) {
        if(StringUtil.isNullOrEmpty(userUUID)) {
            throw new IllegalArgumentException("User UUID is null or empty!");
        }
        ApiFuture<DocumentSnapshot>  apiFuture = getFirestore().collection("users")
                .document(userUUID)
                .get();
        DocumentSnapshot userDocumentSnapshot;
        try {
            userDocumentSnapshot = apiFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        final User user;
        if(userDocumentSnapshot.exists()) {
            user = userDocumentSnapshot.toObject(User.class);
            return user;
        } else {
            log.warn("User not found!");
            return null;
        }
    }

    public User updateUser(User user) throws ExecutionException, InterruptedException {


        final DocumentReference documentReference = getFirestore()
                .collection("users")
                .document(user.getUuid());

        final ApiFuture<DocumentSnapshot> apiFuture = documentReference.get();
        if(!apiFuture.get().exists()) {
            log.warn(String.format("User with UUID: %s not found!", user.getUuid()));
            return null;
        }

        final User oldUser = documentReference.get().get().toObject(User.class);
        if(isNull(oldUser)) {
            log.warn(String.format("Sensor with UUID: %s not found!", user.getUuid()));
            return null;
        }

        user.setRole(oldUser.getRole());
       user.setEmail(oldUser.getEmail());

        if (user.getFirstName() == null) {
            user.setFirstName(oldUser.getFirstName());
        }
        if (user.getLastName() == null) {
            user.setLastName(oldUser.getLastName());
        }

        final ApiFuture<WriteResult> writeResultApiFuture = documentReference.set(user);

        final String timestamp = writeResultApiFuture.get().getUpdateTime().toString();
        final String logMessage = String.format("[%s] User with uuid: %s was successfully updated!", timestamp, user.getUuid());
        log.info(logMessage);

        return user;
    }

    //measurements collection
    public String createMeasurementForSpecificSensor(String sensorUUID, Measurement measurement) throws ExecutionException, InterruptedException {
        final String documentMeasurementName = measurement.getType() + DASH + measurement.getInstantTime().getSeconds();

        log.info("Saving measurement" + measurement);
        DocumentSnapshot sensorDocumentSnapshot = sensorDocumentRef(sensorUUID).get().get();
        if(!sensorDocumentSnapshot.exists()) {
            log.warn(String.format("Sensor with UUID: %s not found!", sensorUUID));
            return null;
        }


            List<String> measurementsType = (List<String>) sensorDocumentSnapshot.get("measurementsType");
        if(! measurementsType.contains(measurement.getType())) {
            log.warn(String.format("Measurement type: %s not found in Sensor measurements type!", measurement.getType()));
            return null; }


                ApiFuture<WriteResult> collectionApiFuture = sensorDocumentRef(sensorUUID)
                        .collection(COLLECTION_MEASUREMENTS_NAME)
                        .document(documentMeasurementName)
                        .create(measurement);

                return collectionApiFuture.get()
                        .getUpdateTime()
                        .toString();  // when the collection is being created
    }

    public Measurement getLastMeasurementByType(String sensorUUID, String measurementType) throws ExecutionException, InterruptedException {

        DocumentSnapshot sensorDocumentSnapshot = sensorDocumentSnapshot(sensorUUID);

        Measurement measurement;

        if (sensorDocumentSnapshot.exists() && sensorDocumentSnapshot.getBoolean("active")) {
            CollectionReference measurementsCollectionRef = sensorDocumentRef(sensorUUID)
                    .collection(COLLECTION_MEASUREMENTS_NAME);

           Query query = measurementsCollectionRef.whereEqualTo("type", measurementType)
                    .orderBy("instantTime", Query.Direction.DESCENDING)
                    .limit(1);

            if (!query.get().get().getDocuments().isEmpty()) {
                 measurement = query.get().get().getDocuments().get(0).toObject(Measurement.class);
    double convertedValue = CommonUtils.convertPpbToMicrograms(measurement);
    measurement.setValue(convertedValue);
                return measurement;
            } else {
                log.info("No measurements found for type " + measurementType);
                return null;
            }
        }
        else {
            log.info("Sensor is inactive!");
            return null;
        }
    }

    public List<Measurement> getMeasurementsBetweenDates(String sensorUUID, String measurementType, Long startDate, Long endDate) throws ExecutionException, InterruptedException, ParseException {

        DocumentSnapshot sensorDocumentSnapshot = getDocumentSnapshot(sensorUUID).get();


        if (sensorDocumentSnapshot.exists() && sensorDocumentSnapshot.getBoolean("active")) {
            CollectionReference measurementsCollectionRef = sensorDocumentRef(sensorUUID).collection(COLLECTION_MEASUREMENTS_NAME);
            Instant instantStartDate = Instant.ofEpochSecond(startDate);
            Instant instantEndDate = Instant.ofEpochSecond(endDate);

            Timestamp timestampStartDate = Timestamp.ofTimeSecondsAndNanos(instantStartDate.getEpochSecond(), instantStartDate.getNano());
            Timestamp timestampEndDate = Timestamp.ofTimeSecondsAndNanos(instantEndDate.getEpochSecond()+1, instantEndDate.getNano() );

            Query query = measurementsCollectionRef.whereEqualTo("type", measurementType)
                 .whereGreaterThanOrEqualTo("instantTime",timestampStartDate)
                  .whereLessThanOrEqualTo("instantTime", timestampEndDate);

            QuerySnapshot querySnapshot = query.get().get();
            List<Measurement> measurementsBetweenDatesList = new ArrayList<>();
            for (QueryDocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                Measurement measurement = documentSnapshot.toObject(Measurement.class);

                measurementsBetweenDatesList.add(measurement);
            }
            log.info(measurementsBetweenDatesList.toString());
            return measurementsBetweenDatesList;

        }
        else {
            log.info("Sensor is inactive!");
            return null;
        }
    }

    public List<Measurement> getMeasurementsOfLastWeek(String sensorUUID, String measurementType) throws ExecutionException, InterruptedException {

        DocumentSnapshot sensorDocumentSnapshot = getDocumentSnapshot(sensorUUID).get();

        if (sensorDocumentSnapshot.exists() && sensorDocumentSnapshot.getBoolean("active")) {
            CollectionReference measurementsCollectionRef = sensorDocumentRef(sensorUUID).collection(COLLECTION_MEASUREMENTS_NAME);

            Query query = measurementsCollectionRef
                    .whereEqualTo("type", measurementType)
                    .orderBy("instantTime", Query.Direction.DESCENDING)
                    .limit(7);

            QuerySnapshot querySnapshot = query.get().get();

            List<Measurement> lastWeekMeasurementsList = new ArrayList<>();
            for (QueryDocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                Measurement measurement = documentSnapshot.toObject(Measurement.class);
              double convertedValue =  CommonUtils.convertPpbToMicrograms(measurement);
                measurement.setValue(convertedValue);
                lastWeekMeasurementsList.add(measurement);
            }
            log.info(lastWeekMeasurementsList.toString());
            return lastWeekMeasurementsList;

        }
        else {
            log.info("Sensor is inactive!");
            return null;
        }
    }

    public AirQualityIndexWithType airQualityIndex(String sensorUUID) throws ExecutionException, InterruptedException {

      Measurement mPM25 = getLastMeasurementByType(sensorUUID, "PM25");
      Measurement mPM10 = getLastMeasurementByType(sensorUUID, "PM10");
      Measurement mNO2 = getLastMeasurementByType(sensorUUID, "NO2");
      Measurement mO3 = getLastMeasurementByType(sensorUUID, "O3");
      Measurement mSO2 = getLastMeasurementByType(sensorUUID, "SO2");

      double maxValueAQI = Double.NEGATIVE_INFINITY;
      String type ="";

      if(mPM10!= null && mNO2!=null) {
      maxValueAQI = Math.max(mPM10.getValue(),mNO2.getValue() );
      type = (mPM10.getValue() > mNO2.getValue()) ? mPM10.getType() : mNO2.getType();

      if (mPM25!= null) {

          if (mPM25.getValue() > maxValueAQI) {
              maxValueAQI = mPM25.getValue();
              type = mPM25.getType();
          }
      }

      if (mO3!= null) {

          if (mO3.getValue() > maxValueAQI) {
              maxValueAQI = mO3.getValue();
              type = mO3.getType();
          }
      }

      if (mSO2!= null) {

          if (mSO2.getValue() > maxValueAQI) {
              maxValueAQI = mSO2.getValue();
              type = mSO2.getType();
          }
      }

      }
      else{
          log.info("Insufficient data - min pollutants for performing AQI are: PM10 and NO2");
      }

    return new AirQualityIndexWithType(maxValueAQI, type);

    }
}
