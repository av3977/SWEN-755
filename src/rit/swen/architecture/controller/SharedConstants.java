package rit.swen.architecture.controller;

public class SharedConstants {
    public static Process senderProcess;
    public static Process backupSenderProcess;
    public static boolean senderIsAlive = false;

    public synchronized static void setSenderIsAlive(boolean senderIsAlive) {
        SharedConstants.senderIsAlive = senderIsAlive;
    }

    public synchronized static void setSenderProcess(Process senderProcess) {
        SharedConstants.senderProcess = senderProcess;
    }

    public static boolean isSenderIsAlive() {
        return senderIsAlive;
    }

    public static boolean backupSenderIsAlive = false;
}
