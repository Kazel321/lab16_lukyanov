package com.example.lab16_lukyanov;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

public class MessageActivity extends AppCompatActivity {

    TextView txt_Number;
    TextView txt_Nick;
    TextView txt_Port;
    TextView txt_DateTime;
    TextView txt_IP;
    EditText txt_Message;

    String nick, dateTime, message, ip;
    int number, port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        txt_Number = findViewById(R.id.textNumHis);
        txt_Nick = findViewById(R.id.textNickHis);
        txt_Port = findViewById(R.id.textPortHis);
        txt_DateTime = findViewById(R.id.textDTHis);
        txt_IP = findViewById(R.id.textIPHis);
        txt_Message = findViewById(R.id.txtMesHis);

        Intent i = getIntent();
        number = i.getIntExtra("mes-num", 0);
        nick = i.getStringExtra("mes-nick");
        dateTime = i.getStringExtra("mes-dateTime");
        message = i.getStringExtra("mes-text");
        ip = i.getStringExtra("mes-ip");
        port = i.getIntExtra("mes-port", 0);

        txt_Number.setText(String.valueOf(number));
        txt_Port.setText(String.valueOf(port));
        txt_Nick.setText(nick);
        txt_DateTime.setText(dateTime);
        txt_IP.setText(ip);
        txt_Message.setText(message);
    }
}