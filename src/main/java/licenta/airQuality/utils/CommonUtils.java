package licenta.airQuality.utils;

import licenta.airQuality.entities.Measurement;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CommonUtils {
    public static double convertPpbToMicrograms(Measurement measurement) {

        final double value = BigDecimal.valueOf(measurement.getValue())
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        double convertPPBM = 0.00;
        if(measurement.getType().equals("NO2")) {
            convertPPBM = BigDecimal.valueOf(1.88 * value).setScale(2, RoundingMode.HALF_UP).doubleValue();
        }
        else if(measurement.getType().equals("O3")){
            convertPPBM = BigDecimal.valueOf(2 * value).setScale(2, RoundingMode.HALF_UP).doubleValue();
        }
        else if(measurement.getType().equals("SO2")) {
            convertPPBM = BigDecimal.valueOf(2.62 * value).setScale(2, RoundingMode.HALF_UP).doubleValue();
        }
        else convertPPBM = value;

        return convertPPBM;

    }

    public static Integer convertToInteger(Double value) {
//        Integer.parseInt(value)
        return value.intValue();
//      return (int) Math.round(value);

    }
}
