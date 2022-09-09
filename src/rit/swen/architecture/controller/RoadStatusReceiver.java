package rit.swen.architecture.controller;

import rit.swen.architecture.monitor.FaultMonitor;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RoadStatusReceiver extends UnicastRemoteObject implements IController {

    private static final int MONITORING_INTERVAL = 4000;
    private static final String REGISTRY_HOST = "localhost";
    private static long previousHeartBeatTimeStamp;
    private static int currentCoordinateStep;

    public static long getPreviousHeartBeatTimeStamp() {
        return previousHeartBeatTimeStamp;
    }

    public static int getCurrentCoordinateStep() {
        return currentCoordinateStep;
    }

    protected RoadStatusReceiver() throws RemoteException {
    }

    @Override
    public void initializeReceiver() throws RemoteException {
        IController controllerStub;
        Registry registry;
        try {
            RoadStatusReceiver roadStatusReceiver = new RoadStatusReceiver();
            registry = LocateRegistry.getRegistry(REGISTRY_HOST);
            registry.rebind("Road Status Receiver: ", roadStatusReceiver);
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
    public void monitorLocalizationModule() throws RemoteException {
        while (true) {
            try {
                Thread.sleep(MONITORING_INTERVAL);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            if (!isAlive()) {
                System.out.println("Receiver: Hearbeat interval exceeded - Localization Component failed - View log for details");
                FaultMonitor.handleFault("Localization", this);
            }
        }
    }

    private boolean isAlive(){
        long interval = System.currentTimeMillis() - previousHeartBeatTimeStamp;
        int error = 100; //100ms error tolerable
        return interval <= (MONITORING_INTERVAL + error);
    }

    public static void main(String [] args) throws RemoteException {
        RoadStatusReceiver receiver = new RoadStatusReceiver();
        receiver.initializeReceiver();
        try{
            receiver.monitorLocalizationModule();
        }catch(Exception ex){
            System.out.println("Vehicle control - receiver exception  - " + ex.getMessage());
        }
    }
}
