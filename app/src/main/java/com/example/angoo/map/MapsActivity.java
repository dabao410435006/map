package com.example.angoo.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationServices;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final int REQUEST_LOCATION = 2;
    private GoogleMap mMap;
    //持續更新位置存取更多資訊時要和goolge建立連線
    GoogleApiClient mGoogleApiClient;
    LocationRequest locationRequest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        //map fragment取得googlemap物件:以SupportMapFragment取得管理器
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        //以非同步的方式
        mapFragment.getMapAsync(this);
        //還未真正連線到google
        if(mGoogleApiClient==null){
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this) //加入連線後回報對象
                    .addOnConnectionFailedListener(this)//連線失敗回報對象
                    .addApi(LocationServices.API).build();

        }
        //自行設計的方法
        createLocationRequest();
    }

    private void createLocationRequest(){
        locationRequest = new LocationRequest();
        //回報速率 更新間隔 (可能其他app在用map)
        locationRequest.setInterval(5000);
        //最快的回報速率 最短間隔 (最少間隔幾秒才會收到下一位置的更新)
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    //googlemap 取得後自動執行
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // ActivityCompat.requestPermissions(Context,危險權限字串陣列,按下對話框後的辨認碼常數)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        } else {
            setupMyLocation();
        }

        //有放大縮小元件
        mMap.getUiSettings().setZoomControlsEnabled(true);
        //LatLng傳入並建立經緯度座標
        LatLng sydney = new LatLng(-34, 151);
        // MarkerOptions()產生具有選項資訊的標記選項物件
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //CameraUpdate地圖元件的視點
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        setupMyLocation();

                    } else {
                        //
                    }
                }
                else {

                }
                break;
        }
    }

    @SuppressLint("MissingPermission")
    private void setupMyLocation() {
        mMap.setMyLocationEnabled(true);
        //按下 我的位置 按鈕時的事件listener
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            //按下按鈕時執行
            public boolean onMyLocationButtonClick() {
                //透過位置服務，取得目前裝置所在
                //getSystemServiced取得系統服務 LOCATION_SERVICE取得LocationManager
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                Criteria criteria = new Criteria();
                //設定為存取精確
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                // 向系統查詢最合適的服務提供者名稱 ( 通常也是 "gps")
                String provider = locationManager.getBestProvider(criteria,true);
                //getLastKnownLocation取得目前裝置位置
                Location location = locationManager.getLastKnownLocation(provider);
                if(location!=null) {
                    //印除錯訊息
                    Log.i("LOCATION",location.getLatitude()+"/"+location.getLongitude());
                    //Zoom放大地圖到第15級CameraUpdateFactory.newLatLngZoom (取得位置,15)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),18));
                }
                return false;
            }

        });
    }
    //連線時實作
    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }
    //中斷連線實作
    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    //連線成功時呼叫
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        @SuppressLint("MissingPermission")
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(location!=null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),18));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
