package com.example.lab7;
import javafx.animation.AnimationTimer;
import javafx.geometry.HPos;
import lab7.Classes.*;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import static java.lang.Thread.sleep;

public class MyApplication extends Application {
    public static TextArea outputTextArea;
    int numberOfClients = 5;
    Bank bank = new Bank(1000);
    Semaphore semaphore = new Semaphore(1, true); // Semaphore with 1 permit for mutual exclusion
    TableView<ThreadInfo> threadTable;

    Client[] clients;
    Thread[] clientThreads;

    @Override
    public void start(Stage primaryStage) {
        // Вкладка 1 - Налаштування потоків та ресурсів
        TabPane tabPane = new TabPane();
        Tab tab1 = new Tab("Settings");
        tab1.setClosable(false);
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        gridPane.setVgap(10);
        gridPane.setHgap(10);

        Label threadsLabel = new Label("Number of Threads:");
        Spinner<Integer> threadsSpinner = new Spinner<>(1, 10, 1);

        Button startButton = new Button("Start Threads");

// Елементи для призупинення та відновлення потоку
        Label threadControlLabel = new Label("Thread Control:");
        Spinner<Integer> threadControlSpinner = new Spinner<>(1, 10, 1);
        Button suspendButton = new Button("Suspend");
        Button resumeButton = new Button("Resume");

        // Текстове поле для виведення повідомлень від потоків
        outputTextArea = new TextArea();
        outputTextArea.setEditable(true);
        outputTextArea.setPrefRowCount(10); // Кількість видимих рядків

        GridPane.setHalignment(suspendButton, HPos.CENTER);
        GridPane.setHalignment(resumeButton, HPos.CENTER);

        gridPane.add(threadsLabel, 0, 0);
        gridPane.add(threadsSpinner, 1, 0);
        // Додайте інші елементи до gridPane

        gridPane.add(threadControlLabel, 0, 1);
        gridPane.add(threadControlSpinner, 1, 1);
        gridPane.add(suspendButton, 0, 2);
        gridPane.add(resumeButton, 1, 2);

        VBox vBox = new VBox(10);
        vBox.getChildren().addAll(gridPane, startButton, outputTextArea);
        tab1.setContent(vBox);
        tabPane.getTabs().add(tab1);

        suspendButton.setOnAction(event -> {
            int threadNumber = threadControlSpinner.getValue();
            clients[threadNumber].pauseClient();
            outputTextArea.appendText("Thread " + (threadNumber - 1) + " suspended\n");

            updateTable();
        });

        resumeButton.setOnAction(event -> {
            int threadNumber = threadControlSpinner.getValue();
            clients[threadNumber].resumeClient();
            outputTextArea.appendText("Thread " + (threadNumber - 1) + " resumed\n");

            updateTable();
        });

        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                // Виконується кожен кадр

                if (now - lastUpdate >= 500_000_000L) { // 500_000_000L наносекунд = 500 мілісекунд
                    // Виконується кожні 500 мілісекунд
                    lastUpdate = now;
                    updateTable();
                }
            }
        };

        // Вкладка 2 - Інформація про потоки
        Tab tab2 = new Tab("Thread Info");
        tab2.setClosable(false);
        threadTable = new TableView<>();
        TableColumn<ThreadInfo, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());

        TableColumn<ThreadInfo, String> stateColumn = new TableColumn<>("State");
        stateColumn.setCellValueFactory(data -> data.getValue().stateProperty());

        TableColumn<ThreadInfo, String> priorityColumn = new TableColumn<>("Priority");
        priorityColumn.setCellValueFactory(data -> data.getValue().priorityProperty());

        startButton.setOnAction(event -> {
            List<ThreadInfo> threadInfoList = new ArrayList<>();
            numberOfClients = threadsSpinner.getValue();
            clients = new Client[numberOfClients];
            clientThreads = new Thread[numberOfClients];

            for (int i = 0; i < numberOfClients; i++) {
                Client client = new Client(bank, "Client " + (i + 1), (i + 1) * 100, (i + 1) * 50, semaphore);
                clients[i] = client;
                Thread clientThread = new Thread(client);
                clientThread.setPriority((int) (Math.random() * 10 + 1));
                clientThreads[i] = clientThread;
                clientThread.start();
                timer.start();

                threadInfoList.add(new ThreadInfo(clientThread.getName(), clientThread.getState().toString(), clientThread.getPriority() + ""));
            }
            threadTable.getItems().addAll(threadInfoList);
        });

        threadTable.getColumns().addAll(nameColumn, stateColumn, priorityColumn);
        tab2.setContent(threadTable);
        tabPane.getTabs().add(tab2);

        // Показати сцену
        Scene scene = new Scene(tabPane, 600, 400);
        primaryStage.setTitle("Thread Management");
        primaryStage.setScene(scene);
        primaryStage.show();

        // При закритті вікна завершити всі потоки
        primaryStage.setOnCloseRequest(event -> {
            for (int i = 0; i < numberOfClients; i++) {
                clientThreads[i].interrupt();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void updateTable() {
        List<ThreadInfo> threadInfoList = new ArrayList<>();
        for (int i = 0; i < numberOfClients; i++) {
            threadInfoList.add(new ThreadInfo(clientThreads[i].getName(), clientThreads[i].getState().toString(), clientThreads[i].getPriority() + ""));
        }
        threadTable.getItems().clear();
        threadTable.getItems().addAll(threadInfoList);
    }
}
