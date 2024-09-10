package production.threads;

import production.Users.Credentials;

import java.util.Optional;

public class DeserializeCurrentUserThread extends FileUtilsThread implements Runnable {

    private Optional<Credentials> credentials;

    public Optional<Credentials> getCredentials() {
        return credentials;
    }

    @Override
    public void run() {
        Optional<Credentials>credentialsOptional = Optional.empty();
        credentialsOptional = super.deserializeCurrentUser();
        credentials = credentialsOptional;

    }
}
