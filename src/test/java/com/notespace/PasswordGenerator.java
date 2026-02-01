package com.notespace;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 运行此类生成密码哈希值
 */
public class PasswordGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "admin123";
        String hashed = encoder.encode(password);

        System.out.println("========================================");
        System.out.println("密码: " + password);
        System.out.println("========================================");
        System.out.println("BCrypt 哈希值:");
        System.out.println(hashed);
        System.out.println("========================================");
        System.out.println();
        System.out.println("SQL 插入语句:");
        System.out.println("INSERT INTO t_user (username, email, password, status) ");
        System.out.println("VALUES ('admin', 'admin@notespace.com', '" + hashed + "', 1);");
        System.out.println("========================================");
    }
}
