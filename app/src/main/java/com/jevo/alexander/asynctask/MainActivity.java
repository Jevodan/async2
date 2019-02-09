package com.jevo.alexander.asynctask;

import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;


public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "myLogs";
    public static final int NOT_CONNECT = 0;
    public static final int CONNECTING = 1;
    public static final int CONNECT = 2;
    public static final int START_DOWNLOAD = 3;
    public static final int CONTINUE_DOWNLOAD = 4;
    public static final int DOWNLOAD_END = 5;
    public static final int DOWNLOAD_NONE = 6;
    TextView mTextView, mTextView2, mTextView3;
    Button mButtonStart, mButtonTest, mButtonLaunchServer;
    ProgressBar mProgressBar, mProgressBar2, mProgressBar3;
    Handler h, h2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();

        h = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                mTextView.setText("Файлов: " + msg.what);
                if (msg.what == 10) {
                    mButtonStart.setEnabled(true);
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
            }
        };

        h2 = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case NOT_CONNECT:
                        mTextView2.setText("Нет соединения");
                        mTextView3.setText("");
                        mProgressBar.setVisibility(View.INVISIBLE);
                        mProgressBar3.setVisibility(View.GONE);
                        mProgressBar2.setVisibility(View.INVISIBLE);
                        mButtonLaunchServer.setEnabled(true);
                        break;
                    case CONNECTING:
                        mTextView2.setText("Получаю трансмиссию");
                        mProgressBar2.setVisibility(View.VISIBLE);
                        break;
                    case CONNECT:
                        mTextView2.setText("Успешно!!!");
                        mProgressBar2.setVisibility(View.INVISIBLE);
                        break;

                    case START_DOWNLOAD:
                        mTextView3.setText("Загружаем " + msg.arg1 + " файлов");
                        mProgressBar3.setMax(msg.arg1);
                        mProgressBar3.setProgress(0);
                        mProgressBar3.setVisibility(View.VISIBLE);
                        break;

                    case CONTINUE_DOWNLOAD:
                        mTextView3.setText("Осталось загрузить: " + msg.arg2 + " файлов");
                        mProgressBar3.setProgress(msg.arg1);
                        break;

                    case DOWNLOAD_END:
                        mTextView3.setText("Файлы загружены успешно!!!");
                        break;

                    case DOWNLOAD_NONE:
                        mTextView3.setText("Файлов для загрузки нет");
                        mProgressBar3.setVisibility(View.INVISIBLE);
                        break;

                    default:
                        break;
                }
            }
        };
        h2.sendEmptyMessage(NOT_CONNECT);

        View.OnClickListener myClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.button_launch_server:
                        mButtonLaunchServer.setEnabled(false);
                        Thread t2 = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                serverConnect();
                            }
                        });
                        t2.start();
                        break;
                    case R.id.button_start:
                        mButtonStart.setEnabled(false);
                        mProgressBar.setVisibility(View.VISIBLE);
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 1; i <= 10; i++) {
                                    downloadFile();
                                    h.sendEmptyMessage(i);
                                    //    mTextView.setText("Закачано файлов: " + i);
                                    Log.d(LOG_TAG, "Закачано файлов: " + i);
                                }
                            }
                        });
                        t.start();
                        break;
                    case R.id.button_test:
                        Log.d(LOG_TAG, "TEST");
                        break;
                    default:
                        break;
                }

            }
        };

        mButtonStart.setOnClickListener(myClickListener);
        mButtonTest.setOnClickListener(myClickListener);
        mButtonLaunchServer.setOnClickListener(myClickListener);
    }



    private void initUI() {
        mTextView = (TextView) findViewById(R.id.text_view_main);
        mButtonStart = (Button) findViewById(R.id.button_start);
        mButtonTest = (Button) findViewById(R.id.button_test);
        mButtonLaunchServer = (Button) findViewById(R.id.button_launch_server);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar2 = (ProgressBar) findViewById(R.id.progressBar2);
        mTextView2 = (TextView) findViewById(R.id.text_view_main2);
        mTextView3 = (TextView) findViewById(R.id.text_view_main3);
        mProgressBar3 = (ProgressBar) findViewById(R.id.progressBar3);
        mProgressBar.setVisibility(View.INVISIBLE);
    }


    private void downloadFile() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void serverConnect() {
        Message msg;
        byte[] file;
        Random rand = new Random();
        try {
            h2.sendEmptyMessage(CONNECTING);
            TimeUnit.SECONDS.sleep(3);
            h2.sendEmptyMessage(CONNECT);
            TimeUnit.SECONDS.sleep(2);
            int filesCount = rand.nextInt(10);
            if (filesCount == 0){
                h2.sendEmptyMessage(DOWNLOAD_NONE);
                TimeUnit.SECONDS.sleep(1);
                h2.sendEmptyMessage(NOT_CONNECT);
                return; // заканчиваем выполнение метода
            }
            msg = h2.obtainMessage(START_DOWNLOAD, filesCount, 0);
            h2.sendMessage(msg);
            TimeUnit.SECONDS.sleep(1);
            for (int i = 1; i <= filesCount ; i++) {
                msg = h2.obtainMessage(CONTINUE_DOWNLOAD, i , filesCount - i);
                TimeUnit.SECONDS.sleep(1);
                h2.sendMessage(msg);
            }
            h2.sendEmptyMessage(DOWNLOAD_END);
            TimeUnit.SECONDS.sleep(1);
            h2.sendEmptyMessage(NOT_CONNECT);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
