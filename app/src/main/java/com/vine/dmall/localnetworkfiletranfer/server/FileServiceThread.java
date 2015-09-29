package com.vine.dmall.localnetworkfiletranfer.server;

import android.content.Context;
import android.os.Environment;

import com.vine.dmall.localnetworkfiletranfer.log.LogInfo;
import com.vine.dmall.localnetworkfiletranfer.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by xiovine on 15/7/23.
 */
public class FileServiceThread extends Thread{

    private Socket socket;
    private OutputStream out;
    private InputStream in;
    private String defaultPath;

    public void setLogListener(FileTranferServer.LogListener logListener) {
        this.logListener = logListener;
    }

    private FileTranferServer.LogListener logListener;

    public void setContext(Context context) {
        this.context = context;
    }

    private Context context;

    /**
     * @throws IOException
     *
     */
    public FileServiceThread(Socket socket) throws IOException
    {
        this.socket = socket;
        this.out = this.socket.getOutputStream();
        this.in = this.socket.getInputStream();
    }

    public String getDefaultPath() {
        return defaultPath;
    }

    public void setDefaultPath(String defaultPath) {
        this.defaultPath = defaultPath;
    }

    @Override
    public void run() {
        //super.run();
        defaultPath = defaultPath == null || defaultPath.trim().length() == 0 ?
                Environment.getExternalStorageDirectory().getAbsolutePath() : defaultPath;
        if (defaultPath.endsWith("/"))
        {
            defaultPath = defaultPath.substring(0, defaultPath.length() - 1);
        }
        String path = defaultPath;

        Map<String, String> headers = new HashMap<String, String>(10);
        try{
            int c = -1;
            StringBuffer sb = new StringBuffer();
            while (-1 != (c = this.in.read()))
            {
                sb.append((char) c);
                if (c == '\n')
                {
                    String s = sb.toString();
                    sb.delete(0, sb.length());
                    if (s != null && s.trim().length() != 0)
                    { //  headers()
                        addHeader(headers, s);
                        //System.out.println(s);
                        continue;
                    }
                    break;
                }
            }
        }
        catch(Exception e){
            return;
        }

        path = headers.get("path");
        if (path == null || path.trim().length() == 0)
        {
            return;
        }
        if (logListener != null)
        {
            LogInfo logInfo = new LogInfo();
            logInfo.setIp(this.socket.getInetAddress().getHostAddress());
            logInfo.setRequestPath(path);
            logInfo.setMethod(headers.get("method"));
            logListener.onConnected(logInfo);
        }

        if ("/".equals(path))
        {
            path = defaultPath;
        }
        else
        {
            path = defaultPath + path;
        }

        File file = new File(path);
        if (!file.exists())
        {
            String html = new String(FileUtil.readAssetsFile(context, "index.jsp"));
            this.send(html.replace("{TITLE}","我的文件").replace("{BODY}", String.format("<p><span style='color:red'>对不起！这里没有找到本地目录。</span></p><a href='%s'>返回</a>", "/")).getBytes(), null);
            html = null;
            return;
        }
        if (file.isFile())
        {

            FileUtil.FileType ft = FileUtil.checkFileType(file.getName());
            String contentType = null;
            switch (ft)
            {
                case TXT:
                    contentType = "text/html";
                    break;
                case IMAGE:
                    contentType = "image/jpeg";
                    break;
                case UNKNOW:
                    contentType = "application/stream"; // stream,�Զ�����
                    break;
            }
            if (file.length() < 1024 * 1024 * 5)
            {
                byte[] bytes = FileUtil.readFile(file.getAbsolutePath());
                this.send(bytes, contentType);
                bytes = null;
                return;
            }
            this.sendHeaders(file.length(), contentType);
            FileUtil.read(file.getAbsolutePath(), new FileUtil.ReadCallback() {

                @Override
                public void onRead(byte[] bytes, int offset, int length)
                {
                    sendPart(bytes, offset, length);
                }
            });
            this.closeSocket();
            return;
        }

        File[] files = file.listFiles();

        if (files == null || files.length == 0)
        {
            String html = new String(FileUtil.readAssetsFile(context, "index.jsp"));
            this.send(
                    html.replace("{TITLE}", "我的文件").replace("{BODY}",
                            String.format("<p><a href='%s'>&lt;&lt;&lt;</a></p>", file.getParentFile().getAbsolutePath().replace(defaultPath, "/").replaceAll("//", "/"))).getBytes(), null);
            html = null;
            return;
        }

        List<File> fs = sort(files);
        StringBuilder sb = new StringBuilder();
        sb.append("<table style='width:100%;'>");

        sb.append("<tr><th><input size=\"5\" style=\"float:left\" onkeyup=\"search(this,event);\"/>Name</th><th>Length</th><th>Type</th><th>Date</th></tr>");

        sb.append(String.format("<tr><a href='%s'>&lt;&lt;&lt;</a></tr>", file.getParentFile().getAbsolutePath().replace(defaultPath, "/").replace("//", "/")));

        for (File f : fs)
        {
            String tr = "<tr class=\"filetrs\"><td onclick='tdClick(this);' onmouseover='this.className=\"lineCursor\"' onmouseout='this.className=\"\"'>%s</td><td style='text-align:center'>%s</td><td style='text-align:center'>%s</td><td style='text-align:right'>%s</td></tr>";
            String fName = f.getName();
            String td_name = null;
            String td_length = null;
            String td_type = null;
            String td_date = null;
            if (f.isFile())
            {
                td_name = "<img src='{FILE_ICON}'/><a href=\"{href}\">{name}</a>".replace("{href}", f.getAbsolutePath().replace(defaultPath, "")).replace("{name}", fName).replace("{FILE_ICON}",
                        "/file:///file.png");
                double length = f.length() / 1024.0;
                length = new BigDecimal(length).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                td_length = "<span>{length} KB</span>".replace("{length}", "" + length);
                td_type = "<span>File</span>";
            }
            else
            {
                td_name = "<img src='{FILE_ICON}'/><a href=\"{href}\">{name}</a>".replace("{href}", f.getAbsolutePath().replace(defaultPath, "")).replace("{name}", fName).replace("{FILE_ICON}",
                        "/file:///dir.png");
                td_length = "<span></span>";
                
                td_type = "<span>Folder</span>";
            }
            td_date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.CHINA).format(f.lastModified());
            tr = String.format(tr, td_name, td_length, td_type, td_date);
            sb.append(tr);
        }

        sb.append("</table>");
        byte[] bytes = FileUtil.readAssetsFile(context, "index.jsp");
        String html = new String(bytes);
        html = html.replace("{TITLE}", "我的文件").replace("{BODY}", sb.toString());
        this.send(html.getBytes(), null);
        html = null;
        bytes = null;

    }

    private Map<String, String> addHeader(Map<String, String> headers, String s)
    {
        if (s.startsWith("GET "))
        {
            headers.put("method", "get");
            int beginIndex = s.indexOf("/");
            int endIndex = s.lastIndexOf("?");
            if (endIndex == -1)
            {
                endIndex = s.lastIndexOf(" ");
            }
            String path = s.substring(beginIndex, endIndex);
            headers.put("path", path);
        }
        else if (s.startsWith("POST "))
        {
            headers.put("method", "post");
            int beginIndex = s.indexOf("/");
            int endIndex = s.lastIndexOf("?");
            if (endIndex == -1)
            {
                endIndex = s.lastIndexOf(" ");
            }
            String path = s.substring(beginIndex, endIndex);
            headers.put("path", path);
        }
        else
        {
            String[] hs = s.split(": ");
            if (hs.length >= 2)
            {
                headers.put(hs[0], hs[1]);
            }
        }
        return headers;
    }

    /**
     *
     * @param msg
     * @param contentType
     */
    public void send(byte[] msg, String contentType)
    {
        try
        {
            // // if (this.gzipOut == null)
            // // this.gzipOut = new GZIPOutputStream(this.out);
            byte[] data = msg;
            //
            if (contentType == null || contentType.trim().length() == 0)
            {
                contentType = "text/html;charset=utf-8";
            }
            this.sendHeaders(data.length, contentType);
            this.out.write(data);
            this.out.write("\r\n".getBytes());
            this.out.flush();
            this.closeSocket();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    public void sendHeaders(long contentLength, String contentType)
    {
        try
        {
            this.out.write("HTTP/1.1 200 OK\r\n".getBytes());
            this.out.write("Content-Type: {CONTENT_TYPE}\r\n".replace("{CONTENT_TYPE}", contentType).getBytes());
            this.out.write("Date: Sun, 01 Sep 2013 06:15:44 GMT\r\n".getBytes());
            this.out.write("Server: Apache\r\n".getBytes());
            this.out.write("Keep-Alive: timeout=15, max=100\r\n".getBytes());
            this.out.write("Connection: Keep-Alive\r\n".getBytes());
            this.out.write("Set-Cookie: JSESSIONID=2515ac1523abae726adf\r\n".getBytes());
            this.out.write("Set-Cookie: Path=/mnt/sdcard\r\n".getBytes());
            this.out.write("Content-Length: {CONTENT_LENGTH}\r\n".replace("{CONTENT_LENGTH}", contentLength + "\r\n".getBytes().length + "").getBytes());
            this.out.write("\r\n".getBytes());
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (contentType == null || contentType.trim().length() == 0)
        {
            contentType = "text/html;charset=utf-8";
        }
    }

    public void sendPart(byte[] bytes, int offset, int length)
    {
        
        try
        {
            this.out.write(bytes, offset, length);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void closeSocket()
    {
        if (this.out != null)
        {
            try
            {
                this.out.close();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (this.in != null)
        {
            try
            {
                this.in.close();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (this.socket != null)
        {
            try
            {
                this.socket.close();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * @param files
     * @return
     */
    private List<File> sort(File[] files)
    {
        List<File> fs = new ArrayList<File>();
        List<File> dirs = new ArrayList<File>();
        for (File f : files)
        {
            if (f.isDirectory())
            {
                dirs.add(f);
            }
            else
            {
                fs.add(f);
            }
        }
        Collections.sort(fs, new Comparator<File>() {

            @Override
            public int compare(File f1, File f2) {
                return f1.getName().compareToIgnoreCase(f2.getName());
            }
        });
        Collections.sort(dirs, new Comparator<File>() {

            @Override
            public int compare(File f1, File f2)
            {
                return f1.getName().compareToIgnoreCase(f2.getName());
            }
        });
        dirs.addAll(fs);
        return dirs;
    }
}
