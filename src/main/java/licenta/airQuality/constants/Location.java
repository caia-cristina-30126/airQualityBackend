package licenta.airQuality.constants;

public enum Location {
    GHEORGHENI_CJ_RO(46.768236, 23.619737),
    MANASTUR_CJ_RO(46.755103, 23.556347),
    MARASTI_CJ_RO(46.780958, 23.613623),
    ZORILOR_CJ_RO(46.755325, 23.595652);

    final Double latitude;
    final Double longitude;

    Location(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
}
