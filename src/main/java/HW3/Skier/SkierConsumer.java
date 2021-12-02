package HW3.Skier;

import HW3.Resort.ResortConsumer;
import HW3.Resort.ResortLiftRideDAO;
import HW3.Server.LiftRide;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SkierConsumer {
    private static final String TASK_QUEUE_NAME = "task_queue";
    private static Map<Integer, List<LiftRide>> skiersMap = new ConcurrentHashMap<>();
    private static Connection connection;
    private static int numThreads;
    private static final String EXCHANGE_NAME = "logs";
    private static final List<LiftRide> liftRides = Collections.synchronizedList(new ArrayList());

    private static SkierLiftRideDAO liftRideDao = new SkierLiftRideDAO();
    private static TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            try {
                synchronized (liftRides){
                    liftRideDao.create(liftRides);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    };


    public static void main(String[] argv) throws Exception {
        numThreads = Integer.parseInt(argv[0]);
        multiThreadRecv(numThreads);
    }


    private static void multiThreadRecv(int numThreads) throws IOException, TimeoutException {
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        ConnectionFactory factory = new ConnectionFactory();
        // Public Queue IP
        factory.setHost("52.23.168.91");
        // Private Queue IP
//        factory.setHost("172.31.80.101");
        factory.setUsername("admin");
        factory.setPassword("admin");
        factory.setConnectionTimeout(100000);
        try {connection = factory.newConnection(executorService);} catch (IOException e) {
//        try {connection = factory.newConnection();} catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        final Channel channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        String queueName = channel.queueDeclare().getQueue();
        System.out.println("queueName: " + queueName);
        channel.queueBind(queueName, EXCHANGE_NAME, "");
        channel.basicQos(1);

        List<Thread> threads = new ArrayList<>();
//        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {

            Runnable runnable = singleThreadRecv(channel, queueName);
            Thread recv = new Thread(runnable, "No." + i);
//            threads.add(recv);
//            recv.start();
            executorService.execute(runnable);
        }

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 1000, 1000*20);

        try {
//            Thread.getAllStackTraces()
            executorService.awaitTermination(100, TimeUnit.HOURS);
        } catch (Exception e) {
            System.out.println("Failing to await all threads to finish");
            e.printStackTrace();
        }

        executorService.shutdown();
        timer.cancel();
        // Wait until all threads are finish
//        executorService.awaitTermination();
        System.out.println("Finished all threads");
//        for (Thread t : threads) {
//            t.start();
//        }
    }

    private static Runnable singleThreadRecv(Channel channel, String queueName) throws IOException {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {

                    System.out.println(("Thread " + Thread.currentThread().getName() + " waiting for messages. To exit press CTRL+C"));

                    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                        String message = new String(delivery.getBody(), "UTF-8");
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

                        LiftRide liftRide = new Gson().fromJson(message, LiftRide.class);
                        System.out.println("Callback thread Name = " + Thread.currentThread().getName() + " Received " + liftRide.toString() + "'");
                        try {
                            storeMessage(liftRide);
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                    };

                    channel.basicConsume(queueName, false, deliverCallback, consumerTag -> {
                    });

                } catch (IOException e) {
                    System.out.println("Something went wrong in consuming message: \n" + e);
                    Logger.getLogger(ResortConsumer.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        };
        return runnable;
    }

    private static void storeMessage(LiftRide liftRide) throws SQLException {
//        int skierID = liftRide.getSkierID();
//
//        if (!skiersMap.containsKey(skierID)) {
//            skiersMap.put(skierID, Collections.synchronizedList(new ArrayList<LiftRide>()));
//        } else {
//            skiersMap.get(skierID).add(liftRide);
//        }
//        SkierLiftRideDAO liftRideDao = SkierLiftRideDAO.getInstance();
//        SkierLiftRideDAO liftRideDao = new SkierLiftRideDAO();
//        liftRideDao.create(liftRide);
        liftRides.add(liftRide);

    }
}


