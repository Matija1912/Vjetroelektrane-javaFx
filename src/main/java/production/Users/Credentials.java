package production.Users;

import production.enums.UserType;

import java.io.Serializable;
import java.util.Optional;

public abstract class Credentials implements Serializable{

    private Integer id;
    private Integer userType;
    private String username;
    private String password;

    public static final Integer USER = 1;
    public static final Integer ADMIN = 2;

    public Credentials(Integer id, Integer userType, String username, String password) {
        this.id = id;
        this.userType = userType;
        this.username = username;
        this.password = password;
    }

    public Integer getUserType() {
        return userType;
    }

    public String getUserTypeString(){
        if(UserType.USER.getValue().equals(userType)){
            return "User";
        }else if(UserType.ADMIN.getValue().equals(userType)){
            return "Admin";
        }
        return "";
    }
    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
