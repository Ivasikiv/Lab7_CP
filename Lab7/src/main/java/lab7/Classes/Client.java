package lab7.Classes;

import com.example.lab7.MyApplication;

import java.util.concurrent.Semaphore;

public class Client implements Runnable {
    private final Bank bank;
    private final String name;
    private final int withdrawAmount;
    private final int depositAmount;
    private final Semaphore semaphore;
    private volatile boolean isPaused = false; // Флаг для призупинення потоку
    private long startTime; // Час створення потоку
    private long endTime; // Час завершення виконання потоку

    public Client(Bank bank, String name, int withdrawAmount, int depositAmount, Semaphore semaphore) {
        this.bank = bank;
        this.name = name;
        this.withdrawAmount = withdrawAmount;
        this.depositAmount = depositAmount;
        this.semaphore = semaphore;
        this.startTime = System.currentTimeMillis(); // Запам'ятовуємо час створення потоку
    }

    @Override
    public void run() {
        try {
            semaphore.acquire(); // Acquiring the semaphore to start the operation

            synchronized (semaphore) {
                while (isPaused) {
                    semaphore.wait(); // Очікуємо на відновлення, якщо isPaused == true
                }
            }
            MyApplication.outputTextArea.appendText(name + " is trying to withdraw cash...\n");
            bank.withdrawCash(withdrawAmount);

            Thread.sleep(1000);

            MyApplication.outputTextArea.appendText(name + " is trying to deposit cash...\n");
            bank.depositCash(depositAmount);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            endTime = System.currentTimeMillis(); // Фіксуємо час завершення виконання
            semaphore.release(); // Releasing the semaphore after operation
        }
    }

    public void pauseClient() {
        isPaused = true; // Встановлення флагу призупинення
    }

    public void resumeClient() {
        isPaused = false; // Встановлення флагу відновлення
        synchronized (semaphore) {
            semaphore.notify(); // Повідомлення потоку, який чекає на відновлення
        }
    }

    public long getExecutionTime() {
        return endTime - startTime; // Повертає час виконання
    }
}
