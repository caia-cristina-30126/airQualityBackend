package licenta.airQuality.entities;


import com.google.cloud.Timestamp;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import licenta.airQuality.constants.MeasurementUnit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Data
@AllArgsConstructor
@NoArgsConstructor

public class Measurement {
    public static final String TYPE_FIELD_NAME = "type";
    public static final String UNIT_FIELD_NAME = "unit";
    public static final String INSTANT_TIME_FIELD_NAME = "instantTime";

    @NotNull
    private MeasurementUnit unit;
    @NotNull
    private Double value;
    @NotNull
    private Timestamp instantTime;
    @NotNull
    private String type;

   // private Date date;

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
