package controller;

import monitor.MonitoringSystem;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Stream;

public class RoadStatusReceiver extends UnicastRemoteObject implements IController {

    private static final int MONITORING_INTERVAL = 2000;
    private static final String REGISTRY_HOST = "localhost";
    public static long previousHeartBeatTimeStamp = System.currentTimeMillis();
    public static int currentCoordinateStep;
    public static int FILE_POINTER = 0;

    public void setSENDER_LAST_STEP(int SENDER_LAST_STEP) {
        this.SENDER_LAST_STEP = SENDER_LAST_STEP;
    }

    public int getSENDER_LAST_STEP() {
        return SENDER_LAST_STEP;
    }

    private int SENDER_LAST_STEP = -1;
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
    public void readStatus(int coordinateStep, String processName) throws RemoteException {
        previousHeartBeatTimeStamp = System.currentTimeMillis();
        final String currentTimeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println(String.format("Central Controller %s: current coordinate step: %s", processName, coordinateStep));
        this.setSENDER_LAST_STEP(coordinateStep);
//        System.out.println("Central Controller: Received heartbeat signal at : " + currentTimeStamp);
    }

    @Override
    public void monitorDetectorModule() throws RemoteException {
        while (true) {
            try {
                String line = readCharsFromFile(SHARED_FILE, FILE_POINTER);
                if (!"".equals(line)) {
                    System.out.println("Read step: " +line);
                    FILE_POINTER+=1;
                }
                Thread.sleep(MONITORING_INTERVAL);
            } catch (InterruptedException | IOException e) {
                System.out.println(e.getMessage());
            }
            if (!isAlive()) {
                System.out.println("Receiver: Heartbeat interval exceeded - Detector Component failed - View log for details");
                MonitoringSystem.handleFault("Detector", this, currentCoordinateStep);
            }
        }
    }
    final String SHARED_FILE = "."+ File.separator +"src" + File.separator + "logs"+ File.separator +"file.txt";
    private String readStep() {
        int read = Integer.MIN_VALUE;
        try{
            FileInputStream fileStream = new FileInputStream(SHARED_FILE);
            ObjectInputStream objStream = new ObjectInputStream(fileStream);
            read = objStream.readInt();
            System.out.println("Integer data :" + read);
            objStream.close();
        }catch (IOException e) {
            System.out.println("Exception while reading: " + e.getMessage());
            e.printStackTrace();
        }
        return String.valueOf(read);
    }
    private boolean isAlive(){
        long interval = System.currentTimeMillis() - previousHeartBeatTimeStamp;
        int error = 8000;
        return interval <= (MONITORING_INTERVAL + error);
    }

    private static void writeData(String filePath, String data, int seek) throws IOException {
        RandomAccessFile file = new RandomAccessFile(filePath, "rw");
        file.seek(seek);
        file.write(data.getBytes());
        file.close();
    }

    public synchronized static String readCharsFromFile(String filePath, int seek) throws IOException {
        String line = "";
        try{
            List<String> allLines = Files.readAllLines(Paths.get(filePath));

            if (allLines.size() > seek) {
                line = allLines.get(seek);
            }
        }
        catch(IOException e){
            System.out.println(e);
        }
        return line;
//        RandomAccessFile file = new RandomAccessFile(filePath, "r");
//        file.seek(seek);
//        byte[] bytes = new byte[chars];
//        file.read(bytes);
//        file.close();
//        return new String(bytes, StandardCharsets.UTF_8);
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
            ex.printStackTrace();
        }
    }
}
