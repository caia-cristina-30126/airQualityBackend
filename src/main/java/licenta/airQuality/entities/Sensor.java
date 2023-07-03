package licenta.airQuality.entities;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.GeoPoint;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class Sensor {


    @NotNull
    private Boolean active;
    @NotNull
    private GeoPoint location;
    private String name;
    @NotNull
    private String uuid;
    private Timestamp creationDate;
    private List<String> measurementsType;
}
