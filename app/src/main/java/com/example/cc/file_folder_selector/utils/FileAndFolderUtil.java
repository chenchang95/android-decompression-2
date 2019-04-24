package com.example.cc.file_folder_selector.utils;

import android.os.Environment;
import android.util.Log;

import com.example.cc.file_folder_selector.model.FileModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件/文件夹工具类
 */
public class FileAndFolderUtil {

    /**
     * 获取外置SD卡和扩展存储卡TF卡路径
     * @return
     */
    public static List<String> getExtSDCardPathList() {
        List<String> paths = new ArrayList<String>();
        String extFileStatus = Environment.getExternalStorageState();
        File extFile = Environment.getExternalStorageDirectory();
        //首先判断一下外置SD卡的状态，处于挂载状态才能获取的到
        if (extFileStatus.equals(Environment.MEDIA_MOUNTED) && extFile.exists() && extFile.isDirectory() && extFile.canWrite()) {
            //外置SD卡的路径
            paths.add(extFile.getAbsolutePath());
        }
        try {
            // obtain executed result of command line code of 'mount', to judge
            // whether tfCard exists by the result
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("mount");
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            int mountPathIndex = 1;
            while ((line = br.readLine()) != null) {
                // format of sdcard file system: vfat/fuse
                if ((!line.contains("fat") && !line.contains("fuse") && !line .contains("storage")) || line.contains("secure")
                        || line.contains("asec") || line.contains("firmware") || line.contains("shell") || line.contains("obb")
                        || line.contains("legacy") || line.contains("data")) {
                    continue;
                }
                String[] parts = line.split(" ");
                int length = parts.length;
                if (mountPathIndex >= length) {
                    continue;
                }
                String mountPath = parts[mountPathIndex];
                if (!mountPath.contains("/") || mountPath.contains("data") || mountPath.contains("Data")) {
                    continue;
                }
                File mountRoot = new File(mountPath);
                if (!mountRoot.exists() || !mountRoot.isDirectory() || !mountRoot.canWrite()) {
                    continue;
                }
                boolean equalsToPrimarySD = mountPath.equals(extFile .getAbsolutePath());
                if (equalsToPrimarySD) {
                    continue;
                }
                //扩展存储卡即TF卡或者SD卡路径
                paths.add(mountPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } return paths;
    }

    /**
     * 得到目录下的所有文件和文件夹
     * @param path
     * @return
     */
    public static List<FileModel> getFilesAllName(String path) {
        File file = new File(path);
        File[] files = file.listFiles();
        if (files == null) return null;
        List<FileModel> list = new ArrayList<>();
        for(int i =0;i<files.length;i++){
            FileModel fileModel = new FileModel();
            fileModel.setFilePath(files[i].getAbsolutePath());
            fileModel.setFileName(files[i].getName());
            if(files[i].isDirectory()){//文件夹
                fileModel.setDirectory(true);
            }else{//文件
                fileModel.setDirectory(false);
            }
            list.add(fileModel);
        }
        return list;
    }

    /**
     * 排序和限制
     * @param list  原始数据
     * @param spotFlag  点开头的文件或文件夹
     * @param fileFlag  文件
     * @param orderByFlag  排序
     */
    public static List<FileModel> sort(List<FileModel> list, boolean spotFlag, boolean fileFlag, boolean orderByFlag){
        //文件排序
        List<FileModel> returnList = new ArrayList<>();
        List<FileModel> fileList = new ArrayList<>();
        List<FileModel> folderList = new ArrayList<>();
        for (FileModel fileModel : list) {
            if(!spotFlag){//不显示点开头的文件或文件夹
                String prefix = fileModel.getFileName().substring(0, 1);
                if(prefix.equals(".")){
                    continue;
                }
            }
            if(fileModel.isDirectory()){
                folderList.add(fileModel);
            }else{
                if(fileFlag){//显示文件
                    fileList.add(fileModel);
                }
            }
        }
        orderByList(folderList, orderByFlag);
        returnList.addAll(folderList);
        orderByList(fileList, orderByFlag);
        returnList.addAll(fileList);
        return returnList;
    }

    /**
     * list排序
     * @param list
     * @param flag
     */
    public static void orderByList(List<FileModel> list, final boolean flag){
        Collections.sort(list, new Comparator<FileModel>() {
            @Override
            public int compare(FileModel f1, FileModel f2) {
                if(flag){
                    return f1.getFileName().compareTo(f2.getFileName());
                }
                return f2.getFileName().compareTo(f1.getFileName());
            }
        });

    }

}
