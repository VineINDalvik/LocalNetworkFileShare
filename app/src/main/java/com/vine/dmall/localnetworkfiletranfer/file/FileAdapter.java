package com.vine.dmall.localnetworkfiletranfer.file;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.vine.dmall.localnetworkfiletranfer.R;
import com.vine.dmall.localnetworkfiletranfer.util.AppUtil;
import com.vine.dmall.localnetworkfiletranfer.util.FileUtil;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by xiovine on 15/7/23.
 */
public class FileAdapter extends BaseAdapter {

    private Context mContext;
    private List<FileInfo> mFileInfos;
    private List<Item> mItems;
    private int mSelectedIndex;
    private FileExplorer mMainActvity;

    /**
     * @return the mList
     */
    public List<FileInfo> getFileInfos()
    {
        return mFileInfos;
    }

    /**
     * @param mList
     *            the mList to set
     */
    public void setFileInfos(List<FileInfo> mList)
    {
        this.mFileInfos = mList;
    }

    public FileAdapter(FileExplorer mainActivity, List<FileInfo> list, List<Item> items)
    {
        this.mMainActvity = mainActivity;
        this.mContext = mMainActvity;
        this.mFileInfos = list;
        this.mItems = items;
    }

    @Override
    public int getCount() {
        return mFileInfos.size();
    }

    @Override
    public Item getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if (null == convertView)
        {
            convertView = LayoutInflater.from(this.mContext).inflate(R.layout.list_item, null);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.imageview_icon);
            holder.textViewLabel = (TextView) convertView.findViewById(R.id.textview_label);
            holder.textViewDate = (TextView) convertView.findViewById(R.id.textview_last_date);
            holder.textViewLength = (TextView) convertView.findViewById(R.id.textview_length);
            holder.checkbox = (CheckBox) convertView.findViewById(R.id.checkbox_select);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }
        final Item item = getItem(position);

        holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1)
            {
                item.isChecked = arg1;
                // // ֪ͨ
                if (mMainActvity != null)
                    mMainActvity.notifiCheckboxChanged(item);
            }
        });

        holder.imageView.setBackgroundResource(getDrawable(item));

        if (item.getType() == FileInfo.EnumFileType.APK)
        {
            holder.imageView.setBackgroundDrawable(AppUtil.getApkIcon(mContext, item.getPath()));
        }
        holder.textViewLabel.setText(item.getLabel());
        holder.textViewDate.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.CHINA).format(item.getDate()));
        boolean isFile = item.isFile();
        holder.textViewLength.setText(isFile ? FileUtil.byte2String(item.getLength()) : "");

        holder.checkbox.setChecked(item.isChecked);

        return convertView;
    }

    class ViewHolder
    {
        ImageView imageView;
        TextView textViewLabel;
        TextView textViewDate;
        TextView textViewLength;
        CheckBox checkbox;
    }

    public static class Item
    {
        private String path;
        private FileInfo.EnumFileType type;
        private String label;
        private long date;
        private long length;
        private boolean isFile;
        private boolean isChecked;

        public Item(String label, long date, long length, FileInfo.EnumFileType type, boolean isFile, String path, boolean checked)
        {
            this.isFile = isFile;
            this.type = type;
            this.label = label;
            this.date = date;
            this.length = length;
            this.path = path;
            this.isChecked = checked;
        }

        /**
         * @return the path
         */
        public String getPath()
        {
            return path;
        }

        /**
         * @param path
         *            the path to set
         */
        public void setPath(String path)
        {
            this.path = path;
        }

        /**
         * @return the type
         */
        public FileInfo.EnumFileType getType()
        {
            return type;
        }

        /**
         * @param type
         *            the type to set
         */
        public void setType(FileInfo.EnumFileType type)
        {
            this.type = type;
        }

        /**
         * @return the name
         */

        /**
         * @return the label
         */
        public String getLabel()
        {
            return label;
        }

        /**
         * @param label
         *            the label to set
         */
        public void setLabel(String label)
        {
            this.label = label;
        }

        /**
         * @return the date
         */
        public long getDate()
        {
            return date;
        }

        /**
         * @param date
         *            the date to set
         */
        public void setDate(long date)
        {
            this.date = date;
        }

        /**
         * @return the length
         */
        public long getLength()
        {
            return length;
        }

        /**
         * @param length
         *            the length to set
         */
        public void setLength(long length)
        {
            this.length = length;
        }

        public boolean isFile()
        {
            return isFile;
        }

        public void setFile(boolean isFile)
        {
            this.isFile = isFile;
        }

        public boolean getChecked()
        {
            return isChecked;
        }

        public void setCheckbox(boolean checked)
        {
            this.isChecked = checked;
        }

    }

    /**
     * @param fileInfo
     * @return
     */
    public int getDrawable(Item fileInfo)
    {
        switch (fileInfo.getType())
        {
            case DIR:
                return R.drawable.folder;
            case TXT:
                return R.drawable.text;
            case APK:
                return R.drawable.apk;
            case IMAGE:
                return R.drawable.image;
            case PACKED:
                return R.drawable.packed;
            case MUSIC:
                return R.drawable.audio;
            default:
                return R.drawable.file;
        }

    }


    /**
     * @return the mItems
     */
    public List<Item> getItems()
    {
        return mItems;
    }

    /**
     * @param mItems
     *            the mItems to set
     */
    public void setItems(List<Item> mItems)
    {
        this.mItems = mItems;
        // this.mSelectedIndex = 0; // ����Ϊ 0 ��
    }
}
