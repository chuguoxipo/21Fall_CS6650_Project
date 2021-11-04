package HW2.ClientPart1;

public class CommandLineIputs {

  private int numThreads;
  private int numSkiers;
  private int numLifts;
  private int numRuns;
  private String IP;

  public CommandLineIputs(int numThreads, int numSkiers, int numLifts, int numRuns,
      String IPOrPort) {
    this.numThreads = numThreads;
    this.numSkiers = numSkiers;
    this.numLifts = numLifts;
    this.numRuns = numRuns;
    this.IP = IPOrPort;
  }

  public int getNumThreads() {
    return numThreads;
  }

  public void setNumThreads(int numThreads) {
    this.numThreads = numThreads;
  }

  public int getNumSkiers() {
    return numSkiers;
  }

  public void setNumSkiers(int numSkiers) {
    this.numSkiers = numSkiers;
  }

  public int getNumLifts() {
    return numLifts;
  }

  public void setNumLifts(int numLifts) {
    this.numLifts = numLifts;
  }

  public int getNumRuns() {
    return numRuns;
  }

  public void setNumRuns(int numRuns) {
    this.numRuns = numRuns;
  }

  public String getIP() {
    return IP;
  }

  public void setIP(String IP) {
    this.IP = IP;
  }

  @Override
  public String toString() {
    return "HW2.ClientPart1.CommandLineIputs{" +
        "numThreads=" + numThreads +
        ", numSkiers=" + numSkiers +
        ", numLifts=" + numLifts +
        ", numRuns=" + numRuns +
        ", IP='" + IP + '\'' +
        '}';
  }
}
