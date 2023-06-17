package licenta.airQuality.service;

import com.google.api.core.ApiFuture;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import io.netty.util.internal.StringUtil;
import licenta.airQuality.entities.AirQualityIndexWithType;
import licenta.airQuality.entities.Measurement;
import licenta.airQuality.entities.User;
import licenta.airQuality.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;

import java.text.ParseException;
import java.time.Instant;
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
    public User createUser(User user) {
        getFirestore().collection("users")
                .document(user.getEmail())
                .create(user);
        return user;
    }


    public User getUserByEmail(String email) {
        if(StringUtil.isNullOrEmpty(email)) {
            throw new IllegalArgumentException("User email is null or empty!");
        }
        ApiFuture<DocumentSnapshot>  apiFuture = getFirestore().collection("users")
                .document(email)
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
                .document(user.getEmail());

        final ApiFuture<DocumentSnapshot> apiFuture = documentReference.get();
        if(!apiFuture.get().exists()) {
            log.warn(String.format("User with email: %s not found!", user.getEmail()));
            return null;
        }

        final User oldUser = documentReference.get().get().toObject(User.class);
        if(isNull(oldUser)) {
            log.warn(String.format("Sensor with email: %s not found!", user.getEmail()));
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
        final String logMessage = String.format("[%s] User with email: %s was successfully updated!", timestamp, user.getEmail());
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

    public List<Measurement> getMeasurementsBetweenDates(String sensorUUID, String measurementType, Long startDate, Long endDate) throws ExecutionException, InterruptedException {
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

    public List<Measurement> getLastMeasurements(String sensorUUID, String measurementType) throws ExecutionException, InterruptedException {

        DocumentSnapshot sensorDocumentSnapshot = getDocumentSnapshot(sensorUUID).get();

        if (sensorDocumentSnapshot.exists() && sensorDocumentSnapshot.getBoolean("active")) {
            CollectionReference measurementsCollectionRef = sensorDocumentRef(sensorUUID).collection(COLLECTION_MEASUREMENTS_NAME);

            Query query = measurementsCollectionRef
                    .whereEqualTo("type", measurementType)
                    .orderBy("instantTime", Query.Direction.DESCENDING)
                    .limit(12);

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
    public List<Measurement> getLastMeasurementOfLastHour(@RequestHeader String sensorUUID) throws ExecutionException, InterruptedException {
        List<String> measurementsList = new ArrayList<>();
        measurementsList.add("PM25");
        measurementsList.add("PM10");
        measurementsList.add("NO2");
        measurementsList.add("O3");
        measurementsList.add("SO2");
        measurementsList.add("temp");
        measurementsList.add("humidity");
        measurementsList.add("pressure");
        List<Measurement> measurementObjectsList = new ArrayList<>();
        for(String measurement: measurementsList) {
            if(measurement!=null){
                measurementObjectsList.add(getLastMeasurementByType(sensorUUID, measurement));
            }
        }

        long latestTimestamp = 0;

        for(Measurement measurementLastHour: measurementObjectsList) {
            if(measurementLastHour != null ) {
                long seconds =  (long) measurementLastHour.getInstantTime().getSeconds();
                int nanos = (int) measurementLastHour.getInstantTime().getNanos();
                long instantTime = seconds * 1000000000L + nanos;
                if (instantTime > latestTimestamp) {
                    latestTimestamp = instantTime;
                }
            }
        }
        List<Measurement> measurementsWithLatestTimestamp = new ArrayList<>();
        for (Measurement measurement : measurementObjectsList) {
            if(measurement!=null ) {
                long seconds = (long) measurement.getInstantTime().getSeconds();
                int nanos = (int) measurement.getInstantTime().getNanos();
                long instantTime = seconds * 1000000000L + nanos;

                if (instantTime == latestTimestamp) {
                    measurementsWithLatestTimestamp.add(measurement);
                }
            }
        }

        return measurementsWithLatestTimestamp;
    }

    public AirQualityIndexWithType airQualityIndex(String sensorUUID) throws ExecutionException, InterruptedException {
        Measurement mPM25 = null;
      Measurement mPM10 = null;
      Measurement mNO2 = null;
      Measurement mO3 = null;
      Measurement mSO2 = null;

      for(Measurement measurement : getLastMeasurementOfLastHour(sensorUUID)){
          if(measurement!=null){
          if(measurement.getType().equals("PM25")){
              mPM25 = measurement;
          }
          if(measurement.getType().equals("PM10")){
              mPM10 = measurement;
          }
          if(measurement.getType().equals("NO2")){
               mNO2 = measurement;
          }
          if(measurement.getType().equals("O3")){
             mO3 = measurement;
          }
          if(measurement.getType().equals("SO2")){
            mSO2 = measurement;
          }}

      }

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
