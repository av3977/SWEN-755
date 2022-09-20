package detectors;

import controller.IController;
import controller.RoadStatusReceiver;
import road.LocationStep;
import road.Road;
import road.RoadType;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ObstacleDetector {

    static BlockingQueue senderLiveQueue;
    public ObstacleDetector(BlockingQueue queue) {
        senderLiveQueue = queue;
    }

    private final int HEARTBEAT_INTERVAL = 2000;
    private Registry registry;

    public static int CURRENT_STEP = 0;
    public boolean stayActive;

    private IController receiverStubProgram;

    public static boolean isDetectorFailed() {
        return DETECTOR_FAILED;
    }

    private static boolean DETECTOR_FAILED;
    public void initialize() {
        try {
            registry = LocateRegistry.getRegistry("localhost");
            receiverStubProgram = (IController) registry.lookup("IController");
            this.stayActive = true;
        }catch (IOException | NotBoundException exception) {
            System.out.println("Sender initialize exception: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    public void sendMainHeatBeat(int location) {
        System.out.println("Inside send main heart beat..." + location);
        RandomAccessFile rd;
        FileChannel fc;
        MappedByteBuffer mem;
        try {
            while (true) {
                long currentTime = Calendar.getInstance().getTime().getTime();
                System.out.println("Detector (Sender): I am alive on step: " + (location++) + " at: " + currentTime);
                receiverStubProgram.readStatus(location);
                /* wait for 2 seconds before sending the next heart beat signal */
                Thread.sleep(HEARTBEAT_INTERVAL);
            }
        } catch (InterruptedException exception) {
            System.out.println("Sender side exception: " + exception.getMessage());
            exception.printStackTrace();
        } catch (RemoteException e) {
            System.out.println("Sender side Remote exception: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Send road report
     * @param location
     * @throws IOException
     */
    public void sendHeartBeat(int location) {
        while (true) {
            try {
                long currentTime = Calendar.getInstance().getTime().getTime();
                RoadStatusReceiver.previousHeartBeatTimeStamp = currentTime;
                System.out.println("Detector (Sender): I am alive on step: " + location + " on " + (Road.roadAhead[location]));
                receiverStubProgram.readStatus(location);
                /*wait for 2 seconds before sending the next heart beat signal*/
                RoadStatusReceiver.currentCoordinateStep = location;
                location++;
                Thread.sleep(HEARTBEAT_INTERVAL);
            } catch (ArrayIndexOutOfBoundsException indexOutOfBoundsException) {
                System.out.println("Seen array index out of bounds: " + indexOutOfBoundsException.getMessage());
                stayActive = false;
                this.stop();
                break;
            } catch (InterruptedException exception) {
                System.out.println("Exception while reporting road status: " + exception.getMessage());
            } catch (RemoteException e) {
                System.out.println("Sender received a RemoteException while reading status: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
        System.out.println("-------Sender thread stopped-------");
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

    public ObstacleDetector() {

    }

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
            System.out.println("Sender initialized");
            Road.buildRoad();
//            sender.sendMainHeatBeat(initiallocation);
            sender.sendHeartBeat(initiallocation);
        }catch(InterruptedException ex){
            ex.printStackTrace();
            System.out.println("Exception message: " + ex.getMessage());
        }
    }

    public void stop() {
        this.stayActive = false;
    }
}
