package production.files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import production.Users.Credentials;
import production.exceptions.IncorrectFileNameException;
import production.generics.CredentialsUpdatesManager;
import production.generics.UpdatesManager;

import java.io.*;
import java.util.*;

public class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);
    private static final String USERS_UPDATES_FILE_NAME = "dat/serialization/usersUpdates.dat";
    private static final String CURRENT_USER_FILE_NAME = "dat/serialization/currentUser.dat";


    //serialize updates
    public static <T, U> void serializeUpdates(UpdatesManager<T, U> update, String fileName) {

        boolean append = new File(fileName).exists();

        try (FileOutputStream fos = new FileOutputStream(fileName, append);
             ObjectOutputStream oos = append ? new AppendableObjectOutputStream(fos) : new ObjectOutputStream(fos)){

            oos.writeObject(update);
            oos.flush();
        } catch (IOException e) {
            logger.error("Error serializing updates.");
            System.out.println(e.getMessage());
        }
    }


    //serialize credentials updates
    public static <T> void serializeCredentialsUpdates(CredentialsUpdatesManager<T> update, String fileName) {

        boolean append = new File(fileName).exists();

        try (FileOutputStream fos = new FileOutputStream(fileName, append);
             ObjectOutputStream oos = append ? new AppendableObjectOutputStream(fos) : new ObjectOutputStream(fos)) {

            oos.writeObject(update);
            oos.flush();
        } catch (IOException e) {
            logger.error("Error serializing credentials updates.");
            System.out.println(e.getMessage());
        }
    }

    private static class AppendableObjectOutputStream extends ObjectOutputStream {

        public AppendableObjectOutputStream(OutputStream out) throws IOException {
            super(out);

        }
        @Override
        protected void writeStreamHeader() throws IOException {
            // Do not write a header when appending to an existing file
            reset();
        }
    }

    //Deserialize users updates
    public static Optional<List<CredentialsUpdatesManager<Credentials>>> deserializeUsersAndAdminsUpdates(){
        Optional<List<CredentialsUpdatesManager<Credentials>>>optionalUpdateList = Optional.empty();
        optionalUpdateList = deserializeCredentialsUpdates(USERS_UPDATES_FILE_NAME);
        return optionalUpdateList;
    }

    //Deserialize turbines and locations updates
    public static <T> Optional<List<UpdatesManager<Credentials, T>>> deserializeLocationAndTurbineUpdates(String filename){
        Optional<List<UpdatesManager<Credentials, T>>>optionalUpdateList = Optional.empty();
        try {
            optionalUpdateList = deserializeUpdates(filename);
        }catch (IncorrectFileNameException e){
            logger.error(e.getMessage());
        }

        return optionalUpdateList;
    }


    @SuppressWarnings("unchecked")

    public static <T, U> Optional<List<UpdatesManager<T, U>>> deserializeUpdates(String fileName) throws IncorrectFileNameException{

        List<UpdatesManager<T, U>>updateList = new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            while(true){
                try {
                    UpdatesManager<T, U> update = (UpdatesManager<T, U>) ois.readObject();
                    updateList.add(update);
                }catch (EOFException e){
                    break;
                }catch (FileNotFoundException fe){
                    throw new IncorrectFileNameException("Incorrect file name!");
                }catch (ClassNotFoundException e) {
                    logger.error("Class not found during deserialization.");
                    System.out.println(e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.error("File error when deserializing updates.");
            return Optional.empty();
        }
        return Optional.of(updateList);
    }

    @SuppressWarnings("unchecked")

    public static <T> Optional<List<CredentialsUpdatesManager<T>>> deserializeCredentialsUpdates(String fileName){
        List<CredentialsUpdatesManager<T>>updateList = new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            while(true){
                try {
                    CredentialsUpdatesManager<T>update = (CredentialsUpdatesManager<T>) ois.readObject();
                    updateList.add(update);
                }catch (EOFException e){
                    break;
                }catch (ClassNotFoundException e){
                    logger.error("Class not found during deserialization.");
                    System.out.println(e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.error("File error when deserializing credentials updates.");
            return Optional.empty();
        }
        return Optional.of(updateList);
    }

    //Current user, session
    public static void serializeCurrentUser(Credentials credentials) throws IncorrectFileNameException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CURRENT_USER_FILE_NAME))) {
            oos.writeObject(credentials);
        }catch (FileNotFoundException fe){
            throw new IncorrectFileNameException("Incorrect file name");
        }catch (IOException e) {
            logger.error("Error serializing current user.");
            System.out.println(e.getMessage());
        }
    }


    public static Optional<Credentials> deserializeCurrentUser(){
        Optional<Credentials>optionalUser = Optional.empty();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(CURRENT_USER_FILE_NAME))) {
            optionalUser = Optional.ofNullable((Credentials) ois.readObject());
            return optionalUser;
        } catch (IOException e) {
            return optionalUser;
        } catch (ClassNotFoundException e){
            logger.error("Class not found exception.");
            System.out.println(e.getMessage());
            return optionalUser;
        }
    }

}
