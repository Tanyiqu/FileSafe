package com.tanyiqu.filesafe.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tanyiqu.filesafe.bean.FileBean;
import com.tanyiqu.filesafe.R;
import com.tanyiqu.filesafe.data.Data;
import com.tanyiqu.filesafe.exception.NoSuchFileToOpenException;
import com.tanyiqu.filesafe.utils.FileUtil;
import com.tanyiqu.filesafe.utils.ToastUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FilesActivity extends AppCompatActivity {

    public static String path;
    String name;

    //首次进入activity时目录是否为空
    String count;
    static RecyclerView recycler;

    //圆形进图条
    ProgressBar loading;

    //目录为空时显示的提示
    LinearLayout noItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);
        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            path = extra.getString("path");
            name = extra.getString("name");
            count = extra.getString("count");
        }else {
            ToastUtil.errorToast(this,"错误");
            finish();
        }
        init();
    }

    private void init() {
//        Util.myLog("init");
        //设置共享元素的图片
        ImageView sceneTransitionImg = findViewById(R.id.img_cover);
        Bitmap bmp= BitmapFactory.decodeFile(path + File.separator + "cover.jpg");
        sceneTransitionImg.setImageBitmap(bmp);

        Data.fileBeanList = new ArrayList<>();

        //初始化变量
        loading = findViewById(R.id.loading);
        noItem = findViewById(R.id.img_no_item);

        initToolBar();
        initRecycler();
        initFAB();

        //判断是否加载进度条
        //如果为空目录
        if(count.equals("0")){
            loading.setVisibility(View.GONE);
            this.noItem.setVisibility(View.VISIBLE);
        }
        //不是空目录
        else {
            //延时任务
            new Handler().postDelayed(new Runnable(){
                public void run() {
                    refreshFileView_list(path);
                    refreshFileView_screen();
                    loading.setVisibility(View.GONE);
                }
            }, 800);
        }
    }

    /**
     * 初始化Toolbar
     */
    private void initToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar_files);
        toolbar.setTitle(name);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    /**
     * 初始化RecyclerView
     */
    private void initRecycler() {
        recycler = findViewById(R.id.recycler_files);
        LinearLayoutManager layoutManager = new LinearLayoutManager(FilesActivity.this,RecyclerView.VERTICAL,false);
        recycler.setLayoutManager(layoutManager);
        FilesAdapter adapter = new FilesAdapter(Data.fileBeanList);
        recycler.setAdapter(adapter);
    }

    /**
     * 初始化悬浮按钮
     */
    private void initFAB() {
        //悬浮按钮
        FloatingActionButton fab_add = findViewById(R.id.fab_add);
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //添加文件
                //调用文件选择器，选择文件进行加密
                Intent intent = new Intent(view.getContext(),FileSelectActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.anim_page_jump_1,R.anim.anim_page_jump_2);
            }
        });
    }

    //刷新列表

    /**
     * 刷新列表
     * 根据路径找到相应的配置文件刷新列表
     * 暂时不用json数组，以后测试效率后考虑更换
     * @param path 路径
     */
    public static void refreshFileView_list(String path){
        List<FileBean> list = new ArrayList<>();;
        String iniPath = path + File.separator + "data.db";
        File iniFile = new File(iniPath);
        try {
            FileReader in = new FileReader(iniFile);
            BufferedReader br = new BufferedReader(in);
            String line = null;
            while((line = br.readLine()) != null){
                String[] strings = line.split(Data.Splitter);
                FileBean fv = new FileBean(strings[0],strings[1],strings[2],strings[3],path);
                list.add(fv);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Data.setFileBeanList(list);
    }
    private void refreshFileView_screen(){
        FilesAdapter adapter = new FilesAdapter(Data.fileBeanList);
        recycler.setAdapter(adapter);
    }

    /**
     * Adapter类
     */
    public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.ViewHolder>{

        private List<FileBean> fileBeanList;

        FilesAdapter(List<FileBean> fileBeanList) {
            this.fileBeanList = fileBeanList;
        }

        public class ViewHolder extends RecyclerView.ViewHolder{

            ConstraintLayout files_item;
            ImageView img_files_logo;
            TextView tv_file_name;
            TextView tv_file_size;
            TextView tv_file_date;

            public String path;             //file所在的目录路径

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                files_item = itemView.findViewById(R.id.files_item);
                img_files_logo = itemView.findViewById(R.id.img_files_logo);
                tv_file_name = itemView.findViewById(R.id.tv_file_name);
                tv_file_size = itemView.findViewById(R.id.tv_file_size);
                tv_file_date = itemView.findViewById(R.id.tv_file_date);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_files_item,parent,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final FileBean item = fileBeanList.get(position);
            final String ext = FileUtil.getFileExt(item.getOriginal_name());
            holder.img_files_logo.setImageDrawable(getDrawable(FileUtil.getImgId(ext)));
            holder.tv_file_name.setText(item.getOriginal_name());
            holder.tv_file_size.setText(item.getSize());
            holder.tv_file_date.setText(item.getDate());
            //设置点击事件
            holder.files_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //打开此文件
                    try {
                        File file = new File(item.getPath() + File.separator + item.getEncrypted_name());
                        FileUtil.openFile(view.getContext(),file ,ext);
                    } catch (NoSuchFileToOpenException e) {
                        ToastUtil.errorToast(view.getContext(),e.getMessage());
                    }
                }
            });

        }

        @Override
        public int getItemCount() {
            return fileBeanList.size();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
//        Util.myLog("restart");

        //这一段是有一个bug，进度条在切换到最近任务再切换回来时 在某些手机会莫名显示出来 红米N7P出现此问题
        if(loading.getVisibility() == View.VISIBLE){
            loading.setVisibility(View.GONE);
        }

        //重新启动时，判断列表是否为空
        if(Data.fileBeanList.size() == 0){
            noItem.setVisibility(View.VISIBLE);
        }else {
            noItem.setVisibility(View.GONE);
        }
        //不在onResume刷新时因为onResume会在activity一开始执行
        refreshFileView_list(path);

        refreshFileView_screen();
    }
}
