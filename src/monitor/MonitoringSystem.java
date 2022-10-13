package monitor;

import controller.IController;
import controller.RoadStatusReceiver;
import detectors.BackupObstacleDetector;
import road.Road;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class MonitoringSystem {

    private String component;
    private static final String LOGGING_FILE_PATH = "."+ File.separator +"src"
        + File.separator + "logs"+ File.separator +"failure_log.txt";


    private static int getSenderFailureStep(String fileName) {
        int maxValue = Integer.MIN_VALUE;
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String read = reader.readLine();
            while (read != null) {
                maxValue = Math.max(maxValue, Integer.parseInt(read.split("-")[1]));
                read = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return maxValue;
    }
    private static Registry registry;
    private static IController receiverStubProgram;


    // Handles faults and logs failures
    public static void handleFault(String component, RoadStatusReceiver failedReceiver, char process) throws RemoteException {
        IController receiverStubProgram = null;
        String active = "";
        try {
            System.out.println("Active Process: " + failedReceiver.getActiveProcess());
            System.out.println("Process names: " + failedReceiver.getProcessesSet());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        File currentDirFile = new File("");
        String helper = currentDirFile.getAbsolutePath();
        System.out.println("FaultMonitor: Sender failed");
        // Log failure
        FileWriter fw = null;
        try
        {
            fw = new FileWriter(LOGGING_FILE_PATH,true);
            fw.write("\n\n" + component + " component failed on : " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")));
        }
        catch(IOException ioe)
        {
            System.err.println("IOException: " + ioe.getMessage());
        }finally {
            try {
                fw.close();
            }catch (IOException | NullPointerException io){
                System.err.println("IOException: " + io.getMessage());
            }
        }
        final String SHARED_FILE = "."+ File.separator +"src"
                + File.separator + "logs"+ File.separator +"file.txt";

        try {
            RandomAccessFile file = new RandomAccessFile(SHARED_FILE, "r");
            file.seek(file.getFilePointer());

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (failedReceiver.getActiveProcess() == 'S') {
            System.out.println("Starting BackupSender from step: " + getSenderFailureStep(SHARED_FILE));
            ProcessBuilder backupsender_builder = new ProcessBuilder("java", "-cp",
                    helper + File.separator + "out"+ File.separator +"production" + File.separator +"assignment-1"
                            + File.separator,
                    "detectors.BackupObstacleDetector", String.valueOf(getSenderFailureStep(SHARED_FILE)));
            backupsender_builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            Process backupSenderProcess;
            try {
                backupSenderProcess = backupsender_builder.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (failedReceiver.getActiveProcess() == 'B') {
            Road.buildRoad();
            ProcessBuilder sender_builder = new ProcessBuilder("java", "-cp",
                    helper + File.separator +"out" + File.separator +"production"
                            + File.separator +"assignment-1",
                    "detectors.ObstacleDetector", String.valueOf(getSenderFailureStep(SHARED_FILE)), "Sender1");
            try {
                sender_builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                sender_builder.start();
            } catch(IOException e){
                System.err.println("IOException: " + e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
