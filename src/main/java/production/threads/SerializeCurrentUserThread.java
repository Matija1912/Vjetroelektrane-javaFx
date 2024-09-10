package production.threads;

import production.Users.Credentials;
import production.files.FileUtils;

public class SerializeCurrentUserThread extends FileUtilsThread implements Runnable {
    private final Credentials credentials;
    public SerializeCurrentUserThread(Credentials credentials){
        this.credentials = credentials;
    }
    @Override
    public void run() {
        super.serializeCurrentUser(credentials);
    }
}
