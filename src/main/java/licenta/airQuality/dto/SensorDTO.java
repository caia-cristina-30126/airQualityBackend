package licenta.airQuality.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SensorDTO {

    private Boolean active;
    private String uuid;
    private GeoPointDTO location;
    private String name;
    private LocalDate creationDate;


}
