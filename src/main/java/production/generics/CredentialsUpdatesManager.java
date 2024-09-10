package production.generics;

import production.Users.Credentials;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Optional;

public class CredentialsUpdatesManager <T> implements Serializable {
    private T updatedElement;
    private String updateType;
    private Credentials user;
    private String oldValue;
    private String newValue;
    private LocalDateTime creationDate;
    public CredentialsUpdatesManager(T updatedElement, String updateType, Credentials user)
    {
        this.updatedElement = updatedElement;
        this.updateType = updateType;
        this.user = user;
        this.creationDate = LocalDateTime.now();
        this.oldValue = "";
        this.newValue = "";

    }

    public CredentialsUpdatesManager(T updatedElement, String updateType, Credentials user, String oldValue, String newValue)
    {
        this.updatedElement = updatedElement;
        this.updateType = updateType;
        this.user = user;
        this.creationDate = LocalDateTime.now();
        this.oldValue = oldValue;
        this.newValue = newValue;

    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public T getUpdatedElement() {
        return updatedElement;
    }

    public Credentials getUser() {
        return user;
    }

    public String getUpdateType() {
        return updateType;
    }
}
