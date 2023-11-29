package lab7.Classes;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

public class ThreadInfo {
    private final SimpleStringProperty name;
    private final SimpleStringProperty state;
    private final SimpleStringProperty priority;

    public ThreadInfo(String name, String state, String priority) {
        this.name = new SimpleStringProperty(name);
        this.state = new SimpleStringProperty(state);
        this.priority = new SimpleStringProperty(priority);
    }

    public ObservableValue<String> nameProperty() {
        return name;
    }

    public ObservableValue<String> stateProperty() {
        return state;
    }

    public ObservableValue<String> priorityProperty() {
        return priority;
    }
}

