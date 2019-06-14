package com.kcirqueapps.chatapp.fragment;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.arsy.maps_library.MapRadar;
import com.arsy.maps_library.MapRipple;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.clustering.ClusterManager;
import com.kcirqueapps.chatapp.R;
import com.kcirqueapps.chatapp.network.api.Api;
import com.kcirqueapps.chatapp.network.api.ApiClient;
import com.kcirqueapps.chatapp.network.model.HttpResponse;
import com.kcirqueapps.chatapp.network.model.User;
import com.kcirqueapps.chatapp.utils.ClusterMarker;
import com.kcirqueapps.chatapp.utils.MyClusterRenderedManager;
import com.kcirqueapps.chatapp.utils.PrefUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static androidx.constraintlayout.widget.Constraints.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class NearByFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 200;
    private GoogleMap mMap;
    private Context context;
    private MapRadar mapRadar;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Activity activity;

    private CompositeDisposable disposable = new CompositeDisposable();
    private ClusterManager<ClusterMarker> mClusterManager;
    private MyClusterRenderedManager mClusterManagerRenderer;
    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        activity = (Activity) context;
    }

    public NearByFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_near_by, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        CircleImageView profileImageView = view.findViewById(R.id.profile_image_view);
        User currentUser = new PrefUtils(context).getUser();
        Glide.with(this).load(ApiClient.URL + currentUser.getPhotoUrl()).apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .placeholder(R.drawable.profile_user).error(R.drawable.profile_user).into(profileImageView);
        profileImageView.setOnClickListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        getLastLocation();
    }


    private boolean checkPermission() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void getLastLocation() {
        if (checkPermission()) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        // Add a marker in Sydney, Australia, and move the camera.
                        mMap.addMarker(new MarkerOptions().position(latLng).title(""));
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(latLng)      // Sets the center of the map to location user
                                .zoom(14)                   // Sets the zoom
                                .build();                   // Creates a CameraPosition from the builder
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                        mapRadar = new MapRadar(mMap, latLng, context);
                        mapRadar.withDistance(3000);
                        mapRadar.withOuterCircleStrokeColor(0xfccd29);
                        mapRadar.withOuterCircleStrokewidth(7);
                        mapRadar.withOuterCircleTransparency(0.4f);
                        mapRadar.withClockWiseAnticlockwise(false);   //enable both side rotation
                        mapRadar.withOuterCircleFillColor(0x12000000);
                        mapRadar.withRadarColors(0x00fccd29, 0xfffccd29);
                        //withRadarColors() have two parameters, startColor and tailColor respectively
                        //startColor should start with transparency, here 00 in front of fccd29 indicates fully transparent
                        //tailColor should end with opaqueness, here f in front of fccd29 indicates fully opaque
                        mapRadar.withRadarSpeed(5);//controls radar speed
                        mapRadar.withRadarTransparency(0.4f);
                        mapRadar.startRadarAnimation();
                        getNearBy(location);
                    }
                }
            });
        } else {
            requestPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
        }
    }

    private void getNearBy(Location location) {
        User currentUser = new PrefUtils(context).getUser();
        Api api = ApiClient.getInstance().getApi();
        if (currentUser != null) {
            api.getNearBy(currentUser.getId(), location.getLatitude(), location.getLongitude())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<HttpResponse<List<User>>>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            disposable.add(d);
                        }

                        @Override
                        public void onSuccess(HttpResponse<List<User>> listHttpResponse) {
                            if (!listHttpResponse.isError()) {
                                addMapMarkers(listHttpResponse.getResponse());
                                Toasty.success(context, "" + listHttpResponse.getResponse().size(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e("NearBy: ", "onError: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });

        }
    }

    private void addMapMarkers(List<User> userList) {

        if (mMap != null) {

            if (mClusterManager == null) {
                mClusterManager = new ClusterManager<ClusterMarker>(getActivity().getApplicationContext(), mMap);
            }
            if (mClusterManagerRenderer == null) {
                mClusterManagerRenderer = new MyClusterRenderedManager(
                        context,
                        mMap,
                        mClusterManager
                );
                mClusterManager.setRenderer(mClusterManagerRenderer);
            }

            for (User user : userList) {

                try {

                    String title = user.getFirstName() + " " + user.getLastName();

                    String snippet = getCompleteAddressString(user.getLat(), user.getLng());
                    ClusterMarker newClusterMarker = new ClusterMarker(
                            new LatLng(user.getLat(), user.getLng()),
                            title,
                            snippet,
                            user.getPhotoUrl(),
                            user
                    );
                    mClusterManager.addItem(newClusterMarker);
                    mClusterMarkers.add(newClusterMarker);

                } catch (NullPointerException e) {
                    Log.e(TAG, "addMapMarkers: NullPointerException: " + e.getMessage());
                }

            }
            mClusterManager.cluster();


        }
    }

    private String getCompleteAddressString(double latitude, double longitude) {

        String strAdd = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);

                strAdd = returnedAddress.getAddressLine(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strAdd;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mapRadar != null && mapRadar.isAnimationRunning()) {
            mapRadar.stopRadarAnimation();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.profile_image_view) {
            NavController navController = Navigation.findNavController(getView());
            navController.navigate(R.id.nav_profile);
        }
    }
}
