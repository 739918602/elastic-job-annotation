package cn.zys.schedule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Title:
 * @Author: ZhangYuSai
 * @Date: 2018/8/15 16:10
 * @Version: 1.0.0
 */

    @SpringBootApplication
    public class JobApp {
        public JobApp() {
        }

        public static void main(String[] args) {
            SpringApplication.run(JobApp.class, args);
        }
    }

