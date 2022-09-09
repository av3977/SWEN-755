package rit.swen.architecture;

import java.io.File;
import java.io.IOException;

public class SimulationStarter {

    public static void main(String[] args) {
        File currentDirFile = new File(".");
        String helper = currentDirFile.getAbsolutePath();
        final String FILE_SEPARATOR = File.separator;
        final String SOURCE_PACKAGE_PREFIX =  "rit/swen/architecture";
        try {
            ProcessBuilder pb = new ProcessBuilder("rmiregistry");
            pb.directory(new File("." + FILE_SEPARATOR +"out" + FILE_SEPARATOR +"production"
                    + FILE_SEPARATOR +"assignment-1" ));
            pb.start();


            Thread.sleep(1000);
            System.out.println("Starting receiver");
            ProcessBuilder receiver_builder = new ProcessBuilder("java" , "-cp",
                    helper + FILE_SEPARATOR + "out"+ FILE_SEPARATOR + "production" + FILE_SEPARATOR +"assignment-1"
                            + FILE_SEPARATOR + SOURCE_PACKAGE_PREFIX + FILE_SEPARATOR,
                    "contoller.RoadStatusReceiver");
            receiver_builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            Process vehicleControlModule = receiver_builder.start();
            Thread.sleep(1000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
