package production.weather;

import java.io.Serializable;

public class Location implements Serializable {
    private Integer id;
    private Double longitude;
    private Double latitude;
    private String country;
    private String city;

    public Location(Integer id, Double latitude, Double longitude, String country, String city) {
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.country = country;
        this.city = city;
    }

    private Location(Builder builder) {
        this.id = builder.id;
        this.latitude = builder.latitude;
        this.longitude = builder.longitude;
        this.country = builder.country;
        this.city = builder.city;
    }

    public static class Builder {
        private Integer id;
        private Double latitude;
        private Double longitude;
        private String country;
        private String city;

        public Builder(Integer id){
            this.id = id;
        }

        public Builder atLatitude(Double latitude){
            this.latitude = latitude;
            return this;
        }
        public Builder atLongitude(Double longitude){
            this.longitude = longitude;
            return this;
        }

        public Builder atCountry(String country){
            this.country = country;
            return this;
        }

        public Builder atCity(String city){
            this.city = city;
            return this;
        }

        public Location build(){
            return new Location(this);
        }

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
