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

public class Measurement {

    @NotNull
    private MeasurementUnit unit;
    @NotNull
    private Double value;

    private Timestamp instantTime;
    @NotNull
    private String type;


    @Override
    public String toString() {

        return "Measurement{" +
                "unit=" + unit +
                ", value=" + value +
                ", instantTime=" + instantTime +
                ", type='" + type + '\'' +
                '}';
    }
}
