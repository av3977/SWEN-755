package rit.swen.architecture.detectors;

import rit.swen.architecture.controller.IController;
import rit.swen.architecture.controller.RoadStatusReceiver;
import rit.swen.architecture.road.LocationStep;
import rit.swen.architecture.road.RoadType;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.concurrent.BlockingQueue;

public class BackupObstacleDetector implements Runnable{
    private final int HEARTBEAT_INTERVAL = 2000;
    private Registry registry;
    public static int CURRENT_STEP = 0;
    private static BlockingQueue senderQueueReference;
    private IController receiverStubProgram;

    public void setStayActivated(boolean stayActivated) {
        System.out.println("Setting stay Activated: " + stayActivated);
        this.stayActivated = stayActivated;
    }

    public boolean isStayActivated() {
        return stayActivated;
    }

    boolean stayActivated = true;

    public void initialize() throws IOException, NotBoundException {
        registry = LocateRegistry.getRegistry(1098);
        receiverStubProgram = (IController) registry.lookup("IController");
        senderQueueReference = RoadStatusReceiver.getSenderLiveQueue();
    }

    public void sendHeartBeat(int location) throws IOException{
        LocationStep current_location = new LocationStep(toRoadType(location), Calendar.getInstance().getTime().getSeconds());
        while(stayActivated){
            try {
                // read status after sending a heartbeat signal.
//                receiverStubProgram.readStatus(current_location.getCoordinateStep());
                long currentTime = Calendar.getInstance().getTime().getTime();

                System.out.println("Detector (BackupSender): I am alive on step: " + (location++) + " at: " + currentTime);
                // wait for 2000ms before sending next heartbeat signal.
                Thread.sleep(HEARTBEAT_INTERVAL);
            }catch(InterruptedException ex){
                System.out.println("BackupSender exception: " + ex.getMessage());
            }
        }
        System.out.println("------KILLED BACKUP SENDER------");
    }

    private static LocationStep getStep(int location){
        DecimalFormat df = new DecimalFormat("##.00");
        location +=1;
        long timeInSeconds = Calendar.getInstance().getTime().getTime();
        return new LocationStep(toRoadType(location), timeInSeconds);
    }

    private static RoadType toRoadType(int location) {
        if (location == 0) {
            return RoadType.NORMAL_ROAD;
        } else if (location == 1) {
            return RoadType.WATER_SPLASH;
        } else if (location == 2) {
            return RoadType.POT_HOLE;
        }
        return RoadType.INVALID_READ;
    }


    public static void main(String [] args){
        int initiallocation;

        if(args.length == 0 ){
            initiallocation = 0;
        }
        else{
            initiallocation = Integer.valueOf(args[0]);
        }
        BackupObstacleDetector backupSender = new BackupObstacleDetector();
        try{
            backupSender.initialize();
            senderQueueReference = ObstacleDetector.senderLiveQueue;
            System.out.println("Backup Sender initialized");
            backupSender.sendHeartBeat(initiallocation);
        }catch(NotBoundException | IOException ex){
            ex.printStackTrace();
        }
    }


    @Override
    public void run() {
        try{
            initialize();
            senderQueueReference = ObstacleDetector.senderLiveQueue;
            System.out.println("Backup Sender initialized");
            sendHeartBeat(RoadStatusReceiver.SENDER_LAST_STEP);
        }catch(NotBoundException | IOException ex){
            ex.printStackTrace();
        }
    }

    public void stop() {
        this.stayActivated = false;
    }
}
