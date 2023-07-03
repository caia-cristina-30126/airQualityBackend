package licenta.airQuality.constants;

public enum Location {
    GHEORGHENI_CJ_RO(46.768236, 23.619737),
    MANASTUR_CJ_RO(46.755103, 23.556347),
    MARASTI_CJ_RO(46.780958, 23.613623),
    ZORILOR_CJ_RO(46.755325, 23.595652),
    RASNOV_BV_RO(45.576114, 25.454630),
    GHIMBAV_BV_RO(45.660217, 25.510104),
    SACELE_BV_RO(45.615978, 25.686445),
    BARTOLOMEU_BV_RO(45.667921, 25.558034),
    GIORC_TM_RO(45.704763, 21.233103),
    FABRIC_TM_RO(45.755514, 21.251998),
    MOARA_SV_RO(47.593131, 26.234255),
    LAAK_THE_HAGUE_NL(52.063852, 4.326606),
    DELFT_THE_HAGUE_NL(52.012093, 4.350277),
    VOORBURG_THE_HAGUE_NL(52.074579, 4.360074),
    SCHIEDAM_ROTTERDAM_NL(51.920964, 4.396627),
    ZUIDERPARK_ROTTERDAM_NL(51.882622, 4.472501),
    KRALINGEN_ROTTERDAM_NL(51.923181, 4.507049);

    // The Hague


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
