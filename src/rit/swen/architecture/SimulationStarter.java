package rit.swen.architecture;

import rit.swen.architecture.controller.SharedConstants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class SimulationStarter {
    public static void main(String[] args) {
        File currentDirFile = new File("");
        String helper = currentDirFile.getAbsolutePath();
        final String FILE_SEPARATOR = File.separator;
        final String SOURCE_PACKAGE_PREFIX =  "rit"+FILE_SEPARATOR+"swen"+FILE_SEPARATOR+"architecture";
        try {
            ProcessBuilder pb = new ProcessBuilder("rmiregistry");
            pb.directory(new File("." + FILE_SEPARATOR +"out" + FILE_SEPARATOR +"production"
                    + FILE_SEPARATOR +"assignment-1" + FILE_SEPARATOR + "rit" + FILE_SEPARATOR + "swen"
                    + FILE_SEPARATOR + "architecture" ));
            pb.start();

            String SOURCE_CLASS_PREFIX = helper + FILE_SEPARATOR + "out" + FILE_SEPARATOR + "production" + FILE_SEPARATOR + "assignment-1"
                    + FILE_SEPARATOR + SOURCE_PACKAGE_PREFIX + FILE_SEPARATOR;
            Thread.sleep(1000);

            System.out.println("Starting to build road...");
            ProcessBuilder road_builder = new ProcessBuilder("java" , "-cp",
                    helper + FILE_SEPARATOR +"out" + FILE_SEPARATOR +"production"
                            + FILE_SEPARATOR +"assignment-1", "rit.swen.architecture.road.Road");
            road_builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            Process roadBuilding = road_builder.start();
            System.out.println("Built road successfully..");
            roadBuilding.destroy();


            System.out.println("Starting receiver controller");
            ProcessBuilder receiver_builder = new ProcessBuilder("java" , "-cp",
                    helper + FILE_SEPARATOR +"out" + FILE_SEPARATOR +"production"
                            + FILE_SEPARATOR + "assignment-1",
                    "rit.swen.architecture.controller.RoadStatusReceiver");
            receiver_builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            Process vehicleControlModule = receiver_builder.start();
            Thread.sleep(1000);

            System.out.println("Starting detector and it's sender");
            ProcessBuilder sender_builder = new ProcessBuilder("java" , "-cp",
                    helper + FILE_SEPARATOR +"out" + FILE_SEPARATOR +"production"
                            + FILE_SEPARATOR +"assignment-1",
                    "rit.swen.architecture.detectors.ObstacleDetector");
            sender_builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            Process detectorSenderModule = sender_builder.start();
            SharedConstants.senderProcess = detectorSenderModule;

            InputStream errors_receiver = vehicleControlModule.getErrorStream();
            String err_r = "Receiver - ";
            if(vehicleControlModule.getErrorStream().read() !=-1){
                for (int i = 0; i < errors_receiver.available(); i++) {
                    err_r += (char)errors_receiver.read();
                }
                System.out.println(err_r);
            }else{
                System.out.println(vehicleControlModule.getOutputStream());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
