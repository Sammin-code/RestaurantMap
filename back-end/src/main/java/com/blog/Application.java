package com.blog;

import com.blog.util.SecretGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

/**
 * Class Name: com.blog.Application
 * Package: PACKAGE_NAME
 * Description:
 * author:
 * Create: 2025/3/4
 * Version: 1.0
 */
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        System.out.println("=== APP STARTED AT " + System.currentTimeMillis() + " ===");
        SpringApplication.run(Application.class, args);
    }

    @EventListener(ContextRefreshedEvent.class)
    public void onApplicationEvent() {
        String secret = SecretGenerator.generateSecret();
        System.out.println("\n=== JWT Secret 生成成功 ===");
        System.out.println("請將此密鑰複製到您的環境變數中：");
        System.out.println("JWT_SECRET=" + secret);
        System.out.println("========================\n");
    }
}
