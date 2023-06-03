package licenta.airQuality.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

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
    private List<String> measurementsType;
    
}
