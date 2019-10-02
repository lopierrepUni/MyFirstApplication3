package com.example.myfirstapplication.Chat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.example.myfirstapplication.R;

import java.util.Random;

public class ChatActivity extends AppCompatActivity {

    private EditText editText;
    ListView lv;
    Message mensaje;
    MessageAdapter messageAdapter;
    MemberData data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        // Aqui es donde se escribe el mensaje aqui a
        editText = (EditText) findViewById(R.id.editText);
        data = new MemberData("usuario",getRandomColor());
        lv = findViewById(R.id.messages_view);
        messageAdapter = new MessageAdapter(getApplicationContext());
        lv.setAdapter(messageAdapter);
    }

    private String getRandomColor() {
        Random r = new Random();
        StringBuffer sb = new StringBuffer("#");
        while(sb.length() < 7){
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, 7);
    }

    public void Send(View view) {
        String message = editText.getText().toString();
        mensaje = new Message(message,data,false);
        if (message.length() > 0){
            messageAdapter.add(mensaje);
            editText.getText().clear();
        }
    }
}
