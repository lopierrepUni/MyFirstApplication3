package com.example.myfirstapplication.MulticastSocket;

import android.os.AsyncTask;

import com.example.myfirstapplication.Chat.MemberData;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MulticastClient extends Thread {

    private DatagramSocket datagramSocket;
    private InetAddress group;
    private byte[] buf = new byte[5124];
    private int portNumber;
    private String ipAddress;
    private MulticastClientInterface caller;
    private MemberData data;


    public MulticastClient(int portNumber, String ipAddress, MulticastClientInterface caller, MemberData data) {
        this.portNumber = portNumber;
        this.ipAddress = ipAddress;
        this.caller = caller;
        this.data = data;
        this.start();
    }

    private void initializeClient() {
        try {
            datagramSocket = new DatagramSocket();
            group = InetAddress.getByName(ipAddress);
        } catch (SocketException e) {
            caller.ErrorFromSocketManager(e);
        } catch (UnknownHostException e) {
            caller.ErrorFromSocketManager(e);
        } catch (IOException e) {
            caller.ErrorFromSocketManager(e);
        }
    }

    public void sendMessage(final String message, final String userName, final String userColor) {
        try {
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        buf = message.getBytes();
                        DatagramPacket mensaje
                                = new DatagramPacket(buf, buf.length, group, portNumber);
                        datagramSocket.send(mensaje);
                        buf = userName.getBytes();
                        DatagramPacket userName
                                = new DatagramPacket(buf, buf.length, group, portNumber);
                        datagramSocket.send(userName);
                        buf = userColor.getBytes();
                        DatagramPacket userColor
                                = new DatagramPacket(buf, buf.length, group, portNumber);
                        datagramSocket.send(userColor);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute();
        } catch (Exception error) {
            caller.ErrorFromSocketManager(error);
        }
    }

    @Override
    public void run() {
        initializeClient();
    }

}
