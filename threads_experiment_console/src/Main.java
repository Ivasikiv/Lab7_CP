import java.util.Map;
import java.util.concurrent.Semaphore;

import static java.lang.Thread.sleep;

class Bank {
    private int totalCash;

    public Bank(int totalCash) {
        this.totalCash = totalCash;
    }

    public synchronized void withdrawCash(int amount) {
        if (totalCash >= amount) {
            totalCash -= amount;
            System.out.println("Withdrawn: " + amount + " Total cash left: " + totalCash);
        } else {
            System.out.println("Insufficient funds.");
        }
    }

    public synchronized void depositCash(int amount) {
        totalCash += amount;
        System.out.println("Deposited: " + amount + " Total cash now: " + totalCash);
    }
}

class MyRunnable implements Runnable {
    private volatile boolean isRunning = true;
    public String threadName;

    public MyRunnable(String threadName) {
        this.threadName = threadName;
    }

    public void run() {
        while (isRunning) {
            try {
                Thread.sleep(5000); // Пауза у 5 секунд
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void stopThread() {
        isRunning = false;
    }

    public void startThread() {
        isRunning = true;
    }
}


class Client implements Runnable {
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

            System.out.println(name + " is trying to withdraw cash...");
            bank.withdrawCash(withdrawAmount);

            Thread.sleep(1000);

            System.out.println(name + " is trying to deposit cash...");
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


public class Main {
    public static void main(String[] args) {
        int numberOfClients = 5;
        Bank bank = new Bank(1000);
        Semaphore semaphore = new Semaphore(1, true); // Semaphore with 1 permit for mutual exclusion

        Client[] clients = new Client[numberOfClients];
        Thread[] clientThreads = new Thread[numberOfClients];

        for (int i = 0; i < numberOfClients; i++) {
            Client client = new Client(bank, "Client " + (i + 1), (i + 1) * 100, (i + 1) * 50, semaphore);
            clients[i] = client;
            Thread clientThread = new Thread(client);
            clientThreads[i] = clientThread;
            clientThread.start();
        }

        // Призупиняємо перший потік на деякий час
        try {
            sleep(2000);
            clients[2].pauseClient();
            System.out.println("Client 3 is paused...");
        } catch (Exception e) {
            e.printStackTrace();
        }


        System.out.println("----------------------------------------------------------------------------");
        Map<Thread, StackTraceElement[]> allThreads = Thread.getAllStackTraces();
        for (Map.Entry<Thread, StackTraceElement[]> entry : allThreads.entrySet()) {
            Thread thread2 = entry.getKey();
            System.out.println("Thread name: " + thread2.getName() + ", State: " + thread2.getState() + ", Priority: " + thread2.getPriority());
        }
        System.out.println("----------------------------------------------------------------------------");

        // Відновлюємо роботу призупиненого потоку
        try {
            sleep(5000);
            clients[2].resumeClient();
            System.out.println("Client 3 is resumed...");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //get execution time
        for (int i = 0; i < numberOfClients; i++) {
            System.out.println("Client " + (i + 1) + " execution time: " + clients[i].getExecutionTime() + " ms");
        }

        System.out.println("----------------------------------------------------------------------------");
        for (Map.Entry<Thread, StackTraceElement[]> entry : allThreads.entrySet()) {
            Thread thread2 = entry.getKey();
            System.out.println("Thread name: " + thread2.getName() + ", State: " + thread2.getState() + ", Priority: " + thread2.getPriority());
        }
        System.out.println("----------------------------------------------------------------------------");
    }
}


//    Map<Thread, StackTraceElement[]> allThreads = Thread.getAllStackTraces();
//        for (Map.Entry<Thread, StackTraceElement[]> entry : allThreads.entrySet()) {
//    Thread thread2 = entry.getKey();
//    System.out.println("Thread name: " + thread2.getName() + ", State: " + thread2.getState());
//    }