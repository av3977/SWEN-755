package rit.swen.architecture.monitor;

import rit.swen.architecture.controller.RoadStatusReceiver;
import rit.swen.architecture.detectors.ObstacleDetector;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by Heena on 2/18/2018.
 */
public class MonitoringSystem {

    private String component;
    private static final String LOGGING_FILE_PATH = "."+ File.separator +"src"
    + File.separator + "rit" + File.separator + "swen" + File.separator + "architecture"
        + File.separator + "logs"+ File.separator +"failure_log.txt";

    /*Handles faults and logs failures*/
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
//                "rit.swen.architecture.detectors.BackupObstacleDetector", String.valueOf(ObstacleDetector.CURRENT_STEP));
//        System.out.println("backup sender command: " + backupsender_builder.command());
//        System.out.println("Built backup sender..");

//        Process backupSenderProcess = null;
//        backupsender_builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
//        try {
//            backupSenderProcess = backupsender_builder.start();
//        } catch(IOException e){
//            System.err.println("IOException: " + e.getMessage());
//        }
//        try {
//            Thread.sleep(15000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        System.out.println("Rebooting sender");
//        ObstacleDetector detector = new ObstacleDetector((new SimulationStarter()).getQueue());
//        new Thread(detector).start();
//        System.out.println("Sender reboot successful, shutting down backup detector.");
//        backupSenderProcess.destroy();
//        ProcessBuilder sender_builder = new ProcessBuilder("java", "-cp",
//                "." + File.separator +"out" + File.separator +"production"
//                        + File.separator +"assignment-1" + File.separator + "rit" + File.separator + "swen"
//                        + File.separator + "architecture" + File.separator ,
//                "detectors.ObstacleDetector", String.valueOf(failedReceiver.getCurrentCoordinateStep()));
//        sender_builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
//        try {
//            Process localizationModule = sender_builder.start();
//        } catch(IOException e){
//            System.err.println("IOException: " + e.getMessage());
//        }

    }
}
