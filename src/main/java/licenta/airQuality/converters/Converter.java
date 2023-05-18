package licenta.airQuality.converters;

public interface Converter<Sensor, SensorDTO> {

    void convert(Sensor source, SensorDTO target);

    SensorDTO convert(Sensor source);
}
