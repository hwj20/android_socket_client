package com.example.test;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/*
     基于socket的网络通信
     Client:
 */



public class MainActivity extends AppCompatActivity {
    private Socket st = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button bt_send = findViewById(R.id.button_send);
        EditText text_input_ip = findViewById(R.id.text_input_ip);
        TextView text_show = findViewById(R.id.text_show);

        bt_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(st == null){
                    String ip = text_input_ip.getText().toString().split(":")[0];
                    int port = Integer.parseInt(text_input_ip.getText().toString().split(":")[1]);
                    try {
                        st = new Socket(InetAddress.getByName(ip), port);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    text_show.setText("已连接\n");
                    return;
                }

                try {
                    OutputStream os = st.getOutputStream();
                    PrintWriter pw = new PrintWriter(os);
                    pw.write(text_input_ip.getText().toString());
                    pw.close();
                    os.close();
                    text_show.append("已发送\n");
                    st.shutdownOutput();

                    InputStream is = st.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);
                    String data_collect = null;
                    while((data_collect = br.readLine()) != null){
                        text_show.append("收到服务器回复："+data_collect+"\n");
                    }
                    st.shutdownInput();

                    is.close();
                    isr.close();
                    br.close();
                }
                catch (IOException e){
                    e.printStackTrace();
                }

                text_input_ip.setText("");

            }
        });
    }

}