package com.vaya.meeting.exception;

import io.jsonwebtoken.io.Encoders;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class GenerateJwtKey {

    public static void main(String[] args) throws Exception {
        KeyGenerator generator =
                KeyGenerator.getInstance("HmacSHA256");

        generator.init(256);

        SecretKey key = generator.generateKey();

        String secret =
                Encoders.BASE64.encode(key.getEncoded());

        System.out.println(secret);
    }
}