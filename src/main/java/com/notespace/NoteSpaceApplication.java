package com.notespace;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@MapperScan("com.notespace.mapper")
@EnableCaching
public class NoteSpaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NoteSpaceApplication.class, args);
        System.out.println("======================================");
        System.out.println("NoteSpace 后端服务启动成功！");
        System.out.println("访问地址: http://localhost:8080/api");
        System.out.println("======================================");
    }
}
