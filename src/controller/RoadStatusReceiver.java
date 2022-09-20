package controller;

import monitor.MonitoringSystem;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;

public class RoadStatusReceiver extends UnicastRemoteObject implements IController {

    private static final int MONITORING_INTERVAL = 4000;
    private static final String REGISTRY_HOST = "localhost";
    public static long previousHeartBeatTimeStamp;
    private static int currentCoordinateStep;
    public static int SENDER_LAST_STEP = -1;
    public static BlockingQueue senderLiveQueue;
    public RoadStatusReceiver(BlockingQueue queue) throws RemoteException {
        super();
        senderLiveQueue = queue;
    }

    public RoadStatusReceiver() throws RemoteException {
        super();
    }

    public static long getPreviousHeartBeatTimeStamp() {
        return previousHeartBeatTimeStamp;
    }

    public static int getCurrentCoordinateStep() {
        return currentCoordinateStep;
    }

    public static BlockingQueue getSenderLiveQueue() {
        return senderLiveQueue;
    }

    @Override
    public void initializeReceiver() throws RemoteException {
        try {
            RoadStatusReceiver roadStatusReceiver = new RoadStatusReceiver();
            Registry registry = LocateRegistry.getRegistry(REGISTRY_HOST);
            registry.rebind("IController", roadStatusReceiver);
        } catch (Exception e) {
            System.out.println("Receiver Exception : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void readStatus(int coordinateStep) throws RemoteException {
        previousHeartBeatTimeStamp = System.currentTimeMillis();
        final String currentTimeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println("Central Controller: current coordinate step: " + coordinateStep);
        System.out.println("Central Controller: Received heartbeat signal at : " + currentTimeStamp);
    }

    @Override
    public void monitorDetectorModule() throws RemoteException {
        while (true) {
            try {
                Thread.sleep(MONITORING_INTERVAL);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            if (!isAlive()) {
                System.out.println("Receiver: Heartbeat interval exceeded - Detector Component failed - View log for details");
                MonitoringSystem.handleFault("Detector", this);
            }
        }
    }
    private boolean isAlive(){
        long interval = System.currentTimeMillis() - previousHeartBeatTimeStamp;
        int error = 100; //100ms error tolerable
        return true;
//        return interval <= (MONITORING_INTERVAL + error);
    }
    public static void main(String[] args) {
        try{

            RoadStatusReceiver receiver = new RoadStatusReceiver();
            receiver.initializeReceiver();
            Thread.sleep(2000);
            System.out.println("Receiver initialized");
            receiver.monitorDetectorModule();
        }catch(Exception ex){
            System.out.println("receiver main exception  - " + ex.getMessage());
        }
    }
}
