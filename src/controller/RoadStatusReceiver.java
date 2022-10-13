package controller;

import monitor.MonitoringSystem;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class RoadStatusReceiver extends UnicastRemoteObject implements IController {

    private static final int MONITORING_INTERVAL = 2000;
    private static final String REGISTRY_HOST = "localhost";
    public static long previousHeartBeatTimeStamp = System.currentTimeMillis();
    public static int currentCoordinateStep;
    public static int FILE_POINTER = 0;
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
    public boolean addProcessName(String processName) throws RemoteException {
        return processesNameSet.add(processName);
    }
    @Override
    public boolean removeProcessName(String processName) throws RemoteException {
        return processesNameSet.remove(processName);
    }

    @Override
    public void setActiveProcessToSender() throws RemoteException {
        activeProcess.setCharAt(0, 'S');
    }

    @Override
    public void setActiveProcessToBackupSender() throws RemoteException {
        activeProcess.setCharAt(0, 'B');
    }

    @Override
    public char getActiveProcess() throws RemoteException {
        return activeProcess.charAt(0);
    }
    public boolean addProcessBuilders(ProcessBuilder processBuilder) throws RemoteException {
        processBuilders.addLast(processBuilder);
        return true;
    }

    public boolean addProcessToProcesses(Process process) throws RemoteException {
        processes.addLast(process);
        return true;
    }

    public boolean cleanProcessQueues() throws RemoteException {
        for (Process p: processes) {
            if (!p.isAlive()) {
                for (ProcessBuilder builder: processBuilders) {
                    String processName = builder.command().get(builder.command().size()-1);
                    processBuilders.remove(processName);
                }
                processes.remove(p);
            }
        }
        return true;
    }
    @Override
    public List<String> getProcessesSet() throws RemoteException {
        return processesNameSet;
    }
    @Override
    public RoadStatusReceiver initializeReceiver() throws RemoteException {
        RoadStatusReceiver roadStatusReceiver = new RoadStatusReceiver();
        try {
            Registry registry = LocateRegistry.getRegistry(REGISTRY_HOST);
            registry.rebind("IController", roadStatusReceiver);
        } catch (Exception e) {
            System.out.println("Receiver Exception : " + e.getMessage());
            e.printStackTrace();
        }
        return roadStatusReceiver;
    }

    @Override
    public void readStatus(int coordinateStep, String processName) throws RemoteException {
        previousHeartBeatTimeStamp = System.currentTimeMillis();
        final String currentTimeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println(String.format("Central Controller %s: current coordinate step: %s", processName, coordinateStep));
//        System.out.println("Central Controller: Received heartbeat signal at : " + currentTimeStamp);
    }

    @Override
    public void monitorDetectorModule() throws RemoteException {
        while (true) {
            try {
                String line = readCharsFromFile(SHARED_FILE, FILE_POINTER);
                System.out.println("Active Process: " + getProcessesSet());
                if (!"".equals(line)) {
                    System.out.println("Read step: " +line);
                    FILE_POINTER+=1;
                }
//                cleanProcessQueues();
                Thread.sleep(MONITORING_INTERVAL);
            } catch (InterruptedException | IOException e) {
                System.out.println(e.getMessage());
            }
            if (!isAlive()) {
                System.out.println("Receiver: Heartbeat interval exceeded - Detector Component failed - View log for details");
                MonitoringSystem.handleFault("Initial Sender", this, getActiveProcess());
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
        int error = 4000;
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

            RoadStatusReceiver receiver = new RoadStatusReceiver(); // instantiate
            receiver = receiver.initializeReceiver(); // bind and return reference
            Thread.sleep(2000);
            System.out.println("Receiver initialized");
            receiver.monitorDetectorModule();
        }catch(Exception ex){
            System.out.println("receiver main exception  - " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
