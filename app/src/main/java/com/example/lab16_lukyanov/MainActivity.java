package com.example.lab16_lukyanov;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    byte[] send_buffer = new byte[1000];
    byte[] receive_buffer = new byte[1000];

    DatagramSocket socket;
    InetAddress local_network;
    SocketAddress local_address;
    int  portGetSet, portSendSet;

    Boolean run = true;
    Boolean first = false;

    EditText txt_Sended;

    EditText txt_IpSet;
    EditText txt_PortSendSet;
    EditText txt_PortGetSet;
    EditText txt_NickSet;

    Button btnSave;

    String ipSet, nickSet, nickGet, sendMes, message, receiveMes;

    String[] settings;

    AlertDialog alertDialog;

    ArrayList<Message> lst = new ArrayList<>();
    ArrayAdapter<Message> adp;

    ListView lstHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        g.chat = new DB(this, "chat.db", null, 1);

        lstHistory = findViewById(R.id.lstHistoryMes);
        adp = new ArrayAdapter<Message>(this, android.R.layout.simple_list_item_1, lst);
        lstHistory.setAdapter(adp);

        lstHistory.setOnItemClickListener((parent, view, position, id) -> {
            Message mes = adp.getItem(position);
            Intent i = new Intent(this, MessageActivity.class);
            i.putExtra("mes-num", mes.number);
            i.putExtra("mes-dateTime", mes.dateTime);
            i.putExtra("mes-ip", mes.ip);
            i.putExtra("mes-nick", mes.nick);
            i.putExtra("mes-port", mes.portGet);
            i.putExtra("mes-text", mes.textMes);
            startActivityForResult(i, 1);
        });

        update_list();

        //dialog settings
        LayoutInflater dialogLayout = LayoutInflater.from(this);
        View dialogView = dialogLayout.inflate(R.layout.dialog_settings, null);
        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setView(dialogView);
        txt_IpSet = dialogView.findViewById(R.id.txtIpSet);
        txt_NickSet = dialogView.findViewById(R.id.txtNickSet);
        txt_PortGetSet = dialogView.findViewById(R.id.txtPortGetSet);
        txt_PortSendSet = dialogView.findViewById(R.id.txtPortSendSet);
        btnSave = dialogView.findViewById(R.id.btnSaveSet);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                run = false;
                g.chat.saveSettings(txt_NickSet.getText().toString(), txt_IpSet.getText().toString(), Integer.parseInt(txt_PortGetSet.getText().toString()), Integer.parseInt(txt_PortSendSet.getText().toString()));
                nickSet = txt_NickSet.getText().toString();
                ipSet = txt_IpSet.getText().toString();
                portSendSet = Integer.parseInt(txt_PortSendSet.getText().toString());
                alertDialog.cancel();
                if (portGetSet != Integer.parseInt(txt_PortGetSet.getText().toString()))
                {
                    portGetSet = Integer.parseInt(txt_PortGetSet.getText().toString());
                    try {
                        socket.close();
                        local_network = InetAddress.getByName("0.0.0.0");
                        local_address = new InetSocketAddress(local_network, portGetSet);
                        socket = new DatagramSocket(null);
                        first = true;
                        socket.bind(local_address);
                    } catch (UnknownHostException | SocketException e) {
                        e.printStackTrace();
                    }
                }
                run = true;
            }
        });

        settings = g.chat.getSettings();

        if(settings != null) {
            nickSet = settings[0];
            ipSet = settings[1];
            portGetSet = Integer.parseInt(settings[2]);
            portSendSet = Integer.parseInt(settings[3]);

            txt_NickSet.setText(nickSet);
            txt_IpSet.setText(ipSet);
            txt_PortGetSet.setText(String.valueOf(portGetSet));
            txt_PortSendSet.setText(String.valueOf(portSendSet));
        }

        txt_Sended = findViewById(R.id.txtSend);

        try {
            local_network = InetAddress.getByName("0.0.0.0");
            local_address = new InetSocketAddress(local_network, portGetSet);
            socket = new DatagramSocket(null);
            socket.bind(local_address);
        }
        catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }

        Runnable receiver = new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                Log.e("TEST", "Receiving is running");
                DatagramPacket received_packet = new DatagramPacket(receive_buffer, receive_buffer.length);
                while (run)
                {
                    try {
                        socket.receive(received_packet);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String s = new String(received_packet.getData(), 0, received_packet.getLength());
                    Log.e("TEST", "RECEIVED: " + s);
                    if (s.indexOf(":") != -1) {
                        String[] mes = s.split(":");
                        Integer size = Integer.parseInt(mes[0]) + 1 + mes[0].length();
                        receiveMes = "Re: ";
                        receiveMes += s.substring(size);
                        nickGet = s.substring(mes[0].length() + 1, size);

                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:MM:ss");
                        LocalDateTime now = LocalDateTime.now();
                        if (!first)
                        g.chat.addMessage(g.chat.getMaxId() + 1, String.valueOf(dtf.format(now)), nickGet, String.valueOf(received_packet.getAddress()).split("/")[1], received_packet.getPort(), receiveMes);
                        else first = false;
                        runOnUiThread(() ->
                        {
                            update_list();
                        });
                    }

                }
            }
        };

        Thread receiving_thread = new Thread(receiver);
        receiving_thread.start();


    }


    DatagramPacket send_packet;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onSend(View v)
    {
        sendMes = nickSet.length() + ":";
        sendMes += nickSet;
        message = txt_Sended.getText().toString();
        sendMes += message;

        send_buffer= sendMes.getBytes();

        try {
            InetAddress remote_address = InetAddress.getByName(ipSet);
            send_packet = new DatagramPacket(send_buffer, send_buffer.length, remote_address, portSendSet);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        send_packet.setLength(sendMes.length());

        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    Log.e("test", "sending thread is running");
                    socket.send(send_packet);
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        };

        Thread sending_thread = new Thread(r);
        sending_thread.start();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:MM:ss");
        LocalDateTime now = LocalDateTime.now();
        Log.e("sendip:  " + send_packet.getAddress(), "sendport:  " + send_packet.getPort());
        Log.e("socketport:  " + socket.getLocalPort(), "socketip:  " + socket.getLocalAddress().toString());
        g.chat.addMessage(g.chat.getMaxId()+1, String.valueOf(dtf.format(now)), nickSet, ipSet, portSendSet, "Se: " + message);
        update_list();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu1, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        switch (id)
        {
            case R.id.itm_clear:
            {
                g.chat.deleteHistory();
                update_list();
                break;
            }
            case R.id.itm_settings:
            {
                alertDialog.show();
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    void update_list()
    {
        lst.clear();
        g.chat.getAllHistory(lst);
        adp.notifyDataSetChanged();
    }
}