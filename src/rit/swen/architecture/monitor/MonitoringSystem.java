package rit.swen.architecture.monitor;

import rit.swen.architecture.SimulationStarter;
import rit.swen.architecture.controller.RoadStatusReceiver;
import rit.swen.architecture.detectors.BackupObstacleDetector;
import rit.swen.architecture.detectors.ObstacleDetector;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

public class MonitoringSystem {

    private String component;
    private static final String LOGGING_FILE_PATH = "."+ File.separator +"src"
    + File.separator + "rit" + File.separator + "swen" + File.separator + "architecture"
        + File.separator + "logs"+ File.separator +"failure_log.txt";

    // Handles faults and logs failures
    public static void handleFault(String component, RoadStatusReceiver failedReceiver){
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
        System.out.println("Starting BackupSender");

//        ProcessBuilder backupsender_builder = new ProcessBuilder("java", "-cp",
//                helper + File.separator + "out"+ File.separator +"production" + File.separator +"assignment-1"
//                        + File.separator,
//                "rit.swen.architecture.detectors.BackupObstacleDetector", String.valueOf(RoadStatusReceiver.senderLiveQueue.peek()));
//        backupsender_builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        System.out.println("Built backup sender..");

        Process backupSenderProcess = null;
        Thread backupProcessThread = null;
        BackupObstacleDetector backupObstacleDetector = new BackupObstacleDetector();

        try {
//            backupSenderProcess = backupsender_builder.start();
            backupProcessThread = new Thread(backupObstacleDetector);
            backupProcessThread.start();
        } catch(Exception e){
            System.err.println("IOException: " + e.getMessage());
        }


        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Rebooting sender");
        ProcessBuilder sender_builder = new ProcessBuilder("java", "-cp",
                helper + File.separator +"out" + File.separator +"production"
                        + File.separator +"assignment-1",
                "rit.swen.architecture.detectors.ObstacleDetector", String.valueOf(13));
        try {
            System.out.println("sender_builder.command(): " + sender_builder.command());
            sender_builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            sender_builder.start();
            backupObstacleDetector.stop();
//            backupProcessThread.wait();
//            backupProcessThread.interrupt();

//            backupProcessThread.stop();
//            backupSenderProcess.destroy();
        } catch(IOException e){
            System.err.println("IOException: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
