package rit.swen.architecture.controller;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IController extends Remote {
    /**
     * set up the controller to receive data from detector senders.
     */
    void initializeReceiver() throws RemoteException;

    /**
     *  read data received from detector(sender).
     */
    void readStatus(int location) throws RemoteException;

    /**
     * audit the health of receiver at each stage, i.e after every second.
     */
    void monitorDetectorModule() throws RemoteException;
}
