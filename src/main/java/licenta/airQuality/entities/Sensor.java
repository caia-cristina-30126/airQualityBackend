package licenta.airQuality.entities;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.GeoPoint;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class Sensor {
    public static final String Active_FIELD_NAME = "active";
   public static final String Location_FIELD_NAME = "location";
    public static final String Name_FIELD_NAME = "name";
    public static final String UUID_FIELD_NAME = "uuid";
    public static final String LocalDate_FIELD_NAME = "lastUpdate";

    @NotNull
    private Boolean active;
    private GeoPoint location;
    private String name;
    private String uuid;
    private Timestamp creationDate;

}
