package com.project.summer.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SceneManager {

    private final ApplicationContext context;

    public void switchScene(Stage stage, String fxmlPath, Object controllerData) throws IOException {
        FXMLLoader loader = context.getBean(FXMLLoader.class);
        loader.setLocation(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        // Pass data to the new controller if it supports it
        Object controller = loader.getController();
        if (controller instanceof Controllable controllerInstance) {
            controllerInstance.initData(controllerData);
        }

        stage.setScene(new Scene(root));
        stage.setTitle("Coffee - Dashboard");
        stage.show();
    }
}

interface Controllable {
    void initData(Object data);
}
