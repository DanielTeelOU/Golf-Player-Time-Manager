package com.example.elijah.golfplayertimemanagement;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class CurrentGamesActivity extends AppCompatActivity {
    private DatabaseReference myRef;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private String GolfCourse;
    private ArrayList<Game> games;
    private ListView listView;
    public String Uid;
    public String anonNum;
    private int random;
    private ArrayList<Users> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_games);
        getSupportActionBar().setTitle("Current Games");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        listView = (ListView)findViewById(R.id.gamelist);
        GolfCourse = getIntent().getStringExtra("courseName");
        //anonNum = getIntent().getStringExtra("courseName");

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        random = new Random().nextInt((100) + 1) ;
        anonNum = "Anon" + random;

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() == null){
            Uid = anonNum;
        }
        else{
            Uid = mAuth.getUid();
        }

        games = new ArrayList<>();
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                int Location = 1;
                String GroupLeader = "";
                String TimeStarted = "";
                for(DataSnapshot ds: dataSnapshot.child("Games").child(GolfCourse).getChildren()){
                    String gameID = ds.getKey();
                    if(ds.child(Uid).exists()) {
                        String playerID = ds.child(Uid).getKey().toString();
                        if( ds.child("GroupLeader").exists()) {
                            GroupLeader = ds.child("GroupLeader").getValue().toString();
                        }
                        if(ds.child("Location").exists()) {
                            Location = Integer.parseInt(ds.child("Location").getValue().toString());
                        }
                        if(ds.child("TimeStarted").exists()) {
                            TimeStarted = ds.child("TimeStarted").getValue().toString();
                        }
                        Game game = new Game(gameID, GolfCourse, playerID, GroupLeader, Location, TimeStarted, 0, "");
                        games.add(game);
                        Log.e("CurrentGamesActivity", game.toString());
                    }else{
                        GroupLeader = ds.child("GroupLeader").getValue().toString();
                        if(ds.child("Location").exists()) {
                            Location = Integer.parseInt(ds.child("Location").getValue().toString());
                        }
                        else{
                            Location = 0;
                        }
                        //Toast.makeText(CurrentGamesActivity.this, TimeStarted, Toast.LENGTH_SHORT).show();
                        Game game = new Game(gameID, GolfCourse, anonNum, GroupLeader, Location, TimeStarted, 0, "");
                        games.add(game);

                    }
                }

                GameAdapter gameAdapter = new GameAdapter(getApplicationContext(), games);
                listView.setAdapter(gameAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }});

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.e("ItemClicked", games.get(i).toString());

                Intent intent = new Intent(CurrentGamesActivity.this, JoinGameSetUpActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("gameID", games.get(i).getGameID());
                bundle.putString("courseName", games.get(i).getCourseID());
                bundle.putInt("location", games.get(i).getLocation());
                intent.putExtra("bundle", bundle);
                bundle.putString("anonNum",anonNum);
                startActivity(intent);
                finish();

            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mAuth = FirebaseAuth.getInstance();
        if (item.getItemId() == android.R.id.home) {
            Toast.makeText(this, "Backarrow pressed", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(CurrentGamesActivity.this, GolfCourseHomeActivity.class);
            intent.putExtra("GolfCourseID", GolfCourse);
            startActivity(intent);
            finish();
            return true;
        }else if(item.getItemId() == R.id.signout){
            Toast.makeText(this, "Signout pressed", Toast.LENGTH_SHORT).show();
            mAuth.signOut();
            Intent intent = new Intent(CurrentGamesActivity.this, PresentationActivity.class );
            startActivity(intent);
            finish();
            return true;
        }
        return false;
    }

    public void PresentationActivityIntent(){
        Intent intent = new Intent(CurrentGamesActivity.this, PresentationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
