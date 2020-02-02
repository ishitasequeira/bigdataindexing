package com.bigdataindexing.project.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PlanController {

    @PostMapping("/plan")
    public ResponseEntity saveplan(String object){

        return new ResponseEntity("WELCOME SPRING", HttpStatus.OK);
    }

    @GetMapping("/plan/{id}")
    public ResponseEntity getplan(@PathVariable String id){

        return new ResponseEntity("WELCOME SPRING", HttpStatus.OK);
    }

    @DeleteMapping("/plan/{id}")
    public ResponseEntity deleteplan(@PathVariable String id){

        return new ResponseEntity("WELCOME SPRING", HttpStatus.OK);
    }


}
