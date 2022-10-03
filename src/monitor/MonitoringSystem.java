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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class MonitoringSystem {

    private String component;
    private static final String LOGGING_FILE_PATH = "."+ File.separator +"src"
        + File.separator + "logs"+ File.separator +"failure_log.txt";


    private static int getSenderFailureStep(String fileName) {
        long lines = 0;
        int minVisitedPath = Integer.MAX_VALUE;
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String read = reader.readLine();
            while (read != null) {
                minVisitedPath = Math.min(minVisitedPath, Integer.parseInt(read.split("-")[1]));
                lines++;
                read = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return minVisitedPath;
    }

    // Handles faults and logs failures
    public static void handleFault(String component, RoadStatusReceiver failedReceiver, int failedAtStep){
        IController receiverStubProgram;
        try {
            receiverStubProgram = (IController) LocateRegistry.getRegistry("localhost").lookup("IController");
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (NotBoundException e) {
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

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Rebooting sender");
        Road.buildRoad();
        ProcessBuilder sender_builder = new ProcessBuilder("java", "-cp",
                helper + File.separator +"out" + File.separator +"production"
                        + File.separator +"assignment-1",
                "detectors.ObstacleDetector", String.valueOf(getSenderFailureStep(SHARED_FILE)), "Reboot1");
        System.out.println("Sender Reboot Command: " + sender_builder.command());
        try {
            System.out.println("sender_builder.command(): " + sender_builder.command());
            sender_builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            sender_builder.start();
        } catch(IOException e){
            System.err.println("IOException: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    backupSenderProcess.destroy();
    }
}
