package ClientPart1;

import org.apache.http.HttpResponse;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.protocol.HttpContext;

public class ServiceUnavailableRetryHandler implements ServiceUnavailableRetryStrategy {

  private final int okCode = 201;
  private final long retryInternal = 500;
  private final int maxRetries = 5;

  @Override
  public boolean retryRequest(HttpResponse response, int executionCount, HttpContext context) {

    int statusCode = response.getStatusLine().getStatusCode();
    String errMsg = String.format("Service Unavailable - StatusCode: %d, Attempt#: %d/%d",
        statusCode, executionCount, maxRetries);

    if (executionCount > maxRetries) {
      System.out.println(errMsg);
      Client1.unsuccessfulRequests.incrementAndGet();
      return false;

    }

    if (statusCode != okCode) {
      System.out.println(errMsg);
      return true;
    }
    return false;
  }

  @Override
  public long getRetryInterval() {
    return this.retryInternal;
  }
}
