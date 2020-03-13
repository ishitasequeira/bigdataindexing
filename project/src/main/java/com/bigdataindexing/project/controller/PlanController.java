package com.bigdataindexing.project.controller;

import com.bigdataindexing.project.exception.*;
import org.apache.tomcat.util.http.parser.Authorization;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@RestController
public class PlanController {

    Map<String, String> map = new HashMap<>();

    @Autowired
    JedisPool jedisPool;


    @PostMapping(path = "/plan", produces = "application/json")
    public ResponseEntity<Object> saveplan(@RequestBody String data, @RequestHeader("Authorization") String authorization) throws URISyntaxException {

        authorize(authorization);
        //took the String and converted into json object
        JSONObject jsonObject = new JSONObject(new JSONTokener(data));

        validateObject(jsonObject);

        Jedis jedis = jedisPool.getResource();
        if (jedis.get("plan_" + jsonObject.get("objectId")) != null) {
            jedis.close();
            throw new PlanAlreadyPresentException("Plan has already present");
        }

        String id = savePlan(jsonObject);

        jedis.set(id, jsonObject.toString());
        jedis.close();

        String etag = generateEtag(jsonObject);
        map.put(id, etag);

        Response exceptionResponse = new Response(HttpStatus.CREATED.toString(), "Plan created with id " + id);

        return ResponseEntity.created(new URI(jsonObject.get("objectId").toString())).eTag(etag).body(exceptionResponse);
    }


    @GetMapping(path = "/plan/{id}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> getplan(@PathVariable String id, @RequestHeader(name = "If-None-Match") String etag,  @RequestHeader("Authorization") String authorization) {
        authorize(authorization);
        if (etag == null) {
            throw new MissingEtagException("If-None-Match header missing");
        }
        String abc = map.get(id);
        if (abc != null) {
            if (abc.equals(etag)) {

                Response response = new Response(HttpStatus.NOT_MODIFIED.toString(), "The object has not been modified");
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).body(response.toString());

            }
        }

        JSONObject jsonObject = getJedisJSONObject(id);
        if (map.get(id) == null) {
            etag = generateEtag(jsonObject);
            map.put(id, etag);
        } else {
            etag = map.get(id);
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .body(jsonObject.toString());

    }

    @DeleteMapping(path = "/plan/{id}")
    public ResponseEntity deleteplan(@PathVariable String id, @RequestHeader(name = "If-Match") String etag) {

        if (etag == null) {

            throw new MissingEtagException("If-Match header missing");
        }

        if (map.get(id) != null && !map.get(id).equals(etag)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .eTag(etag)
                    .body(new Response(HttpStatus.BAD_REQUEST.toString(), "The object has been modified by other user.Please get the latest version of the object"));
        }

        if(deleteJedisObject(id)) {
            throw new SuccessResponse("The Plan has been deleted");
        } else {
            throw new InvalidInputException("Some error was encountered while deleting the plan");
        }

    }

    @PutMapping(path = "/plan/{id}")
    public ResponseEntity putPlan(@PathVariable String id, @RequestHeader(name = "If-Match") String etag, @RequestHeader("Authorization") String authorization, @RequestBody String data) {
        authorize(authorization);
        if (etag == null) {

            throw new MissingEtagException("If-Match header missing");
        }
        if (map.get(id) != null && !map.get(id).equals(etag)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .eTag(etag)
                    .body(new Response(HttpStatus.BAD_REQUEST.toString(), "The object has been modified by other user.Please get the latest version of the object"));
        }
        //took the String and converted into json object
        JSONObject jsonObject = new JSONObject(new JSONTokener(data));

        validateObject(jsonObject);

        String objectId = "plan_" + jsonObject.getString("objectId");
        if(!id.equals(objectId)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .eTag(etag)
                    .body(new Response(HttpStatus.BAD_REQUEST.toString(), "The object id's do not match"));
        }

        deleteJedisObject(id);

        Jedis jedis = jedisPool.getResource();

        savePlan(jsonObject);

        jedis.set(id, jsonObject.toString());
        jedis.close();

        etag = generateEtag(jsonObject);
        map.put(id, etag);

        Response exceptionResponse = new Response(HttpStatus.NO_CONTENT.toString(), "Plan with id " + id + " updated successfully!!");

        return ResponseEntity.status(HttpStatus.NO_CONTENT).eTag(etag).body(exceptionResponse);
    }

    @PatchMapping(path = "/plan/{id}")
    public ResponseEntity patchPlan(@PathVariable String id, @RequestHeader(name = "If-Match") String etag,  @RequestHeader("Authorization") String authorization) {
        authorize(authorization);

        if (etag == null) {
            throw new MissingEtagException("If-Match header missing");
        }

        if (map.get(id) != null && !map.get(id).equals(etag)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .eTag(etag)
                    .body(new Response(HttpStatus.BAD_REQUEST.toString(), "The object has been modified by other user.Please get the latest version of the object"));
        }
        Jedis jedis = jedisPool.getResource();
        String object = jedis.get(id);
        if (object == null) {
            jedis.close();
            throw new PlanNotFoundException("Plan is not present");
        } else {

        }
        return null;
    }

    // to create a etag

    public String generateEtag(JSONObject object) {

        String key = String.valueOf(object.toString().hashCode());
        return key;
    }

    public JSONObject getJedisJSONObject(String id) {

        Jedis jedis = jedisPool.getResource();

        String object = jedis.get(id);

        JSONObject jsonObject = new JSONObject(new JSONTokener(object));
        Set<String> keys = jedis.keys(id + "*");
        List<String> key_val = new ArrayList<>(jsonObject.keySet());

        for (int i = 0; i < key_val.size(); i++) {

            String str = key_val.get(i);
            if (jsonObject.get(str) instanceof JSONArray) {
                System.out.println(jsonObject.get(str));
                JSONArray jsonArray = (JSONArray) jsonObject.get(str);
                JSONArray l = new JSONArray();
                for (int j = 0; j < jsonArray.length(); j++) {
                    System.out.println(jsonArray.length());
                    JSONObject ob = new JSONObject(new JSONTokener(jedis.get(jsonArray.getString(j))));
                    List<String> k = new ArrayList<>(ob.keySet());
                    for (int a = 0; a < k.size(); a++) {
                        if (keys.contains(ob.get(k.get(a)))) {
                            System.out.println(str + " = " + jedis.get(ob.get(k.get(a)).toString()));
                            ob.put(
                                    k.get(a),
                                    new JSONObject(new JSONTokener(jedis.get(ob.get(k.get(a)).toString()))));
                        }
                    }
                    l.put(ob);
                }
                jsonObject.put(str, l);
            }
            if (keys.contains(jsonObject.get(str))) {
                System.out.println(str + " = " + jedis.get(jsonObject.get(str).toString()));
                jsonObject.put(
                        str, new JSONObject(new JSONTokener(jedis.get(jsonObject.get(str).toString()))));
            }
        }
        return jsonObject;
    }

    public String savePlan(JSONObject jsonObject) {

        Jedis jedis = jedisPool.getResource();
        String id = "plan_" + jsonObject.getString("objectId");
        Iterator<String> keys = jsonObject.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            if (jsonObject.get(key) instanceof JSONObject) {
                JSONObject jsonObj = (JSONObject) jsonObject.get(key);
                String str = id + "_" + key + "_" + jsonObj.get("objectId");
                jsonObject.put(key, str);
                jedis.set(str, String.valueOf(jsonObj));
                System.out.println(str);
            }
            if (jsonObject.get(key) instanceof JSONArray) {
                JSONArray jsonArray = jsonObject.getJSONArray(key);

                JSONArray list = new JSONArray();

                for (Object jsonObj : jsonArray) {
                    String str = id + "_" + key + "_" + ((JSONObject) jsonObj).get("objectId");
                    List<String> keys1 = new ArrayList<>(((JSONObject) jsonObj).keySet());
                    for (int i = 0; i < keys1.size(); i++) {
                        String key1 = keys1.get(i);
                        if (((JSONObject) jsonObj).get(key1) instanceof JSONObject) {
                            JSONObject jsonObj1 = (JSONObject) ((JSONObject) jsonObj).get(key1);
                            String str1 = str + "_" + jsonObj1.get("objectId");
                            ((JSONObject) jsonObj).put(key1, str1);
                            jedis.set(str1, String.valueOf(jsonObj1));
                        }
                    }

                    list.put(str);
                    jedis.set(str, String.valueOf(jsonObj));
                }
                jsonObject.put(key, list);
                System.out.println(key + " json array");
            }

        }

        jedis.close();

        return id;
    }

    public void validateObject(JSONObject jsonObject) {

        //created json schema for the object
        JSONObject jsonSchema = new JSONObject(
                new JSONTokener(PlanController.class.getResourceAsStream("/jsonschema.json")));

        Schema schema = SchemaLoader.load(jsonSchema);
        try {
            //validated the schema against object
            schema.validate(jsonObject);
        } catch (ValidationException e) {
            e.getCausingExceptions().stream().map(ValidationException::getMessage).forEach(System.out::println);
            throw new InvalidInputException("Invalid Input. Error: " + e.getMessage());
        }
    }

    public boolean deleteJedisObject(String id) {
        Jedis jedis = jedisPool.getResource();
        String object = jedis.get(id);

        Set<String> keys = (Set<String>) jedis.keys(id + "*");

        if (object == null) {
            jedis.close();
            throw new PlanNotFoundException("Plan is not present");
        } else {
            for (String s : keys) {
                jedis.del(s);
            }
            jedis.close();
            map.remove(id);
            return Boolean.TRUE;
        }
    }

    public boolean putPlanHelper(JSONObject jsonObject, JSONObject jedisJsonObject){

        List<String> keys = (List<String>) jsonObject.keySet();
        for(int i =0 ; i<keys.size();i++){
            if(jedisJsonObject.get(keys.get(i))!=null){
                if(jsonObject.get(keys.get(i)) instanceof JSONArray){

                }else  if(jsonObject.get(keys.get(i)) instanceof JSONArray){

                }else {
                    if(jsonObject.get(keys.get(i) ) != jedisJsonObject.get(keys.get(i))){
                        jedisJsonObject.put(keys.get(i), jsonObject.get(keys.get(i)));
                    }
                }

            }else {
                throw new InvalidInputException("Invalid Input.");
            }
        }

        return false;
    }

    public ResponseEntity<Response> authorize(String authorization){
        if (authorization == null || authorization.isEmpty()){
            Response exceptionResponse = new Response(HttpStatus.UNAUTHORIZED.toString(), "Invalid or expired token!!!");
            return   ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exceptionResponse);
        }
        String auth = authorization.split(" ")[1];

        if(!AuthorizationController.authorize(auth)){
            Response exceptionResponse = new Response(HttpStatus.UNAUTHORIZED.toString(), "Invalid or expired token!!!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exceptionResponse);

        }
        return null;
    }


}
