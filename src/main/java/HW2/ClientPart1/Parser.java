package HW2.ClientPart1;


public class Parser {

  public static CommandLineIputs parse(String args[]) {
    int numThreads = 0, numSkiers = 0,
        numLifts = 40, numRuns = 10;
    String IP = null;

    for (int i = 0; i < args.length - 1; i++) {

      String currArg = args[i];
      String currValue = args[i + 1];
      if (currArg.equals(CommandLineConstants.numThreads)) {
        if (isValidNumThreads(currValue)) {
          numThreads = Integer.parseInt(currValue);
        }
      }
      if (currArg.equals(CommandLineConstants.numSkiers)) {
        if (isValidNumSkiers(currValue)) {
          numSkiers = Integer.parseInt(currValue);
        }
      }
      if (currArg.equals(CommandLineConstants.numLifts)) {
        if (isValidNumLifts(currValue)) {
          numLifts = Integer.parseInt(currValue);

        }
      }
      if (currArg.equals(CommandLineConstants.numRuns)) {
        if (isValidNumRuns(currValue)) {
          numRuns = Integer.parseInt(currValue);

        }
      }
      if (currArg.equals(CommandLineConstants.IP)) {
        IP = currValue;
      }
    }

    CommandLineIputs inputs = new CommandLineIputs(numThreads, numSkiers, numLifts, numRuns, IP);
    return inputs;
  }

  private static boolean isValidNumThreads(String s) {
    return (isInteger(s, 10) &&
            Integer.parseInt(s) >= 0 &&
            Integer.parseInt(s) <= 512);
  }

  private static boolean isValidNumSkiers(String s) {
    return (isInteger(s, 10) &&
            Integer.parseInt(s) >= 0 &&
            Integer.parseInt(s) <= 100000);
  }

  private static boolean isValidNumLifts(String s) {
    return (isInteger(s, 10) &&
            Integer.parseInt(s) >= 5 &&
            Integer.parseInt(s) <= 60);
  }

  private static boolean isValidNumRuns(String s) {
    return (isInteger(s, 10) &&
            Integer.parseInt(s) >= 0 &&
            Integer.parseInt(s) <= 20);
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
