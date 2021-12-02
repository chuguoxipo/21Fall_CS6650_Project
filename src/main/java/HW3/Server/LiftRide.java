package HW3.Server;

public class LiftRide {
  private int skierID;
  private int liftID;
  private int time;
  private int resortID;
  private String seasonID;
  private String dayID;


  public LiftRide(int skierID, int liftID, int time, int resortID, String seasonID,
      String dayID) {
    this.skierID = skierID;
    this.liftID = liftID;
    this.time = time;
    this.resortID = resortID;
    this.seasonID = seasonID;
    this.dayID = dayID;
  }

  public LiftRide(int skierID, int liftID, int time) {
    this.skierID = skierID;
    this.liftID = liftID;
    this.time = time;
  }

  public int getTime() {
    return time;
  }

  public int getLiftID() {
    return liftID;
  }

  public void setTime(int time) {
    this.time = time;
  }

  public void setLiftID(int liftID) {
    this.liftID = liftID;
  }

  public int getSkierID() {
    return skierID;
  }

  public void setSkierID(int skierID) {
    this.skierID = skierID;
  }

  public int getResortID() {
    return resortID;
  }

  public void setResortID(int resortID) {
    this.resortID = resortID;
  }

  public String getSeasonID() {
    return seasonID;
  }

  public void setSeasonID(String seasonID) {
    this.seasonID = seasonID;
  }

  public String getDayID() {
    return dayID;
  }

  public void setDayID(String dayID) {
    this.dayID = dayID;
  }

  @Override
  public String toString() {
    return "LiftRide{" +
        "skierID=" + skierID +
        ", liftID=" + liftID +
        ", time=" + time +
        ", resortID=" + resortID +
        ", seasonID='" + seasonID + '\'' +
        ", dayID='" + dayID + '\'' +
        '}';
  }
}
