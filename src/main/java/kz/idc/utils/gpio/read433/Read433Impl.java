package kz.idc.utils.gpio.read433;

public class Read433Impl implements Read433 {
    private final int RCSWITCH_MAX_CHANGES = 750;

    private final int[] timings = new int[RCSWITCH_MAX_CHANGES];
    //private int nReceivedValue = 0;

    private int changeCount = 0;
    private int lastTime = 0;
    private int repeatCount = 0;

    public int readDataByTime(){
        int data = 0;
        int time = (int) (System.nanoTime() / 1000);
        int duration = time - lastTime;
        if (duration > 5000 && duration > (timings[0] - 200) && duration < (timings[0] + 200) ) {
            repeatCount++;
            changeCount--;
            if (repeatCount == 2) {
                data = receiveProtocol(changeCount);
                repeatCount = 0;
            }
            changeCount = 0;
        } else if (duration > 5000) {
            changeCount = 0;
        }

        if (changeCount >= RCSWITCH_MAX_CHANGES) {
            changeCount = 0;
            repeatCount = 0;
        }
        timings[changeCount++] = duration;
        lastTime = time;
        return data;
    }

    private int receiveProtocol(int changeCount){
        int nReceivedValue = 0;
        StringBuilder binCode = new StringBuilder();
        long delay = timings[0] / 31;
        int nReceiveTolerance = 120;
        long delayTolerance = (long) (delay * nReceiveTolerance * 0.01);
        for (int i = 1; i < changeCount ; i = i+2) {
            if (timings[i] > delay - delayTolerance && timings[i] < delay+delayTolerance && timings[i+1] > delay*3-delayTolerance && timings[i+1] < delay*3+delayTolerance) {
                binCode.append("0");
            } else if (timings[i] > delay*3-delayTolerance && timings[i] < delay*3+delayTolerance && timings[i+1] > delay-delayTolerance && timings[i+1] < delay+delayTolerance) {
                binCode.append("1");
            } else {
                i = changeCount;
                binCode = new StringBuilder();
                nReceivedValue = 0;
            }
        }

        if (changeCount > 6 && !binCode.toString().equals("")) {    // ignore < 4bit values as there are no devices sending 4bit values => noise
            nReceivedValue = Integer.parseInt(binCode.toString(), 2);
            return nReceivedValue;
        }
        return nReceivedValue;
    }
}
