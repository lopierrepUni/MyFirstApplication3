package com.example.myfirstapplication.MulticastSocket;

public interface MulticastClientInterface {
    public void MessageHasBeenReceived(String message, String userName, String userColor);
    public void ErrorFromSocketManager(Exception error);
}
