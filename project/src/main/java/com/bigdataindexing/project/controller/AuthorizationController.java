package com.bigdataindexing.project.controller;

import com.bigdataindexing.project.exception.InvalidInputException;
import com.bigdataindexing.project.exception.Response;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.Date;

@RestController
public class AuthorizationController {
   static RSAKey rsaJWK = null;
    static {
        // RSA signatures require a public and private RSA key pair, the public key
        // must be made known to the JWS recipient in order to verify the signatures

        try {
            rsaJWK = new RSAKeyGenerator(2048).keyID("RedHat").generate();
    } catch (JOSEException e) {
      e.printStackTrace();
        }
    }

  @PostMapping("/token")
  public ResponseEntity generateToken() {
    // Create RSA-signer with the private key
    JWSSigner signer = null;
    try {
      signer = new RSASSASigner(rsaJWK);
    } catch (JOSEException e) {
      e.printStackTrace();
    }

    // Prepare JWT with claims set
    JWTClaimsSet claimsSet =
        new JWTClaimsSet.Builder()
            .subject("demo2")
            .expirationTime(new Date(new Date().getTime() + 60 * 1000))
            .build();

    SignedJWT signedJWT =
        new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaJWK.getKeyID()).build(), claimsSet);

    // Compute the RSA signature
    try {
      signedJWT.sign(signer);
    } catch (JOSEException e) {
      e.printStackTrace();
    }

    // To serialize to compact form, produces something like
    // eyJhbGciOiJSUzI1NiJ9.SW4gUlNBIHdlIHRydXN0IQ.IRMQENi4nJyp4er2L
    // mZq3ivwoAjqa1uUkSBKFIX7ATndFF5ivnt-m8uApHO4kfIFOrW7w2Ezmlg3Qd
    // maXlS9DhN0nUk_hGI3amEjkKd0BWYCB8vfUbUv0XGjQip78AI4z1PrFRNidm7
    // -jPDm5Iq0SZnjKjCNS5Q15fokXZc8u0A
    String s = signedJWT.serialize();
    Response exceptionResponse = new Response(HttpStatus.OK.toString(), s);

    return ResponseEntity.status(HttpStatus.OK).body(exceptionResponse);
  }


  public static boolean authorize( String token){

      // On the consumer side, parse the JWS and verify its RSA signature
      SignedJWT signedJWT = null;
      try {
          signedJWT = SignedJWT.parse(token);
      } catch (ParseException e) {
          throw new InvalidInputException("token not verified");
      }
      RSAKey rsaPublicJWK = rsaJWK.toPublicJWK();

      JWSVerifier verifier = null;
      try {
          verifier = new RSASSAVerifier(rsaPublicJWK);
      } catch (JOSEException e) {
          throw new InvalidInputException("token not verified");
      }
      try {
          return signedJWT.verify(verifier);
      } catch (JOSEException e) {
         throw new InvalidInputException("token not verified");
      }
  }
}
