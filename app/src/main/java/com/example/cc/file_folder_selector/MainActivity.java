package com.example.cc.file_folder_selector;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cc.file_folder_selector.adaptor.FileItemAdaptor;
import com.example.cc.file_folder_selector.model.FileModel;
import com.example.cc.file_folder_selector.utils.FileAndFolderUtil;
import com.example.cc.file_folder_selector.utils.UnpackFileUtil;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,View.OnClickListener {

    //需解压文件路径
    private static String compressedFile;
    //文件类型
    private static String fileType;
    //是否restart
    private static boolean restart;
    private static List<String> rootPathList;
    private static String basePath;
    private static List<FileModel> baseFileList;
    private static boolean spotFlag;
    private static boolean fileFlag;
    private static boolean orderByFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("解压软件");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        CheckBox pointBeginFile = findViewById(R.id.point_begin_file);
        spotFlag = pointBeginFile.isChecked();
        CheckBox hideFile = findViewById(R.id.hide_file);
        fileFlag = hideFile.isChecked();
        CheckBox orderFile = findViewById(R.id.order_file);
        orderByFlag = orderFile.isChecked();

        if(basePath == null){
            rootPathList = FileAndFolderUtil.getExtSDCardPathList();
            basePath = rootPathList.get(0);
        }
        changePath(basePath);

        unpackInit();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        restart = true;
        unpackInit();
    }

    @SuppressLint("RestrictedApi")
    public void unpackInit(){
        Intent intent = getIntent();
        String action = intent.getAction();
        if(intent.ACTION_VIEW.equals(action) || restart) {
            restart = false;
            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setVisibility(View.VISIBLE);
        }
    }

    public void decompressionFileClick(View v){
        unpackFile();
    }

    public void unpackFile(){
        Intent intent = getIntent();
        Uri uri = intent.getData();
        compressedFile = Uri.decode(uri.getEncodedPath());
        fileType = compressedFile.substring(compressedFile.lastIndexOf(".")+1, compressedFile.length());
        if(fileType.equalsIgnoreCase("zip")){
            String msg = UnpackFileUtil.unZip(compressedFile, basePath, null);
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            changePath(basePath);
        }else if(fileType.equalsIgnoreCase("rar")){
            String msg = UnpackFileUtil.unRar(compressedFile, basePath, null);
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            changePath(basePath);
        }else{
            Toast.makeText(this,"该文件格式暂不支持",Toast.LENGTH_LONG).show();
        }
    }

    public void lastFolderClick(View v){
        lastFolder();
    }

    public boolean lastFolder(){
        if(!basePath.equals(rootPathList.get(0))){
            basePath = basePath.substring(0, basePath.lastIndexOf("/"));
            changePath(basePath);
            return true;
        }
        return false;
    }

    public void pointBeginFileClick(View v){
        CheckBox c = v.findViewById(R.id.point_begin_file);
        spotFlag = c.isChecked();
        changePath(basePath);
    }

    public void hideFileClick(View v){
        CheckBox c = v.findViewById(R.id.hide_file);
        fileFlag = c.isChecked();
        changePath(basePath);
    }

    public void orderFileClick(View v){
        CheckBox c = v.findViewById(R.id.order_file);
        orderByFlag = c.isChecked();
        changePath(basePath);
    }


    /**
     * 改变地址
     * @param path
     */
    public void changePath(String path){
        basePath = path;
        baseFileList = FileAndFolderUtil.getFilesAllName(basePath);
        baseFileList = FileAndFolderUtil.sort(baseFileList, spotFlag, fileFlag, orderByFlag);
        TextView nowPath = findViewById(R.id.now_path);
        nowPath.setText(basePath);
        loadList(baseFileList);
    }

    /**
     * 加载listView
     * @param fileList
     */
    public void loadList(List<FileModel> fileList){
        View view = findViewById(R.id.content_main);
        ListView layout = view.findViewById(R.id.file_item);
        FileItemAdaptor adaptor = new FileItemAdaptor(this, fileList, this);
        layout.setAdapter(adaptor);
    }

    /**
     * listView中的点击事件
     * @param v
     */
    @Override
    public void onClick(View v) {
        //v.getTag()  为当前选中的下标
        FileModel fileModel = baseFileList.get((int) v.getTag());
        if(fileModel.isDirectory()){
            changePath(fileModel.getFilePath());
        }
    }

    /**
     * 返回键
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(!lastFolder()){
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //顶部右侧工具栏
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.create_folder) {
            Log.v("msg", "点击了创建文件夹");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
