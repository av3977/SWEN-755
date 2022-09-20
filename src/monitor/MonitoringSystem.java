package monitor;

import controller.IController;
import controller.RoadStatusReceiver;
import detectors.BackupObstacleDetector;
import road.Road;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MonitoringSystem {

    private String component;
    private static final String LOGGING_FILE_PATH = "."+ File.separator +"src"
    + File.separator + "rit" + File.separator + "swen" + File.separator + "architecture"
        + File.separator + "logs"+ File.separator +"failure_log.txt";

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
        File currentDirFile = new File(".");
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

        System.out.println("Starting BackupSender from step: " + failedAtStep);
        ProcessBuilder backupsender_builder = new ProcessBuilder("java", "-cp",
                helper + File.separator + "out"+ File.separator +"production" + File.separator +"assignment-1"
                        + File.separator,
                "detectors.BackupObstacleDetector", String.valueOf(failedAtStep));
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
                "detectors.ObstacleDetector", String.valueOf(0));
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
