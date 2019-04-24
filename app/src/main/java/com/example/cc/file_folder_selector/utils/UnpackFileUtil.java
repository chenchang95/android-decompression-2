package com.example.cc.file_folder_selector.utils;

import android.util.Log;

import net.lingala.zip4j.core.ZipFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import de.innosystec.unrar.Archive;
import de.innosystec.unrar.rarfile.FileHeader;

public class UnpackFileUtil {

    /**
     * 判断是否需要密码
     */
    public static boolean needPassword(String srcPath, String fileType){
        if(fileType == null) return false;
        try{
            if(fileType.equals("zip")){
                ZipFile zf = new ZipFile(new File(srcPath));
                return zf.isEncrypted();
            }
        }catch (Exception e){
            Log.e("needPassword", e.getLocalizedMessage());
        }
        return false;
    }

    /**
     * 解压zip
     */
    public static String unZip(String srcPath, String destPath, String password){
        File zipFile = new File(srcPath);
        try{
            ZipFile zf = new ZipFile(zipFile);
            zf.setFileNameCharset("GBK");
            if(!zf.isValidZipFile()){
                return "该zip文件已损坏";
            }
            if(zf.isEncrypted()){//需要密码
                zf.setPassword(password);
            }
            zf.extractAll(destPath);
            return "解压成功";
        }catch (Exception e){
            return e.getMessage();
        }
    }

    /**
     * 解压rar
     */
    public static String unRar(String srcPath, String destPath, String password) {
        if(!destPath.endsWith("/")){
            destPath = destPath + "/";
        }
        File srcFile = new File(srcPath);
        FileOutputStream fileOut = null;
        Archive fileArchive = null;
        try {
            fileArchive = new Archive(srcFile, password, false);
            int total = fileArchive.getFileHeaders().size();
            if(total == 0) return "该压缩包为rar5，暂不支持";
            for (int i = 0; i < total; i++) {
                FileHeader fh = fileArchive.getFileHeaders().get(i);
                String entryPath = "";
                if (fh.isUnicode()) {//解決中文乱码
                    entryPath = fh.getFileNameW().trim();
                } else {
                    entryPath = fh.getFileNameString().trim();
                }
                entryPath = entryPath.replaceAll("\\\\", "/");
                File file = new File(destPath + entryPath);
                if (fh.isDirectory()) {
                    file.mkdirs();
                } else {
                    File parent = file.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }
                    fileOut = new FileOutputStream(file);
                    fileArchive.extractFile(fh, fileOut);
                    fileOut.close();
                    fileOut = null;
                }
            }
            return "解压成功";
        } catch (Exception e){
            return "解压失败";
        } finally {
            try {
                if (fileOut != null) {
                    fileOut.close();
                }
                if (fileArchive != null) {
                    fileArchive.close();
                }
            } catch (IOException e) {
                Log.e("msg", e.getMessage());
            }
        }
    }

    /**
     * 解压7z
     */
    public static void un7z(){

    }
}
