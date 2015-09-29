package com.vine.dmall.localnetworkfiletranfer.file;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.vine.dmall.localnetworkfiletranfer.R;
import com.vine.dmall.localnetworkfiletranfer.util.AppUtil;
import com.vine.dmall.localnetworkfiletranfer.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FileExplorer extends ActionBarActivity implements View.OnClickListener,AdapterView.OnItemClickListener{

    public static final String SDCARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    private ListView mListView;
    private TextView mCurPathTextView;
    private Button mBtnSelectOk;
    private EditText mEtPath;
    private TextView mTvTip;
    private Button mBtnRename;
    private Button mBtnRemove;
    private Button mBtnEdit;
    private ScrollView mSvMenu;

    private FileAdapter mAdapter;
    private List<FileInfo> mFileInfos;
    private StringBuffer mCurPath;

    public static final String ACTION_FOLDER_SELECT = "folder";
    public static final String ACTION_FILE_SELECT = "file";
    public static final String INTENT_DEFAULT_PATH = "default_path";

    private boolean isSingleSelect = true;
    private boolean isFolderSelect;

    private String defaultPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_explorer);
        init();
    }

    private void init()
    {
        mSvMenu = (ScrollView) this.findViewById(R.id.scrollview_menu);

        mBtnRename = (Button) this.findViewById(R.id.btn_rename);
        mBtnRename.setOnClickListener(this);
        mBtnEdit = (Button) this.findViewById(R.id.btn_edit);
        mBtnEdit.setOnClickListener(this);
        mBtnRemove = (Button) this.findViewById(R.id.btn_remove);
        mBtnRemove.setOnClickListener(this);
        mBtnSelectOk = (Button) this.findViewById(R.id.btn_select_ok);
        mBtnSelectOk.setOnClickListener(this);
        mEtPath = (EditText) this.findViewById(R.id.et_path);
        mListView = (ListView) this.findViewById(R.id.content_listview);
        mListView.setOnItemClickListener(this);

        mCurPathTextView = (TextView) this.findViewById(R.id.textview_cur_path);
        mTvTip = (TextView) this.findViewById(R.id.tv_tip);

        mCurPath = new StringBuffer();

        mCurPath.append(SDCARD_PATH);


        reloadAndUpdate(mCurPath.toString());
    }

    private void reloadAndUpdate(String path)
    {
        mFileInfos = this.getList(path);
        List<FileAdapter.Item> items = getItems(mFileInfos);
        if (null == this.mAdapter)
        {
            this.mAdapter = new FileAdapter(this, mFileInfos, items);
            this.mListView.setAdapter(mAdapter);
        }
        else
        {
            this.mAdapter.setFileInfos(mFileInfos);
            this.mAdapter.setItems(items);
            this.mAdapter.notifyDataSetChanged();
        }
        this.mCurPathTextView.setText(path);
        this.mEtPath.setText(path);
        this.mEtPath.setSelection(path.length());
    }

    private List<FileInfo> getList(String path)
    {
        List<FileInfo> outFile = new LinkedList<FileInfo>();
        List<FileInfo> outDir = new LinkedList<FileInfo>();
        FileUtil.listFile(path, outFile, outDir);
        AppUtil.sortByASCII(outDir, false);
        AppUtil.sortByASCII(outFile, false);
        outDir.addAll(outFile);
        return outDir;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_BACK:
                if (this.mCurPath.toString().equals(SDCARD_PATH))
                {
                    Intent intent = new Intent();
                    intent.putExtra("path", this.defaultPath);
                    setResult(RESULT_OK, intent);
                    finish();
                    return super.onKeyDown(keyCode, event);
                }
                int index = this.mCurPath.lastIndexOf("/");
                if (index == -1)
                    return super.onKeyDown(keyCode, event);
                this.mCurPath.delete(index, this.mCurPath.length());
                this.reloadAndUpdate(this.mCurPath.toString());
                this.mCurPathTextView.setText(this.mCurPath.toString());
                return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private List<FileAdapter.Item> getItems(List<FileInfo> fileInfos)
    {
        List<FileAdapter.Item> items = new ArrayList<FileAdapter.Item>(30);
        for (FileInfo f : fileInfos)
        {
            items.add(new FileAdapter.Item(f.getName(), f.getDate(), f.getLength(), f.getType(), f.isFile(), f.getPath(), false));
        }
        return items;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_file_explorer, menu);
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

    public void notifiCheckboxChanged(FileAdapter.Item item)
    {
        System.out.println("-> " + item.getLabel());
        boolean b = item.getChecked();
        if (!b)
        {
            //showScrollMenu(false);
            // this.mTvPath.setText(this.mCurPath.toString());
            return;
        }
        //showScrollMenu(true);
        String fName = item.getLabel();
        for (FileAdapter.Item it : this.mAdapter.getItems())
        {
            if (it != item)
            {
                it.setCheckbox(false);
            }
        }
        this.mAdapter.notifyDataSetChanged();

        StringBuffer path = new StringBuffer(this.mCurPath.toString());

        if (!path.toString().endsWith("/"))
        {
            path.append("/");
        }
        path.append(fName);
        if (this.isFolderSelect)
        {
            File f = new File(path.toString());
            if (f.isFile())
            {
                path.delete(0, path.length());
                path.append(f.getParentFile().getAbsolutePath());
            }
            this.mEtPath.setText(path);
        }
        else {
            this.mEtPath.setText(path);
        }
        this.mEtPath.setSelection(path.length());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        FileInfo fileInfo = mFileInfos.get(position);
        if (!fileInfo.isFile())
        {
            if (!this.mCurPath.toString().endsWith("/"))
            {
                this.mCurPath.append("/");
            }
            this.mCurPath.append(fileInfo.getName());
            reloadAndUpdate(this.mCurPath.toString());
            this.mCurPathTextView.setText(this.mCurPath.toString());
            return;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_select_ok:
                Intent intent = new Intent();
                String path = this.mEtPath.getText().toString();
                intent.putExtra("path", path);
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }
}
