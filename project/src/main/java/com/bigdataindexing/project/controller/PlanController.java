package com.bigdataindexing.project.controller;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@RestController
public class PlanController {

    @Autowired
    JedisPool jedisPool;



    @PostMapping("/plan")
    public ResponseEntity saveplan(@RequestBody String data){

        //took the String and converted into json object
        JSONObject jsonObject = new JSONObject(new JSONTokener(data));

        //created json schema for the object
        JSONObject jsonSchema = new JSONObject(
                new JSONTokener(PlanController.class.getResourceAsStream("/jsonschema.json")));
            System.out.println(jsonObject);

        Schema schema = SchemaLoader.load(jsonSchema);
        try{
            //validated the schema against object
        schema.validate(jsonObject);
        }catch (ValidationException e){
            System.out.println(e);
            e.getCausingExceptions().stream().map(ValidationException::getMessage).forEach(System.out::println);
            return new ResponseEntity(null, HttpStatus.BAD_REQUEST);
        }
        Jedis jedis = jedisPool.getResource();
        System.out.println(jsonObject.toString());
        jedis.set((String) jsonObject.get("objectId"), jsonObject.toString());
        jedis.close();
        return new ResponseEntity(jsonObject.get("objectId"), HttpStatus.CREATED);
    }

    @GetMapping("/plan/{id}")
    public ResponseEntity<JSONObject> getplan(@PathVariable String id){
        Jedis jedis = jedisPool.getResource();
        jedis.close();
        String object = jedis.get(id);
        if(object == null){
            return new ResponseEntity(null, HttpStatus.BAD_REQUEST);
        }else{
        JSONObject jsonObject = new JSONObject(new JSONTokener(object));
        return new ResponseEntity(jsonObject.toString(), HttpStatus.OK);
        }
    }

    @DeleteMapping("/plan/{id}")
    public ResponseEntity deleteplan(@PathVariable String id){
        Jedis jedis = jedisPool.getResource();
        String object = jedis.get(id);
        if(object == null){
            return new ResponseEntity(null, HttpStatus.BAD_REQUEST);
        }else{
            JSONObject jsonObject = new JSONObject(new JSONTokener(object));
            jedis.del(id);
            jedis.close();
            return new ResponseEntity("Object with id:"+id+" has been deleted", HttpStatus.OK);
        }
    }


}
