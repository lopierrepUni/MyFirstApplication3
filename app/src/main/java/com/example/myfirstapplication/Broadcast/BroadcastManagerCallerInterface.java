package com.example.myfirstapplication.Broadcast;

public interface BroadcastManagerCallerInterface {

    void MessageReceivedThroughBroadcastManager(
            String channel, String type,String message);

    void ErrorAtBroadcastManager(Exception error);

}
