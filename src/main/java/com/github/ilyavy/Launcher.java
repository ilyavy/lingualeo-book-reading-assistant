package com.github.ilyavy;

import com.github.ilyavy.controller.App;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Launcher {
    public static void main(String[] args) {
        Application.launch(App.class);
    }
}
