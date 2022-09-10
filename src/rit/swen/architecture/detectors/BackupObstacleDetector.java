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

public class BackupObstacleDetector {
    private final int HEARTBEAT_INTERVAL = 1500;
    private Registry registry;
    public static int CURRENT_STEP = 0;
    private IController receiverStubProgram;
    public void initialize() throws IOException, NotBoundException {

        /*get access to rmi registry once started*/
        registry = LocateRegistry.getRegistry();
        receiverStubProgram = (IController) registry.lookup("IController");
    }

    public void sendHeartBeat(int location) throws IOException{
        LocationStep current_location = new LocationStep(toRoadType(location), Calendar.getInstance().getTime().getSeconds());

        while(true){
            try {
                /*something that this highly available module does here, may crash*/
                int current_time = Calendar.getInstance().getTime().getSeconds();
                current_location = getStep(current_location.getCoordinateStep());

                /*report status, by sending a heartbeat signal to monitoring module*/
                receiverStubProgram.readStatus(current_location.getCoordinateStep());
                long currentTime = Calendar.getInstance().getTime().getTime();

                System.out.println("Detector (BackupSender): I am alive on step: " + (location++) + " at: " + currentTime);

                /*wait for 2 seconds before sending the next heart beat signal*/
                Thread.sleep(HEARTBEAT_INTERVAL);

                /*Deliberately not catching the Arithmetic Exception - / by 0*/
            }catch(InterruptedException | RemoteException ex){
                System.out.println("BackupSender exception: " + ex.getMessage());
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
            Thread.sleep(2000);
            backupSender.sendHeartBeat(initiallocation);
        }catch(NotBoundException | IOException | InterruptedException ex){
            ex.printStackTrace();
        }
        System.out.println("Backup Sender initialized");
    }
}
