package com.example.elijah.golfplayertimemanagement;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class Game2Activity extends AppCompatActivity {
    String CourseName;
    String GameID;
    String Difficulty;
    private DatabaseReference myRef;
    private FirebaseDatabase database;
    private TextView holenum;
    private FirebaseAuth mAuth;
    private TextView Par;
    private TextView Yards;
    private int holeNum = 1;
    private Button NextHole;
    private Button BackHole;
    private Button map;
    private Button shot;
    private TextView score;
    int playerPar = 0;
    private Button newReq;
    private String uid;
    private FirebaseUser user;
    public String email;
    private TextView login;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game2);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        uid = user.getUid();
        email = mAuth.getCurrentUser().getEmail();

        String Uid = mAuth.getUid();

        if(!getIntent().getStringExtra("CourseName").equals(null)) {
            CourseName = getIntent().getStringExtra("CourseName");
        }
        if(!getIntent().getStringExtra("GameID").equals(null)) {
            GameID = getIntent().getStringExtra("GameID");
        }
        if(!getIntent().getStringExtra("Difficulty").equals(null)) {
            Difficulty = getIntent().getStringExtra("Difficulty");
        }

        getHoleDetails(holeNum);
        login = (TextView) findViewById(R.id.playLog);
        newReq = (Button) findViewById(R.id.request);
        NextHole = (Button) findViewById(R.id.nexthole);
        BackHole = (Button) findViewById(R.id.backhole);
        map = (Button)findViewById(R.id.map);
        score = (TextView)findViewById(R.id.playerPar);
        shot = (Button)findViewById(R.id.ShotName);

        login.setText("Logged in as:" + email + "!");


        if(playerPar== 0){
            shot.setText("Take Tee Shot");
        }else{
            shot.setText("Take A Stroke");
        }
        score.setText("Score: " + playerPar);
        shot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerPar +=1;
                score.setText("Score: " + playerPar);
                shot.setText("Take A Stroke");
                myRef.child("Games").child(GameID).child("Players").child(Uid).child("Score").child("Hole").child("Hole"+holeNum).setValue(playerPar);
            }
        });
        NextHole.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holeNum +=1;
                //Hole Number cant be greater than 18
                if (holeNum>18){
                    holeNum = 18;
                }
                getHoleDetails(holeNum);
                playerPar =0;
                score.setText("Score:" +playerPar);
                shot.setText("Take Tee Shot");
            }
        });

        newReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Toast.makeText(Game2Activity.this, "Request Sent!", Toast.LENGTH_SHORT).show();
                //Toast.makeText(Game2Activity.this, user, Toast.LENGTH_SHORT).show();


                myRef.child("Request").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        //String email = dataSnapshot.child(uid).child("email").getValue(String.class);

                        String emailTrun = email.split("@")[0];

                        myRef.child("Request").child(CourseName).push().setValue("User:" + emailTrun + ";Hole:"+ holeNum);
                        Toast.makeText(Game2Activity.this, "Request Sent!", Toast.LENGTH_SHORT).show();

                        newReq.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                        Toast.makeText(Game2Activity.this, "Request Failed!", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });



        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("ButtonClicked", "ButtonClicked");

                myRef.child("GolfCourse").child(CourseName).child("Holes").child("Hole"+holeNum).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            Log.e("ButtonClicked", "DataSnapShotExists");


                            String startLat = dataSnapshot.child("StartLat").getValue().toString();
                            String  startLng = dataSnapshot.child("StartLong").getValue().toString();
                            String  endLat = dataSnapshot.child("EndLat").getValue().toString();
                            String  endLng = dataSnapshot.child("EndLong").getValue().toString();
                            Log.e("Location", startLat+ "  " + startLng + " " +endLat + " " + endLng);
                            Intent intent = new Intent(Game2Activity.this, Maps2Activity.class);
                            intent.putExtra("StartLat", startLat);
                            intent.putExtra("StartLng", startLng);
                            intent.putExtra("EndLat", endLat);
                            intent.putExtra("EndLng", endLng);
                            intent.putExtra("holeNum", holenum.getText());
                            startActivity(intent);
                        }else{
                            Log.e("SnapShot", "DoesNot Exitst");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });

        BackHole.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holeNum -=1;
                //Hole number cant be less than 1
                if(holeNum == 0 || holeNum < 0){
                    holeNum =1;
                }
                //Hole Number cant be greater than 18
                if (holeNum>18){
                    holeNum = 18;
                }
                getHoleDetails(holeNum);

            }
        });
    }

    //Get the hole number
    private void getHoleDetails(int Holenum){
        myRef.child("GolfCourse").child(CourseName).child("Holes").child("Hole"+Holenum).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    String hole = dataSnapshot.getKey().toString();
                    String par = dataSnapshot.child(Difficulty).child("Par").getValue().toString();
                    String yards= dataSnapshot.child(Difficulty).child("Yards").getValue().toString();
                    UIHoleNum(hole, par, yards);
                    UpdateCurrentHole(hole);
                    Toast.makeText(Game2Activity.this, hole, Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(Game2Activity.this, "Hole does not exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    //UpdateHoleUI
    private void UIHoleNum(String hole, String par, String yards){
        holenum = (TextView) findViewById(R.id.hole);
        Par = (TextView) findViewById(R.id.par);
        Yards = (TextView) findViewById(R.id.yards);
        holenum.setText(hole);
        Par.setText("Par: " + par);
        Yards.setText("Yards: " + yards);
    }
    //UpdateCurrent Hole in Firebase
    private void UpdateCurrentHole(String hole){
        myRef.child("Games").child(GameID).child("CurrentHole").setValue(hole);
    }

    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius =6967488;// radius of earth in yards
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double yards = Radius * c;
        double feet = yards*3;

        Log.e("Radius Value", String.valueOf(yards) + " " + String.valueOf(feet));

        return yards;
    }

}
