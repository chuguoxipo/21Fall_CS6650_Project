package HW2.Server;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Consumer {
    private static final String TASK_QUEUE_NAME = "task_queue";
    private static Map<Integer, List<LiftRide>> skiersMap = new ConcurrentHashMap<>();
    private static Connection connection;
    private static int numThreads;

    public static void main(String[] argv) throws Exception {
        numThreads = Integer.parseInt(argv[0]);
        multiThreadRecv(numThreads);
    }


    private static void multiThreadRecv(int numThreads) throws IOException, TimeoutException {
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("52.23.168.91");
        factory.setUsername("admin");
        factory.setPassword("admin");
        factory.setConnectionTimeout(100000);
        try {connection = factory.newConnection(executorService);} catch (IOException e) {
//        try {connection = factory.newConnection();} catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        List<Thread> threads = new ArrayList<>();
//        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {
            Runnable runnable = singleThreadRecv();
            Thread recv = new Thread(runnable, "No." + i);
//            threads.add(recv);
//            recv.start();
            executorService.execute(runnable);
        }
        try {
//            Thread.getAllStackTraces()
            executorService.awaitTermination(100, TimeUnit.HOURS);
        } catch (Exception e) {
            System.out.println("Failing to await all threads to finish");
            e.printStackTrace();
        }

        executorService.shutdown();
        // Wait until all threads are finish
//        executorService.awaitTermination();
        System.out.println("Finished all threads");
//        for (Thread t : threads) {
//            t.start();
//        }
    }

    private static Runnable singleThreadRecv() throws IOException {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    final Channel channel = connection.createChannel();

                    channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
                    channel.basicQos(1);
                    System.out.println(("Thread " + Thread.currentThread().getName() + " waiting for messages. To exit press CTRL+C"));

                    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                        String message = new String(delivery.getBody(), "UTF-8");
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

                        LiftRide liftRide = new Gson().fromJson(message, LiftRide.class);
                        System.out.println("Callback thread Name = " + Thread.currentThread().getName() + " Received " + liftRide.toString() + "'");
                        storeMessage(liftRide);
                    };

                    channel.basicConsume(TASK_QUEUE_NAME, false, deliverCallback, consumerTag -> {
                    });

                } catch (IOException e) {
                    Logger.getLogger(Consumer.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        };
        return runnable;
    }

    private static void storeMessage(LiftRide liftRide) {
        int skierID = liftRide.getSkierID();

        if (!skiersMap.containsKey(skierID)) {
            skiersMap.put(skierID, Collections.synchronizedList(new ArrayList<LiftRide>()));
        } else {
            skiersMap.get(skierID).add(liftRide);
        }

    }
}


