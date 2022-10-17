package detectors;

import controller.IController;
import controller.RoadStatusReceiver;
import road.Road;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class ObstacleDetector {

    static BlockingQueue senderLiveQueue;
    public ObstacleDetector(BlockingQueue queue) {
        senderLiveQueue = queue;
    }

    private final int HEARTBEAT_INTERVAL = 2000;
    private Registry registry;

    public static int CURRENT_STEP = 0;
    public boolean stayActive;

    private IController receiverStubProgram;
    private String processName;

    public int getHops() {
        return hops;
    }

    public void setHops(int hops) {
        this.hops = hops;
    }

    private int hops;

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getProcessName() {
        return processName;
    }

    public static boolean isDetectorFailed() {
        return DETECTOR_FAILED;
    }

    private static boolean DETECTOR_FAILED;
    public void initialize(String processName) {
        try {
            registry = LocateRegistry.getRegistry("localhost");
            receiverStubProgram = (IController) registry.lookup("IController");
            receiverStubProgram.addProcessName(processName);
            receiverStubProgram.setActiveProcessToSender();
        }catch (IOException | NotBoundException exception) {
            System.out.println("Sender initialize exception: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    final String SHARED_FILE = "."+ File.separator +"src"
            + File.separator + "logs"+ File.separator +"file.txt";
    public synchronized void writeStep(int stepLocation) {
        FileOutputStream fos = null;
        try {
            File current = new File(SHARED_FILE);
            if (!current.exists())
                current.createNewFile();
            fos = new FileOutputStream(current);
            OutputStreamWriter output = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            output.write(stepLocation);
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void appendData(String filePath, int location) throws IOException {
        RandomAccessFile raFile = new RandomAccessFile(filePath, "rw");
        raFile.seek(raFile.length());
        raFile.write(String.valueOf(this.getProcessName() + "-" + location + "\n").getBytes());
        raFile.close();
    }
    /**
     * Send road report
     * @param location
     * @throws IOException
     */
    public void sendHeartBeat(int location) throws RemoteException {
        System.out.println("Sender sending heartbeat starting now.");
        Random random = new Random();
        while (true) {
            try {
                long currentTime = Calendar.getInstance().getTime().getTime();
                RoadStatusReceiver.previousHeartBeatTimeStamp = currentTime;
                try {
                    appendData(SHARED_FILE, location);
                } catch (IOException e) {
                    System.out.println("IO Exception while writing to sender: " + e.getMessage());
                    throw new RuntimeException(e);
                }
                /**
                 * wait for 2 seconds before sending the next heart beat signal
                 */
                System.out.println("Car running on: " + Road.roadAhead[location%10]);
                receiverStubProgram.readStatus(location, this.getProcessName());
                RoadStatusReceiver.currentCoordinateStep = location;
                location+=this.getHops();
                int threadSleep = random.ints(2000, 5000)
                        .findFirst()
                        .getAsInt();
                if (threadSleep > 4000)
                    break;
                Thread.sleep(threadSleep);
            } catch (ArrayIndexOutOfBoundsException indexOutOfBoundsException) {
                System.out.println("Seen array index out of bounds: " + this.getProcessName());
                break;
            } catch (InterruptedException exception) {
                System.out.println("Exception while reporting road status: " + exception.getMessage());
                break;
            } catch (RemoteException e) {
                System.out.println("Receiver read status exception: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
        receiverStubProgram.removeProcessName(this.getProcessName());
    }
    public ObstacleDetector() {}

    public static void main(String [] args){
        int initiallocation;
        String pname = "Default";
        if (args.length == 2 && (args[1] != null || !"".equals(args[1]))) {
            pname = args[1];
        }
        if(args.length == 0 ){
            initiallocation = 0;
        } else{
            initiallocation = Integer.valueOf(args[0]);
            CURRENT_STEP = initiallocation;
        }
        ObstacleDetector sender = new ObstacleDetector();
        sender.setProcessName(pname);
        if (!"Default".equals(pname)) {
            sender.setHops(Integer.parseInt(pname.substring(pname.length() - 1)));
        } else {
            sender.setHops(1);
        }
        try{
            sender.initialize(pname);
            Thread.sleep(1500);
            System.out.println("Sender initialized");
            Road.buildRoad();
            try {
                sender.sendHeartBeat(initiallocation);
            } catch (RemoteException exception) {
                System.out.println("Remote Exception from sendHeartBeat: " + exception.getMessage());
                exception.printStackTrace();
            }
        }catch(InterruptedException ex){
            ex.printStackTrace();
            System.out.println("Exception message: " + ex.getMessage());
        }
    }
}
