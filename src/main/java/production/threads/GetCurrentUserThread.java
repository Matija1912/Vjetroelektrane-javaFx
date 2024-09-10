package production.threads;

import com.app.vjetroelektranejavafx.VjetroelektraneApp;
import javafx.application.Platform;
import production.Users.Admin;
import production.Users.Credentials;
import production.Users.User;
import production.WindTurbines.HorizontalAxisTurbine;
import production.WindTurbines.StaticHorizontalAxisTurbine;
import production.WindTurbines.Turbine;
import production.WindTurbines.VerticalAxisTurbine;
import production.database.DatabaseUtils;
import production.files.FileUtils;

import java.util.Optional;

public class GetCurrentUserThread extends FileUtilsThread implements Runnable{

    @Override
    public void run() {
        while(!FileUtilsThread.isCurrentUserFileInUse){
            Optional<Credentials> optionalCredentials = super.deserializeCurrentUser();
            if(optionalCredentials.isPresent()){
                Credentials credentials = optionalCredentials.get();
                if(credentials instanceof User){
                    User user = (User) credentials;
                    Platform.runLater(() -> {
                        VjetroelektraneApp.getMainStage().setTitle(user.getUsername() + " (" + user.getUserTypeString() + ")");
                    });
                }else if(credentials instanceof Admin){
                    Admin admin = (Admin) credentials;
                    Platform.runLater(() -> {
                        VjetroelektraneApp.getMainStage().setTitle(admin.getUsername() + " (" + admin.getUserTypeString() + ")");
                    });
                }
            }else {
                return;
            }
            try {
                System.out.println("username refreshed");
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
