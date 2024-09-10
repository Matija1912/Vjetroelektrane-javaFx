package production.generics;

import production.Users.Credentials;

import java.io.Serializable;
import java.time.LocalDateTime;

public class UpdatesManager<T, U> implements Serializable {
    private T user;
    private U addedElement;

    private String updateType;
    private LocalDateTime creationDate;

    public UpdatesManager(T user, U addedElement) {
        this.user = user;
        this.addedElement = addedElement;
        this.updateType = "added";
        this.creationDate = LocalDateTime.now();
    }

    public UpdatesManager(T user, String updateType, U addedElement) {
        this.user = user;
        this.addedElement = addedElement;
        this.updateType = updateType;
        this.creationDate = LocalDateTime.now();
    }

    public String getUpdateType() {
        return updateType;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public T getUser() {
        return user;
    }

    public U getAddedElement() {
        return addedElement;
    }
}
