package monitor;

import controller.RoadStatusReceiver;
import detectors.BackupObstacleDetector;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

        ProcessBuilder backupsender_builder = new ProcessBuilder("java", "-cp",
                helper + File.separator + "out"+ File.separator +"production" + File.separator +"assignment-1"
                        + File.separator,
                "detectors.BackupObstacleDetector", String.valueOf(RoadStatusReceiver.senderLiveQueue.peek()));
        backupsender_builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        try {
            backupsender_builder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Built backup sender..");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Rebooting sender");
        ProcessBuilder sender_builder = new ProcessBuilder("java", "-cp",
                helper + File.separator +"out" + File.separator +"production"
                        + File.separator +"assignment-1",
                "detectors.ObstacleDetector", String.valueOf(12));
        try {
            System.out.println("sender_builder.command(): " + sender_builder.command());
            sender_builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            sender_builder.start();
        } catch(IOException e){
            System.err.println("IOException: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
