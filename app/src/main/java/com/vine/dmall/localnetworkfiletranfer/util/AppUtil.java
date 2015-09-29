package com.vine.dmall.localnetworkfiletranfer.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import com.vine.dmall.localnetworkfiletranfer.file.FileInfo;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by xiovine on 15/7/23.
 */
public class AppUtil {

    public static void sort(List<FileInfo> list, Comparator<FileInfo> comparator)
    {
        Collections.sort(list, comparator);
    }

    /**
     *
     * @param list
     */
    public static void sortByASCII(List<FileInfo> list, final boolean desc)
    {
        sort(list, new Comparator<FileInfo>() {

            @Override
            public int compare(FileInfo f1, FileInfo f2)
            {
                if (desc)
                    return f2.getName().toLowerCase().compareTo(f1.getName().toLowerCase());
                return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
            }

        });
    }

    /**
     *
     * @param list
     * @param desc
     */
    public static void sortByDate(List<FileInfo> list, final boolean desc)
    {
        sort(list, new Comparator<FileInfo>() {

            @Override
            public int compare(FileInfo f1, FileInfo f2)
            {
                if (desc)
                {
                    return new Date(f1.getDate()).compareTo(new Date(
                            f2.getDate()));
                }
                return new Date(f2.getDate()).compareTo(new Date(f1.getDate()));
            }
        });
    }

    /**
     *  apk icon
     * @param context
     * @param path
     * @return
     */
    public static Drawable getApkIcon(Context context, String path)
    {
        PackageManager pm = context.getPackageManager();
        PackageInfo ainfo = pm.getPackageArchiveInfo(path, 0);
        if (ainfo != null)
            return ainfo.applicationInfo.loadIcon(pm);
        return null;

    }
}
