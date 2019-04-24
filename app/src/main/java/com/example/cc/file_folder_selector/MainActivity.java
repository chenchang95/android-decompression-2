package com.example.cc.file_folder_selector;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cc.file_folder_selector.adaptor.FileItemAdaptor;
import com.example.cc.file_folder_selector.model.FileModel;
import com.example.cc.file_folder_selector.utils.FileAndFolderUtil;
import com.example.cc.file_folder_selector.utils.UnpackFileUtil;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,View.OnClickListener,View.OnLongClickListener {

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

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

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

    /**
     * 解压初始化
     */
    @SuppressLint("RestrictedApi")
    public void unpackInit(){
        Intent intent = getIntent();
        String action = intent.getAction();
        if(intent.ACTION_VIEW.equals(action) || restart) {
            restart = false;
            Uri uri = intent.getData();
            compressedFile = Uri.decode(uri.getEncodedPath());
            fileType = compressedFile.substring(compressedFile.lastIndexOf(".")+1, compressedFile.length());
            if(fileType.equals("zip") || fileType.equals("rar")) {
                FloatingActionButton fab = findViewById(R.id.fab);
                fab.setVisibility(View.VISIBLE);
            }else{
                Toast.makeText(this,"该文件格式暂不支持",Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * 右下角悬浮按钮点击事件
     * @param v
     */
    public void decompressionFileClick(View v){
        unpackFileCheck();
    }

    /**
     * 解压文件
     */
    public void unpackFileCheck(){
        if(fileType.equalsIgnoreCase("zip")){
            if(UnpackFileUtil.needPassword(compressedFile, "zip")){
                showInputBox("请输入密码","zip");
            }else{
                unpackFile("zip", null);
            }
        }else if(fileType.equalsIgnoreCase("rar")){
            if(UnpackFileUtil.unRar(compressedFile, basePath, null).equals("解压失败")){
                showInputBox("请输入密码","rar");
            }else{
                unpackFile("rar", null);
            }
        }
    }

    /**
     * 解压文件
     * @param fileType
     * @param password
     */
    public void unpackFile(String fileType, String password){
        String msg = "解压中...";
        if(fileType.equals("zip")){
            msg = UnpackFileUtil.unZip(compressedFile, basePath, password);
        }else if(fileType.equals("rar")){
            msg = UnpackFileUtil.unRar(compressedFile, basePath, password);
        }
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        changePath(basePath);
    }

    /**
     * 弹出输入框
     * @param title
     * @param type
     */
    public void showInputBox(String title, final String type){
        final EditText inputServer = new EditText(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setView(inputServer);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if(type.equals("zip")){
                    unpackFile("zip", inputServer.getText().toString());
                }else if(type.equals("rar")){
                    unpackFile("rar", inputServer.getText().toString());
                }else if(type.equals("createFolder")){
                    String folderPath = basePath +"/"+ inputServer.getText().toString();
                    File folder = new File(folderPath);
                    if(!folder.exists()){
                        folder.mkdir();
                        changePath(basePath);
                    }
                }
            }
        });
        builder.show();
    }

    public void showTipsBox(String title, String content, final FileModel fileModel){
        AlertDialog alertDialog = new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(content)
            .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    FileAndFolderUtil.deleteDirWithFile(new File(fileModel.getFilePath()));
                    changePath(basePath);
                }
            })
            .create();
        alertDialog.show();
    }

    /**
     * 返回上层目录
     * @return
     */
    public boolean lastFolder(){
        if(!basePath.equals(rootPathList.get(0))){
            basePath = basePath.substring(0, basePath.lastIndexOf("/"));
            changePath(basePath);
            return true;
        }
        return false;
    }

    /**
     * 点文件CheckBox点击事件
     * @param v
     */
    public void pointBeginFileClick(View v){
        CheckBox c = v.findViewById(R.id.point_begin_file);
        spotFlag = c.isChecked();
        changePath(basePath);
    }

    /**
     * 文件CheckBox点击事件
     * @param v
     */
    public void hideFileClick(View v){
        CheckBox c = v.findViewById(R.id.hide_file);
        fileFlag = c.isChecked();
        changePath(basePath);
    }

    /**
     * 排序CheckBox点击事件
     * @param v
     */
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
        }else{
            show(fileModel.getFilePath());
        }
    }

    @Override
    public boolean onLongClick(View v) {
        //v.getTag()  为当前选中的下标
        FileModel fileModel = baseFileList.get((int) v.getTag());
        if(fileModel.isDirectory()){
            showTipsBox("提示", "你确定要删除该文件吗？", fileModel);
        }else{
            showTipsBox("提示", "你确定要删除该文件吗？", fileModel);
        }
        return false;
    }

    //显示打开方式
    public void show(String filesPath){
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        startActivity(showOpenTypeDialog(filesPath));
    }

    public static Intent showOpenTypeDialog(String param) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "*/*");
        return intent;
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
            //把返回上层目录绑定到返回键
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
            showInputBox("请输入文件夹名称", "createFolder");
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
