package rit.swen.architecture.controller;

import rit.swen.architecture.SimulationStarter;
import rit.swen.architecture.detectors.ObstacleDetector;
import rit.swen.architecture.monitor.MonitoringSystem;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;

public class RoadStatusReceiver extends UnicastRemoteObject implements IController, Runnable {

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

    protected RoadStatusReceiver() throws RemoteException {
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
        IController controllerStub;
        Registry registry;
        try {
            RoadStatusReceiver roadStatusReceiver = new RoadStatusReceiver();
            registry = LocateRegistry.createRegistry(1098);
            registry.rebind("IController", roadStatusReceiver);
        } catch (Exception e) {
            System.out.println("Receiver: Exception : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void readStatus(int coordinateStep) throws RemoteException {
        this.currentCoordinateStep = coordinateStep;
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


            try {
//                if (!((boolean) senderLiveQueue.take())) {
                System.out.println("Sender report Queue [RS]: " + senderLiveQueue);
                SENDER_LAST_STEP = (int) senderLiveQueue.take();
                System.out.println("SENDER TOOK STEP: " + SENDER_LAST_STEP);
                if (senderLiveQueue.size() == 0 || ObstacleDetector.isDetectorFailed()) {
                    System.out.println("Sender isn't alive anymore...");
                    System.out.println("Time to activate backup sender");
                    System.out.println("Receiver: Heartbeat interval exceeded - Localization Component failed - View log for details");
                    MonitoringSystem.handleFault("Localization", this);
                }
            } catch (ClassCastException e) {
                continue;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    @Override
    public void run() {
        try{
            RoadStatusReceiver receiver = new RoadStatusReceiver();
            receiver.initializeReceiver();
            receiver.monitorDetectorModule();
        }catch(Exception ex){
            System.out.println("Vehicle control - receiver exception  - " + ex.getMessage());
        }
    }
}
