package com.example.testpy;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.chaquo.python.Kwarg;
import com.chaquo.python.PyObject;
import com.chaquo.python.android.AndroidPlatform;
import com.chaquo.python.Python;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    TextView textView, area;
    Button button;
    private Python py;
    private PyObject obj;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.cal);
        area = findViewById(R.id.area);
        button = findViewById(R.id.selectbtn);
        initPython();
        this.py = Python.getInstance();
        this.obj = py.getModule("test");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
            }
        });
    }
    String path;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
             Uri uri = data.getData();
             if ("file".equalsIgnoreCase(uri.getScheme())){//使用第三方应用打开
                 path = uri.getPath();
                 textView.setText(path);
                 CalareaTask calareaTask=new CalareaTask(path, this.area, this.obj);
                 calareaTask.execute();
//                 calculate(path);
                 return;
             }
             if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                 path = getPath(this, uri);
                 textView.setText(path);
                 CalareaTask calareaTask=new CalareaTask(path, this.area, this.obj);
                 calareaTask.execute();
//                 calculate(path);
             } else {//4.4以下下系统调用方法
                 path = getRealPathFromURI(uri);
                 textView.setText(path);
                 CalareaTask calareaTask=new CalareaTask(path, this.area, this.obj);
                 calareaTask.execute();
//                 calculate(path);
             }
        }
    }
    @SuppressLint("NewApi")
    public String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if(null!=cursor&&cursor.moveToFirst()){;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
            cursor.close();
        }
        return res;
    }

    public String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public void initPython(){
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
    }

    public void calculate(final String path){
        area.setText("正在计算中...");
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                PyObject obj1 = obj.callAttr("getresult",new Kwarg("picpath", path));
                Float result = obj1.toJava(Float.class);
                if(result == Float.valueOf(0).floatValue()){
                    area.setText("检测失败");
                }
                area.setText("面积是: "+result);
            }
        });
        t.start();
//        try {
//            t.join();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
    }
    public class CalareaTask extends AsyncTask<Void,String,Float>{

        private String path;
        private TextView area;
        private PyObject obj;
        CalareaTask(String path, TextView area, PyObject obj){
            this.path = path;
            this.area = area;
            this.obj = obj;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            area.setText("正在计算中...");
        }

        @Override
        protected Float doInBackground(Void... voids) {
            PyObject obj1 = obj.callAttr("getresult",new Kwarg("picpath", path));
            Float result = obj1.toJava(Float.class);
            return result;
        }

        @Override
        protected void onPostExecute(Float result) {
            super.onPostExecute(result);
            if(Float.compare(result, 0.0f) == 0){
                area.setText("检测失败");
            }
            else {
                area.setText("面积是: " + result);
            }
        }
    }
}
