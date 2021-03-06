package com.example.elijah.golfplayertimemanagement;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class GolfCourseHomeActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Button startGamebtn;
    private DatabaseReference myRef;
    private FirebaseDatabase database;
    private String CourseName;
    private Button startgameButton;
    private Button checkStatus;
    private String Uid;
    private String difficulty;
    private Button ViewCourse;

    private Button Historybtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_golf_course_home);
        Historybtn = (Button)findViewById(R.id.ViewHistory);

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() == null){
            Uid = "Anonymous";
            Historybtn.setVisibility(View.INVISIBLE);
        }
        else{
            Uid = mAuth.getUid();
        }


        startGamebtn = (Button)findViewById(R.id.StartGame);
        ViewCourse = (Button)findViewById(R.id.ViewCourse);
        Historybtn = (Button)findViewById(R.id.ViewHistory);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();



        if(getIntent().getExtras() != null){
            CourseName = getIntent().getStringExtra("GolfCourseID");
        }
        getSupportActionBar().setTitle(CourseName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button joingame = (Button) findViewById(R.id.JoinGame);
        joingame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GolfCourseHomeActivity.this, CurrentGamesActivity.class);
                intent.putExtra("courseName", CourseName);
                startActivity(intent);
                finish();
            }
        });

        startGamebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!CourseName.equals(null)) {
                    Log.e("GolfCourseHome", CourseName);
                   SetUpGame(CourseName);
                }
            }
        });

        ViewCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GolfCourseHomeActivity.this, ViewCourseActivity.class);
                intent.putExtra("courseName", CourseName);
                startActivity(intent);
                finish();

            }
        });


        Historybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GolfCourseHomeActivity.this, HistoryListActivity.class);
                intent.putExtra("courseName", CourseName);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //loadInfo();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mAuth = FirebaseAuth.getInstance();
        if (item.getItemId() == android.R.id.home) {
            Toast.makeText(this, "Backarrow pressed", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(GolfCourseHomeActivity.this, SelectGolfCourseActivity.class);
            startActivity(intent);
            finish();
            return true;
        }else if(item.getItemId() == R.id.signout){
            Toast.makeText(this, "Signout pressed", Toast.LENGTH_SHORT).show();
            mAuth.signOut();
            PresentationActivityIntent();
            return true;
        }
        return false;
    }

    public void PresentationActivityIntent(){
        Intent intent = new Intent(GolfCourseHomeActivity.this, PresentationActivity.class);
        startActivity(intent);
        finish();
    }

    private void showGameSetUpUpdateDialog(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        LayoutInflater inflate = getLayoutInflater();

        final View dialogView = inflate.inflate(R.layout.game_setup_dialog, null);
        dialogBuilder.setView(dialogView);


        Button startgameButton = (Button)dialogView.findViewById(R.id.StartGame);

//        RadioButton blue = (RadioButton) findViewById(R.id.BlueSquare);
//        RadioButton pink;
//        RadioButton Yellow;
//        RadioButton red;

        Log.e("dialog", CourseName );
        Log.e("dialog", Uid );




        dialogBuilder.setTitle("Request Status");
        dialogBuilder.setCancelable(true);


        AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.show();
        /*
        startgameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                String currentDate = sdf.format(new Date());
                String UniqueKey = myRef.child("Games").push().getKey();
                SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
                String currentTime= time.format(new Date());

                Toast.makeText(GolfCourseHomeActivity.this, difficulty, Toast.LENGTH_SHORT).show();

                myRef.child("Games").child(UniqueKey).child("CurrentHole").setValue("Hole1");
                myRef.child("Games").child(UniqueKey).child("CourseID").setValue(CourseName);
                myRef.child("Games").child(UniqueKey).child("Date").setValue(currentDate);
                myRef.child("Games").child(UniqueKey).child("Time Started").setValue(currentTime);
                myRef.child("Games").child(UniqueKey).child("CurrentHole").setValue("Hole1");
                myRef.child("Players").child(Uid).child("CourseID").setValue(CourseName);
                myRef.child("Players").child(Uid).child("GameID").setValue(UniqueKey);
                myRef.child("Players").child(Uid).child("score").child("Hole").child("Hole1").setValue(0);



                alertDialog.dismiss();
                Intent intent = new Intent(GolfCourseHomeActivity.this, Game2Activity.class);
                intent.putExtra("CourseName", CourseName);
                intent.putExtra("GameID", UniqueKey);
                intent.putExtra("Difficulty", difficulty);
                startActivity(intent);
                finish();


            }
        });

         */

    }



    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        difficulty = "";

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.BlueSquare:
                if (checked)
                    difficulty = "Blue_Square";
                    Toast.makeText(this, "blue is clicked", Toast.LENGTH_SHORT).show();
                    break;
            case R.id.PinkDiamond:
                if (checked)
                    difficulty = "Pink_Diamond";
                    Toast.makeText(this, "pink is clicked", Toast.LENGTH_SHORT).show();
                    break;
            case R.id.RedCircle:
                if (checked)
                    difficulty = "Red_Circle";
                    Toast.makeText(this, "Red is clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.YellowTriange:
                if (checked)
                    difficulty = "Yellow_Triangle";
                    Toast.makeText(this, "Yellow is clicked", Toast.LENGTH_SHORT).show();
                break;
        }


    }


    public void SetUpGame(String courseName){

        Intent intent = new Intent(GolfCourseHomeActivity.this, GameSetUpActivity.class);
        intent.putExtra("Activity", "GolfCourseHomeActivity");
        intent.putExtra("courseName", courseName);
        startActivity(intent);

        //showGameSetUpUpdateDialog();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(CourseName == null){
            Intent intent = new Intent(GolfCourseHomeActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        }else{
            Log.e("GolfCourseHomeActivity", CourseName);
        }
    }
}
