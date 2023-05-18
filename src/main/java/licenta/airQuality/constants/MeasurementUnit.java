package licenta.airQuality.constants;

public enum MeasurementUnit {
    PERCENT("%"),
    CELSIUS_DEGREE(" C"),
    HECTOPASCALS("hPa"),
    PARTS_PER_BILLION("ppb"), // 1ppb=1.88ug/m3
    MICROGRAMS_PER_CUBIC_METER("ug/m3");
    final String symbol;
    MeasurementUnit(String symbol) {
     this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
