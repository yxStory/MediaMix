package com.example.mediamix;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class MediaRecord extends AppCompatActivity implements View.OnClickListener {
    Button select1,arraw,bomb,shot;
    //利用SoundPool结合HashMap存放并播放短促的声音
    SoundPool soundPool;
    HashMap<Integer,Integer> soundMap=new HashMap<Integer,Integer>();

    Button up,down,vib;
    ToggleButton mute;
    //音频
    AudioManager aManager;
    //振动器
    Vibrator vibrator;

    ImageButton record, stop, play, stopPlay, image;
    //创建MediaPlayer播放音频
    MediaPlayer mPlayer;
    //创建MediaRecorder录制音频
    MediaRecorder mediaRecorder;
    EditText editText;
    File recordFile;
    //设置播放状态，0x11为没有播放，0x12为正在播放，0x13为暂停播放
    int status = 0x11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_record);
        record = (ImageButton) findViewById(R.id.record);
        stop = (ImageButton) findViewById(R.id.stop);
        play = (ImageButton) findViewById(R.id.play);
        stopPlay = (ImageButton) findViewById(R.id.stopPlay);
        image = (ImageButton) findViewById(R.id.image);
        editText = (EditText) findViewById(R.id.editText);
        select1 = (Button) findViewById(R.id.select1);

        record.setOnClickListener(this);
        stop.setOnClickListener(this);
        play.setOnClickListener(this);
        stopPlay.setOnClickListener(this);
        image.setOnClickListener(this);
        select1.setOnClickListener(this);

        //三个短促声音的按钮
        arraw=(Button)findViewById(R.id.arrow);
        bomb=(Button)findViewById(R.id.bomb);
        shot=(Button)findViewById(R.id.shot);
        arraw.setOnClickListener(this);
        bomb.setOnClickListener(this);
        shot.setOnClickListener(this);
        //创建音频池，最多容纳10个音频，音频品质为5
        soundPool=new SoundPool(10, AudioManager.STREAM_SYSTEM,5);
        //将音频文件加入到音频池，并用HashMap封装
        soundMap.put(1,soundPool.load(this,R.raw.arrow,1));
        soundMap.put(2,soundPool.load(this,R.raw.bomb,1));
        soundMap.put(3,soundPool.load(this,R.raw.shot,1));

        //调节音量
        up=(Button)findViewById(R.id.up);
        down=(Button)findViewById(R.id.down);
        mute=(ToggleButton)findViewById(R.id.mute);
        vib=(Button)findViewById(R.id.vib);
        up.setOnClickListener(this);
        down.setOnClickListener(this);
        vib.setOnClickListener(this);
        mute.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                aManager.setStreamMute(AudioManager.STREAM_MUSIC,isChecked);
            }
        });
        //获取系统的音频服务
        aManager=(AudioManager)getSystemService(Service.AUDIO_SERVICE);
        //获取系统的震动服务
        vibrator=(Vibrator)getSystemService(Service.VIBRATOR_SERVICE);

        //让按钮不可用
        stop.setEnabled(false);
        stopPlay.setEnabled(false);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.record:
                startRecorder();
                stop.setEnabled(true);
                record.setEnabled(false);
                break;
            case R.id.stop:
                stopRecorder();
                record.setEnabled(true);
                stop.setEnabled(false);
                break;
            case R.id.play:
                if (status == 0x11) {
                    playRecorder();
                    status = 0x12;
                    play.setImageResource(R.drawable.pause);
                } else if (status == 0x12) {
                    mPlayer.pause();
                    status = 0x13;
                    play.setImageResource(R.drawable.play);
                } else if (status == 0x13) {
                    mPlayer.start();
                    status = 0x12;
                    play.setImageResource(R.drawable.pause);
                }
                stopPlay.setEnabled(true);
                break;
            case R.id.stopPlay:
                if (status == 0x12 || status == 0x13) {
                    stopPlayRecorder();
                    stopPlay.setEnabled(false);
                    status = 0x11;
                    play.setImageResource(R.drawable.play);
                }
                break;
            case R.id.image:
                new AlertDialog.Builder(MediaRecord.this)
                        .setTitle("提醒：")
                        .setCancelable(false)
                        .setMessage("请确认是否打开相机？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent();
                                intent.setAction("android.media.action.STILL_IMAGE_CAMERA");
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
                break;
            case R.id.select1:
                Intent turnIntent = new Intent(MediaRecord.this, VideoPlay.class);
                startActivity(turnIntent);
                break;
            case R.id.arrow:
                soundPool.play(soundMap.get(1),1,1,0,0,1);
                break;
            case R.id.bomb:
                soundPool.play(soundMap.get(2),1,1,0,0,1);
                break;
            case R.id.shot:
                soundPool.play(soundMap.get(3),1,1,0,0,1);
                break;
            case R.id.up:
                aManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE,AudioManager.FLAG_SHOW_UI);
                break;
            case R.id.down:
                aManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_LOWER,AudioManager.FLAG_SHOW_UI);
                break;
            case R.id.vib:
                //震动2秒
                vibrator.vibrate(2000);
                break;
        }
    }

    private void playRecorder() {

        try {
            mPlayer = new MediaPlayer();
            if (recordFile != null) {
                mPlayer.reset();
                mPlayer.setDataSource(recordFile.getAbsolutePath());
                mPlayer.prepare();
                mPlayer.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopPlayRecorder() {
        if (recordFile != null) {
            mPlayer.stop();
            mPlayer.release();
        }
    }

    private void startRecorder() {

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(MediaRecord.this, "SD卡不存在，请插入Sd卡", 8000).show();
            return;
        }
        try {
            //设置存储地址
            recordFile = new File(Environment
                    .getExternalStorageDirectory().getCanonicalFile() + "/myRecord.mp3");
            if (recordFile.exists()) {
                recordFile.delete();
            }
            editText.setText(recordFile.toString());
            //创建MediaRecorder对象
            mediaRecorder = new MediaRecorder();
            //设置声音来源，麦克风
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //设置录制声音的输出格式
            mediaRecorder.setOutputFormat(MediaRecorder.AudioSource.DEFAULT);
            //设置录制声音的编码格式
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioSource.DEFAULT);
            //设置输出文件的位置
            mediaRecorder.setOutputFile(recordFile.getAbsolutePath());

            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        mediaRecorder.start();
    }

    private void stopRecorder() {
        if (recordFile != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
        }
    }
//
//    @Override
//    protected void onDestroy() {
//        if (mPlayer.isPlaying()) {
//            //停止播放
//            mediaRecorder.stop();
//            mPlayer.stop();
//        }
//        //释放资源
//        mediaRecorder.release();
//        mPlayer.release();
//
//        super.onDestroy();
//    }

}
