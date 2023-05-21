package licenta.airQuality.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class AirQualityIndexWithType {
    private double value;
    private String type;
}
