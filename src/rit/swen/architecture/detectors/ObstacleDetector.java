package rit.swen.architecture.detectors;

import rit.swen.architecture.controller.IController;
import rit.swen.architecture.controller.RoadStatusReceiver;
import rit.swen.architecture.road.LocationStep;
import rit.swen.architecture.road.RoadType;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.concurrent.BlockingQueue;

public class ObstacleDetector implements Runnable{

    static BlockingQueue senderLiveQueue;
    public ObstacleDetector(BlockingQueue queue) {
        senderLiveQueue = queue;
    }

    private final int HEARTBEAT_INTERVAL = 2000;
    private Registry registry;

    public static int CURRENT_STEP = 0;
    private IController receiverStubProgram;
    private static boolean DETECTOR_FAILED;
    public void initialize() throws IOException, NotBoundException {
//        DETECTOR_FAILED = false;
        registry = LocateRegistry.getRegistry();
        receiverStubProgram = (IController) registry.lookup("IController");
    }

    /**
     * Send road report
     * @param location
     * @throws IOException
     */
    public void sendHeartBeat(int location) {
        LocationStep current_location = new LocationStep(toRoadType(location), Calendar.getInstance().getTime().getTime());
        System.out.println("Inside Sender's sendHeartBeat()..");
        while (true) {
            try {
                long currentTime = Calendar.getInstance().getTime().getTime();
                current_location = getStep(current_location.getCoordinateStep());

                String currentTimeStamp = String.format(String.valueOf(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")), currentTime);
                /**
                 * ToDO: Fix date format.
                 */

                RoadStatusReceiver.previousHeartBeatTimeStamp = currentTime;
                System.out.println("Detector (Sender): I am alive on step: " + (CURRENT_STEP++) + " at: " + currentTime);
                /*wait for 2 seconds before sending the next heart beat signal*/
                if (CURRENT_STEP >= 6) {
                    DETECTOR_FAILED = true;
                }

                if (!DETECTOR_FAILED) {
                    senderLiveQueue.put(true);
                } else {
                    senderLiveQueue.put(false);
                    System.out.println("EXITING SENDER..");
                    break;
                }
                Thread.sleep(HEARTBEAT_INTERVAL);
            } catch (InterruptedException exception) {
                System.out.println("Exception while reporting road status: " + exception.getMessage());
            }
        }
    }

    private static LocationStep getStep(int location){
        DecimalFormat df = new DecimalFormat("##.00");
        location +=1;
        long timeInSeconds = Calendar.getInstance().getTime().getTime();
        return new LocationStep(toRoadType(location), timeInSeconds);
    }

    private static RoadType toRoadType(int location) {
        RoadType type = RoadType.INVALID_READ;
        switch (location) {
            case 0:
                type = RoadType.NORMAL_ROAD;
                break;
            case 1:
                type = RoadType.WATER_SPLASH;
                break;
            case 2:
                type = RoadType.POT_HOLE;
                break;
        }
        return type;
    }

    public ObstacleDetector() {}

    public static void main(String [] args){
        int initiallocation;
        if(args.length == 0 ){
            initiallocation = 0;
        }
        else{
            initiallocation = Integer.valueOf(args[0]);
            CURRENT_STEP = initiallocation;
        }
        ObstacleDetector sender = new ObstacleDetector();
        try{
            sender.initialize();
            Thread.sleep(2000);
            sender.sendHeartBeat(initiallocation);
        }catch(NotBoundException | IOException | InterruptedException ex){
            System.out.println("Exception message: " + ex.getMessage());
            ex.printStackTrace();
        }
        System.out.println("sender initialized");
    }

    @Override
    public void run() {
        ObstacleDetector sender = new ObstacleDetector();
        try{
            sender.initialize();
            Thread.sleep(2000);
            System.out.println("sender initialized");
            // Run infinitely.
            sender.sendHeartBeat(0);
        }catch(NotBoundException | IOException | InterruptedException ex){
            System.out.println("Exception message: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
