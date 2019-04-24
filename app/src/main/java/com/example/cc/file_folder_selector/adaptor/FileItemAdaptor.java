package com.example.cc.file_folder_selector.adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.cc.file_folder_selector.MainActivity;
import com.example.cc.file_folder_selector.R;
import com.example.cc.file_folder_selector.model.FileModel;

import java.util.List;

public class FileItemAdaptor extends BaseAdapter implements View.OnClickListener{

    private Context context;
    private List<FileModel> fileList;
    private MainActivity listener;

    public FileItemAdaptor(){
    }

    public FileItemAdaptor(Context context, List<FileModel> fileList, MainActivity listener){
        this.context = context;
        this.fileList = fileList;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return fileList.size();
    }

    @Override
    public Object getItem(int i) {
        return fileList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(context).inflate(R.layout.list_item, null);
        TextView fileNameView = view.findViewById(R.id.file_name);
        FileModel fileModel = fileList.get(i);
        fileNameView.setText(fileModel.getFileName());
        view.setTag(i);
        view.setOnClickListener(listener);
        view.setOnLongClickListener(listener);
        return view;
    }

    @Override
    public void onClick(View view) {
        listener.onClick(view);
    }
}
