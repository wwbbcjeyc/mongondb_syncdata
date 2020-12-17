package com.zjtd.syncdata;

import java.io.*;

/**
 * @Author wangwenbo
 * @Date 2020/12/18 12:14 上午
 * @Version 1.0
 */
public class BsonTimSerializableSave {
    public static void saveBsonObj(Object bsonObj, String objName) {
        try {
            FileOutputStream fs = new FileOutputStream(objName);
            ObjectOutputStream os = new ObjectOutputStream(fs);
            os.writeObject(bsonObj);
            os.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Object getBsonObj(String objName) {
        Object resultobj = null;
        try {
            FileInputStream fs = new FileInputStream(objName);
            ObjectInputStream is = new ObjectInputStream(fs);
            resultobj = is.readObject();
            is.close();
        } catch (EOFException ex) {
            return  null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return resultobj;
    }

}
