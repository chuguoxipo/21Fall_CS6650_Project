package HW2.ClientPart1;

import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class SingleThreadLatency {
//  private static String url = "http://newskierservicelb-1-1525652022.us-east-1.elb.amazonaws.com/21Fall_CS6650_Project_war/skiers/1/seasons/2019/days/1/skiers/123";
  private static String url = "http://localhost:8080/21Fall_CS6650_Project_war_exploded/skiers/1/seasons/2019/days/1/skiers/123";

  public static void main(String[] args) {

    long start = System.currentTimeMillis();
    CloseableHttpClient client = HttpClientBuilder.create().build();

    for (int j = 0; j < 10000; j++) {
      String json = new StringBuilder()
          .append("{")
          .append("\"skierID\":\"1\",")
          .append("\"liftID\":\"2\",")
          .append("\"time\":\"20\"")
          .append("}").toString();

      try {
        HttpPost httppost = new HttpPost(url);
        httppost.setEntity(new StringEntity(json));
        httppost.setHeader("Accept", "application/json");
        httppost.setHeader("Content-type", "application/json");

        HttpResponse httpResponse = client.execute(httppost);
        HttpEntity responseEntity = httpResponse.getEntity();

        EntityUtils.consume(responseEntity);

      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    try {
      client.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    long end = System.currentTimeMillis();
    long wallTime = end - start;
    System.out.println("Took: " + wallTime + " milliseconds.");
    System.out.println("Expected Throughput in requests per second for a single thread is : "
        + 10000 / (wallTime / 1000));
  }
}
