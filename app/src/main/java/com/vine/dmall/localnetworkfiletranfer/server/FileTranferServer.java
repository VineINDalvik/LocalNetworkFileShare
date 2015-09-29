package com.vine.dmall.localnetworkfiletranfer.server;

import android.content.Context;

import com.vine.dmall.localnetworkfiletranfer.log.LogInfo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by xiovine on 15/7/23.
 */
public class FileTranferServer {

    public enum Status
    {
        RUNNING, STOPED
    }

    private int port;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private ServerSocket serverSocket;
    private String localIp;

    private LogListener listener;

    public void setDefaultPath(String defaultPath) {
        this.defaultPath = defaultPath;
    }

    private String defaultPath;

    private Context context;

    private boolean running;

    public interface LogListener
    {
        void onConnected(LogInfo logInfo);
    }


    public Status getStatus()
    {
        return this.serverSocket == null || this.serverSocket.isClosed() ? Status.STOPED : Status.RUNNING;
    }

    public FileTranferServer()
    {

    }



    public void start()
    {
        this.running = true;
        try
        {
            if (this.serverSocket != null && !this.serverSocket.isClosed())
            {
                this.serverSocket.close();
            }
            this.serverSocket = new ServerSocket(port);

            for (;this.running;)
            {
                this.setLocalIp(this.serverSocket.getInetAddress().getHostAddress());
                Socket clientSocket = serverSocket.accept();
                System.out.println("connected to " + clientSocket.getInetAddress().getHostAddress());
                FileServiceThread th = new FileServiceThread(clientSocket);
                th.setContext(context);
                th.setDefaultPath(defaultPath);
                th.setLogListener(listener);
                th.setDaemon(true);
                th.start();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void stop()
    {
        this.running = false;
        if (this.serverSocket == null || this.serverSocket.isClosed())
        {
            return;
        }
        try
        {
            this.serverSocket.close();
            this.serverSocket = null;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void setContext(Context context)
    {
        this.context = context;
    }

    public void setLocalIp(String localIp)
    {
        this.localIp = localIp;
    }
}
