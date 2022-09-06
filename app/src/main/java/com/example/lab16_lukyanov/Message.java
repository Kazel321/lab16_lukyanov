package com.example.lab16_lukyanov;

public class Message {
    public int number;
    public String ip;
    public String nick;
    public String dateTime;
    public int portGet;
    public String textMes;

    public String toString() {return String.valueOf(number) + " | " + nick + " | " + textMes;}
}
