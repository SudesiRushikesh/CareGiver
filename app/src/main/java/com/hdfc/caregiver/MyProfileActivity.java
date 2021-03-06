package com.hdfc.caregiver;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.ayz4sci.androidfactory.permissionhelper.PermissionHelper;
import com.hdfc.app42service.StorageService;
import com.hdfc.app42service.UploadService;
import com.hdfc.config.CareGiver;
import com.hdfc.config.Config;
import com.hdfc.dbconfig.DbHelper;
import com.hdfc.libs.AppUtils;
import com.hdfc.libs.SessionManager;
import com.hdfc.libs.Utils;
import com.shephertz.app42.paas.sdk.android.App42CallBack;
import com.shephertz.app42.paas.sdk.android.upload.Upload;
import com.shephertz.app42.paas.sdk.android.upload.UploadFileType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import pl.tajchert.nammu.PermissionCallback;

/**
 * Created by Admin on 28-01-2016.
 */
public class MyProfileActivity extends AppCompatActivity {

    //private static int intWhichScreen;
    private static String strCustomerImgName = "", strImage;
    //public static Bitmap bitmap = null;
    private static Uri uri;
    private static Handler backgroundThreadHandler;
    //private AppUtils appUtils;
    private static ProgressDialog mProgress = null;
    private static boolean isAllowed;
    //private static boolean isImageChanged=false;
    private ImageView profileImage;
    private Utils utils;
    //ImageView backbutton, edit, imageplace;
    private EditText phone;
    private EditText place;
    private EditText name;
    //TextView textViewName;
    private Button buttonContinue, buttonCancel;
    private int Flag = 0;
    private ProgressDialog progressDialog;
    //private AppUtils appUtils;
    private PermissionHelper permissionHelper;
    private SessionManager sessionManager;

    private ProgressBar progressBar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_profile);

        isAllowed = false;

        name = (EditText) findViewById(R.id.input_name);
        phone = (EditText) findViewById(R.id.input_mobile);
        place = (EditText) findViewById(R.id.input_place);
        profileImage = (ImageView) findViewById(R.id.person_icon);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        EditText email = (EditText) findViewById(R.id.input_email);
        Button signOut = (Button) findViewById(R.id.buttonLogOut);
        buttonContinue = (Button) findViewById(R.id.buttonContinue);
        Button buttonBack = (Button) findViewById(R.id.buttonBack);
        buttonCancel = (Button) findViewById(R.id.buttonCancel);

        sessionManager = new SessionManager(MyProfileActivity.this);

        permissionHelper = PermissionHelper.getInstance(this);

        if (buttonBack != null) {
            buttonBack.setVisibility(View.VISIBLE);
            buttonBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goBack();
                }
            });
        }

        utils = new Utils(MyProfileActivity.this);
        //appUtils = new AppUtils(MyProfileActivity.this);
        progressDialog = new ProgressDialog(MyProfileActivity.this);
        mProgress = new ProgressDialog(MyProfileActivity.this);

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //permissionHelper.getAllowButton()

                permissionHelper.verifyPermission(
                        new String[]{getString(R.string.access_storage)},
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        new PermissionCallback() {
                            @Override
                            public void permissionGranted() {
                                //action to perform when permission granteed
                                isAllowed = true;
                                openProfilePicker();
                            }

                            @Override
                            public void permissionRefused() {
                                //action to perform when permission refused
                                isAllowed = false;
                                Utils.toast(2, 2, getString(R.string.access_storage_false),
                                        MyProfileActivity.this);
                            }
                        }
                );

            }
        });

        if (signOut != null) {
            signOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (Utils.isConnectingToInternet(MyProfileActivity.this)) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                MyProfileActivity.this);
                        builder.setTitle(getString(R.string.confirm_logout));
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AppUtils.logout(getApplicationContext());
                                Intent dashboardIntent = new Intent(MyProfileActivity.this,
                                        LoginActivity.class);
                                startActivity(dashboardIntent);
                                finish();
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.show();
                    } else {
                        Utils.toast(2, 2, getString(R.string.warning_internet),
                                MyProfileActivity.this);
                    }
                }
            });

        }

        name.setEnabled(false);
        name.setFocusableInTouchMode(false);
        name.clearFocus();

        phone.setEnabled(false);
        phone.setFocusableInTouchMode(false);
        phone.clearFocus();

        place.setEnabled(false);
        place.setFocusableInTouchMode(false);
        place.clearFocus();

        if (email != null) {
            email.setEnabled(false);
            email.setFocusableInTouchMode(false);
            email.clearFocus();
        }

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                buttonContinue.setText(R.string.edit_settings);
                buttonCancel.setVisibility(View.GONE);
                name.setError(null);
                phone.setError(null);
                place.setError(null);

                Flag = 0;

                name.setEnabled(false);
                name.setFocusableInTouchMode(false);
                name.clearFocus();

                phone.setEnabled(false);
                phone.setFocusableInTouchMode(false);
                phone.clearFocus();

                place.setEnabled(false);
                place.setFocusableInTouchMode(false);
                place.clearFocus();


            }
        });

        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateProfile();
            }
        });

        if (Config.providerModel != null) {
            if (email != null) {
                email.setText(Config.providerModel.getStrEmail());
            }
            phone.setText(Config.providerModel.getStrContacts());
            place.setText(Config.providerModel.getStrAddress());
            name.setText(Config.providerModel.getStrName());

            File file = null;

            String strPath = "";

            try {
                strPath = sessionManager.getProfileImage();

                if (strPath != null && !strPath.equalsIgnoreCase("")
                        && !strPath.equalsIgnoreCase("N"))
                    file = new File(strPath);

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (file != null && file.exists()) {
                strImage = strPath;
            } else {
                strImage = Config.providerModel.getStrImgUrl();
            }

            Utils.loadGlide(MyProfileActivity.this, strImage, profileImage, progressBar);
        }
    }

    private void validateProfile() {

        name.setError(null);
        phone.setError(null);

        boolean cancel = false;
        View focusView = null;

        if (Flag == 0) {
            buttonContinue.setText(R.string.save_settings);
            // edit.setImageResource(R.mipmap.done);

            buttonCancel.setVisibility(View.VISIBLE);

            name.setEnabled(true);
            name.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            name.setFocusableInTouchMode(true);
            name.requestFocus();

            phone.setEnabled(true);
            phone.setInputType(InputType.TYPE_CLASS_PHONE);
            phone.setFocusableInTouchMode(true);

            place.setEnabled(true);
            place.setInputType(InputType.TYPE_CLASS_TEXT);
            place.setFocusableInTouchMode(true);
            place.requestFocus();
            Flag = 1;

        } else if (Flag == 1) {

            buttonContinue.setText(R.string.edit_settings);
            buttonCancel.setVisibility(View.GONE);
            name.setError(null);
            phone.setError(null);
            place.setError(null);

            String strName = name.getText().toString().trim();
            String strPhone = phone.getText().toString().trim();
            String strPlace = place.getText().toString().trim();
            //Toast.makeText(MyProfileActivity.this,"Data updated Successfully",Toast.LENGTH_LONG).show();

            if (TextUtils.isEmpty(strName)) {
                name.setError(getString(R.string.error_field_required));
                focusView = name;
                cancel = true;
            }

            if (TextUtils.isEmpty(strPhone)) {
                phone.setError(getString(R.string.error_field_required));
                focusView = phone;
                cancel = true;
            } else if (!utils.validCellPhone(strPhone)) {
                phone.setError(getString(R.string.error_invalid_contact_no));
                focusView = phone;
                cancel = true;
            }

            if (TextUtils.isEmpty(strPlace)) {
                place.setError(getString(R.string.error_field_required));
                focusView = place;
                cancel = true;
            }

            if (cancel) {
                focusView.requestFocus();
            } else {

                Flag = 0;

                name.setEnabled(false);
                name.setFocusableInTouchMode(false);
                name.clearFocus();

                phone.setEnabled(false);
                phone.setFocusableInTouchMode(false);
                phone.clearFocus();

                place.setEnabled(false);
                place.setFocusableInTouchMode(false);
                place.clearFocus();

                if (!Config.providerModel.getStrContacts().equalsIgnoreCase(strPhone)
                        || !Config.providerModel.getStrAddress().equalsIgnoreCase(strPlace)
                        || !Config.providerModel.getStrName().equalsIgnoreCase(strName)) {

                    Config.providerModel.setStrContacts(strPhone);
                    Config.providerModel.setStrAddress(strPlace);
                    Config.providerModel.setStrName(strName);

                    AppUtils.updateProviderJson(Config.providerModel.getStrProviderId(), true);

                    if (utils.isConnectingToInternet()) {
                        updateProviderDoc(false);
                    } else {
                        utils.toast(1, 1, getString(R.string.account_updated));
                    }
                } else {
                    utils.toast(1, 1, getString(R.string.no_changes));
                }
            }
        }

    }

    /*@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }*/

    private void openProfilePicker() {
        if (Flag == 1 && isAllowed) {

            Calendar calendar = Calendar.getInstance();
            String strFileName = String.valueOf(calendar.getTimeInMillis()) + ".jpeg";

            utils.selectImage(String.valueOf(strFileName) + ".jpeg", null, MyProfileActivity.this,
                    true);
        } else {
            Utils.showProfileImage(strImage, MyProfileActivity.this);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        permissionHelper.finish();
        super.onDestroy();
    }

    private void updateProviderDoc(final boolean isBackground) {

        if (!isBackground) {
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        StorageService storageService = new StorageService(MyProfileActivity.this);

        Cursor cursor = CareGiver.getDbCon().fetch(
                DbHelper.strTableNameCollection, new String[]{DbHelper.COLUMN_DOCUMENT},
                DbHelper.COLUMN_COLLECTION_NAME + "=? and " + DbHelper.COLUMN_OBJECT_ID + "=?",
                new String[]{Config.collectionProvider, Config.providerModel.getStrProviderId()},
                null, "0, 1", true,
                null, null
        );

        String strDocument = "";

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            strDocument = cursor.getString(0);
        }
        CareGiver.getDbCon().closeCursor(cursor);

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(strDocument);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (jsonObject != null) {

            storageService.updateDocs(jsonObject,
                    Config.providerModel.getStrProviderId(),
                    Config.collectionProvider, new App42CallBack() {
                        @Override
                        public void onSuccess(Object o) {

                            CareGiver.getDbCon().updateProvider(
                                    new String[]{"4"},
                                    new String[]{"new_updated"},
                                    new String[]{Config.providerModel.getStrProviderId(),
                                            Config.collectionProvider});

                            sessionManager.saveProfileImage("N");

                            if (!isBackground) {
                                progressDialog.dismiss();
                                utils.toast(1, 1, getString(R.string.account_updated));
                            }
                        }

                        @Override
                        public void onException(Exception e) {
                            if (!isBackground) {
                                progressDialog.dismiss();
                                if (e == null)
                                    utils.toast(2, 2, getString(R.string.warning_internet));
                                else
                                    utils.toast(1, 1, getString(R.string.error));
                            }
                        }
                    });
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        permissionHelper.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == Activity.RESULT_OK) { //&& data != null
            try {
                backgroundThreadHandler = new BackgroundThreadHandler();
                mProgress.setMessage(getString(R.string.loading));
                mProgress.show();
                switch (requestCode) {
                    case Config.START_CAMERA_REQUEST_CODE:
                        strCustomerImgName = Utils.customerImageUri.getPath();
                        Thread backgroundThreadCamera = new BackgroundThreadCamera();
                        backgroundThreadCamera.start();
                        break;

                    case Config.START_GALLERY_REQUEST_CODE:
                        if (intent.getData() != null) {
                            uri = intent.getData();
                            Thread backgroundThread = new BackgroundThreadGallery();
                            backgroundThread.start();
                        }
                        break;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void goBack() {
        Intent intent = new Intent(MyProfileActivity.this, DashboardActivity.class);
        Config.intSelectedMenu = Config.intRatingsScreen;
        startActivity(intent);
        finish();
    }

    private void checkImage(final boolean isBackground) {

        try {

            if (!isBackground) {
                progressDialog.setMessage(getResources().getString(R.string.uploading_image));
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            UploadService uploadService = new UploadService(this);

            uploadService.removeImage(Config.strCustomerImageName, Config.providerModel.
                            getStrEmail(),
                    new App42CallBack() {
                        public void onSuccess(Object response) {
                            uploadImage(isBackground);
                        }

                        @Override
                        public void onException(Exception e) {
                            if (!isBackground) {
                                if (e == null) {
                                    if (progressDialog.isShowing())
                                        progressDialog.dismiss();
                                    utils.toast(2, 2, getString(R.string.warning_internet));
                                } else {
                                    //utils.toast(1, 1, getString(R.string.error));
                                    uploadImage(isBackground);
                                }
                            }
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadImage(final boolean isBackground) {

        try {
            UploadService uploadService = new UploadService(this);

            Calendar calendar = Calendar.getInstance();
            String strnameimg = String.valueOf(calendar.getTimeInMillis());

            String emailid = Config.providerModel.getStrEmail();
            String imagename = emailid.substring(0,4);
            imagename = imagename.concat("_"+strnameimg);

            uploadService.uploadImageCommon(strCustomerImgName, imagename,
                    "Profile Picture", Config.providerModel.getStrEmail(),
                    UploadFileType.IMAGE, new App42CallBack() {

                        public void onSuccess(Object response) {

                            if (!isBackground) {
                                if (progressDialog.isShowing())
                                    progressDialog.dismiss();
                            }

                            if (response != null) {
                                Upload upload = (Upload) response;
                                ArrayList<Upload.File> fileList = upload.getFileList();

                                if (fileList.size() > 0) {

                                    Config.providerModel.setStrImgUrl(fileList.get(0).getUrl());
                                    AppUtils.updateProviderJson(Config.providerModel.
                                            getStrProviderId(), false);

                                    updateProviderDoc(false);

                                } else {
                                    if (!isBackground)
                                        utils.toast(2, 2, getString(R.string.error));
                                }
                            } else {
                                if (!isBackground)
                                    utils.toast(2, 2, getString(R.string.warning_internet));
                            }
                        }

                        @Override
                        public void onException(Exception e) {

                            if (!isBackground) {

                                if (progressDialog.isShowing())
                                    progressDialog.dismiss();

                                if (e == null)
                                    utils.toast(2, 2, getString(R.string.warning_internet));
                                else
                                    utils.toast(1, 1, getString(R.string.error));
                            }
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        goBack();
    }


    private class BackgroundThreadHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            if (mProgress.isShowing())
                mProgress.dismiss();

            try {
                sessionManager.saveProfileImage(strCustomerImgName);

                   /* Glide.with(MyProfileActivity.this)
                            .load(sessionManager.getProfileImage())
                            .centerCrop()
                            .bitmapTransform(new CropCircleTransformation(MyProfileActivity.this))
                            .placeholder(R.drawable.person_icon)
                            .crossFade()
                            .into(profileImage);*/


                Utils.loadGlide(MyProfileActivity.this, sessionManager.getProfileImage(),
                        profileImage, progressBar);

                if (utils.isConnectingToInternet())
                    checkImage(false);
                   /* else
                        Utils.toast(2, 2, getString(R.string.warning_internet),
                                MyProfileActivity.this);*/

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class BackgroundThreadGallery extends Thread {
        @Override
        public void run() {

            try {
                if (uri != null) {
                    Calendar calendar = new GregorianCalendar();
                    String strFileName = String.valueOf(calendar.getTimeInMillis()) + ".jpeg";
                    File galleryFile = utils.createFileInternalImage(strFileName);
                    strCustomerImgName = galleryFile.getAbsolutePath();
                    InputStream is = getContentResolver().openInputStream(uri);
                    utils.copyInputStreamToFile(is, galleryFile);
                    utils.compressImageFromPath(strCustomerImgName, Config.intCompressWidth,
                            Config.intCompressHeight, Config.iQuality);
                }
                backgroundThreadHandler.sendEmptyMessage(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class BackgroundThreadCamera extends Thread {
        @Override
        public void run() {
            try {
                if (strCustomerImgName != null && !strCustomerImgName.equalsIgnoreCase("")) {
                    utils.compressImageFromPath(strCustomerImgName, Config.intCompressWidth,
                            Config.intCompressHeight, Config.iQuality);
                }
                backgroundThreadHandler.sendEmptyMessage(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}