package com.example.elijah.golfplayertimemanagement;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class Game3Activity extends AppCompatActivity  {
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private GoogleMap mMap;
    private DatabaseReference myRef;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private Bundle bundle;
    private LocationCallback locationCallback;
    private Chronometer mChrono;
    public String email;
    public Map<String, Object> taskMap;
    public Date currentTime;
    private DateFormat df;
    public String currentTime1;
    public String holenum;
    public int holeNum;
    public String CourseName;
    public String gameID;
    public String Uid;
    public String anonNum;
    public String emailTrun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game3);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
        bundle = getIntent().getBundleExtra("bundle");
        GameFragment defaultFragment = new GameFragment();
        getSupportActionBar().setTitle("Game");

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        mAuth = FirebaseAuth.getInstance();
        taskMap = new HashMap<>();
        df = new SimpleDateFormat("h:mm a");

        mChrono = (Chronometer) findViewById(R.id.chrono);
        mChrono.setVisibility(View.INVISIBLE);


        start();

        (new Handler()).postDelayed(this::showElapsed, 10000);

        if(bundle != null) {
            defaultFragment.setArguments(bundle);
            Log.e("Game2Activity", bundle.getString("courseName"));
            CourseName = bundle.getString("courseName");
            gameID = bundle.getString("gameID");
            anonNum = bundle.getString("anonNum");


        }else{
            defaultFragment.setArguments(savedInstanceState);
        }

        if(mAuth.getCurrentUser() == null){
            email = anonNum;
        }
        else{
            email = mAuth.getCurrentUser().getEmail();
        }

        myRef.child("Games").child(CourseName).child(gameID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    holenum = dataSnapshot.child("Location").getValue().toString();
                }else{
                    Toast.makeText(getApplicationContext(), "The Game Has Ended", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Game3Activity.this, GolfCourseHomeActivity.class);
                    intent.putExtra("GolfCourseID", CourseName);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        Log.e("Game2Activity", bundle.toString());







        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() == null){
            Uid = anonNum;
        }
        else{
            Uid = mAuth.getUid();
        }


        for(int i = 0; i<getSupportFragmentManager().getFragments().size();i++){
            getSupportFragmentManager().getFragments().get(i).setArguments(bundle);

        }

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, defaultFragment).commit();



    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            Fragment selectedFragment = null;
            switch (menuItem.getItemId()){
                case R.id.NavHome:
                    selectedFragment = new GameFragment();
                    getSupportActionBar().setTitle("Your Game");
                    break;
                case R.id.NavAdmin:
                    selectedFragment = new AdminFragment();
                    getSupportActionBar().setTitle("Staff Requests");

                    break;
                case R.id.Navgame:
                    selectedFragment = new OverviewFragment();
                    getSupportActionBar().setTitle("Game Overview");

                    break;
                case R.id.NavNext:
                    selectedFragment = new NextHoleFragment();
                    getSupportActionBar().setTitle("Next Hole Info");

                    break;
            }

            selectedFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    selectedFragment).commit();



            return true;


        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if(gameID == null || CourseName == null){
            Intent intent = new Intent(Game3Activity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //loadInfo();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_game_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mAuth = FirebaseAuth.getInstance();

       if(item.getItemId() == R.id.signout1){
            Toast.makeText(this, "Signout pressed", Toast.LENGTH_SHORT).show();
            mAuth.signOut();
            Intent intent = new Intent(Game3Activity.this, PresentationActivity.class);
            startActivity(intent);
            finish();
            return true;
        }else if(item.getItemId() == R.id.endGame){

           myRef.child("Games").child(CourseName).child(gameID).addListenerForSingleValueEvent(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                   myRef.child("GameHistory").child(CourseName).child(gameID).setValue(dataSnapshot.getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                       @Override
                       public void onComplete(@NonNull Task<Void> task) {
                           if(task.isSuccessful()){
                               myRef.child("Games").child(CourseName).child(gameID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                   @Override
                                   public void onComplete(@NonNull Task<Void> task) {
                                       if(task.isSuccessful()){
                                           Toast.makeText(Game3Activity.this, "Game Ended", Toast.LENGTH_SHORT).show();
                                           Intent intent = new Intent(Game3Activity.this, GolfCourseHomeActivity.class);
                                           intent.putExtra("GolfCourseID", CourseName);
                                           startActivity(intent);
                                           finish();
                                       }
                                   }
                               });
                           }
                       }
                   });
               }

               @Override
               public void onCancelled(@NonNull DatabaseError databaseError) {

               }
           });
       }
        return false;
    }


    private void start(){
        mChrono.start();
        //Toast.makeText(ReqsAssistActivity.this, mChrono.toString(), Toast.LENGTH_SHORT).show();
    }
    private void showElapsed() {
        long elapsed= SystemClock.elapsedRealtime() - mChrono.getBase();
        if( elapsed >= 900000){
            Toast.makeText(Game3Activity.this, "Your time is up!: ",
                    Toast.LENGTH_SHORT).show();
            mChrono.stop();
            //UNCOMMENT THIS IF WE WANT OVERDUE PLAYERS TO GET REPORTED

            myRef.child("Requests").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override


                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(email != anonNum) {
                    emailTrun = email.split("@")[0];
                }
                else{
//                    emailTrun = anonNum;
                    email = anonNum;
                }
                currentTime1 = df.format(Calendar.getInstance().getTime());
                holeNum = Integer.parseInt(holenum);
                taskMap.put("User", email);
                taskMap.put("Request", "Player has exceeded 15 minutes, time is up!");
                taskMap.put("Location", holeNum );
                taskMap.put("Time", currentTime1);
                taskMap.put("Status", "Pending");

                myRef.child("Requests").child(CourseName).push().setValue(taskMap);


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                    Toast.makeText(Game3Activity.this, "Request Failed!", Toast.LENGTH_SHORT).show();
                }
            });



        }
        Toast.makeText(Game3Activity.this, "Elapsed milliseconds: " + elapsed,
                Toast.LENGTH_SHORT).show();
    }


}
