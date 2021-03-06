package com.hdfc.caregiver;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ayz4sci.androidfactory.permissionhelper.PermissionHelper;
import com.hdfc.app42service.App42GCMService;
import com.hdfc.app42service.PushNotificationService;
import com.hdfc.app42service.StorageService;
import com.hdfc.app42service.UploadService;
import com.hdfc.config.CareGiver;
import com.hdfc.config.Config;
import com.hdfc.dbconfig.DbHelper;
import com.hdfc.libs.AsyncApp42ServiceApi;
import com.hdfc.libs.Utils;
import com.hdfc.models.ActivityModel;
import com.hdfc.models.ImageModel;
import com.hdfc.models.MilestoneModel;
import com.hdfc.views.TouchImageView;
import com.shephertz.app42.paas.sdk.android.App42CallBack;
import com.shephertz.app42.paas.sdk.android.App42Exception;
import com.shephertz.app42.paas.sdk.android.storage.Storage;
import com.shephertz.app42.paas.sdk.android.upload.Upload;
import com.shephertz.app42.paas.sdk.android.upload.UploadFileType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import pl.tajchert.nammu.PermissionCallback;

public class FeatureActivity extends AppCompatActivity {

    //public static int IMAGE_COUNT = 0;
    //private static int iActivityPosition = -1;
    private static String strImageName = "";
    private static StorageService storageService;
    private static Handler backgroundThreadHandler, backgroundThreadHandlerLoad;
    private static ActivityModel act;
    private static String strName, strCustomerName, strCustomerUrl, strDependentName;
    private static ArrayList<String> imagePaths = new ArrayList<>();
    private static ArrayList<Bitmap> bitmaps = new ArrayList<>();
    private static boolean bLoad, isCompleted;
    private static boolean bViewLoaded, mImageChanged;
    private static ArrayList<ImageModel> imageModels;
    private boolean isAccessible;
    private int mImageCount, mImageUploadCount;
    private LinearLayout layout;
    private RelativeLayout loadingPanel;
    private Utils utils;
    private int bWhichScreen = 3;
    private boolean success;
    private TextView textViewTime, txtdescription;
    private LinearLayout linearLayoutAttach;
    private String strCustomerEmail, strCustomerNo, strDependentNo;
    private Button done;
    private PermissionHelper permissionHelper;
    //private boolean isAllowed;
    //private FeedBackModel feedBackModel;
    private ProgressBar progressBar2;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_features);

        ImageView imgLogoHeaderTaskDetail = (ImageView) findViewById(R.id.imgLogoHeaderTaskDetail);
        //ImageView imageViewFeedback = (ImageView) findViewById(R.id.imageViewFeedback);
        done = (Button) findViewById(R.id.buttonVegetibleDone);
        linearLayoutAttach = (LinearLayout) findViewById(R.id.linearLayout1);
        ProgressBar progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);
        progressBar2 = (ProgressBar) findViewById(R.id.progressBar2);
        progressDialog = new ProgressDialog(FeatureActivity.this);
        Button cancel = (Button) findViewById(R.id.buttonBack);
        TextView textViewActivityName = (TextView) findViewById(R.id.txtActivityName);
        textViewTime = (TextView) findViewById(R.id.txtActivityTime);
        txtdescription = (TextView) findViewById(R.id.txtdescription);

        isAccessible = true;
        isCompleted = false;

        permissionHelper = PermissionHelper.getInstance(this);

        loadingPanel = (RelativeLayout) findViewById(R.id.loadingPanel);
        layout = (LinearLayout) findViewById(R.id.linear);

        bLoad = false;

        //IMAGE_COUNT = 0;
        mImageUploadCount = 0;
        //multiBitmapLoader = new MultiBitmapLoader(FeatureActivity.this);

        TextView dependentName = (TextView) findViewById(R.id.textViewHeaderTaskDetail);

        LinearLayout linearName = (LinearLayout) findViewById(R.id.linearName);

        mImageCount = 0;
        mImageChanged = false;
        bitmaps.clear();

        try {
            Bundle b = getIntent().getExtras();

            bWhichScreen = b.getInt("WHICH_SCREEN", 3);

            if (cancel != null) {
                cancel.setVisibility(View.VISIBLE);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goBack();
                    }
                });
            }

            act = (ActivityModel) b.getSerializable("ACTIVITY");
            /*iActivityPosition = b.getInt("ACTIVITY_POSITION", -1);

            if (act == null || iActivityPosition > -1)
                act = Config.activityModels.get(iActivityPosition);*/

            Cursor cursor1 = CareGiver.getDbCon().fetch(
                    DbHelper.strTableNameCollection, new String[]{DbHelper.COLUMN_DOCUMENT},
                    DbHelper.COLUMN_COLLECTION_NAME + "=? and " + DbHelper.COLUMN_OBJECT_ID
                            + "=? and " + DbHelper.COLUMN_PROVIDER_ID + "=?",
                    new String[]{Config.collectionDependent, act.getStrDependentID(),
                            Config.providerModel.getStrProviderId()},
                    null, "0,1", true, null, null
            );

            JSONObject jsonObject = null;

            if (cursor1.getCount() > 0) {
                cursor1.moveToFirst();
                try {
                    if (cursor1.getString(0) != null && !cursor1.getString(0).equalsIgnoreCase("")) {
                        jsonObject = new JSONObject(cursor1.getString(0));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            CareGiver.getDbCon().closeCursor(cursor1);

            String name = "";
            try {
                if (jsonObject != null) {

                    if (jsonObject.getString("dependent_name") != null
                            && !jsonObject.getString("dependent_name").equalsIgnoreCase("")) {
                        name = jsonObject.optString("dependent_name");
                        strDependentName = jsonObject.optString("dependent_name");
                    }

                    if (jsonObject.getString("dependent_contact_no") != null
                            && !jsonObject.getString("dependent_contact_no").equalsIgnoreCase("")) {
                        strDependentNo = jsonObject.optString("dependent_contact_no");
                    }

                    if (imgLogoHeaderTaskDetail != null
                            && jsonObject.getString("dependent_profile_url") != null
                            && !jsonObject.getString("dependent_profile_url").
                            equalsIgnoreCase("")) {

                        Utils.loadGlide(FeatureActivity.this,
                                jsonObject.getString("dependent_profile_url"),
                                imgLogoHeaderTaskDetail,
                                progressBar1);
                    }
                } else {
                    isAccessible = false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (name.length() > 20)
                name = name.substring(0, 18) + "..";

            if (dependentName != null) {
                dependentName.setText(name);
            }

            Cursor cursor2 = CareGiver.getDbCon().fetch(
                    DbHelper.strTableNameCollection,
                    new String[]{DbHelper.COLUMN_DOCUMENT},
                    DbHelper.COLUMN_COLLECTION_NAME + "=? and "
                            + DbHelper.COLUMN_OBJECT_ID + "=? and " + DbHelper.COLUMN_PROVIDER_ID
                            + "=?",
                    new String[]{Config.collectionCustomer, act.getStrCustomerID(),
                            Config.providerModel.getStrProviderId()}, null, "0,1", true, null, null
            );

            JSONObject jsonObject1 = null;

            if (cursor2.getCount() > 0) {
                cursor2.moveToFirst();
                try {
                    if (cursor2.getString(0) != null && !cursor2.getString(0).equalsIgnoreCase("")) {
                        jsonObject1 = new JSONObject(cursor2.getString(0));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            CareGiver.getDbCon().closeCursor(cursor2);

            try {
                if (jsonObject1 != null) {
                    if (jsonObject1.getString("customer_name") != null
                            && !jsonObject1.getString("customer_name").equalsIgnoreCase("")) {
                        strCustomerName = jsonObject1.getString("customer_name");
                    }

                    if (jsonObject1.getString("customer_email") != null
                            && !jsonObject1.getString("customer_email").equalsIgnoreCase("")) {
                        strCustomerEmail = jsonObject1.optString("customer_email");
                    }

                    if (jsonObject1.getString("customer_contact_no") != null
                            && !jsonObject1.getString("customer_contact_no").equalsIgnoreCase("")) {
                        strCustomerNo = jsonObject1.optString("customer_contact_no");
                    }

                    if (jsonObject1.getString("customer_profile_url") != null
                            && !jsonObject1.getString("customer_profile_url").
                            equalsIgnoreCase("")) {
                        strCustomerUrl = jsonObject1.getString("customer_profile_url");
                    }
                } else {
                    isAccessible = false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            /*if (imageViewFeedback != null) {

                if (act.getFeedBackModels().size() > 0) {

                    imageViewFeedback.setVisibility(View.VISIBLE);

                    imageViewFeedback.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            final AlertDialog.Builder builder = new AlertDialog.Builder(
                                    FeatureActivity.this);

                            LayoutInflater inflater = getLayoutInflater();
                            View dialogView = inflater.inflate(R.layout.feedback_popup, null);
                            builder.setView(dialogView);

                            TextView ratingTime = (TextView) dialogView.findViewById(R.id.
                                    ratingTime);
                            ImageView ratingImage = (ImageView) dialogView.findViewById(
                                    R.id.ratinegImage);

                            ratingTime.setText(act.getFeedBackModels().get(0).getStrFeedBackTime());

                            if (act.getFeedBackModels().get(0).getIntFeedBackRating() == 1) {
                                ratingImage.setImageDrawable(getResources().getDrawable(R.drawable.
                                        smiley_1));
                            } else if (act.getFeedBackModels().get(0).getIntFeedBackRating() == 2) {
                                ratingImage.setImageDrawable(getResources().getDrawable(R.drawable.
                                        smiley_2));
                            } else if (act.getFeedBackModels().get(0).getIntFeedBackRating() == 3) {
                                ratingImage.setImageDrawable(getResources().getDrawable(R.drawable.
                                        smiley_3));
                            } else if (act.getFeedBackModels().get(0).getIntFeedBackRating() == 4) {
                                ratingImage.setImageDrawable(getResources().getDrawable(R.drawable.
                                        smiley_4));
                            } else {
                                ratingImage.setImageDrawable(getResources().getDrawable(R.drawable.
                                        smiley_5));
                            }

                            builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            AlertDialog alertDialog = builder.create();
                            //builder.show();
                            alertDialog.show();

                        }
                    });
                }

            }*/

            if (linearName != null && jsonObject1 != null) {
                linearName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(
                                FeatureActivity.this);
                        LayoutInflater inflater = getLayoutInflater();
                        View dialogView = inflater.inflate(R.layout.name_popup, null);
                        builder.setView(dialogView);

                        TextView textName = (TextView) dialogView.findViewById(R.id.textPopupName);
                        ImageView imageDialog = (ImageView) dialogView.findViewById(R.id.popupImage);

                        try {

                            if (strCustomerName.length() > 20)
                                strCustomerName = strCustomerName.substring(0, 18) + "..";

                            textName.setText(strCustomerName);

                            Utils.loadGlide(FeatureActivity.this, strCustomerUrl, imageDialog,
                                    progressBar2);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        //builder.show();
                        alertDialog.show();
                    }
                });
            }

            String strActivityName = act.getStrActivityName();
            if (strActivityName.length() > 20)
                strActivityName = strActivityName.substring(0, 18) + "..";

            if (textViewActivityName != null) {
                textViewActivityName.setText(strActivityName);
            }

            String strDescription = act.getStrActivityDesc();
            if (txtdescription != null) {
                txtdescription.setText("Description: " + strDescription);
            }

            bViewLoaded = false;

            if (act.getStrActivityStatus().equalsIgnoreCase("completed"))
                isCompleted = true;

            storageService = new StorageService(FeatureActivity.this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void goBack() {

        if (mImageCount > 0 || mImageChanged) {

            AlertDialog.Builder alertbox =
                    new AlertDialog.Builder(FeatureActivity.this);
            alertbox.setTitle(getString(R.string.delete_image));
            alertbox.setMessage(getString(R.string.confirm_unsaved_images));
            alertbox.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    goBackIntent();
                }
            });
            alertbox.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    arg0.dismiss();
                }
            });
            alertbox.show();

        } else {
            goBackIntent();
        }
    }

    private void goBackIntent() {
        mImageCount = 0;
        mImageUploadCount = 0;
        mImageChanged = false;
        bitmaps.clear();
        //imageModels.clear();
        //IMAGE_COUNT = 0;

        if (bWhichScreen == 1)
            Config.intSelectedMenu = Config.intNotificationScreen;
        else if (bWhichScreen == 2)
            Config.intSelectedMenu = Config.intRatingsScreen;
        else
            Config.intSelectedMenu = Config.intDashboardScreen;

        Intent intent = new Intent(FeatureActivity.this, DashboardActivity.class);
        intent.putExtra("LOAD", bLoad);
        intent.putExtra("RETAIN_DATE", true);
        startActivity(intent);
        finish();
    }

    private void updateImages() {

        JSONObject jsonToUpdate = null;

        JSONArray jsonArrayImagesAdded = new JSONArray();

        try {
            jsonToUpdate = new JSONObject();

            ArrayList<ImageModel> mTImageModels = new ArrayList<>();

            if (imageModels.size() > 0) {

                for (ImageModel mUpdateImageModel : imageModels) {

                    JSONObject jsonObjectImages = new JSONObject();

                    jsonObjectImages.put("image_name", mUpdateImageModel.getStrImageName());
                    jsonObjectImages.put("image_url", mUpdateImageModel.getStrImageUrl());
                    jsonObjectImages.put("image_description", mUpdateImageModel.getStrImageDesc());
                    jsonObjectImages.put("image_taken", mUpdateImageModel.getStrImageTime());

                    jsonArrayImagesAdded.put(jsonObjectImages);
                    mTImageModels.add(mUpdateImageModel);
                }
            } else {
                jsonArrayImagesAdded.put("{\"0\":\"empty\"}");
            }

            jsonToUpdate.put("images", jsonArrayImagesAdded);

            //Config.activityModels.get(iActivityPosition).clearImageModel();
            //Config.activityModels.get(iActivityPosition).setImageModels(mTImageModels);
            imageModels = mTImageModels;

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (utils.isConnectingToInternet()) {

            final JSONArray jsonArray = jsonArrayImagesAdded;

            //dloadingPanel.setVisibility(View.VISIBLE);
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setCancelable(false);
            progressDialog.show();

            storageService.updateDocs(jsonToUpdate,
                    act.getStrActivityID(),
                    Config.collectionActivity, new App42CallBack() {
                        @Override
                        public void onSuccess(Object o) {
                            //IMAGE_COUNT = 0;


                            //
                            String selection = DbHelper.COLUMN_OBJECT_ID + " = ? and "
                                    + DbHelper.COLUMN_COLLECTION_NAME + "=?";

                            String[] selectionArgs = {
                                    act.getStrActivityID()
                                    , Config.collectionActivity};

                            Cursor cursor = CareGiver.getDbCon().fetch(
                                    DbHelper.strTableNameCollection,
                                    DbHelper.COLLECTION_FIELDS, selection, selectionArgs,
                                    null, "0, 1", true,
                                    null, null
                            );

                            if (cursor.getCount() > 0) {
                                cursor.moveToFirst();

                                try {
                                    JSONObject jsonObject = new JSONObject(cursor.getString(2));
                                    jsonObject.put("images", jsonArray);

                                    //
                                    String values1[] = {act.getStrActivityID(),
                                            "",
                                            jsonObject.toString(),
                                            Config.collectionActivity, "", "2",
                                            act.getStrProviderID()
                                    };

                                    // WHERE clause arguments
                                    CareGiver.getDbCon().updateInsert(
                                            DbHelper.strTableNameCollection,
                                            selection, values1, DbHelper.COLLECTION_FIELDS,
                                            selectionArgs);
                                    ////
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            CareGiver.getDbCon().closeCursor(cursor);

                            try {
                                JSONObject jsonObjectPush = new JSONObject();
                                String strDateNow;
                                Calendar calendar = Calendar.getInstance();
                                Date dateNow = calendar.getTime();
                                strDateNow = Utils.convertDateToString(dateNow);

                                String strPushMessage = getString(
                                        R.string.notification_activity_image)
                                        + act.getStrActivityName();

                                //ios start
                                JSONObject jsonObjectTemp = new JSONObject();
                                jsonObjectTemp.put("alert", strPushMessage);
                                jsonObjectPush.put("aps", jsonObjectTemp);
                                //ios end

                                jsonObjectPush.put("created_by", Config.providerModel.
                                        getStrProviderId());
                                jsonObjectPush.put("time", strDateNow);
                                jsonObjectPush.put("user_type", "dependent");
                                jsonObjectPush.put("user_id", act.getStrDependentID());

                                //todo add for customer
                                jsonObjectPush.put("created_by_type", "provider");
                                jsonObjectPush.put(App42GCMService.ExtraMessage, strPushMessage);
                                jsonObjectPush.put("alert", strPushMessage);

                                sendPushToDependent(jsonObjectPush);
                                insertNotification(jsonObjectPush);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                            ///
                            goToActivityList(getString(R.string.image_upload));
                        }

                        @Override
                        public void onException(Exception e) {
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                            utils.toast(2, 2, getString(R.string.warning_internet));
                        }
                    });
        } else {
            utils.toast(2, 2, getString(R.string.warning_internet));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        permissionHelper.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == Activity.RESULT_OK) {
            try {

                loadingPanel.setVisibility(View.VISIBLE);
                switch (requestCode) {
                    case Config.START_CAMERA_REQUEST_CODE:

                        backgroundThreadHandler = new BackgroundThreadHandler();
                        strImageName = Utils.customerImageUri.getPath();
                        Thread backgroundThreadCamera = new BackgroundThreadCamera();
                        backgroundThreadCamera.start();
                        break;

                    case Config.START_GALLERY_REQUEST_CODE:
                        backgroundThreadHandler = new BackgroundThreadHandler();

                        imagePaths.clear();

                        String[] all_path = intent.getStringArrayExtra("all_path");

                        Collections.addAll(imagePaths, all_path);

                        Thread backgroundThread = new BackgroundThread();
                        backgroundThread.start();

                        break;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage() {

        if (mImageCount > 0) {

            bLoad = false;

            if (mImageUploadCount < imageModels.size()) {

                final ImageModel mUploadImageModel = imageModels.get(mImageUploadCount);

                if (mUploadImageModel.ismIsNew()) {

                    UploadService uploadService = new UploadService(this);

                    uploadService.uploadImageCommon(mUploadImageModel.getStrImagePath(),
                            mUploadImageModel.getStrImageName(), mUploadImageModel.getStrImageDesc(),
                            Config.providerModel.getStrEmail(), UploadFileType.IMAGE,
                            new App42CallBack() {
                                public void onSuccess(Object response) {

                                    if (response != null) {

                                        Upload upload = (Upload) response;
                                        ArrayList<Upload.File> fileList = upload.getFileList();

                                        if (fileList.size() > 0) {

                                            Upload.File file = fileList.get(0);

                                            imageModels.get(mImageUploadCount).setmIsNew(false);
                                            imageModels.get(mImageUploadCount).setStrImageUrl(file.getUrl());

                                            try {
                                                mImageUploadCount++;
                                                if (mImageUploadCount >= imageModels.size()) {
                                                    updateImages();
                                                } else {
                                                    uploadImage();
                                                }

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                        } else {
                                            mImageUploadCount++;
                                            if (mImageUploadCount >= imageModels.size()) {
                                                updateImages();
                                            } else {
                                                uploadImage();
                                            }
                                        }
                                    } else {
                                        //loadingPanel.setVisibility(View.GONE);
                                        if (progressDialog.isShowing())
                                            progressDialog.dismiss();
                                        utils.toast(2, 2, getString(R.string.warning_internet));
                                    }
                                }

                                @Override
                                public void onException(Exception e) {

                                    if (progressDialog.isShowing())
                                        progressDialog.dismiss();

                                    if (e != null) {
                                        Utils.log(e.getMessage(), " Failure ");
                                        mImageUploadCount++;
                                        if (mImageUploadCount >= imageModels.size()) {
                                            updateImages();
                                        } else {
                                            uploadImage();
                                        }
                                    } else {
                                        utils.toast(2, 2, getString(R.string.warning_internet));
                                    }
                                }
                            });
                } else {
                    mImageUploadCount++;

                    if (mImageUploadCount >= imageModels.size()) {
                        updateImages();
                    } else {
                        uploadImage();
                    }
                }
            } else {
                updateImages();
            }
        } else {
            loadingPanel.setVisibility(View.GONE);

            if (mImageChanged) {
                bLoad = false;
                updateImages();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        permissionHelper.finish();
        //IMAGE_COUNT = 0;
        mImageCount = 0;
        mImageChanged = false;
        mImageUploadCount = 0;
        bitmaps.clear();
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    private void addImages() {

        layout.removeAllViews();

        if (imageModels != null) {
            for (int i = 0; i < imageModels.size(); i++) {
                try {
                    final ImageView imageView = new ImageView(FeatureActivity.this);
                    imageView.setPadding(0, 0, 3, 0);

                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT);
                    layoutParams.setMargins(10, 10, 10, 10);

                    Utils.log(String.valueOf(imageModels.get(i).getStrImageName() + " # " +
                            imageModels.get(i).getStrImagePath()), " height 0 ");

                    Utils.log(String.valueOf(bitmaps.get(i).getHeight()), " height ");

                    imageView.setLayoutParams(layoutParams);
                    imageView.setImageBitmap(bitmaps.get(i));
                    imageView.setTag(imageModels.get(i));
                    imageView.setTag(R.id.three, i);
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);

                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            final ImageModel mImageModel = (ImageModel) v.getTag();

                            final int mPosition = (int) v.getTag(R.id.three);

                            final Dialog dialog = new Dialog(FeatureActivity.this);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                            dialog.setContentView(R.layout.image_dialog_layout);

                            TouchImageView mOriginal = (TouchImageView) dialog.findViewById(R.id.imgOriginal);
                            TextView textViewClose = (TextView) dialog.findViewById(R.id.textViewClose);
                            Button buttonDelete = (Button) dialog.findViewById(R.id.textViewTitle);

                            if (isCompleted)
                                buttonDelete.setVisibility(View.INVISIBLE);


                            textViewClose.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //mOriginal.
                                    dialog.dismiss();
                                }
                            });

                            buttonDelete.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    //
                                    final AlertDialog.Builder alertbox =
                                            new AlertDialog.Builder(FeatureActivity.this);
                                    alertbox.setTitle(getString(R.string.delete_image));
                                    alertbox.setMessage(getString(R.string.confirm_delete_image));
                                    alertbox.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface arg0, int arg1) {

                                            try {
                                                File fDelete = utils.getInternalFileImages(mImageModel.getStrImageName());

                                                if (fDelete.exists()) {
                                                    success = fDelete.delete();

                                                    if (mImageModel.ismIsNew())
                                                        mImageCount--;

                                                    mImageChanged = true;

                                                    imageModels.remove(mImageModel);

                                                    bitmaps.remove(mPosition);
                                                }
                                                if (success) {
                                                    utils.toast(2, 2, getString(R.string.file_deleted));
                                                }
                                                arg0.dismiss();
                                                dialog.dismiss();
                                                addImages();
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    alertbox.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            arg0.dismiss();
                                        }
                                    });
                                    alertbox.show();
                                    //
                                }
                            });


                            try {
                                mOriginal.setImageBitmap(bitmaps.get(mPosition));
                                //, Config.intWidth, Config.intHeight)
                            } catch (OutOfMemoryError oOm) {
                                oOm.printStackTrace();
                            }
                            dialog.setCancelable(true);

                            dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT); //Controlling width and height.
                            dialog.show();

                        }
                    });

                    layout.addView(imageView);
                } catch (Exception | OutOfMemoryError e) {
                    e.printStackTrace();
                }
            }
        }

        loadingPanel.setVisibility(View.GONE);

        if (mImageCount > 0)
            done.setVisibility(View.VISIBLE);
        else
            done.setVisibility(View.GONE);

        if (mImageChanged)
            done.setVisibility(View.VISIBLE);

    }

    private void goToActivityList(String strAlert) {

        if (progressDialog.isShowing())
            progressDialog.dismiss();

        mImageCount = 0;
        mImageChanged = false;

        done.setVisibility(View.GONE);

        utils.toast(2, 2, strAlert);

        reloadMilestones();
    }

    @Override
    protected void onResume() {

        super.onResume();

        imageModels = act.getImageModels();

        utils = new Utils(FeatureActivity.this);

        try {

            if (textViewTime != null && act.getStrActivityDate() != null) {
                textViewTime.setText(Utils.formatDate(act.getStrActivityDate()));
            }

            if (done != null) {

                if (act.getStrActivityStatus().equalsIgnoreCase("completed"))
                    done.setVisibility(View.GONE);

                done.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (utils.isConnectingToInternet()) {

                            if (mImageCount > 0) {
                                //loadingPanel.setVisibility(View.VISIBLE);
                                progressDialog.setMessage(getString(R.string.loading));
                                progressDialog.setCancelable(false);
                                progressDialog.show();
                                uploadImage();

                            } else {
                                if (mImageChanged) {
                                    updateImages();
                                } else
                                    utils.toast(2, 2, "Select a Image");
                            }
                        } else {
                            utils.toast(2, 2, getString(R.string.warning_internet));
                        }
                    }
                });
            }

            if (linearLayoutAttach != null) {

                if (act.getStrActivityStatus().equalsIgnoreCase("completed"))
                    linearLayoutAttach.setVisibility(View.GONE);

                linearLayoutAttach.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Open popup window
                        //if (p != null)
                        // if (IMAGE_COUNT < 20) {
                        //showStatusPopup(FeatureActivity.this, p);
                        Calendar calendar = Calendar.getInstance();
                        strName = String.valueOf(calendar.getTimeInMillis());
                        strImageName = strName + ".jpeg";

                        permissionHelper.verifyPermission(
                                new String[]{getString(R.string.access_storage)},
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                new PermissionCallback() {
                                    @Override
                                    public void permissionGranted() {
                                        //action to perform when permission granted
                                        utils.selectImage(strImageName, null,
                                                FeatureActivity.this, false);
                                    }

                                    @Override
                                    public void permissionRefused() {
                                        Utils.toast(1, 1,
                                                getString(R.string.access_storage_false),
                                                FeatureActivity.this);
                                    }
                                }
                        );
                    }
                });
            }

            if (!bViewLoaded) {

                bViewLoaded = true;

                reloadMilestones();

                loadingPanel.setVisibility(View.VISIBLE);

                if (Utils.isConnectingToInternet(FeatureActivity.this)) {

                    backgroundThreadHandlerLoad = new BackgroundThreadHandlerLoad();
                    Thread backgroundThreadImagesLoad = new BackgroundThreadImagesLoad();
                    backgroundThreadImagesLoad.start();
                } else {
                    backgroundThreadHandler = new BackgroundThreadHandler();
                    Thread backgroundThreadImages = new BackgroundThreadImages();
                    backgroundThreadImages.start();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reloadMilestones() {

        final ActivityModel activityModel = act;

        try {

            final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.milestoneLayout);

            if (linearLayout != null) {
                linearLayout.removeAllViews();
            }

            Drawable drawable = null;

            for (final MilestoneModel milestoneModel : activityModel.getMilestoneModels()) {

                TextView textViewName = new TextView(FeatureActivity.this);
                textViewName.setTextAppearance(this, R.style.MilestoneStyle);
                textViewName.setText(milestoneModel.getStrMilestoneName());
                textViewName.setTextColor(getResources().getColor(R.color.colorWhite));
                textViewName.setPadding(25, 20, 0, 20);
                textViewName.setGravity(Gravity.CENTER | Gravity.LEFT);
               /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    textViewName.setGravity(View.TEXT_ALIGNMENT_CENTER);
                }*/
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1);
                params.setMargins(5, 5, 5, 5);
                textViewName.setTag(milestoneModel);

                //todo check this logic
                if (milestoneModel.getStrMilestoneScheduledDate() == null
                        || milestoneModel.getStrMilestoneScheduledDate().equalsIgnoreCase("")) {
                    if (milestoneModel.getStrMilestoneDate() != null
                            && !milestoneModel.getStrMilestoneDate().equalsIgnoreCase("")
                            && !milestoneModel.getStrMilestoneStatus().equalsIgnoreCase("completed"))
                        milestoneModel.setStrMilestoneStatus("opened");
                }


                if (milestoneModel.getStrMilestoneScheduledDate() != null
                        && !milestoneModel.getStrMilestoneScheduledDate().equalsIgnoreCase("")) {

                    String strDate = milestoneModel.getStrMilestoneScheduledDate();

                    Calendar calendar = Calendar.getInstance();

                    Date date = null;
                    String strdateCopy;
                    Date milestoneDate = null;

                    try {
                        strdateCopy = Utils.readFormat.format(calendar.getTime());
                        date = Utils.readFormat.parse(strdateCopy);
                        milestoneDate = Utils.convertStringToDate(strDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if (date != null && milestoneDate != null) {

                        if (milestoneDate.before(date) && !milestoneModel.getStrMilestoneStatus().
                                equalsIgnoreCase("completed"))
                            milestoneModel.setStrMilestoneStatus("pending");
                        else {
                            if (milestoneDate.after(date) && !milestoneModel.
                                    getStrMilestoneStatus().equalsIgnoreCase("completed"))
                                milestoneModel.setStrMilestoneStatus("inprocess");
                        }
                    } else {
                        milestoneModel.setStrMilestoneStatus("opened");
                    }
                }

                final String strMilestoneStatus = milestoneModel.getStrMilestoneStatus();

                Drawable drawableBg = null;

                if (strMilestoneStatus.equalsIgnoreCase("completed")) {
                    drawable = getResources().getDrawable(R.mipmap.tick);
                    drawableBg = getResources().getDrawable(R.drawable.button_success);
                }

                if (strMilestoneStatus.equalsIgnoreCase("pending")) {
                    drawable = getResources().getDrawable(R.mipmap.tick_disable);
                    drawableBg = getResources().getDrawable(R.drawable.button_error);
                }

                if (strMilestoneStatus.equalsIgnoreCase("opened")
                        || milestoneModel.getStrMilestoneStatus().equalsIgnoreCase("reopened")) {
                    drawable = getResources().getDrawable(R.mipmap.star_grey);
                    drawableBg = getResources().getDrawable(R.drawable.button_orange);
                }

                if (strMilestoneStatus.equalsIgnoreCase("inactive")) {
                    drawable = getResources().getDrawable(R.mipmap.star_grey);
                    drawableBg = getResources().getDrawable(R.drawable.button_inactive);
                }

                if (strMilestoneStatus.equalsIgnoreCase("inprocess")) {
                    drawable = getResources().getDrawable(R.mipmap.star_gold);
                    drawableBg = getResources().getDrawable(R.drawable.button_orange);
                }

                if (drawable != null) {
                    textViewName.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                    textViewName.setCompoundDrawablePadding(30);
                }


                if (drawableBg != null) {
                    textViewName.setBackgroundDrawable(drawableBg);
                }

                textViewName.setLayoutParams(params);
                //sv.addView(textViewName);

                if (linearLayout != null) {
                    linearLayout.addView(textViewName);
                }

                textViewName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (mImageCount > 0 || mImageChanged) {
                            utils.toast(2, 2, getString(R.string.save_image));
                        } else {

                            if (isAccessible) {
                                final MilestoneModel milestoneModelObject = (MilestoneModel) v.getTag();

                                Bundle args = new Bundle();

                                Intent intent = new Intent(FeatureActivity.this, MilestoneActivity.class);
                                args.putSerializable("Act", activityModel);
                                args.putSerializable("Milestone", milestoneModelObject);
                                args.putInt("WHICH_SCREEN", bWhichScreen);
                                args.putString("CUSTOMER_EMAIL", strCustomerEmail);
                                args.putString("CUSTOMER_NO", strCustomerNo);
                                args.putString("DEPENDENT_NO", strDependentNo);
                                args.putString("DEPENDENT_NAME", strDependentName);
                                intent.putExtras(args);
                                startActivity(intent);
                                finish();
                            } else {
                                utils.toast(2, 2, getString(R.string.sync_data));
                            }
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertNotification(final JSONObject jsonObject) {

        if (utils.isConnectingToInternet()) {

            storageService.insertDocs(Config.collectionNotification, jsonObject,
                    new AsyncApp42ServiceApi.App42StorageServiceListener() {

                        @Override
                        public void onDocumentInserted(Storage response) {
                            try {
                                if (response.isResponseSuccess()) {
                                    //sendPush(jsonObject);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onUpdateDocSuccess(Storage response) {
                        }

                        @Override
                        public void onFindDocSuccess(Storage response) {
                        }

                        @Override
                        public void onInsertionFailed(App42Exception ex) {
                        }

                        @Override
                        public void onFindDocFailed(App42Exception ex) {
                        }

                        @Override
                        public void onUpdateDocFailed(App42Exception ex) {
                        }
                    });
        }
    }

    private void sendPushToDependent(JSONObject jsonObject) {

        if (utils.isConnectingToInternet()) {

            PushNotificationService pushNotificationService = new PushNotificationService(
                    FeatureActivity.this);

            pushNotificationService.sendPushToUser(strCustomerEmail, jsonObject.toString(),
                    new App42CallBack() {

                        @Override
                        public void onSuccess(Object o) {
                        }

                        @Override
                        public void onException(Exception ex) {
                            if (ex != null)
                                Utils.log(ex.getMessage(), " PUSH ");
                        }
                    });

        }
    }

    private class BackgroundThreadHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            addImages();
        }
    }

    private class BackgroundThreadHandlerLoad extends Handler {
        @Override
        public void handleMessage(Message msg) {
            backgroundThreadHandler = new BackgroundThreadHandler();
            Thread backgroundThreadImages = new BackgroundThreadImages();
            backgroundThreadImages.start();
        }
    }

    private class BackgroundThread extends Thread {
        @Override
        public void run() {

            try {
                for (int i = 0; i < imagePaths.size(); i++) {
                    Calendar calendar = Calendar.getInstance();
                    String strTime = String.valueOf(calendar.getTimeInMillis());
                    String strFileName = strTime + ".jpeg";

                    Date date = calendar.getTime();

                    File mCopyFile = utils.getInternalFileImages(strFileName);
                    utils.copyFile(new File(imagePaths.get(i)), mCopyFile);


                    ImageModel imageModel = new ImageModel(strFileName, "", strFileName,
                            Utils.convertDateToString(date), mCopyFile.getAbsolutePath());

                    imageModel.setmIsNew(true);

                    imageModels.add(imageModel);

                    utils.compressImageFromPath(mCopyFile.getAbsolutePath(), Config.intCompressWidth,
                            Config.intCompressHeight, Config.iQuality);


                    bitmaps.add(utils.getBitmapFromFile(mCopyFile.getAbsolutePath(),
                            Config.intWidth, Config.intHeight));

                    mImageCount++;
                }
                backgroundThreadHandler.sendEmptyMessage(0);
            } catch (OutOfMemoryError | Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class BackgroundThreadCamera extends Thread {
        @Override
        public void run() {
            try {
                if (strImageName != null && !strImageName.equalsIgnoreCase("")) {

                    Calendar calendar = Calendar.getInstance();
                    String strTime = String.valueOf(calendar.getTimeInMillis());
                    String strFileName = strTime + ".jpeg";

                    File mCopyFile = utils.getInternalFileImages(strFileName);

                    utils.copyFile(new File(strImageName), mCopyFile);

                    Date date = calendar.getTime();

                    ImageModel imageModel = new ImageModel(strFileName, "", strFileName,
                            Utils.convertDateToString(date), mCopyFile.getAbsolutePath());
                    imageModel.setmIsNew(true);

                    imageModels.add(imageModel);

                    utils.compressImageFromPath(mCopyFile.getAbsolutePath(), Config.intCompressWidth,
                            Config.intCompressHeight, Config.iQuality);

                    bitmaps.add(utils.getBitmapFromFile(mCopyFile.getAbsolutePath(), Config.intWidth,
                            Config.intHeight));

                    mImageCount++;
                }

            } catch (Exception | OutOfMemoryError e) {
                e.printStackTrace();
            }
            backgroundThreadHandler.sendEmptyMessage(0);
        }
    }

    private class BackgroundThreadImages extends Thread {
        @Override
        public void run() {
            try {

                for (ImageModel imageModel : imageModels) {
                    if (imageModel.getStrImageName() != null && !imageModel.getStrImageName().
                            equalsIgnoreCase("")) {
                        bitmaps.add(utils.getBitmapFromFile(utils.getInternalFileImages(
                                imageModel.getStrImageName()).getAbsolutePath(), Config.intWidth,
                                Config.intHeight));

                        imageModel.setmIsNew(false);
                    }
                }
            } catch (Exception | OutOfMemoryError e) {
                e.printStackTrace();
            }
            backgroundThreadHandler.sendEmptyMessage(0);
        }
    }

    private class BackgroundThreadImagesLoad extends Thread {
        @Override
        public void run() {
            try {

                for (ImageModel imageModel : imageModels) {
                    if (imageModel.getStrImageName() != null && !imageModel.getStrImageName().
                            equalsIgnoreCase("")) {
                        File file = utils.getInternalFileImages(imageModel.getStrImageName());

                        if (!file.exists() || file.length() <= 0) {
                            Utils.loadImageFromWeb(imageModel.getStrImageName(),
                                    imageModel.getStrImageUrl(), FeatureActivity.this);
                        }
                    }
                }

            } catch (Exception | OutOfMemoryError e) {
                e.printStackTrace();
            }
            backgroundThreadHandlerLoad.sendEmptyMessage(0);
        }
    }

}