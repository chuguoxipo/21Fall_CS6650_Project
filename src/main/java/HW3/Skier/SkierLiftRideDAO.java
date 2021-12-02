package HW3.Skier;

import HW3.Resort.ResortConnectionManager;
import HW3.Resort.ResortLiftRideDAO;
import HW3.Server.LiftRide;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SkierLiftRideDAO {
  private static int BATCH_PROCESS_SIZE = 100;

  protected ResortConnectionManager connectionManager;

  // Single pattern: instantiation is limited to one object.
  private static ResortLiftRideDAO instance = null;
  Connection connection = null;

  //  String insertLiftRide = "INSERT IGNORE INTO LiftRide(SkierID,LiftID,Time,ResortID,SeasonID,DayID) VALUES(?,?,?,?,?,?);";
  String insertLiftRide = "INSERT IGNORE INTO LiftRide(SkierID,LiftID,Time,ResortID,SeasonID,DayID) VALUES ";
  PreparedStatement insertStmt;
  protected SkierLiftRideDAO() {

    connectionManager = new ResortConnectionManager();
    try {
      connection = connectionManager.getConnection("Skier");

    } catch (SQLException e) {
      e.printStackTrace();

    }
  }
  public static SkierLiftRideDAO getInstance() {
//    if(instance == null) {
//      instance = new SkierLiftRideDAO();
//    }
//    return instance;
    return new SkierLiftRideDAO();
  }



  AtomicInteger batchCount =  new AtomicInteger();

  StringBuffer buffer = new StringBuffer(insertLiftRide);

  /**
   * Save the LiftRide instance by storing it in your MySQL instance.
   * This runs a INSERT statement.
   */
  public void create(List<LiftRide> liftRides) throws SQLException {

    try {
      if (!liftRides.isEmpty()) {
        for (LiftRide liftRide : liftRides) {
          StringBuffer statement = new StringBuffer();
          statement.append(" (");
          statement.append(liftRide.getSkierID());
          statement.append(",");
          statement.append(liftRide.getLiftID());
          statement.append(",");
          statement.append(liftRide.getTime());
          statement.append(",");
          statement.append(liftRide.getResortID());
          statement.append(",");
          statement.append(liftRide.getSeasonID());
          statement.append(",");
          statement.append(liftRide.getDayID());
          statement.append("),");
          buffer.append(statement);
        }

        buffer.deleteCharAt(buffer.length() - 1);
        buffer.append(";");
        String completeStmt = buffer.toString();
        System.out.println("completeStmt of Resort: " + completeStmt);
        insertStmt = connection.prepareStatement(completeStmt);
        insertStmt.executeUpdate();

        insertStmt.clearParameters();
//        insertStmt.close();
        batchCount.set(0);
        buffer = new StringBuffer(insertLiftRide);
        liftRides.clear();
      }



//      insertStmt = connection.prepareStatement(insertLiftRide);
//      insertStmt.setInt(1, liftRide.getSkierID());
//      insertStmt.setInt(2, liftRide.getLiftID());
//      insertStmt.setInt(3, liftRide.getTime());
//      insertStmt.setInt(4, liftRide.getResortID());
//      insertStmt.setString(5, liftRide.getSeasonID());
//      insertStmt.setString(6, liftRide.getDayID());

//      insertStmt.addBatch();
//      if(batchCount.get() == BATCH_PROCESS_SIZE) {
//
//      } else {
//        batchCount.incrementAndGet();
//      }

//      return liftRide;
    } catch (SQLException e) {
      e.printStackTrace();
      throw e;
    }
    finally {
//      if(connection != null) {
//        connection.close();
//      }
//      if(insertStmt != null) {
//        insertStmt.close();
//      }
    }


  }

}
