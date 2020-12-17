package com.zjtd.syncdata;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.FullDocument;
import org.bson.BsonDocument;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;


/**
 * @Author wangwenbo
 * @Date 2020/12/18 12:07 上午
 * @Version 1.0
 */
public class MongoDB_SyncData {

    public static void main(String[] args) {
        MongoDB_SyncData mongodbSync = new MongoDB_SyncData();
        MongoDatabase database = mongodbSync.getDatabase(); //获取数据库
        MongoCursor<ChangeStreamDocument<Document>> cursor = null;
        Logger logger = Logger.getLogger("mongodbLogger");//需要和log4j配置文件保持一致
        Object token = BsonTimSerializableSave.getBsonObj("token.ser"); //token文件用于记录偏移量
        if (token != null) {
            BsonDocument afterToken = BsonDocument.parse(token.toString());
            //根据偏移量监控mongodb数据库的变化
            cursor = database.watch().fullDocument(FullDocument.UPDATE_LOOKUP).resumeAfter(afterToken).iterator();
        } else {
            //偏移量为空 直接监控mongodb
            cursor = database.watch().fullDocument(FullDocument.UPDATE_LOOKUP).iterator();
        }
        while (cursor.hasNext()) {
            ChangeStreamDocument<Document> csd = cursor.next();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("content", csd.getFullDocument().toJson()); //数据库操作内容
            jsonObject.put("collection", csd.getNamespace().getFullName()); //对应数据库 表
            jsonObject.put("op", csd.getOperationType().getValue());//操作类型
            jsonObject.put("ts", csd.getClusterTime()); //生成数据的时间戳
            logger.info(jsonObject.toJSONString());
            BsonDocument resumeToken = csd.getResumeToken();
            BsonTimSerializableSave.saveBsonObj(resumeToken, "token.ser"); //将偏移量存入此文件
        }


    }

    /**
     * 获取数据库
     *
     * @return
     */
    public MongoDatabase getDatabase() {
        MongoClient mongoClient = null;
        MongoDatabase database = null;
        try {
            MongoCredential credential = MongoCredential.
                    createScramSha1Credential("admin", "test", "admin".toCharArray());
            MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
            builder.connectionsPerHost(20); //与目标库能够建立的最大连接数
            builder.maxWaitTime(1000 * 60 * 1);//等待时长
            builder.connectTimeout(1000 * 60);//连接超时时长
            MongoClientOptions mongoClientOptions = builder.build();
            List<ServerAddress> serverList = new ArrayList<>(); //mongodb集群地址
            serverList.add(new ServerAddress("mongodb001", 27017));
            serverList.add(new ServerAddress("mongodb002", 27017));
            serverList.add(new ServerAddress("mongodb003", 27017));
            mongoClient = new MongoClient(serverList, credential, mongoClientOptions);
            database = mongoClient.getDatabase("test");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return database;
    }

}
