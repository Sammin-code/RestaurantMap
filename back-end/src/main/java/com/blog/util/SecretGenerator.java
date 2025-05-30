package com.blog.util;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class SecretGenerator {
  private static final SecureRandom secureRandom = new SecureRandom();

  public static String generateSecret() {
    byte[] randomBytes = new byte[64];
    secureRandom.nextBytes(randomBytes);
    return Base64.getEncoder().encodeToString(randomBytes);
  }

  public static void main(String[] args) {
    String secret = generateSecret();
    System.out.println("Generated JWT Secret: " + secret);
    System.out.println("\n請將此密鑰複製到您的環境變數中：");
    System.out.println("JWT_SECRET=" + secret);
  }
}