package production.enums;

public enum TurbineType {
    HORIZONTAL_AXIS(1),
    VERTICAL_AXIS(2),
    STATIC_HORIZONTAL_AXIS(3);

    private final Integer value;

    TurbineType(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}

