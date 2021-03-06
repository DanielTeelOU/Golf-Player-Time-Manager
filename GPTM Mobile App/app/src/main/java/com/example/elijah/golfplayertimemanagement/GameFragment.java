package com.example.elijah.golfplayertimemanagement;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class GameFragment extends Fragment implements OnMapReadyCallback {
    String CourseName;
    String GameID;
    String Difficulty;
    private DatabaseReference myRef;
    private FirebaseDatabase database;
    private TextView holenum;
    private FirebaseAuth mAuth;
    private TextView Par;
    private TextView Yards;
    public int holeNum = 1;
    private Button NextHole;
    private Button BackHole;
    private Button shot;
    private TextView score;
    int playerPar = 0;
    private GoogleMap mMap;
    private GPS myGPS;
    private TimerTask scanTask;
    public String anonNum;
    public String Uid;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_game, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map3);
        mapFragment.getMapAsync(this);
        setRetainInstance(true);
        getActivity().setTitle("Fore!");
        myGPS = new GPS(getContext());




       // start();

        //(new Handler()).postDelayed(this::showElapsed, 10000);


        Bundle bundle = getArguments();
        if(bundle != null) {
            CourseName = bundle.getString("courseName");
            GameID = bundle.getString("gameID");
            anonNum = bundle.getString("anonNum");
            if(bundle.containsKey("Difficulty")) {
                Difficulty = bundle.getString("Difficulty");
            }else if(bundle.containsKey("difficulty")){
                Difficulty = bundle.getString("difficulty");
            }else{
                Difficulty = "";
                Log.e("GameFragment", "No difficulty");
            }

        }else{
            Log.e("Bundle", "Bundle is null");
        }



        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getUid()!=null) {
            Uid = mAuth.getUid();
        }
        else{
            Uid = anonNum;
        }

        NextHole = (Button) rootView.findViewById(R.id.fragnexthole);
        BackHole = (Button) rootView.findViewById(R.id.fragbackhole);
        score = (TextView)rootView.findViewById(R.id.fragplayerPar);
        shot = (Button)rootView.findViewById(R.id.fragShotName);
        holenum = (TextView) rootView.findViewById(R.id.fraghole);
        Par = (TextView) rootView.findViewById(R.id.fragpar);
        Yards = (TextView) rootView.findViewById(R.id.fragyards);
        getHoleDetails2();

        if((ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        myRef.child("Games").child(CourseName).child(GameID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    int location = Integer.parseInt(dataSnapshot.child("Location").getValue().toString());
                    if(dataSnapshot.child(Uid).child("score").child("holes").child("hole"+location).exists()) {
                        playerPar = Integer.parseInt(dataSnapshot.child(Uid).child("score").child("holes").child("hole" + location).getValue().toString());
                    }else{
                        playerPar = 0;
                    }
                    score.setText("Score: "+playerPar);
                    if(playerPar== 0){
                        shot.setText("Take Tee Shot");
                    }else{
                        shot.setText("Take A Stroke");
                    }
                    shot.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            playerPar +=1;
                            myRef.child("Games").child(CourseName).child(GameID).child(Uid).child("score").child("holes").child("hole"+holeNum).setValue(playerPar)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        score.setText("Score: " + playerPar);
                                        shot.setText("Take A Stroke");
                                    }
                                }
                            });
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }});





        return rootView;
    }

    private void getHoleDetails2(){
        myRef.child("Games").child(CourseName).child(GameID).child("Location").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                String holeNum = dataSnapshot.getValue().toString();
                Log.e("getHoleDetails", "Hole" + holeNum);
                myRef.child("GolfCourse").child(CourseName).child("Holes").child("Hole" + holeNum).child(Difficulty).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String yards = dataSnapshot.child("Yards").getValue().toString();
                            String Par = dataSnapshot.child("Par").getValue().toString();
                            Log.e("getHoleDetails", holeNum + Par + yards);
                            UIHoleNum(holeNum, Par, yards);
                            if (mMap != null) {
                                MapCurrentHole(mMap, Integer.parseInt(holeNum));
                                MapHole2(Integer.parseInt(holeNum), mMap);
                            }

                        } else {
                            Log.e("getHoleDetails", "Snapshot doesn't exist");
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
                    UpdateCurrentHole(Holenum);

                    Toast.makeText(getContext(), hole, Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getContext(), "Hole does not exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        myGPS.stopLocation();
    }

    //UpdateHoleUI
    private void UIHoleNum(String hole, String par, String yards){
        holenum.setText("Hole"+hole);
        if(!par.equals("No par set")) {
            Par.setText("Par: " + par);
        }
        if(!yards.equals("No distance set")) {
            Yards.setText("Yards: " + yards);
        }
    }
    //UpdateCurrent Hole in Firebase
    private void UpdateCurrentHole(int hole){
        myRef.child("Games").child(CourseName).child(GameID).child("Location").setValue(String.valueOf(hole));
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.resetMinMaxZoomPreference();
        mMap.setMyLocationEnabled(true);




        myRef.child("Games").child(CourseName).child(GameID).child("Location").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    holeNum = Integer.parseInt(dataSnapshot.getValue().toString());
                    Log.e("Hole Num", String.valueOf(holeNum));
                    getHoleDetails2();

                    NextHole.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            holeNum +=1;
                            //Hole Number cant be greater than 18
                            if (holeNum>18){
                                holeNum = 18;
                            }
                            myRef.child("Games").child(CourseName).child(GameID).child("Location").setValue(holeNum).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        getHoleDetails2();
                                        myRef.child("Games").child(CourseName).child(GameID).child(Uid).child("score").child("holes").child("hole"+holeNum).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.exists()) {
                                                    playerPar = Integer.parseInt(dataSnapshot.getValue().toString());
                                                }else{
                                                    playerPar = 0;
                                                }
                                                if(playerPar== 0){
                                                    shot.setText("Take Tee Shot");
                                                }else{
                                                    shot.setText("Take A Stroke");
                                                }
                                                score.setText("Score:" + playerPar);
                                                mMap.clear();
                                                Log.e("Hole Num", String.valueOf(holeNum));

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
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

                            myRef.child("Games").child(CourseName).child(GameID).child("Location").setValue(holeNum).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        getHoleDetails2();
                                        myRef.child("Games").child(CourseName).child(GameID).child(Uid).child("score").child("holes").child("hole"+holeNum).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.exists()) {
                                                    playerPar = Integer.parseInt(dataSnapshot.getValue().toString());
                                                }else{
                                                    playerPar = 0;
                                                }
                                                if(playerPar== 0){
                                                    shot.setText("Take Tee Shot");
                                                }else{
                                                    shot.setText("Take A Stroke");
                                                }
                                                score.setText("Score:" + playerPar);
                                                mMap.clear();
                                                Log.e("Hole Num", String.valueOf(holeNum));

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            });
                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void MapHole2(int Holenum, GoogleMap mMap){
        myRef.child("GolfCourse").child(CourseName).child("Holes").child("Hole"+Holenum).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("Geofence").exists()) {
                        List<LatLng> lstLatLngRoute = new LinkedList<>();
                        ArrayList<String> points = new ArrayList<>();
                        if(dataSnapshot.child("Geofence").child("CourseOutline").exists()) {
                            int i = 0;
                            for (DataSnapshot ds : dataSnapshot.child("Geofence").child("CourseOutline").getChildren()) {
                                if (dataSnapshot.exists()) {
                                    points.add(String.valueOf(i));
                                    i++;
                                }
                            }
                            Log.e("Number of points", points.toString());
                            for (int j = 0; j < points.size(); j++) {
                                double lat = Double.parseDouble(dataSnapshot.child("Geofence").child("CourseOutline").child(points.get(j)).child("lat").getValue().toString());
                                double lng = Double.parseDouble(dataSnapshot.child("Geofence").child("CourseOutline").child(points.get(j)).child("lng").getValue().toString());
                                LatLng latLng = new LatLng(lat, lng);
                                lstLatLngRoute.add(latLng);
                            }
                            Log.e("long lat", lstLatLngRoute.toString());

                            Polyline polyline = GameFragment.this.mMap.addPolyline(new PolylineOptions().clickable(true).addAll(lstLatLngRoute));
                            polyline.setTag("Hole" + holeNum);
                            getHolebounds((LinkedList) lstLatLngRoute);
                            mapTap(mMap, (LinkedList) lstLatLngRoute);

                            MapMyLocation(mMap,(LinkedList) lstLatLngRoute);
                        }


                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getHolebounds(LinkedList lstLatLngRoute){
        LatLngBounds.Builder build = new LatLngBounds.Builder();
        for(int l = 0; l< lstLatLngRoute.size();l++){
            build.include((LatLng) lstLatLngRoute.get(l));
        }
        LatLngBounds bounds = build.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,50));
    }



    //Being called in the MapHole2 method
    private void mapTap(GoogleMap mMap, LinkedList lstLatLngRoute){
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                LatLngBounds.Builder build = new LatLngBounds.Builder();
                for(int l = 0; l< lstLatLngRoute.size();l++){
                    build.include((LatLng) lstLatLngRoute.get(l));
                }
                LatLngBounds bounds = build.build();
                if(bounds.contains(latLng)){
                    myRef.child("GolfCourse").child(CourseName).child("Holes").child("Hole"+holeNum).child("Geofence").child("Hole").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                double lat = Double.valueOf(dataSnapshot.child("lat").getValue().toString());
                                double lng = Double.valueOf(dataSnapshot.child("lng").getValue().toString());
                                LatLng holelatlng = new LatLng(lat,lng);
                                double yards =  CalculationByDistance(MyLocation(), latLng);
                                if(yards <=65) {
                                    Toast.makeText(getContext(), yards + " yards. Try a Lob wedge", Toast.LENGTH_LONG).show();
                                }else if(yards >= 90 && yards < 110){
                                    Toast.makeText(getContext(), yards + " yards. Try a Sand Wedge", Toast.LENGTH_LONG).show();
                                }
                                else if(yards >= 110 && yards < 120){
                                    Toast.makeText(getContext(), yards + " yards. Try a 9-iron", Toast.LENGTH_LONG).show();
                                } else if(yards > 120 && yards <= 130){
                                    Toast.makeText(getContext(), yards + " yards. Try a 8-iron", Toast.LENGTH_LONG).show();
                                }else if(yards > 130 && yards <= 140){
                                    Toast.makeText(getContext(), yards + " yards. Try a 7-iron ", Toast.LENGTH_LONG).show();
                                }else if(yards > 140 && yards <= 150){
                                    Toast.makeText(getContext(), yards + " yards. Try a 6-iron", Toast.LENGTH_LONG).show();
                                } else if(yards > 150 && yards <= 160){
                                    Toast.makeText(getContext(), yards + " yards. Try a 5-iron", Toast.LENGTH_LONG).show();
                                } else if(yards > 160 && yards <= 170){
                                    Toast.makeText(getContext(), yards + " yards. Try a 4-iron", Toast.LENGTH_LONG).show();
                                }else if(yards > 170 && yards <= 180){
                                    Toast.makeText(getContext(), yards + " yards. Try a 3-iron", Toast.LENGTH_LONG).show();
                                } else if(yards > 180 && yards <= 190){
                                    Toast.makeText(getContext(), yards + " yards. Try a 2-iron", Toast.LENGTH_LONG).show();
                                }else if(yards > 190 && yards <= 210){
                                    Toast.makeText(getContext(), yards + " yards. Try a 3-wood", Toast.LENGTH_LONG).show();
                                }else if(yards >=230){
                                    Toast.makeText(getContext(), yards + " yards. Try a Driver", Toast.LENGTH_LONG).show();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }else{
                    Toast.makeText(getContext(), "That is not in the correct bounds", Toast.LENGTH_SHORT).show();
                }


            }
        });
    }


    private BitmapDescriptor bitmapDescriptorFromVector(Context context,@DrawableRes int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(40, 20, vectorDrawable.getIntrinsicWidth() + 40, vectorDrawable.getIntrinsicHeight() + 20);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void MapCurrentHole(GoogleMap mMap, int holeNum){
        myRef.child("GolfCourse").child(CourseName).child("Holes").child("Hole"+holeNum).child("Geofence").child("Hole").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Log.e("ButtonClicked", "DataSnapShotExists");
                    List<LatLng> lstLatLngRoute = new LinkedList<>();
                    double startLat = 0;
                    double startLng = 0;

                    LatLng Start = new LatLng(startLat, startLng);

                    Log.e("Geofence", "Exist");
                    startLat = Double.parseDouble(String.valueOf(dataSnapshot.child("lat").getValue().toString()));
                    startLng = Double.parseDouble(String.valueOf(dataSnapshot.child("lng").getValue().toString()));

                    Start = new LatLng(startLat, startLng);
                    Log.e("StartDetails", "Start = " + Start.toString());
                    if(getContext() != null) {
                        mMap.addMarker(new MarkerOptions().position(Start).title("Hole" + holeNum).icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_golf_course_black_24dp)));
                    }


//
//                        double yards = CalculationByDistance(Start, End);
//                        Log.e("Yards", String.valueOf(yards));
//                        float zoomLevel = getZoomLevel(yards);
//                        Log.e("Location", startLat + "  " + startLng + " " + endLat + " " + endLng);
//
//                        lstLatLngRoute.add(Start);
//                        lstLatLngRoute.add(End);
//
//                        zoomRoute(mMap, lstLatLngRoute);


                }else{
                    Log.e("SnapShot", "DoesNot Exitst");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private int getZoomLevel(double radius){
        double scale = radius / 2000;
        return ((int) (16 - Math.log(scale) / Math.log(2)));
    }

    public void zoomRoute(GoogleMap googleMap, List<LatLng> lstLatLngRoute) {

        if (googleMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        int routePadding = 100;
        LatLngBounds latLngBounds = boundsBuilder.build();

        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding));

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


    private LatLng MyLocation(){
        LatLng mylatlng = null;
        if(getContext() !=null) {
            Location location = myGPS.getMylocation();
            mylatlng = new LatLng(location.getLatitude(), location.getLongitude());
            Log.e("MyLocation", mylatlng.toString());

        }
        return mylatlng;
    }


    private void MapMyLocation(GoogleMap mMap, LinkedList lstLatLngRoute){
        LatLngBounds.Builder build = new LatLngBounds.Builder();
        for(int l = 0; l< lstLatLngRoute.size();l++){
            build.include((LatLng) lstLatLngRoute.get(l));
        }
        LatLngBounds bounds = build.build();
        if(getContext() != null) {
            if (bounds.contains(MyLocation())) {
                Log.e("MapMyLocation", MyLocation().toString());
                mMap.setOnMyLocationClickListener(new GoogleMap.OnMyLocationClickListener() {
                    @Override
                    public void onMyLocationClick(@NonNull Location location) {
                        LatLng mylocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(mylocation).title("me"));
                    }
                });
            }
        }


    }

//    private void updateLocation(){
//        final Handler handler = new Handler();
//        Timer timer = new Timer();
//        scanTask = new TimerTask() {
//            @Override
//            public void run() {
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                      MyLocation();
//
//
//                    }
//                });
//            }};
//        timer.schedule(scanTask, 10000, 10000);
//    }
private void start(){
    myGPS.getMylocation();
    //Toast.makeText(ReqsAssistActivity.this, mChrono.toString(), Toast.LENGTH_SHORT).show();
}

    @Override
    public void onPause() {
        super.onPause();
        myGPS.stopLocation();
    }


    @Override
    public void onResume() {
        super.onResume();
        myGPS.getMylocation();
    }


}
