package controller;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;

public interface IController extends Remote {
    List<String> processesNameSet = new ArrayList<>();

    Deque<ProcessBuilder> processBuilders = new ArrayDeque<>();

    Deque<Process> processes = new ArrayDeque<>();
    /**
     * set up the controller to receive data from detector senders.
     */

    StringBuilder activeProcess = new StringBuilder("N");
    RoadStatusReceiver initializeReceiver() throws RemoteException;

    /**
     *  read data received from detector(sender).
     */
    void readStatus(int location, String processName) throws RemoteException;

    /**
     * audit the health of receiver at each stage, i.e after every second.
     */
    void monitorDetectorModule() throws RemoteException;

    boolean addProcessName(String processName) throws RemoteException;

    boolean removeProcessName(String processName) throws RemoteException;
    void setActiveProcessToSender() throws RemoteException;
    void setActiveProcessToBackupSender() throws RemoteException;

    char getActiveProcess() throws RemoteException;
    List<String> getProcessesSet() throws RemoteException;
}
