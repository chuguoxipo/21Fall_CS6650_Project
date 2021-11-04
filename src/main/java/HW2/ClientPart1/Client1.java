package HW2.ClientPart1;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

public class Client1 {

  private static AtomicInteger successfulRequests = new AtomicInteger();
  protected static AtomicInteger unsuccessfulRequests = new AtomicInteger();


  public static void main (String[] args) throws InterruptedException {

    PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
    cm.setMaxTotal(5000);
    cm.setDefaultMaxPerRoute(5000);

    CloseableHttpClient client = HttpClientBuilder
        .create()
        .setRetryHandler(new HttpRequestRetryHandler() {
          @Override
          public boolean retryRequest(IOException e, int count, HttpContext contr) {
            if (count >= 5) {
              // Do not retry if over max retry count
              unsuccessfulRequests.incrementAndGet();
              return false;
            }
            if (e instanceof InterruptedIOException) {
              // Timeout
              return true;
            }
            return false;
          }
        })
        .setServiceUnavailableRetryStrategy(new ServiceUnavailableRetryHandler())
        .setConnectionManager(cm)
        .build();

    // Deal with command Line inputs
//    System.out.println(args[1]);
    CommandLineIputs inputs = Parser.parse(args);

    int numThreads = inputs.getNumThreads();
    int numSkiers = inputs.getNumSkiers();
    int numLifts = inputs.getNumLifts();
    int numRuns = inputs.getNumRuns();
    String IP = inputs.getIP();

    // Number of Threads
    int phaseOneThreads = numThreads / 4;
    int phaseTwoThreads = numThreads;
    int phaseThreeThreads = numThreads / 4;

    CountDownLatch phaseOneThreadsAllCompletionCounter = new CountDownLatch(phaseOneThreads);
    CountDownLatch phaseTwoThreadsAllCompletionCounter = new CountDownLatch(phaseTwoThreads);
    CountDownLatch phaseThreeThreadsAllCompletionCounter = new CountDownLatch(phaseThreeThreads);

    // Skier ID Range
    System.out.println("phaseOneThreads: " + phaseOneThreads);
    System.out.println("phaseTwoThreads: " + phaseTwoThreads);
    System.out.println("phaseThreeThreads: " + phaseThreeThreads);
    int phaseOneSkierIDRange = getSkierIDRange(phaseOneThreads, numSkiers);
    int phaseTwoSkierIDRange = getSkierIDRange(phaseTwoThreads, numSkiers);
    int phaseThreeSkierIDRange = getSkierIDRange(phaseThreeThreads, numSkiers);

    // Get start time & end time
    int phaseOneStartTime = 1;
    int phaseOneEndTime = 90;
    int phaseTwoStartTime = 91;
    int phaseTwoEndTime = 360;
    int phaseThreeStartTime = 361;
    int phaseThreeEndTime = 420;

    // Instances of CountDownLatch
    CountDownLatch phaseOneThreadsCompleted = new CountDownLatch((phaseOneThreads + 10 - 1) / 10);
    CountDownLatch phaseTwoThreadsCompleted = new CountDownLatch((phaseTwoThreads + 10 - 1) / 10);
    CountDownLatch phaseThreeThreadsCompleted = new CountDownLatch(phaseThreeThreads);

    // Start
    long start = System.currentTimeMillis();

    // Phase 1
    phaseLogic(phaseOneThreads, phaseOneSkierIDRange, phaseOneStartTime, phaseOneEndTime, numLifts,
        phaseOneThreadsCompleted, numRuns, numSkiers, phaseOneThreadsAllCompletionCounter, client,
        0.2, IP);
    // Phase 2
    phaseLogic(phaseTwoThreads, phaseTwoSkierIDRange, phaseTwoStartTime, phaseTwoEndTime,
        numLifts,phaseTwoThreadsCompleted, numRuns, numSkiers, phaseTwoThreadsAllCompletionCounter, client,
        0.6, IP);
    // phase 3
    phaseLogic(phaseThreeThreads, phaseThreeSkierIDRange, phaseThreeStartTime, phaseThreeEndTime,
        numLifts, phaseThreeThreadsCompleted, numRuns, numSkiers, phaseThreeThreadsAllCompletionCounter, client,
        0.1, IP);


    // Final Printout
    phaseOneThreadsAllCompletionCounter.await();
    phaseTwoThreadsAllCompletionCounter.await();
    phaseThreeThreadsAllCompletionCounter.await();

    // End
    long end = System.currentTimeMillis();

    // Client Part1 output
    System.out.println("Number of successful requests sent: " + successfulRequests);
    System.out.println("Number of unsuccessful requests sent: " + unsuccessfulRequests);
    long wallTime = (end - start);
    System.out.println("Took: " + wallTime + " milliseconds.");
    System.out.println("Total throughput in requests per second: " +
        successfulRequests.doubleValue() / (wallTime/1000));
  }

  private static void phaseLogic(int numThreads, int skierIDRange, int startTime, int endTime,
      int numLifts, CountDownLatch completed, int numRuns, int numSkiers,
      CountDownLatch phaseAllThreadsCompletionCounter, CloseableHttpClient client, double phaseNumOfRequestMultiplier,
      String IP) throws InterruptedException {

    int numRequests = (int) (numRuns * numSkiers * phaseNumOfRequestMultiplier) / numThreads;
    int additionalRequests = 0;

    if ((numRuns * numSkiers * phaseNumOfRequestMultiplier) % numThreads != 0) {
      additionalRequests = (int) (numRuns * numSkiers * phaseNumOfRequestMultiplier) % numThreads;
    }

    List<Runnable> pendingStartThreads = new ArrayList<>();

    for (int i = 0; i < numThreads; i++) {
      int startSkierID = getStartSkierID(i, skierIDRange);
      int endSkierID = getEndSkierID(i, skierIDRange);
      if (startSkierID > numSkiers) {
        continue;
      }
      String json;
      if (endSkierID > numSkiers) {
        json = getJsonMessage(startSkierID, numSkiers,
            startTime, endTime, numLifts);
      } else {
        json = getJsonMessage(startSkierID, endSkierID,
            startTime, endTime, numLifts);
      }

      Runnable thread = getSingleThread(json, numRequests, client, completed,
          i, additionalRequests, phaseAllThreadsCompletionCounter, IP);

      pendingStartThreads.add(thread);
    }

    for (Runnable thread : pendingStartThreads) {
      new Thread(thread).start();
    }

    completed.await();
  }

  private static Runnable getSingleThread(String json, int numRequests, CloseableHttpClient client, CountDownLatch completed,
      int currentIteration, int numAdditionalRequests, CountDownLatch phaseAllThreadsCompletionCounter,
      String IP) {
    Runnable thread = () -> {
      for (int j = 0; j < numRequests; j++) {
        runSingleRequest(json, client, completed, IP);
      }

      if (currentIteration == 0) {
        for (int j = 0; j < numAdditionalRequests; j++) {
          runSingleRequest(json, client, completed, IP);
        }
      }

      completed.countDown();
      phaseAllThreadsCompletionCounter.countDown();
    };

    return thread;
  }

  private static void runSingleRequest(String json, CloseableHttpClient client, CountDownLatch completed,
      String IP) {
    try {
      HttpPost httppost = new HttpPost(IP);
      httppost.setEntity(new StringEntity(json));
      httppost.setHeader("Accept", "application/json");
      httppost.setHeader("Content-type", "application/json");

      CloseableHttpResponse httpResponse = client.execute(httppost);
      HttpEntity responseEntity = httpResponse.getEntity();
      EntityUtils.consume(responseEntity);
      successfulRequests.incrementAndGet();
    } catch (Exception e) {
      e.printStackTrace();
      unsuccessfulRequests.incrementAndGet();
      completed.countDown();
    }
  }

  private static String getJsonMessage(int startSkierID, int endSkierID,
      int startTime, int endTime, int numLifts) {
    String skierID = String.valueOf(getRandom(startSkierID, endSkierID));
    String liftID = String.valueOf(getRandom(1, numLifts));
    String time = String.valueOf(getRandom(startTime, endTime));

    String json = new StringBuilder()
        .append("{")
        .append("\"skierID\":" + skierID + ",")
        .append("\"liftID\":" + liftID + ",")
        .append("\"time\":" + time)
        .append("}").toString();

    return json;
  }

  private static int getSkierIDRange ( int numThreads, int numSkiers) {
    return numSkiers / numThreads;
  }

  private static int getStartSkierID (int i, int skierIDRange) {
    return i * skierIDRange + 1;
  }

  private static int getEndSkierID (int i, int skierIDRange) {
    return (i + 1) * skierIDRange;
  }

  private static int getRandom (int min, int max) {
    return ThreadLocalRandom.current().nextInt(min, max + 1);
  }
}
