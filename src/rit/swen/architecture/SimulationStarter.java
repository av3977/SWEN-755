package rit.swen.architecture;

import rit.swen.architecture.controller.RoadStatusReceiver;
import rit.swen.architecture.detectors.ObstacleDetector;
import rit.swen.architecture.road.Road;
import rit.swen.architecture.road.RoadType;

import java.io.File;
import java.rmi.RemoteException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SimulationStarter {
    public BlockingQueue queue;

    public BlockingQueue getQueue() {
        return queue;
    }

    public SimulationStarter() {
        queue = new LinkedBlockingQueue();
    }
    public static void main(String[] args) throws RemoteException {
        System.out.println("Hello, starting simulation..! ");
        SimulationStarter starter = new SimulationStarter();
        File currentDirFile = new File("");
        String helper = currentDirFile.getAbsolutePath();
        ObstacleDetector obstacleDetector  = new ObstacleDetector(starter.queue);
        RoadStatusReceiver roadStatusReceiver  = new RoadStatusReceiver(starter.queue);
        String FILE_SEPARATOR = File.separator;

        try {
            ProcessBuilder pb = new ProcessBuilder("rmiregistry");
            pb.directory(new File("." + FILE_SEPARATOR +"out" + FILE_SEPARATOR +"production"
                    + FILE_SEPARATOR +"assignment-1" + FILE_SEPARATOR + "rit" + FILE_SEPARATOR + "swen"
                    + FILE_SEPARATOR + "architecture" ));
            pb.start();
            Road.buildRoad();
            Thread.sleep(1000);

            new Thread(roadStatusReceiver).start();
            new Thread(obstacleDetector).start();

//            roadBuilding.destroy();
//
//
//            System.out.println("Starting receiver controller");
//            ProcessBuilder receiver_builder = new ProcessBuilder("java" , "-cp",
//                    helper + FILE_SEPARATOR +"out" + FILE_SEPARATOR +"production"
//                            + FILE_SEPARATOR + "assignment-1",
//                    "rit.swen.architecture.controller.RoadStatusReceiver");
//            receiver_builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
//            Process vehicleControlModule = receiver_builder.start();
//            Thread.sleep(1000);
//
//            System.out.println("Starting detector and it's sender");
//            ProcessBuilder sender_builder = new ProcessBuilder("java" , "-cp",
//                    helper + FILE_SEPARATOR +"out" + FILE_SEPARATOR +"production"
//                            + FILE_SEPARATOR +"assignment-1",
//                    "rit.swen.architecture.detectors.ObstacleDetector");
//            sender_builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
//            Process detectorSenderModule = sender_builder.start();
//
//            InputStream errors_receiver = vehicleControlModule.getErrorStream();
//            String err_r = "Receiver - ";
//            if(vehicleControlModule.getErrorStream().read() !=-1){
//                for (int i = 0; i < errors_receiver.available(); i++) {
//                    err_r += (char)errors_receiver.read();
//                }
//                System.out.println(err_r);
//            }else{
//                System.out.println(vehicleControlModule.getOutputStream());
//            }
//
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
