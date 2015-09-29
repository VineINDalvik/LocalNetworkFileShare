package com.vine.dmall.localnetworkfiletranfer.util;

import android.annotation.SuppressLint;
import android.content.Context;

import com.vine.dmall.localnetworkfiletranfer.file.FileInfo;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by xiovine on 15/7/23.
 */
public class FileUtil {

    public enum Encoding
    {
        UTF8, GBK, BIN
    }

    public enum FileType
    {
        TXT, IMAGE, UNKNOW
    }

    public static byte[] readFile(String file)
    {
        BufferedInputStream bis = null;
        ByteArrayOutputStream baos = null;
        try
        {
            bis = new BufferedInputStream(new FileInputStream(file));
            byte[] buffer = new byte[1024 << 1];
            int len = 0;
            baos = new ByteArrayOutputStream();
            while (-1 != (len = bis.read(buffer, 0, buffer.length)))
            {
                baos.write(buffer, 0, len);
                baos.flush();
            }
            return baos.toByteArray();
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally
        {
            if (bis != null)
            {
                try
                {
                    bis.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            if (baos != null)
            {
                try
                {
                    baos.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static void read(String file, ReadCallback callback)
    {
        BufferedInputStream bis = null;
        try
        {
            bis = new BufferedInputStream(new FileInputStream(file));
            byte[] buffer = new byte[1024];
            int len = 0;
            while (-1 != (len = bis.read(buffer, 0, buffer.length)))
            {
                callback.onRead(buffer, 0, len);
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (bis != null)
            {
                try
                {
                    bis.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public static interface ReadCallback
    {
        void onRead(byte[] bytes, int offset, int length);
    }

    public static byte[] readAssetsFile(Context context, String file)
    {
        InputStream in = null;
        ByteArrayOutputStream baos = null;
        try
        {
            in = context.getResources().getAssets().open(file);
            baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while (-1 != (len = in.read(buffer, 0, buffer.length)))
            {
                baos.write(buffer, 0, len);
                baos.flush();
            }
            return baos.toByteArray();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (baos != null)
            {
                try
                {
                    baos.close();
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return null;

    }

    private static String[] textExts = { ".txt", ".java", ".cpp", ".html", ".htm", ".js" };
    private static String[] imageExts = { ".jpg", ".png", ".bmp", ".jpeg", ".gif" };

    public static FileType checkFileType(String fileName)
    {

        for (String ext : textExts)
        {
            boolean b = fileName.toLowerCase().endsWith(ext);
            if (b)
            {
                return FileType.TXT;
            }
        }
        for (String ext : imageExts)
        {
            boolean b = fileName.toLowerCase().endsWith(ext);
            if (b)
            {
                return FileType.IMAGE;
            }
        }
        return FileType.UNKNOW;
    }

    /**
     *
     * @param bytes
     * @return
     */
    public static String byte2String(long bytes)
    {
        BigDecimal bd;

        if (bytes < 1024 * 1024)
        {
            bd = new BigDecimal(bytes / 1024.0);
            return bd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()
                    + " KB";
        }
        else if (bytes < 1024 * 1024 * 1024)
        {
            bd = new BigDecimal(bytes / 1024 / 1024.0);
            return bd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()
                    + " MB";
        }
        else if (bytes < 1024 * 1024 * 1024 * 1024)
        {
            bd = new BigDecimal(bytes / 1024 / 1024 / 1024.0);
            return bd.setScale(2, BigDecimal.ROUND_HALF_UP) + " GB";
        }
        return bytes + " Byte";
    }

    public static void listFile(String path, final List<FileInfo> outFile,
                                final List<FileInfo> outDir)
    {
        File file = new File(path);
        file.listFiles(new FileFilter() {
            FileInfo fileInfo;

            @SuppressLint("NewApi")
            @Override
            public boolean accept(File file)
            {
                fileInfo = new FileInfo();
                fileInfo.setName(file.getName());
                fileInfo.setDate(file.lastModified());
                fileInfo.setFile(file.isFile());
                if (file.isDirectory())
                {
                    fileInfo.setType(FileInfo.EnumFileType.DIR);
                    fileInfo.setPath(file.getAbsolutePath());
                    outDir.add(fileInfo);
                }
                else
                {
                    fileInfo.setType(getFileType(getExt(file.getName())));
                    fileInfo.setLength(file.length());
                    fileInfo.setPath(file.getAbsolutePath());
                    outFile.add(fileInfo);
                }
                return true;
            }
        });
    }

    public static FileInfo.EnumFileType getFileType(String ext)
    {

        if ("txt".equalsIgnoreCase(ext) || "java".equalsIgnoreCase(ext))
        {
            return FileInfo.EnumFileType.TXT;
        }
        else if ("png".equalsIgnoreCase(ext) || "jpg".equalsIgnoreCase(ext))
        {
            return FileInfo.EnumFileType.IMAGE;
        }
        else if ("mp3".equalsIgnoreCase(ext) || "mp4".equalsIgnoreCase(ext)
                || "amr".equalsIgnoreCase(ext))
        {
            return FileInfo.EnumFileType.MUSIC;
        }
        else if ("rar".equalsIgnoreCase(ext) || "zip".equalsIgnoreCase(ext)
                || "tar".equalsIgnoreCase(ext))
        {
            return FileInfo.EnumFileType.PACKED;
        }
        else if ("apk".equalsIgnoreCase(ext))
        {
            return FileInfo.EnumFileType.APK;
        }
        return FileInfo.EnumFileType.FILE;
    }

    public static String getExt(String name)
    {
        int s = name.lastIndexOf(".");
        if (s == -1)
            return "";
        return name.substring(s + 1);
    }


}
