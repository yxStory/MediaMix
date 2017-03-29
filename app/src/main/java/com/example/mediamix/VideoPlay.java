package com.example.mediamix;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;

import java.io.IOException;

/**
 * Created by yx on 2017/3/29.
 */
public class VideoPlay extends Activity implements View.OnClickListener{
    ImageButton play,stop;
    //创建SurfaceView显示视频图像
    SurfaceView sView;
    //创建MediaPlayer播放视频
    MediaPlayer mPlayer;
    //设置播放状态，0x11为没有播放，0x12为正在播放，0x13为暂停播放
    int status=0x11;
    //设置播放位置
    int position=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_video);
        play=(ImageButton)findViewById(R.id.play);
        stop=(ImageButton)findViewById(R.id.stop);
        sView=(SurfaceView)findViewById(R.id.sView);
        play.setOnClickListener(this);
        stop.setOnClickListener(this);
        Intent intent=getIntent();
        mPlayer=new MediaPlayer();
        //设置SurfaceV自己不管理缓存
        sView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //设置播放时不打开屏幕
        sView.getHolder().setKeepScreenOn(true);
        sView.getHolder().addCallback(new SurfaceListener());
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.play:
                if(status==0x11){
                    startAndPlay();
                    status=0x12;
                    play.setImageResource(R.drawable.pause);
                }
                else if(status==0x12){
                    mPlayer.pause();
                    status=0x13;
                    play.setImageResource(R.drawable.play);
                }
                else if(status==0x13){
                    mPlayer.start();
                    status=0x12;
                    play.setImageResource(R.drawable.pause);
                }
                break;
            case R.id.stop:
                if(status==0x12||status==0x13){
                    mPlayer.stop();
                    status=0x11;
                    play.setImageResource(R.drawable.play);
                }
        }
    }

    private void startAndPlay() {
        try{
            mPlayer.reset();
            //设置播放地址
            mPlayer.setDataSource("/storage/emulated/0/DCIM/Camera/VID_20170327_153815.mp4");
            //设置视频画面输出到SurfaceView
            mPlayer.setDisplay(sView.getHolder());
            //准备并播放
            mPlayer.prepare();
            mPlayer.start();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private class SurfaceListener implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            //从其他应用回来时，position位置不为0，从原来位置播放视频，并将position置0
            if(position>0){
                try{
                    startAndPlay();
                    mPlayer.seekTo(position);
                    position=0;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        }
    }

    //打开其他应用时，暂停播放，保存位置
    @Override
    protected void onPause() {
        if(mPlayer.isPlaying()){
            position=mPlayer.getCurrentPosition();
            mPlayer.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if(mPlayer.isPlaying()){
            //如果正在播放，停止播放
            mPlayer.stop();
        }
        //释放资源
        mPlayer.release();
        super.onDestroy();
    }
}
