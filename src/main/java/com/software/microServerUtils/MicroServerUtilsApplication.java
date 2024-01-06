package com.software.microServerUtils;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
//@EnableFeignClients
public class MicroServerUtilsApplication {
    public static void main(String[] args) {
        SpringApplication.run(MicroServerUtilsApplication.class, args);
    }
}
