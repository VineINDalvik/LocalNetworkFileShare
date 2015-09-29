package com.vine.dmall.localnetworkfiletranfer;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.vine.dmall.localnetworkfiletranfer.file.FileExplorer;
import com.vine.dmall.localnetworkfiletranfer.server.FileTranferServer;
import com.vine.dmall.localnetworkfiletranfer.util.NetUtil;
import com.vine.dmall.localnetworkfiletranfer.util.QRUtil;
import com.vine.dmall.localnetworkfiletranfer.util.WifiAPUtil;


public class MainActivity extends ActionBarActivity {

    private String filePath;
    private TextView fileUrl,apInfo;
    private Button openAP;
    private ImageView fileQRCode;
    private static int FILE_SELECT_REQUEST = 1;
    private boolean isUpdateServerStatusRunning;
    private FileTranferServer server;
    private String url;
    private static int PORT = 8888;
    private WifiAPUtil apUtil;
    private boolean isOpenAP;
    private static String AP_NAME = "Dmall";
    private static String AP_PSD = "dmall";
    private WifiManager wifiManager;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopServer();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fileUrl = (TextView) findViewById(R.id.file_url);
        fileQRCode = (ImageView) findViewById(R.id.file_qrcode);
        apInfo = (TextView) findViewById(R.id.hotspot_info);
        openAP = (Button) findViewById(R.id.connect_hotspot);

        server = new FileTranferServer();
        server.setContext(this);

        apUtil = WifiAPUtil.newInstance(this);
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        filePath = data.getExtras().getString("path");//得到新Activity 关闭后返回的数据

        startServer();
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void selectFile(View view){
        startActivityForResult(new Intent(MainActivity.this, FileExplorer.class), FILE_SELECT_REQUEST);
    }

    public void con2Hotspot(View view){
        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        }
        if(!apUtil.isAPOpen()) {
            isOpenAP = apUtil.startWifiAp(AP_NAME, AP_PSD);
            if (isOpenAP) {
                apInfo.setText("热点名称：" + AP_NAME + "  " + "热点密码：" + AP_PSD);
                openAP.setText("关闭热点");
            }
        }else{
            if(apUtil.closeWifiAp()) {
                apInfo.setText("");
                openAP.setText("开启热点");
            }
        }
    }

    private void startServer() {
        this.isUpdateServerStatusRunning = true;
        final Handler handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case 1:
                        System.out.println("received message");
                        break;
                    case 2:
                        String address = NetUtil.getLocalIpAddress();
//
//                        if(filePath != null && filePath.contains("/storage/sdcard0"))
//                            filePath = filePath.replace("/storage/sdcard0","");
//                        if(filePath == null) filePath = "";
                        url = "http://" + address + ":" + PORT ;
                        QRUtil.createQRImage(url, fileQRCode);
                        fileUrl.setText(url);
                        break;
                }
            };
        };
        //

        server.setPort(PORT);
        server.setDefaultPath(filePath);


        Thread serverThread;
        serverThread = new Thread() {
            @Override
            public void run() {
                server.start();
                Message msg = handler.obtainMessage(1, 1, 0);
                msg.what = 0;
                System.out.println("message sended!");
                msg.sendToTarget();
            }
        };

        serverThread.setDaemon(true);
        serverThread.setPriority(Thread.MAX_PRIORITY);
        serverThread.start();

        Thread th = new Thread() {
            @Override
            public void run() {
                for (; isUpdateServerStatusRunning;) {
                    Message msg = handler.obtainMessage(2, 0, 0);
                    if (server.getStatus() == FileTranferServer.Status.RUNNING) {
                        msg.arg1 = 1;
                    }
                    msg.sendToTarget();
                    try {
                        sleep(1000L);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
        };
        th.setDaemon(true);
        th.start();
    }

    private void stopServer() {
        this.server.stop();
        this.isUpdateServerStatusRunning = false;
    }
}
