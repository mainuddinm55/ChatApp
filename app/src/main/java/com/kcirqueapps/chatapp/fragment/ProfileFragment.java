package com.kcirqueapps.chatapp.fragment;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.kcirqueapps.chatapp.BuildConfig;
import com.kcirqueapps.chatapp.R;
import com.kcirqueapps.chatapp.activity.GroupActivity;
import com.kcirqueapps.chatapp.activity.SearchActivity;
import com.kcirqueapps.chatapp.activity.SplashScreenActivity;
import com.kcirqueapps.chatapp.network.api.Api;
import com.kcirqueapps.chatapp.network.api.ApiClient;
import com.kcirqueapps.chatapp.network.model.Group;
import com.kcirqueapps.chatapp.network.model.HttpResponse;
import com.kcirqueapps.chatapp.network.model.User;
import com.kcirqueapps.chatapp.utils.PrefUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener {

    private static final int REQUEST_TAKE_PHOTO = 0;
    private static final int REQUEST_PICK_PHOTO = 2;
    private static final int CAMERA_PIC_REQUEST = 1111;

    private static final String TAG = ProfileFragment.class.getSimpleName();

    private static final int MEDIA_TYPE_IMAGE = 1;
    private Uri fileUri;
    private String mImageFileLocation = "";
    private static final String IMAGE_DIRECTORY_NAME = "Android File Upload";
    private ProgressDialog pDialog;
    private String postPath;


    private CompositeDisposable disposable = new CompositeDisposable();
    private Api api;
    private TextView friendCountTextView;
    private TextView groupCountTextView;
    private CircleImageView profileImageView;
    private User currentUser;

    public ProfileFragment() {
        // Required empty public constructor
    }

    private Context context;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        initDialog();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        profileImageView = view.findViewById(R.id.profile_image_view);
        TextView nameTextView = view.findViewById(R.id.name_text_view);
        TextView emailTextView = view.findViewById(R.id.email_text_view);
        TextView phoneTextView = view.findViewById(R.id.mobile_text_view);
        TextView birthdayTextView = view.findViewById(R.id.birthday_text_view);
        friendCountTextView = view.findViewById(R.id.friend_count_text_view);
        groupCountTextView = view.findViewById(R.id.group_count_text_view);
        LinearLayout friendLayout = view.findViewById(R.id.friend_layout);
        LinearLayout groupLayout = view.findViewById(R.id.group_layout);
        ImageButton uploadBtn = view.findViewById(R.id.upload_image_btn);
        Button logoutBtn = view.findViewById(R.id.logout_btn);
        //Glide.with(view.getContext()).load(R.drawable.profile_user).into(profileImageView);
        friendLayout.setOnClickListener(this);
        groupLayout.setOnClickListener(this);
        uploadBtn.setOnClickListener(this);
        logoutBtn.setOnClickListener(this);
        api = ApiClient.getInstance().getApi();
        currentUser = new PrefUtils(context).getUser();
        if (currentUser != null) {
            Log.e(TAG, "onViewCreated: ");
            nameTextView.setText(String.format("%s %s", currentUser.getFirstName(), currentUser.getLastName()));
            emailTextView.setText(currentUser.getEmail());
            phoneTextView.setText(currentUser.getMobile());
            birthdayTextView.setText(currentUser.getDateOfBirth().substring(0, 10));
            String url = ApiClient.URL + currentUser.getPhotoUrl();
            Log.e(TAG, "URL: " + url);

            Glide.with(context).load(url).apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL)).placeholder(R.drawable.profile_user).error(R.drawable.profile_user).into(profileImageView);


            getFriendCount(currentUser.getId());
            getGroupCount(currentUser.getId());
        }
    }

    private void getGroupCount(int id) {
        disposable.add(
                api.getGroups(id).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<HttpResponse<List<Group>>>() {
                            @Override
                            public void onSuccess(HttpResponse<List<Group>> listHttpResponse) {
                                if (!listHttpResponse.isError()) {
                                    groupCountTextView.setText(String.format("%s", listHttpResponse.getResponse().size()));
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                            }
                        })
        );
    }

    private void getFriendCount(int id) {
        disposable.add(api.friendList(id).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<HttpResponse<List<User>>>() {
                    @Override
                    public void onSuccess(HttpResponse<List<User>> listHttpResponse) {
                        if (!listHttpResponse.isError()) {
                            friendCountTextView.setText(String.format("%s", listHttpResponse.getResponse().size()));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                })
        );
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.friend_layout:
                Intent searchIntent = new Intent(context, SearchActivity.class);
                searchIntent.putExtra(SearchActivity.EXTRA_TYPE, "friend");
                startActivity(searchIntent);
                getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                break;
            case R.id.group_layout:
                startActivity(new Intent(context, GroupActivity.class));
                getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                break;
            case R.id.upload_image_btn:
                new MaterialDialog.Builder(context)
                        .title(R.string.uploadImages)
                        .items(R.array.uploadImages)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                switch (which) {
                                    case 0:
                                        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                        startActivityForResult(galleryIntent, REQUEST_PICK_PHOTO);
                                        break;
                                    case 1:
                                        captureImage();
                                        break;
                                }
                            }
                        })
                        .show();
                break;
            case R.id.logout_btn:
                logout();
                break;
        }
    }

    private void logout() {
        PrefUtils prefUtils = new PrefUtils(context);
        prefUtils.clearUser();
        Intent logoutIntent = new Intent(context, SplashScreenActivity.class);
        logoutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(logoutIntent);
        getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO || requestCode == REQUEST_PICK_PHOTO) {
                if (data != null) {
                    // Get the Image from data
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = context.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    assert cursor != null;
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String mediaPath = cursor.getString(columnIndex);
                    // Set the Image in ImageView for Previewing the Media

                    cursor.close();


                    postPath = mediaPath;
                    Glide.with(context).load(selectedImage).into(profileImageView);
                    uploadFile();
                }


            } else if (requestCode == CAMERA_PIC_REQUEST) {
                if (Build.VERSION.SDK_INT > 21) {
                    postPath = mImageFileLocation;
                } else {
                    postPath = fileUri.getPath();
                }
                Glide.with(context).load(postPath).into(profileImageView);

                uploadFile();
            }
        } else if (resultCode != RESULT_CANCELED) {
            Toast.makeText(context, "Sorry, there was an error!", Toast.LENGTH_LONG).show();
        }
    }


    protected void initDialog() {

        pDialog = new ProgressDialog(context);
        pDialog.setMessage(getString(R.string.msg_loading));
        pDialog.setCancelable(true);
    }


    private void showDialog() {

        if (!pDialog.isShowing()) pDialog.show();
    }

    private void hideDialog() {

        if (pDialog.isShowing()) pDialog.dismiss();
    }


    /**
     * Launching camera app to capture image
     */
    private void captureImage() {
        if (Build.VERSION.SDK_INT > 21) { //use this if Lollipop_Mr1 (API 22) or above
            Intent callCameraApplicationIntent = new Intent();
            callCameraApplicationIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

            // We give some instruction to the intent to save the image
            File photoFile = null;

            try {
                // If the createImageFile will be successful, the photo file will have the address of the file
                photoFile = createImageFile();
                // Here we call the function that will try to catch the exception made by the throw function
            } catch (IOException e) {
                Logger.getAnonymousLogger().info("Exception error in generating the file");
                e.printStackTrace();
            }
            // Here we add an extra file to the intent to put the address on to. For this purpose we use the FileProvider, declared in the AndroidManifest.
            Uri outputUri = FileProvider.getUriForFile(
                    context,
                    BuildConfig.APPLICATION_ID + ".provider",
                    photoFile);
            callCameraApplicationIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);

            // The following is a new line with a trying attempt
            callCameraApplicationIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Logger.getAnonymousLogger().info("Calling the camera App by intent");

            // The following strings calls the camera app and wait for his file in return.
            startActivityForResult(callCameraApplicationIntent, CAMERA_PIC_REQUEST);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

            // start the image capture Intent
            startActivityForResult(intent, CAMERA_PIC_REQUEST);
        }


    }

    private File createImageFile() throws IOException {
        Logger.getAnonymousLogger().info("Generating the image - method started");

        // Here we create a "non-collision file name", alternatively said, "an unique filename" using the "timeStamp" functionality
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmSS").format(new Date());
        String imageFileName = "IMAGE_" + timeStamp;
        // Here we specify the environment location and the exact path where we want to save the so-created file
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/photo_saving_app");
        Logger.getAnonymousLogger().info("Storage directory set");

        // Then we create the storage directory if does not exists
        if (!storageDirectory.exists()) storageDirectory.mkdir();

        // Here we create the file using a prefix, a suffix and a directory
        File image = new File(storageDirectory, imageFileName + ".jpg");
        // File image = File.createTempFile(imageFileName, ".jpg", storageDirectory);

        // Here the location is saved into the string mImageFileLocation
        Logger.getAnonymousLogger().info("File name and path set");

        mImageFileLocation = image.getAbsolutePath();
        // fileUri = Uri.parse(mImageFileLocation);
        // The file is returned to the previous intent across the camera application
        return image;
    }


    private Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * returning image / video
     */
    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    // Uploading Image/Video
    private void uploadFile() {
        if (postPath == null || postPath.equals("")) {
            Toast.makeText(context, "please select an image ", Toast.LENGTH_LONG).show();
            return;
        } else {
            showDialog();
            // Map is used to multipart the file using okhttp3.RequestBody
            Map<String, RequestBody> map = new HashMap<>();
            File file = new File(postPath);
            String type = getMimeType(file);
            // Parsing any Media type file
            RequestBody requestBody = RequestBody.create(MediaType.parse(type), file);
            // Create MultipartBody.Part using file request-body,file name and part name
            map.put("file\"; filename=\"" + file.getName() + "\"", requestBody);
            //Create request body with text description and text media type


            MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
            disposable.add(
                    api.uploadFile(part)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(new DisposableSingleObserver<HttpResponse<com.kcirqueapps.chatapp.network.model.File>>() {
                                @Override
                                public void onSuccess(HttpResponse<com.kcirqueapps.chatapp.network.model.File> fileHttpResponse) {

                                    if (!fileHttpResponse.isError()) {
                                        disposable.add(api.setProfileImageUrl(fileHttpResponse.getResponse().getPath(), currentUser.getId())
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribeWith(new DisposableSingleObserver<HttpResponse<User>>() {
                                                    @Override
                                                    public void onSuccess(HttpResponse<User> userHttpResponse) {
                                                        hideDialog();
                                                        if (!userHttpResponse.isError()) {
                                                            new PrefUtils(context).putUser(userHttpResponse.getResponse());
                                                            Toasty.success(context, userHttpResponse.getMessage(), Toasty.LENGTH_SHORT).show();
                                                        } else {
                                                            Toasty.error(context, userHttpResponse.getMessage(), Toasty.LENGTH_SHORT).show();
                                                        }
                                                    }

                                                    @Override
                                                    public void onError(Throwable e) {
                                                        hideDialog();
                                                    }
                                                }));
                                    } else {
                                        hideDialog();
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {
                                    hideDialog();
                                    e.printStackTrace();
                                }
                            })
            );
        }
    }

    @NonNull
    private static String getMimeType(@NonNull File file) {
        String type = null;
        final String url = file.toString();
        final String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        }
        if (type == null) {
            type = "image/*"; // fallback type. You might set it to */*
        }
        return type;
    }
}
