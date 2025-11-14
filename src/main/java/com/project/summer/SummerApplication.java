package com.project.summer;

import com.project.summer.view.JavaFxApplication;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SummerApplication {

    public static void main(String[] args) {
        Application.launch(JavaFxApplication.class, args);
    }
}
