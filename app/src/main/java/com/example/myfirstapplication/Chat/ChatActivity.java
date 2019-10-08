package com.example.myfirstapplication.Chat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.example.myfirstapplication.MulticastSocket.MulticastClient;
import com.example.myfirstapplication.MulticastSocket.MulticastClientInterface;
import com.example.myfirstapplication.R;

import java.util.Random;

public class ChatActivity extends AppCompatActivity implements MulticastClientInterface {

    private EditText editText;
    private Message message;
    private MulticastClient client;
    ListView lv;
    MessageAdapter messageAdapter;
    MemberData data;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            Bundle extras=getIntent().getExtras();


        setContentView(R.layout.activity_chat);
        // Aqui es donde se escribe el mensaje aqui a
        editText = (EditText) findViewById(R.id.editText);
        String name=extras.getString("user_name");


        data = new MemberData(name, getRandomColor());
        lv = findViewById(R.id.messages_view);
        messageAdapter = new MessageAdapter(getApplicationContext());
        lv.setAdapter(messageAdapter);
        client = new MulticastClient(4446, "230.0.0.0", this, data);

    }

    private String getRandomColor() {
        Random r = new Random();
        StringBuffer sb = new StringBuffer("#");
        while (sb.length() < 7) {
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, 7);
    }

    private String getRandomName() {
        String[] adjs = {"autumn", "hidden", "bitter", "misty", "silent", "empty", "dry", "dark", "summer", "icy", "delicate", "quiet", "white", "cool", "spring", "winter", "patient", "twilight", "dawn", "crimson", "wispy", "weathered", "blue", "billowing", "broken", "cold", "damp", "falling", "frosty", "green", "long", "late", "lingering", "bold", "little", "morning", "muddy", "old", "red", "rough", "still", "small", "sparkling", "throbbing", "shy", "wandering", "withered", "wild", "black", "young", "holy", "solitary", "fragrant", "aged", "snowy", "proud", "floral", "restless", "divine", "polished", "ancient", "purple", "lively", "nameless"};
        String[] nouns = {"waterfall", "river", "breeze", "moon", "rain", "wind", "sea", "morning", "snow", "lake", "sunset", "pine", "shadow", "leaf", "dawn", "glitter", "forest", "hill", "cloud", "meadow", "sun", "glade", "bird", "brook", "butterfly", "bush", "dew", "dust", "field", "fire", "flower", "firefly", "feather", "grass", "haze", "mountain", "night", "pond", "darkness", "snowflake", "silence", "sound", "sky", "shape", "surf", "thunder", "violet", "water", "wildflower", "wave", "water", "resonance", "sun", "wood", "dream", "cherry", "tree", "fog", "frost", "voice", "paper", "frog", "smoke", "star"};
        return (
                adjs[(int) Math.floor(Math.random() * adjs.length)] +
                        "_" +
                        nouns[(int) Math.floor(Math.random() * nouns.length)]
        );
    }

    public void Send(View view) {
        String mensaje = editText.getText().toString();
        message = new Message(mensaje, data, true
        );
        if (mensaje.length() > 0) {
            client.sendMessage(mensaje,data.getName(),data.getColor());
            messageAdapter.add(message);
            lv.setSelection(lv.getCount() - 1);
            editText.getText().clear();
        }
    }

    @Override
    public void MessageHasBeenReceived(String message, String userName, String userColor) {
        if (!userName.equals(data.getName())) {
            Message messageToSend = new Message(message, new MemberData(userName,userColor), false);
            if (message.length() > 0) {
                messageAdapter.add(messageToSend);
                lv.setSelection(lv.getCount() - 1);
                editText.getText().clear();
            }
        }
    }

    @Override
    public void ErrorFromSocketManager(Exception error) {

    }
}
