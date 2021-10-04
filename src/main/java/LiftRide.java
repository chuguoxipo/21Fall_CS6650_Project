public class LiftRide {
  private int time;
  private int liftID;


  public LiftRide(int time, int liftID) {
    this.time = time;
    this.liftID = liftID;
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

  @Override
  public String toString() {
    return "LiftRide{" +
        "time=" + time +
        ", liftID=" + liftID +
        '}';
  }
}