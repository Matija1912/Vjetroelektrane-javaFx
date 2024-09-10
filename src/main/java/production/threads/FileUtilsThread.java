package production.threads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import production.Users.Credentials;
import production.exceptions.IncorrectFileNameException;
import production.files.FileUtils;

import java.util.Optional;

public abstract class FileUtilsThread {

    private static final Logger logger = LoggerFactory.getLogger(FileUtilsThread.class);
    public static Boolean isCurrentUserFileInUse = false;

    public synchronized void serializeCurrentUser(Credentials credentials){
        while (isCurrentUserFileInUse){
            try {
                wait();
            }catch (InterruptedException e){
                throw new RuntimeException();
            }
        }
        isCurrentUserFileInUse = true;
        try {
            FileUtils.serializeCurrentUser(credentials);
        }catch (IncorrectFileNameException e){
            logger.error(e.getMessage());
        }

        isCurrentUserFileInUse = false;
        notifyAll();
    }

    public synchronized Optional<Credentials> deserializeCurrentUser(){
        while (isCurrentUserFileInUse){
            try {
                wait();
            }catch (InterruptedException e){
                throw new RuntimeException();
            }
        }
        isCurrentUserFileInUse = true;
        Optional<Credentials> credentials = FileUtils.deserializeCurrentUser();
        isCurrentUserFileInUse = false;
        notifyAll();
        return credentials;
    }
}
