package com.vine.dmall.localnetworkfiletranfer.file;

/**
 * Created by xiovine on 15/7/23.
 */
public class FileInfo {
    public static enum EnumFileType
    {
        DIR, FILE, APK, TXT, IMAGE, PACKED, MUSIC
    }

    private String name;
    private boolean isFile;
    private long length;
    private long date;
    private EnumFileType type;
    private String path;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public long getLength()
    {
        return length;
    }

    public void setLength(long length)
    {
        this.length = length;
    }

    public long getDate()
    {
        return date;
    }

    public void setDate(long date)
    {
        this.date = date;
    }

    public EnumFileType getType()
    {
        return type;
    }

    public void setType(EnumFileType type)
    {
        this.type = type;
    }

    public boolean isFile()
    {
        return isFile;
    }

    public void setFile(boolean isFile)
    {
        this.isFile = isFile;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }
}
