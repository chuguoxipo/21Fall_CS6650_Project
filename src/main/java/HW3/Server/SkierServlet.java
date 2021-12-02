package HW3.Server;

import com.google.gson.Gson;

import com.google.gson.JsonElement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

@WebServlet(name = "HW2.Server.SkierServlet", value = "/HW2.Server.SkierServlet")
public class SkierServlet extends HttpServlet {
//  private static Log log = LogFactory.getLog(SkierServlet.class);
//  private static Log log = LogFactory.getLog(SkierServlet.class);
  private static final String TASK_QUEUE_NAME = "task_queue";
  private static final int numThreads = 200;
  private Connection connection;
  private ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
  private static final String EXCHANGE_NAME = "logs";

  @Override
  public void init() throws ServletException {
    super.init();
    System.out.println("running init");
//    log.info("log is printing: running init");
    ConnectionFactory factory = new ConnectionFactory();
//    factory.setHost("52.23.168.91");
    factory.setHost("172.31.80.101");
    factory.setUsername("admin");
    factory.setPassword("admin");
//    System.out.println("trying to connect: 52.23.168.91");
    System.out.println("trying to connect: 172.31.80.101");
    System.out.println("username: admin");
    System.out.println("password: admin");



    try {this.connection = factory.newConnection(executorService);} catch (IOException e) {
      e.printStackTrace();
    } catch (TimeoutException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    System.out.println("receiving doGet request.");
//    log.info("log is printing: receiving doGet request.");

    getSkierParams(req, res);
  }


  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    System.out.println("receiving doPost request.");
//    log.info("log is printing: receiving doPost request.");
    res.setContentType("text/plain");
    String urlPath = req.getPathInfo();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("missing parameters");
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)

//    System.out.println("line 72");
    if (!isUrlValid(urlParts)) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write("invalid parameters");
    } else {
      Gson gson = new Gson();

      StringBuilder sb = new StringBuilder();
      String s;
      while ((s = req.getReader().readLine()) != null) {
        sb.append(s);
      }
      String jsonIn = sb.toString();

      // TODO: process url params in `urlParts`
      int resortID = Integer.parseInt(urlParts[1]);
      String seasonID = urlParts[3];
      String dayID = urlParts[5];
      int skierID = Integer.parseInt(urlParts[7]);

      SkierURLParameters skierParams = new SkierURLParameters(resortID, seasonID, dayID, skierID);

      LiftRide liftRide1 = gson.fromJson(jsonIn, LiftRide.class);
      System.out.println(liftRide1.toString());

      JsonElement jsonElement = gson.toJsonTree(liftRide1);
      jsonElement.getAsJsonObject().addProperty("resortID", String.valueOf(skierParams.getResortID()));
      jsonElement.getAsJsonObject().addProperty("seasonID", String.valueOf(skierParams.getSeasonID()));
      jsonElement.getAsJsonObject().addProperty("dayID", String.valueOf(skierParams.getDayID()));

      String jsonOut = gson.toJson(jsonElement);
      LiftRide liftRide2 = gson.fromJson(jsonOut, LiftRide.class);
      System.out.println(liftRide2.toString());

      // Create new channel
      String message = jsonOut;
      Runnable runnable = new Runnable() {
        @Override
        public void run() {

          try {

            try (Channel channel = connection.createChannel()) {
              channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
              channel.basicPublish(EXCHANGE_NAME, "",
                      null,
                      message.getBytes("UTF-8")
              );
              System.out.println("Sent '" + message + "'");
            } catch (Exception e ) {
//          System.out.println("99");
              System.out.println(e);
//          e.printStackTrace();
              throw e;
            }
          } catch (IOException | TimeoutException e) {
//        System.out.println("105");
            System.out.println(e);
            e.printStackTrace();
          }
        }
      };

      executorService.execute(runnable);

      res.setStatus(HttpServletResponse.SC_CREATED);
      // do any sophisticated processing with urlParts which contains all the url params


      res.getWriter().write(" We've got your LiftRide info!");
    }
}



  private SkierURLParameters getSkierParams(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {

    res.setContentType("text/plain");
    String urlPath = req.getPathInfo();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("missing parameters");
      return null;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)

    if (!isUrlValid(urlParts)) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write("invalid parameters");
      return null;
    } else {
      res.setStatus(HttpServletResponse.SC_OK);
      // do any sophisticated processing with urlParts which contains all the url params
      // TODO: process url params in `urlParts`
      int resortID = Integer.parseInt(urlParts[1]);
      String seasonID = urlParts[3];
      String dayID = urlParts[5];
      int skierID = Integer.parseInt(urlParts[7]);

      SkierURLParameters skierParams = new SkierURLParameters(resortID, seasonID, dayID, skierID);
      res.getWriter().write("It works!");
      return skierParams;
    }
  }

  private boolean isUrlValid(String[] urlParts) {
    // TODO: validate the request url path according to the API spec
    // urlPath  = "/1/seasons/2019/days/1/skiers/123"
    // urlParts = [, 1, seasons, 2019, days, 1, skiers, 123]
    if (urlParts.length < 8) return false;

    boolean isEmpty = urlParts[0].isEmpty();
    boolean isValidResortID = isInteger(urlParts[1], 10);
    boolean seasons = urlParts[2].equals("seasons");
    boolean isValidSeasonID = !urlParts[3].isEmpty();
    boolean days = urlParts[4].equals("days");
    boolean isValidDayID = isInteger(urlParts[5], 10) && Integer.parseInt(urlParts[5]) >= 1
                            && Integer.parseInt(urlParts[5]) <= 366;
    boolean skiers = urlParts[6].equals("skiers");
    boolean isValidSkierID = isInteger(urlParts[7], 10);

    return (isEmpty && isValidResortID && seasons && isValidSeasonID && days && isValidDayID
            && skiers && isValidSkierID);
  }


  private static boolean isInteger(String s, int radix) {
    if(s.isEmpty()) return false;
    for(int i = 0; i < s.length(); i++) {
      if(i == 0 && s.charAt(i) == '-') {
        if(s.length() == 1) return false;
        else continue;
      }
      if(Character.digit(s.charAt(i),radix) < 0) return false;
    }
    return true;
  }
}
