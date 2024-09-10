package production.Users;

import java.io.Serializable;
import java.util.Set;

public class Admin extends Credentials implements Serializable {
    private String role;


    public Admin(Integer id, String username, String password, String role) {
        super(id, Credentials.ADMIN, username, password);
        this.role = role;
    }


    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
