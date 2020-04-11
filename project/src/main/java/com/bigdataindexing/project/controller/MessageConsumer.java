package com.bigdataindexing.project.controller;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;


@Service
public class MessageConsumer {

    @Value("${elasticsearch.queue:elasticQueue}")
    private String ELASTICQUEUE;

    @Autowired
    JedisPool jedisPool;

    @Autowired
    RestHighLevelClient client;

    private static final String INDEX = "plan";
    private static final String TYPE = "plan";

    @EventListener(ApplicationReadyEvent.class)
    public void storeToES() throws InterruptedException {
        System.out.println("Event Listener started!!");

        while (true) {
            Jedis jedis = jedisPool.getResource();
            String str = jedis.lpop(ELASTICQUEUE);
            if (str != null && !str.isEmpty()) {
                JSONObject object = new JSONObject(new JSONTokener(str));
                System.out.println(object);
                System.out.println("inside if");
                JSONObject jsonObject = object.getJSONObject("data");

                try {
                    String typeOfRequest = object.get("typeOfRequest").toString();
                    System.out.println(typeOfRequest);
                    switch (typeOfRequest) {
                        case "POST":
                            IndexRequest request = new IndexRequest(INDEX, TYPE, object.getString("key"))
                                    .source(jsonObject,XContentType.JSON);
                            IndexResponse response = client.index(request, null);
                            System.out.println(response);
                            break;
                        case "UPDATE" :
                            UpdateRequest updateRequest = new UpdateRequest(INDEX, TYPE, object.getString("key"))
                                    .fetchSource(true);    // Fetch Object after its update
                            updateRequest.doc(jsonObject, XContentType.JSON);
                            UpdateResponse updateResponse = client.update(updateRequest);

                            break;
                        case "DELETE":
                            DeleteRequest deleteRequest = new DeleteRequest(INDEX, TYPE, object.getString("key"));
                            DeleteResponse deleteResponse = client.delete(deleteRequest);
                            break;
                    }

                } catch(ElasticsearchException e) {
                    e.getDetailedMessage();
                } catch (java.io.IOException ex){
                    ex.getLocalizedMessage();
                }

            }
            jedis.close();
        }

    }

}

