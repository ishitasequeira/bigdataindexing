package com.bigdataindexing.project.controller;

import com.bigdataindexing.project.exception.*;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
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

@RestController
public class PlanController {

    @Autowired
    JedisPool jedisPool;



    @PostMapping(path = "/plan", produces = "application/json")
    public ResponseEntity<Object> saveplan(@RequestBody String data) throws URISyntaxException {

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


            throw new InvalidInputException("Invalid Input. Error: " + e.getMessage());
        }
        Jedis jedis = jedisPool.getResource();
        System.out.println(jsonObject.toString());
        if(jedis.get((String) jsonObject.get("objectId"))!=null){
            throw new PlanAlreadyPresentException("Plan has already present");
        }
        jedis.set((String) jsonObject.get("objectId"), jsonObject.toString());
        jedis.close();
//        throw new PlanCreated(jsonObject);
        Response exceptionResponse = new Response(HttpStatus.CREATED.toString(), "Plan created with id "+ jsonObject.get("objectId"));
        ResponseEntity responseEntity = new ResponseEntity(exceptionResponse.toString(), HttpStatus.CREATED);
//        return responseEntity;
        return ResponseEntity.created(new URI(jsonObject.get("objectId").toString())).eTag(Long.toString(jsonObject.hashCode())).body(exceptionResponse);
    }


    @GetMapping(path = "/plan/{id}", produces = "application/json" )
    public ResponseEntity<Object> getplan(@PathVariable String id){
        Jedis jedis = jedisPool.getResource();
        jedis.close();
        String object = jedis.get(id);
        if(object == null){
            throw new PlanNotFoundException("Plan is not present");
        }else{
        JSONObject jsonObject = new JSONObject(new JSONTokener(object));
//        return new ResponseEntity.().eTag(Long.toString(foo.getVersion()))
//                    .body(jsonObject.toString());
//
//        }
            System.out.println("bsbs");
            return ResponseEntity.ok()
                    .eTag(Long.toString(jsonObject.hashCode()))
                    .body(jsonObject.toString());
        }
    }

    @DeleteMapping(path = "/plan/{id}")
    public ResponseEntity deleteplan(@PathVariable String id){
        Jedis jedis = jedisPool.getResource();
        String object = jedis.get(id);
        if(object == null){
            throw new PlanNotFoundException("Plan is not present");
        }else{
            JSONObject jsonObject = new JSONObject(new JSONTokener(object));
            jedis.del(id);
            jedis.close();
//            return new ResponseEntity("Object with id:"+id+" has been deleted", HttpStatus.OK);
            throw new SuccessResponse("The Plan has been deleted");
        }
    }


}
