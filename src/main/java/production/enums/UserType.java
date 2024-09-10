package production.enums;

public enum UserType {
    USER(1),
    ADMIN(2);
    private final Integer value;

    UserType(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
