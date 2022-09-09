package rit.swen.architecture.detectors;

import rit.swen.architecture.controller.IController;
import rit.swen.architecture.road.LocationStep;
import rit.swen.architecture.road.RoadType;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.SQLOutput;
import java.text.DecimalFormat;
import java.util.Calendar;

public class ObstacleDetector {

    private final int HEARTBEAT_INTERVAL = 2000;
    private Registry registry;

    private IController controller;
    public void initialize() throws IOException, NotBoundException {

        /**
         * render RMI registry.
         */
        registry = LocateRegistry.getRegistry();
        controller = (IController) registry.lookup("ReceiverInterface");

    }

    /**
     * Send road report
     * @param location
     * @throws IOException
     */
    public void sendHeartBeat(int location) {
        LocationStep current_location = new LocationStep(toRoadType(location), Calendar.getInstance().getTime().getTime());
        while (true) {
            try {
                long currentTime = Calendar.getInstance().getTime().getTime();
                current_location = getStep(current_location.getCoordinateStep());
                System.out.println("Detector: I am alive at: " + currentTime);
                /*wait for 2 seconds before sending the next heart beat signal*/
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


    public static void main(String [] args){
        int initiallocation;
        if(args.length == 0 ){
            initiallocation = 0;
        }
        else{
            initiallocation = Integer.valueOf(args[0]);
        }
        ObstacleDetector sender = new ObstacleDetector();
        try{
            sender.initialize();
            Thread.sleep(2000);
            sender.sendHeartBeat(initiallocation);

        }catch(NotBoundException | IOException | InterruptedException ex){
            ex.printStackTrace();
        }
        System.out.println("sender initialized");
    }
}
