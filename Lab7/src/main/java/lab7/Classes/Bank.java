package lab7.Classes;

import com.example.lab7.MyApplication;

public class Bank {
    private int totalCash;

    public Bank(int totalCash) {
        this.totalCash = totalCash;
    }

    public synchronized void withdrawCash(int amount) {
        if (totalCash >= amount) {
            totalCash -= amount;
            MyApplication.outputTextArea.appendText("Withdrawn: " + amount + " Total cash left: " + totalCash + "\n");
        } else {
            MyApplication.outputTextArea.appendText("Insufficient funds.\n");
        }
    }

    public synchronized void depositCash(int amount) {
        totalCash += amount;
        MyApplication.outputTextArea.appendText("Deposited: " + amount + " Total cash now: " + totalCash + "\n");
    }
}

