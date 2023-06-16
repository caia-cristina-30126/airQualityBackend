package licenta.airQuality.entities;

import com.google.cloud.Timestamp;
import jakarta.validation.constraints.NotNull;
import licenta.airQuality.constants.MeasurementUnit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class User {

    @NotNull
    private String email;
    private String firstName;
    private String lastName;
    private String role;

}
