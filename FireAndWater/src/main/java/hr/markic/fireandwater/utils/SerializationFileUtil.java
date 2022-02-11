package hr.markic.fireandwater.utils;

import hr.markic.fireandwater.model.Player;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SerializationFileUtil {

    public static final String SERIALIZATION_FILE_NAME = "gameState.ser";

    public static List<Object> readFromFile() {

        List<Object> listOfObjects = new ArrayList<>();

        try(ObjectInputStream ois =
                    new ObjectInputStream(new FileInputStream(
                            SERIALIZATION_FILE_NAME))) {
            listOfObjects.addAll((List<Object>)ois.readObject());
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }

        return listOfObjects;
    }

    public static void writeToFile(List<Object> listOfObjects) {

        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream(
                             SerializationFileUtil.SERIALIZATION_FILE_NAME)))
        {
            oos.writeObject(listOfObjects);
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean isFileExists() {
        return new File(SERIALIZATION_FILE_NAME).exists();
    }
}
