package HW2.ClientPart2;

import HW2.ClientPart1.CommandLineIputs;
import HW2.ClientPart1.Parser;
import HW2.ClientPart1.ServiceUnavailableRetryHandler;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

public class Client2 {

  private static AtomicInteger successfulRequests = new AtomicInteger();
  protected static AtomicInteger unsuccessfulRequests = new AtomicInteger();


  public static void main (String[] args) throws InterruptedException {

    // Client Manager Instance
    PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
    cm.setMaxTotal(5000);
    cm.setDefaultMaxPerRoute(5000);

    // Client Instance
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

    List<SingleRequestPerformance> resultsRecorder = Collections.synchronizedList(new ArrayList<>());

    // Start
    long start = System.currentTimeMillis();

    // Phase 1
    System.out.println("Phase 1 started...");
    phaseLogic(phaseOneThreads, phaseOneSkierIDRange, phaseOneStartTime, phaseOneEndTime, numLifts,
        phaseOneThreadsCompleted, numRuns, numSkiers, phaseOneThreadsAllCompletionCounter, client,
        0.2, resultsRecorder, IP);
    // Phase 2
    System.out.println("Phase 2 started...");
    phaseLogic(phaseTwoThreads, phaseTwoSkierIDRange, phaseTwoStartTime, phaseTwoEndTime,
        numLifts,phaseTwoThreadsCompleted, numRuns, numSkiers, phaseTwoThreadsAllCompletionCounter, client,
        0.6, resultsRecorder, IP);
    // phase 3
    System.out.println("Phase 3 started...");
    phaseLogic(phaseThreeThreads, phaseThreeSkierIDRange, phaseThreeStartTime, phaseThreeEndTime,
        numLifts, phaseThreeThreadsCompleted, numRuns, numSkiers, phaseThreeThreadsAllCompletionCounter, client,
        0.1, resultsRecorder, IP);

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

    System.out.println("-----------------------------------------");

    // Write to CSV
    writeToCSV(resultsRecorder, numThreads);

    // Client Part2 output
    System.out.println("Mean response time is: " + getMeanResponseTime(resultsRecorder) + " milliseconds");
    System.out.println("Throughput in requests per second is: " + getThroughput(resultsRecorder));
    System.out.println("Median response time is: " + percentile(resultsRecorder, 50) + " milliseconds");
    System.out.println("p99 response time is: " + percentile(resultsRecorder, 99) + " milliseconds");
    System.out.println("Max response time is: " + getMaxResponseTime(resultsRecorder) + " milliseconds");
  }

  private static long getMeanResponseTime(List<SingleRequestPerformance> resultsRecorder) {
    long sum = 0;
    for (SingleRequestPerformance performance : resultsRecorder) {
      sum += performance.end - performance.start;
    }
    return sum / resultsRecorder.size();
  }
  private static long getThroughput(List<SingleRequestPerformance> resultsRecorder) {
    int totalRequests = resultsRecorder.size();
    long wallTime = resultsRecorder.get(totalRequests - 1).end - resultsRecorder.get(0).start;
    return totalRequests / (wallTime / 1000);
  }
  private static long getMaxResponseTime(List<SingleRequestPerformance> resultsRecorder) {
    long max = 0;
    for (SingleRequestPerformance performance : resultsRecorder) {
      max = Math.max(max, performance.end - performance.start);
    }
    return max;
  }

  private static long percentile(List<SingleRequestPerformance> resultsRecorder, double percentile) {
    Collections.sort(resultsRecorder, new Comparator<SingleRequestPerformance>() {
      @Override
      public int compare(SingleRequestPerformance o1, SingleRequestPerformance o2) {
        return (int) ((o1.end - o1.start) - (o2.end - o2.start));
      }
    });
    int index = (int) Math.ceil(percentile / 100.0 * resultsRecorder.size());
    return resultsRecorder.get(index-1).end - resultsRecorder.get(index-1).start;
  }

  private static void writeToCSV(List<SingleRequestPerformance> resultsRecorder, int numThreads) {
    try {
      FileWriter csvWriter = new FileWriter(numThreads + "_performanceResults.csv");
      csvWriter.append("StartTime" + "," + "EndTime" + "," + "TotalTimeUsed" + "," + "StatusCode" + "," + "RequestType" + "\n");
      for (SingleRequestPerformance performance : resultsRecorder) {
        csvWriter.append(performance.toString());
      }
      csvWriter.flush();
      csvWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void phaseLogic(int numThreads, int skierIDRange, int startTime, int endTime,
      int numLifts, CountDownLatch completed, int numRuns, int numSkiers,
      CountDownLatch phaseAllThreadsCompletionCounter, CloseableHttpClient client,
      double phaseNumOfRequestMultiplier, List<SingleRequestPerformance> singleRequestPerformanceList,
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
          i, additionalRequests, phaseAllThreadsCompletionCounter, singleRequestPerformanceList, IP);

      pendingStartThreads.add(thread);
    }

    for (Runnable thread : pendingStartThreads) {
      new Thread(thread).start();
    }

    completed.await();

  }

  private static Runnable getSingleThread(String json, int numRequests, CloseableHttpClient client, CountDownLatch completed,
      int currentIteration, int numAdditionalRequests, CountDownLatch phaseAllThreadsCompletionCounter,
      List<SingleRequestPerformance> singleRequestPerformanceList, String IP) {
    Runnable thread = () -> {
      for (int j = 0; j < numRequests; j++) {
        runSingleRequest(json, client, completed, singleRequestPerformanceList, IP);
      }

      if (currentIteration == 0) {
        for (int j = 0; j < numAdditionalRequests; j++) {
          runSingleRequest(json, client, completed, singleRequestPerformanceList, IP);
        }
      }

      completed.countDown();
      phaseAllThreadsCompletionCounter.countDown();
    };

    return thread;
  }

  private static void runSingleRequest(String json, CloseableHttpClient client, CountDownLatch completed, List<SingleRequestPerformance> singleRequestPerformanceList,
  String IP) {
    try {
      HttpPost httppost = new HttpPost(IP);
      httppost.setEntity(new StringEntity(json));
      httppost.setHeader("Accept", "application/json");
      httppost.setHeader("Content-type", "application/json");

      long start = System.currentTimeMillis();
      CloseableHttpResponse httpResponse = client.execute(httppost);
      long end = System.currentTimeMillis();
      int statusCode = httpResponse.getStatusLine().getStatusCode();
      String requestType = httppost.getMethod();
      SingleRequestPerformance performance = new SingleRequestPerformance(start, end, statusCode, requestType);
      singleRequestPerformanceList.add(performance);
      HttpEntity responseEntity = httpResponse.getEntity();
//            System.out.println(EntityUtils.toString(responseEntity));
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

  public static class SingleRequestPerformance {
    public long start;
    public long end;
    public int statusCode;
    public String requestType;

    public SingleRequestPerformance(long start, long end, int statusCode, String requestType) {
      this.start = start;
      this.end = end;
      this.statusCode = statusCode;
      this.requestType = requestType;
    }

    @Override
    public String toString() {
//            return "Start: " + start + " End: " + end + " TimeUsed: " + (end - start) + " StatusCode: "
//            + statusCode + " RequestType: " + requestType;
      return start + "," + end + "," + (end - start) + "," + statusCode + "," + requestType + "\n";
    }
  }
}
