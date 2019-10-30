package com.cmtech.android.bledevice.ecgmonitor.view;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledevice.ecgmonitor.adapter.EcgFileListAdapter;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgFileExplorer;
import com.cmtech.android.bledevice.ecgmonitor.model.OpenedEcgFilesManager;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.hrprocessor.EcgHrStatisticsInfo;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledeviceapp.R;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.DIR_ECG_SIGNAL;

/**
  *
  * ClassName:      EcgFileExploreActivity
  * Description:    Ecg文件浏览Activity
  * Author:         chenm
  * CreateDate:     2018/11/10 下午5:34
  * UpdateUser:     chenm
  * UpdateDate:     2019/4/12 下午5:34
  * UpdateRemark:   制作类图，优化代码
  * Version:        1.0
 */

public class EcgFileExploreActivity extends AppCompatActivity implements OpenedEcgFilesManager.OnOpenedEcgFilesListener {
    private static final String TAG = "EcgFileExploreActivity";

    private static final int DEFAULT_LOADED_FILENUM_EACH_TIMES = 10; // 缺省每次加载的文件数

    private EcgFileExplorer explorer;      // 文件浏览器实例
    private EcgFileListAdapter fileAdapter; // 文件Adapter
    private RecyclerView rvFiles; // 文件RecycleView
    private TextView tvPromptInfo; // 提示信息

    public class TopSmoothScroller extends LinearSmoothScroller {
        TopSmoothScroller(Context context) {
            super(context);
        }
        @Override
        protected int getHorizontalSnapPreference() {
            return SNAP_TO_START;
        }
        @Override
        protected int getVerticalSnapPreference() {
            return SNAP_TO_START;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecg_record_explorer);

        // 创建ToolBar
        Toolbar toolbar = findViewById(R.id.tb_ecg_record_explorer);
        setSupportActionBar(toolbar);

        try {
            explorer = new EcgFileExplorer(DIR_ECG_SIGNAL, EcgFileExplorer.FILE_ORDER_MODIFIED_TIME, this);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "心电记录目录错误。", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        rvFiles = findViewById(R.id.rv_ecg_record_list);
        LinearLayoutManager fileLayoutManager = new LinearLayoutManager(this);
        fileLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvFiles.setLayoutManager(fileLayoutManager);
        rvFiles.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        fileAdapter = new EcgFileListAdapter(this);
        rvFiles.setAdapter(fileAdapter);
        rvFiles.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int lastVisibleItem;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                //判断RecyclerView的状态 是空闲时，同时，是最后一个可见的ITEM时才加载
                if(newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem == fileAdapter.getItemCount()-1) {
                    explorer.loadNextFiles(DEFAULT_LOADED_FILENUM_EACH_TIMES);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if(layoutManager != null)
                    lastVisibleItem = layoutManager.findLastVisibleItemPosition();
            }
        });

        tvPromptInfo = findViewById(R.id.tv_prompt_info);
        tvPromptInfo.setText("正在载入信号");
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(explorer.loadNextFiles(DEFAULT_LOADED_FILENUM_EACH_TIMES) == 0) {
                    tvPromptInfo.setText("无信号可载入。");
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ecgfile_explore, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED, null);
                finish();
                break;

            case R.id.ecg_record_update:
                importFromWechat();
                break;

            case R.id.ecg_record_delete:
                deleteSelectedFile();
                break;

            case R.id.share_with_wechat:
                shareSelectedFileThroughWechat();
                break;

        }
        return true;
    }

    private void importFromWechat() {
        explorer.importFromWechat();
        tvPromptInfo.setText("正在载入信号");
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(explorer.loadNextFiles(DEFAULT_LOADED_FILENUM_EACH_TIMES) == 0) {
                    tvPromptInfo.setText("无信号可载入。");
                }
            }
        });
    }

    public void deleteSelectedFile() {
        explorer.deleteSelectFile(this);
    }

    public void shareSelectedFileThroughWechat() {
        explorer.shareSelectedFileThroughWechat(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        explorer.close();
    }

    public void selectFile(final EcgFile ecgFile) {
        explorer.selectFile(ecgFile);
    }

    public List<File> getUpdatedFiles() {
        return explorer.getUpdatedFiles();
    }

    @Override
    public void onFileSelected(final EcgFile selectedFile) {
        fileAdapter.updateSelectedFile(selectedFile);

        if(selectedFile != null) {
            TopSmoothScroller smoothScroller = new TopSmoothScroller(EcgFileExploreActivity.this);
            smoothScroller.setTargetPosition(fileAdapter.getItemPosition(selectedFile));
            if(rvFiles.getLayoutManager() != null)
                rvFiles.getLayoutManager().startSmoothScroll(smoothScroller);
        }
    }

    @Override
    public void onFileListChanged(final List<EcgFile> fileList) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(fileList == null || fileList.isEmpty()) {
                    rvFiles.setVisibility(View.INVISIBLE);
                    tvPromptInfo.setVisibility(View.VISIBLE);
                }else {
                    rvFiles.setVisibility(View.VISIBLE);
                    tvPromptInfo.setVisibility(View.INVISIBLE);
                }

                fileAdapter.updateFileList(fileList, getUpdatedFiles());
            }
        });

    }

    public EcgHrStatisticsInfo getSelectedFileHrStatisticsInfo() {
        return explorer.getSelectedFileHrStatisticsInfo();
    }

    public void saveSelectedFileComment() {
        explorer.saveSelectedFileComment();
    }

}
