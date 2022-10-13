package detectors;

import controller.IController;
import controller.RoadStatusReceiver;
import road.LocationStep;
import road.RoadType;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class BackupObstacleDetector {
    private final int HEARTBEAT_INTERVAL = 2000;
    private Registry registry;
    public static int CURRENT_STEP = 3;
    private static BlockingQueue senderQueueReference;
    private IController receiverStubProgram;

    boolean stayActivated = true;

    public void initialize() throws IOException, NotBoundException {
        registry = LocateRegistry.getRegistry();
        receiverStubProgram = (IController) registry.lookup("IController");
        receiverStubProgram.addProcessName("BackupSender");
        receiverStubProgram.setActiveProcessToBackupSender();
    }

    final String SHARED_FILE = "."+ File.separator +"src"
            + File.separator + "logs"+ File.separator +"file.txt";
    private synchronized static void appendData(String filePath, int location) throws IOException {
        RandomAccessFile raFile = new RandomAccessFile(filePath, "rw");
        raFile.seek(raFile.length());
        raFile.write(String.valueOf("BackupSender" + "-" + location + "\n").getBytes());
        raFile.close();
    }

    public void sendHeartBeat(int location) throws IOException{
        Random random = new Random();
        while(true){
            try {
                long currentTime = Calendar.getInstance().getTime().getTime();
                RoadStatusReceiver.previousHeartBeatTimeStamp = currentTime;
                appendData(SHARED_FILE, location);
                System.out.println("Detector (BackupSender): I am alive on step: " + (location++) + " at: " + currentTime);
                int threadSleep = random.ints(3000, 5000)
                        .findFirst()
                        .getAsInt();

                System.out.println("Thread Sleep: " + threadSleep);
                if (threadSleep > 4000)
                    break;
                Thread.sleep(threadSleep);
                Thread.sleep(HEARTBEAT_INTERVAL);
            } catch(InterruptedException ex){
                System.out.println("BackupSender exception: " + ex.getMessage());
                System.out.println("------KILLED BACKUP SENDER------");
            }
        }
        receiverStubProgram.removeProcessName("BackupSender");
    }


    private static LocationStep getStep(int location){
        DecimalFormat df = new DecimalFormat("##.00");
        location +=1;
        long timeInSeconds = Calendar.getInstance().getTime().getTime();
        return new LocationStep(toRoadType(location), timeInSeconds);
    }

    private static RoadType toRoadType(int location) {
        if (location == 0) {
            return RoadType.NORMAL_ROAD;
        } else if (location == 1) {
            return RoadType.WATER_SPLASH;
        } else if (location == 2) {
            return RoadType.POT_HOLE;
        }
        return RoadType.INVALID_READ;
    }

    public static void main(String [] args){
        int initiallocation;

        if(args.length == 0 ){
            initiallocation = 0;
        }
        else{
            initiallocation = Integer.valueOf(args[0]);
        }
        BackupObstacleDetector backupSender = new BackupObstacleDetector();
        try{
            backupSender.initialize();
            senderQueueReference = ObstacleDetector.senderLiveQueue;
            System.out.println("Backup Sender initialized");
            backupSender.sendHeartBeat(initiallocation);
        }catch(NotBoundException | IOException ex){
            ex.printStackTrace();
        }
    }
}
