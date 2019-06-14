package com.kcirqueapps.chatapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.kcirqueapps.chatapp.BuildConfig;
import com.kcirqueapps.chatapp.datasource.ChatViewModel;
import com.kcirqueapps.chatapp.service.MessagingService;
import com.kcirqueapps.chatapp.R;
import com.kcirqueapps.chatapp.adapter.ChatAdapter;
import com.kcirqueapps.chatapp.network.api.Api;
import com.kcirqueapps.chatapp.network.api.ApiClient;
import com.kcirqueapps.chatapp.network.model.Conversion;
import com.kcirqueapps.chatapp.network.model.Group;
import com.kcirqueapps.chatapp.network.model.HttpResponse;
import com.kcirqueapps.chatapp.network.model.User;
import com.kcirqueapps.chatapp.utils.FileUtil;
import com.kcirqueapps.chatapp.utils.PrefUtils;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener, ChatAdapter.ItemClickedListener {
    public static final int SINGLE_CHAT = 1;
    public static final int GROUP_CHAT = 2;

    private static final int REQUEST_TAKE_PHOTO = 0;
    private static final int REQUEST_PICK_PHOTO = 2;
    private static final int CAMERA_PIC_REQUEST = 1111;

    private static final String TAG = ChatActivity.class.getSimpleName();

    public static final int MEDIA_TYPE_IMAGE = 1;
    private Uri fileUri;
    private String mImageFileLocation = "";
    public static final String IMAGE_DIRECTORY_NAME = "Android File Upload";
    ProgressDialog pDialog;
    private String postPath;


    private CompositeDisposable disposable = new CompositeDisposable();
    public static final String EXTRA_USER = "com.kcirqueapps.chatapp.activity.EXTRA_USER";
    public static final String EXTRA_TYPE = "com.kcirqueapps.chatapp.activity.EXTRA_TYPE";
    public static final String EXTRA_GROUP = "com.kcirqueapps.chatapp.activity.EXTRA_GROUP";
    private User chatUser;
    private User currentUser;
    private int chatType;
    private Group group;
    private Api api;
    private EditText messageEditText;
    private ImageView imageView;
    private ProgressBar progressBar;
    private TextView previewTextView, noChatTextView;
    private ChatAdapter chatAdapter;
    private BroadcastReceiver broadcastReceiver;
    private String mediaUrl;
    private String mediaType = "Text";

    public static final int FILE_PICKER_REQUEST_CODE = 1;
    private String pdfPath;
    private String fileName;
    private DownloadManager downloadManager;

    private ChatViewModel chatViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(null);
        whiteNotificationBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        chatViewModel = ViewModelProviders.of(this).get(ChatViewModel.class);
        initDialog();
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        CircleImageView profileImageView = findViewById(R.id.profile_image_view);
        TextView nameTextView = findViewById(R.id.name_text_view);
        messageEditText = findViewById(R.id.message_edit_text);
        noChatTextView = findViewById(R.id.no_conversion_text_view);
        progressBar = findViewById(R.id.progress_bar);
        imageView = findViewById(R.id.image_view);
        previewTextView = findViewById(R.id.preview_text_view);
        ImageButton picBtn = findViewById(R.id.pic_btn);
        ImageButton sendMessageBtn = findViewById(R.id.send_btn);
        ImageButton attachFileBtn = findViewById(R.id.attach_file_btn);
        RecyclerView chatRecyclerView = findViewById(R.id.chat_recycler_view);
        chatRecyclerView.setHasFixedSize(true);
        picBtn.setOnClickListener(this);
        attachFileBtn.setOnClickListener(this);
        chatAdapter = new ChatAdapter(this);
        chatAdapter.setItemClickedListener(this);
        chatRecyclerView.setAdapter(chatAdapter);
        currentUser = new PrefUtils(this).getUser();
        api = ApiClient.getInstance().getApi();

        nameTextView.setOnClickListener(this);
        profileImageView.setOnClickListener(this);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            chatType = bundle.getInt(EXTRA_TYPE);
            chatUser = (User) bundle.getSerializable(EXTRA_USER);
            group = (Group) bundle.getSerializable(EXTRA_GROUP);
            if (chatUser != null) {
                Glide.with(this).load(ApiClient.URL + chatUser.getPhotoUrl()).apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL)).placeholder(R.drawable.profile_user).error(R.drawable.profile_user).into(profileImageView);
                nameTextView.setText(String.format("%s %s", chatUser.getFirstName(), chatUser.getLastName()));
            } else if ((group != null)) {
                FirebaseMessaging.getInstance().subscribeToTopic(String.format("%s%s", group.getName(), group.getId()))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.e("Subscribe with topic", "onSuccess: ");
                            }
                        });
                Glide.with(this).load(R.drawable.profile_user).into(profileImageView);
                nameTextView.setText(String.format("%s", group.getName()));
            }
            progressBar.setVisibility(View.VISIBLE);
            readMessage();
        }

        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTextMessage(messageEditText.getText().toString());

            }
        });

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(MessagingService.ACTION_RECEIVED_MESSAGE)) {
                    readMessage();
                    Log.e("Chat", "onReceive: ");
                }
            }
        };


    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(MessagingService.ACTION_RECEIVED_MESSAGE));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    private void readMessage() {
        switch (chatType) {
            case SINGLE_CHAT:
                readSingleMessage();
                break;
            case GROUP_CHAT:
                readGroupMessage();
                break;
        }
    }

    private void readSingleMessage() {
        if (currentUser != null && chatUser != null) {
            chatViewModel.getConversionList(currentUser.getId(), chatUser.getId(), "Single", 0).observe(this, new Observer<PagedList<Conversion>>() {
                @Override
                public void onChanged(PagedList<Conversion> conversions) {
                    chatAdapter.submitList(conversions);
                    toggleNoChat();
                }
            });

        }
    }

    private void toggleNoChat() {
        progressBar.setVisibility(View.GONE);
    }

    private void readGroupMessage() {
        if (currentUser != null && group != null) {
            chatViewModel.getConversionList(0, 0, "Group", group.getId()).observe(this, new Observer<PagedList<Conversion>>() {
                @Override
                public void onChanged(PagedList<Conversion> conversions) {
                    chatAdapter.submitList(conversions);
                    toggleNoChat();
                }
            });
        }
    }

    private void sendTextMessage(String message) {
        switch (chatType) {
            case SINGLE_CHAT:
                sendSingleMessage(message);
                break;
            case GROUP_CHAT:
                sendGroupMessage(message);
                break;
        }
    }

    private void sendGroupMessage(String message) {
        if (currentUser != null && group != null) {
            disposable.add(
                    api.conversion(
                            group.getId(),
                            currentUser.getId(),
                            "Group",
                            message,
                            mediaUrl,
                            fileName,
                            mediaType)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(new DisposableSingleObserver<HttpResponse>() {
                                @Override
                                public void onSuccess(HttpResponse httpResponse) {
                                    if (!httpResponse.isError()) {
                                        messageEditText.setText(null);
                                        imageView.setImageBitmap(null);
                                        imageView.setVisibility(View.GONE);
                                        previewTextView.setVisibility(View.GONE);
                                        readMessage();
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {
                                    e.printStackTrace();
                                }
                            })
            );
        }
    }

    private void sendSingleMessage(String message) {
        if (chatUser != null && currentUser != null) {
            disposable.add(
                    api.conversion(
                            chatUser.getId(), currentUser.getId(), "Single", message, mediaUrl, fileName, mediaType
                    ).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(new DisposableSingleObserver<HttpResponse>() {
                                @Override
                                public void onSuccess(HttpResponse httpResponse) {
                                    if (!httpResponse.isError()) {
                                        messageEditText.setText(null);
                                        imageView.setImageBitmap(null);
                                        imageView.setVisibility(View.GONE);
                                        previewTextView.setVisibility(View.GONE);
                                        readMessage();
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {
                                    e.printStackTrace();
                                }
                            })
            );
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    private void whiteNotificationBar(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.profile_image_view || v.getId() == R.id.name_text_view) {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra(ProfileActivity.EXTRA_USER, chatUser);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        } else if (v.getId() == R.id.pic_btn) {
            new MaterialDialog.Builder(this)
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
        } else if (v.getId() == R.id.attach_file_btn) {
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
            chooseFile.setType("*/*");
            startActivityForResult(
                    Intent.createChooser(chooseFile, "Choose a file"),
                    100
            );
            // launchPicker();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private void launchPicker() {
        new MaterialFilePicker()
                .withActivity(this)
                .withRequestCode(FILE_PICKER_REQUEST_CODE)
                .withHiddenFiles(true)
                .withFilter(Pattern.compile(".*\\.pdf$"))
                .withTitle("Select PDF file")
                .start();
    }

    public String getFileName(File file) {
        Uri uri = Uri.fromFile(new File(file.getAbsolutePath()));

        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO || requestCode == REQUEST_PICK_PHOTO) {
                if (data != null) {
                    // Get the Image from data
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    assert cursor != null;
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String mediaPath = cursor.getString(columnIndex);
                    // Set the Image in ImageView for Previewing the Media

                    cursor.close();


                    postPath = mediaPath;
                    Glide.with(this).load(selectedImage).into(imageView);
                    imageView.setVisibility(View.VISIBLE);
                    uploadImageFile();
                }


            } else if (requestCode == CAMERA_PIC_REQUEST) {
                if (Build.VERSION.SDK_INT > 21) {
                    postPath = mImageFileLocation;
                } else {
                    postPath = fileUri.getPath();
                }
                Glide.with(this).load(postPath).into(imageView);
                imageView.setVisibility(View.VISIBLE);
                uploadImageFile();
            } else if (requestCode == FILE_PICKER_REQUEST_CODE) {
                String path = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
                File file = new File(path);
                String fileName = getFileName(file);
                previewTextView.setText(fileName);
                previewTextView.setVisibility(View.VISIBLE);
                if (path != null) {
                    Log.d("Path: ", path);
                    pdfPath = path;
                    uploadPdfFile();
                    //Toast.makeText(this, "Picked file: " + path, Toast.LENGTH_LONG).show();
                }
            } else if (requestCode == 100) {
                Uri uri = data.getData();

                try {
                    File file = FileUtil.from(ChatActivity.this, uri);
                    String fileName = getFileName(file);
                    previewTextView.setText(fileName);
                    previewTextView.setVisibility(View.VISIBLE);
                    uploadPdfFile(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        } else if (resultCode != RESULT_CANCELED) {
            Toast.makeText(this, "Sorry, there was an error!", Toast.LENGTH_LONG).show();
        }
    }

    public String getPath(Uri uri) {

        String path = null;
        String[] projection = {MediaStore.Files.FileColumns.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        if (cursor == null) {
            path = uri.getPath();
        } else {
            cursor.moveToFirst();
            int column_index = cursor.getColumnIndexOrThrow(projection[0]);
            path = cursor.getString(column_index);
            cursor.close();
        }

        return ((path == null || path.isEmpty()) ? (uri.getPath()) : path);
    }

    /**
     * Checking device has camera hardware or not
     */
    private boolean isDeviceSupportCamera() {
        // this device has a camera
        // no camera on this device
        return getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA);
    }

    protected void initDialog() {

        pDialog = new ProgressDialog(this);
        pDialog.setMessage(getString(R.string.msg_loading));
        pDialog.setCancelable(true);
    }


    protected void showDialog() {

        if (!pDialog.isShowing()) pDialog.show();
    }

    protected void hideDialog() {

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
                    this,
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

    File createImageFile() throws IOException {
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


    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    public Uri getOutputMediaFileUri(int type) {
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
    private void uploadImageFile() {
        if (postPath == null || postPath.equals("")) {
            Toast.makeText(this, "please select an image ", Toast.LENGTH_LONG).show();
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
            disposable.add(api.uploadFile(part).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableSingleObserver<HttpResponse<com.kcirqueapps.chatapp.network.model.File>>() {
                        @Override
                        public void onSuccess(HttpResponse<com.kcirqueapps.chatapp.network.model.File> fileHttpResponse) {
                            hideDialog();
                            if (!fileHttpResponse.isError()) {
                                mediaUrl = fileHttpResponse.getResponse().getPath();
                                mediaType = fileHttpResponse.getResponse().getMimetype();
                                fileName = fileHttpResponse.getResponse().getOriginalname();
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
    } // Uploading Image/Video

    private void uploadPdfFile() {
        if (pdfPath == null) {
            Toast.makeText(this, "please select an image ", Toast.LENGTH_LONG).show();
            return;
        } else {
            showDialog();
            // Map is used to multipart the file using okhttp3.RequestBody
            Map<String, RequestBody> map = new HashMap<>();
            File file = new File(pdfPath);
            String type = getMimeType(file);
            // Parsing any Media type file
            RequestBody requestBody = RequestBody.create(MediaType.parse(type), file);
            // Create MultipartBody.Part using file request-body,file name and part name
            map.put("file\"; filename=\"" + file.getName() + "\"", requestBody);
            //Create request body with text description and text media type
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
            disposable.add(api.uploadFile(part).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableSingleObserver<HttpResponse<com.kcirqueapps.chatapp.network.model.File>>() {
                        @Override
                        public void onSuccess(HttpResponse<com.kcirqueapps.chatapp.network.model.File> fileHttpResponse) {
                            hideDialog();
                            if (!fileHttpResponse.isError()) {
                                mediaUrl = fileHttpResponse.getResponse().getPath();
                                mediaType = fileHttpResponse.getResponse().getMimetype();
                                fileName = fileHttpResponse.getResponse().getOriginalname();
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

    private void uploadPdfFile(File file) {

        showDialog();
        // Map is used to multipart the file using okhttp3.RequestBody
        Map<String, RequestBody> map = new HashMap<>();

        String type = getMimeType(file);
        // Parsing any Media type file
        RequestBody requestBody = RequestBody.create(MediaType.parse(type), file);
        // Create MultipartBody.Part using file request-body,file name and part name
        map.put("file\"; filename=\"" + file.getName() + "\"", requestBody);
        //Create request body with text description and text media type
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
        disposable.add(api.uploadFile(part).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<HttpResponse<com.kcirqueapps.chatapp.network.model.File>>() {
                    @Override
                    public void onSuccess(HttpResponse<com.kcirqueapps.chatapp.network.model.File> fileHttpResponse) {
                        hideDialog();
                        if (!fileHttpResponse.isError()) {
                            mediaUrl = fileHttpResponse.getResponse().getPath();
                            mediaType = fileHttpResponse.getResponse().getMimetype();
                            fileName = fileHttpResponse.getResponse().getOriginalname();
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

    @NonNull
    static String getMimeType(@NonNull File file) {
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

    @Override
    public void onDownloadItemClicked(String url) {
        Toast.makeText(this, "File Downloading", Toast.LENGTH_SHORT).show();
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        long fileDownloadId = downloadManager.enqueue(request);
    }
}
