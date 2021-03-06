package com.cmtech.android.bledeviceapp.activity;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.model.Account;
import com.vise.utils.file.FileUtil;
import com.vise.utils.view.BitmapUtil;

import java.io.File;
import java.io.IOException;

import static com.cmtech.android.bledeviceapp.global.AppConstant.DIR_IMAGE;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;

/**
 *  AccountActivity: 账户设置Activity
 *  Created by bme on 2018/10/27.
 */

public class AccountActivity extends AppCompatActivity {
    private EditText etName;
    private ImageView ivImage;
    private EditText etNote;
    private String cacheImageFile = ""; // 账户头像文件名缓存

    private Account account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        if(!MyApplication.getAccountManager().isLocalLogin())  {
            Toast.makeText(this, R.string.login_failure, Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
        }

        account = MyApplication.getAccount();

        // 创建ToolBar
        Toolbar toolbar = findViewById(R.id.tb_set_account_info);
        setSupportActionBar(toolbar);

        etName = findViewById(R.id.et_account_name);
        ivImage = findViewById(R.id.iv_account_image);
        etNote = findViewById(R.id.et_account_note);

        updateUI();

        ivImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAlbum();
            }
        });

        Button btnOk = findViewById(R.id.btn_account_info_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Account account = MyApplication.getAccount();
                account.setName(etName.getText().toString());

                String icon = account.getIcon();
                if(!cacheImageFile.equals(icon)) {
                    // 把原来的图像文件删除
                    if(!TextUtils.isEmpty(icon)) {
                        File iconFile = new File(icon);
                        if(iconFile.exists())
                            iconFile.delete();
                    }

                    // 把当前图像保存到DIR_IMAGE
                    if(TextUtils.isEmpty(cacheImageFile)) {
                        account.setIcon("");
                    } else {
                        try {
                            ivImage.setDrawingCacheEnabled(true);
                            Bitmap bitmap = ivImage.getDrawingCache();
                            bitmap = BitmapUtil.scaleImageTo(bitmap, 100, 100);
                            File toFile = FileUtil.getFile(DIR_IMAGE, account.getPlatName()+account.getPlatId() + ".jpg");
                            BitmapUtil.saveBitmap(bitmap, toFile);
                            ivImage.setDrawingCacheEnabled(false);
                            String filePath = toFile.getCanonicalPath();
                            account.setIcon(filePath);
                        } catch (IOException e) {
                            e.printStackTrace();
                            account.setIcon("");
                        }
                    }
                }

                account.setNote(etNote.getText().toString());
                account.save();

                account.upload(AccountActivity.this, new ICodeCallback() {
                    @Override
                    public void onFinish(int code) {
                        String str = (code == RETURN_CODE_SUCCESS) ? "账户信息已上传。" : "更新账户信息错误。";
                        Toast.makeText(AccountActivity.this, str, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });
            }
        });

        Button btnCancel = findViewById(R.id.btn_userinfo_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });
    }

    private void updateUI() {
        etName.setText(account.getName());

        cacheImageFile = account.getIcon();
        if(TextUtils.isEmpty(cacheImageFile)) {
            Glide.with(this).load(R.mipmap.ic_user).into(ivImage);
        } else {
            Glide.with(this).load(cacheImageFile).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(ivImage);
        }

        etNote.setText(account.getNote());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    cacheImageFile = handleImageOnKitKat(data);
                } else {
                    cacheImageFile = handleImageBeforeKitKat(data);
                }
                if (!TextUtils.isEmpty(cacheImageFile)) {
                    Glide.with(AccountActivity.this).load(cacheImageFile).centerCrop().into(ivImage);
                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_modify_account, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED, null);
                finish();
                break;

            case R.id.download_account_info:
                download();
                break;
        }
        return true;
    }

    private void download() {
        account.download(this, new ICodeCallback() {
            @Override
            public void onFinish(int code) {
                if (code == RETURN_CODE_SUCCESS) {
                    updateUI();
                } else {
                    Toast.makeText(AccountActivity.this, "下载账户信息错误。", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    @TargetApi(19)
    private String handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if(uri == null) return null;

        if(DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if("content".equalsIgnoreCase(uri.getScheme())) {
            imagePath = getImagePath(uri, null);
        } else if("file".equalsIgnoreCase(uri.getScheme())) {
            imagePath = uri.getPath();
        }
        return imagePath;
    }

    private String handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        return getImagePath(uri, null);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }
}
