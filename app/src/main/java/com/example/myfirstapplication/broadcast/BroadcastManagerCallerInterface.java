package com.example.myfirstapplication.broadcast;

public interface BroadcastManagerCallerInterface {

    void MessageReceivedThroughBroadcastManager(
            String channel,String message);

    void ErrorAtBroadcastManager(Exception error);
}
