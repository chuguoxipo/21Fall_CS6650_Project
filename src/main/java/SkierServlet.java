import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "SkierServlet", value = "/SkierServlet")
public class SkierServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {

    getSkierParams(req, res);
  }


  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    getSkierParams(req, res);

    Gson gson = new Gson();
//    try {
//      StringBuilder sb = new StringBuilder();
//      String s;
//      while ((s = req.getReader().readLine()) != null) {
//        sb.append(s);
//      }
//      LiftRide liftRide = gson.fromJson(sb.toString(), LiftRide.class);
//      res.getWriter().write(liftRide.toString());
//      System.out.println(liftRide.toString());
//    } catch (IOException e) {
//      e.printStackTrace();
//    }

}



  private void getSkierParams(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {

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

    if (!isUrlValid(urlParts)) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write("invalid parameters");
    } else {
      res.setStatus(HttpServletResponse.SC_OK);
      // do any sophisticated processing with urlParts which contains all the url params
      // TODO: process url params in `urlParts`
      int resortID = Integer.parseInt(urlParts[1]);
      String seasonID = urlParts[3];
      String dayID = urlParts[5];
      int skierID = Integer.parseInt(urlParts[7]);

      SkierURLParameters skierParams = new SkierURLParameters(resortID, seasonID, dayID, skierID);
//      res.getWriter().write("It works!");
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
