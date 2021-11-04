package HW2.Server;

public class LiftRide {
  private int skierID;
  private int liftID;
  private int time;


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

  @Override
  public String toString() {
    return "LiftRide{" +
        "skierID=" + skierID +
        ", liftID=" + liftID +
        ", time=" + time +
        '}';
  }
}
