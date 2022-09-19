import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SimulationStarter {
    public BlockingQueue queue;
    static List<Runnable> list = new ArrayList<Runnable>();

    public BlockingQueue getQueue() {
        return queue;
    }

    public SimulationStarter() {
        queue = new LinkedBlockingQueue();
    }
    public static void main(String[] args) throws RemoteException {
        System.out.println("Hello, starting simulation..! ");
        SimulationStarter starter = new SimulationStarter();
        starter.queue.add(-1); // start car engine.
        File currentDirFile = new File("");
        String helper = currentDirFile.getAbsolutePath();
        String FILE_SEPARATOR = File.separator;

        try {
            ProcessBuilder pb = new ProcessBuilder("rmiregistry");
            pb.directory(new File("." + File.separator +"out" + File.separator +"production"+ File.separator +"assignment-1"));
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.start();

            Thread.sleep(1000);

            System.out.println("Starting receiver controller");
            ProcessBuilder receiver_builder = new ProcessBuilder("java" , "-cp",
                    helper + FILE_SEPARATOR +"out" + FILE_SEPARATOR +"production"
                            + FILE_SEPARATOR + "assignment-1" + File.separator ,
                    "controller.RoadStatusReceiver");

            receiver_builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            System.out.println("Receiver process command: " + receiver_builder.command());
            Process receiverProcess = receiver_builder.start();
            Thread.sleep(2000);

            System.out.println("Starting detector and it's sender");
            ProcessBuilder sender_builder = new ProcessBuilder("java" , "-cp",
                    helper + FILE_SEPARATOR +"out" + FILE_SEPARATOR +"production"
                            + FILE_SEPARATOR +"assignment-1" + File.separator ,
                    "detectors.ObstacleDetector", "0");
            System.out.println("Sender process command: " + sender_builder.command());
            sender_builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            Process senderProcess = sender_builder.start();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void addPB(final ProcessBuilder pb) {
        list.add(new Runnable() {
            public void run() {
                try {
                    pb.start();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });
    }
}
