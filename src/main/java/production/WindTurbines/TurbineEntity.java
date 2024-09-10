package production.WindTurbines;

public class TurbineEntity {
    private Integer id;
    private String manufacturer;

    public TurbineEntity(Integer id, String manufacturer) {
        this.id = id;
        this.manufacturer = manufacturer;
    }

    public String getFullName(){
        String nameId = String.valueOf(getId());
        String nameManufacturer = getManufacturer();
        return nameId + ": " + nameManufacturer;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
}
