package com.example.test;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Objects;

/*
     基于socket的网络通信
     Client:
 */



public class MainActivity extends AppCompatActivity {
    private volatile Socket st = null;
    private boolean is_login = false;
    private int fail_to_receive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button bt_send = findViewById(R.id.button_send);
        EditText text_input_ip = findViewById(R.id.text_input_ip);
        EditText text_input_message = findViewById(R.id.text_input_message);
        EditText text_input_id = findViewById(R.id.text_input_id);
        TextView text_show = findViewById(R.id.text_show);
        text_show.setMovementMethod(ScrollingMovementMethod.getInstance());

        Runnable receive = () -> {
//            text_show.append("接受进程一开始\n");
            while(true){
                st_send(st,"AiL&GeT:1");      // 发送心跳包
//                text_show.append("已发送心跳包\n");
                try{
                    Thread.sleep(50);
                }catch(Exception e){
                    Log.e("error", e.toString());
                }

                byte[]  data = st_read(st);

                if(data == null){
                    Log.v("null:","null");
                    fail_to_receive++;
                    continue;
                }
                String bs = new String(data);
//                Log.v("hello:", new String(data)+"hello");
                String flag = "";
                int len = 0;
                try{
                    flag = bs.split(":")[0];
                    len = Integer.parseInt(bs.split(":")[1]);

                }catch (Exception e){
                    Log.e("error", e.toString());
                    continue;
                }
                if(Objects.equals(flag,"Len")&& len != 0){
//                    text_show.append("收到数据包\n");
                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(st.getInputStream()));

                        String s = "";
                        for(int i = 0; i < len; i++){
                            s = br.readLine();
                            text_show.append(s+"\n");
                        }
//                        text_show.append("数据读完了\n");
//                        br.close();
                    }
                    catch (Exception e){
                        Log.e("error", e.toString());
                    }
                }
                if(st.isClosed()){
                    text_show.append("连接已断开\n");
                    break;
                }

            }
        };
        Runnable connect = () -> {
            if(st == null) {
                String ip;
                int port;

                try {
                    ip = text_input_ip.getText().toString().split("[:：]")[0];
                    port = Integer.parseInt(text_input_ip.getText().toString().split("[:：]")[1]);
                } catch (Exception e) {
                    text_show.setText("ip:port输入错误\n");
//                       Log.e("")
                    return;
                }
                try {
                    st = new Socket(InetAddress.getByName(ip), port);
                } catch (Exception e) {
                    text_show.setText("连接发生错误\n");

                    Log.e("error", e.toString());
                    st = null;
                    return;
                }
                text_show.setText("已连接\n");
//                    text_input_ip.setText("");
            }

            if(!is_login) {
                if (Objects.equals(text_input_id.getText().toString(), "")) {
                    text_show.setText("请输入id\n");
                    return;
                }
                if(st_send(st, "RoG:"+text_input_id.getText().toString()+":")){
                    text_show.append("尝试注册/登录:"+text_input_id.getText().toString()+"\n");
                }
                try {
                    Thread.sleep(1000);
                }catch (Exception e){
                    Log.e("error", e.toString());
                }
                if(versify(st_read(st))){
                    is_login = true;
                    text_show.append("注册/登录成功\n");
                }
                else {
                    text_show.append("登录超时");
                }

                if(is_login){
                    fail_to_receive = 0;
                    Thread receive_thread = new Thread(receive);
                    receive_thread.start();
                    while(true){
                        if(st == null)  break;
                    }
                }
            }
        };

        Runnable send = () -> {
            if(Objects.equals(text_input_message.getText().toString(), "")||!is_login){
                return;
            }
            else{
                st_send(st,"MsG:"+text_input_message.getText().toString()+":");
                text_input_message.setText("");
            }
        };


// 长连接的心跳包（手写） 以及可以用Handler改写
        // TODO: 2021/3/15


        bt_send.setOnClickListener(v -> {
            if(!is_login) {
                Thread login_thread = new Thread(connect);
                login_thread.start();
            }
            else{
                Thread send_thread = new Thread(send);
                send_thread.start();
            }

        });

    }

    // TODO: 2021/3/15
    protected boolean versify(byte[] b){
//        String key = "ok";
        byte[] key = new byte[1024];
        key[0] = 'o';
        key[1] = 'k';
        String ks = new String(key);
        String bs = new String(b);
        return Objects.equals(bs, ks);
    }

    protected boolean st_send(Socket st, String msg){
        try {
            byte[]  b = new byte[1024];
            OutputStream os = st.getOutputStream();
            byte[] mb = msg.getBytes();
            if(mb.length <1023){
                System.arraycopy(mb, 0, b, 0, mb.length);
            }
            else{
                throw new IndexOutOfBoundsException();
            }
            os.write(b);
            os.flush();
            return true;
        }
        catch (Exception e){
            Log.e("error", e.toString());
            return false;
        }
    }


    protected byte[] st_read(Socket st){
        try {
            byte[] read_bytes = new byte[1024];
            InputStream is = st.getInputStream();
            is.read(read_bytes);
            return read_bytes;
        }
        catch (Exception e){
            Log.e("error", e.toString());
            return null;
        }
    }

}