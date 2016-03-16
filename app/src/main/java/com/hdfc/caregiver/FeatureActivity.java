package com.hdfc.caregiver;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.hdfc.adapters.FeatureAdapter;
import com.hdfc.app42service.UploadService;
import com.hdfc.config.Config;
import com.hdfc.libs.Libs;
import com.hdfc.models.ActivityModel;
import com.hdfc.models.ClientModel;
import com.hdfc.models.FeatureModel;
import com.shephertz.app42.paas.sdk.android.App42CallBack;
import com.shephertz.app42.paas.sdk.android.App42Exception;
import com.shephertz.app42.paas.sdk.android.upload.Upload;
import com.shephertz.app42.paas.sdk.android.upload.UploadFileType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class FeatureActivity extends AppCompatActivity implements Serializable{

    ImageView attach,back,imageAttach,imageCamera;
    static ImageView imageView;
    static Bitmap bitmap = null;
    private static int intWhichScreen;
    public static Uri uri;
    String serializedObject = "";

    public static ActivityModel activityModel = new ActivityModel();
    static String veg;
    public static List<String> vegetable;
    private Libs libs;
    public static String strImageName = "", strImagePathToServer = "";
    /*public static String strCustomerImgName = "";*/
    private String strCustomerImagePath="";

    public JSONObject json;
    public static JSONArray jsonArray;
    static FeatureAdapter featureAdapter;
    List<String> lstFeatures;
    String chk;
    ArrayList<String> selchkboxlist=new ArrayList<String>();

    Button done;
    Serializable ac;
    public static String strCustomerImgNameCamera;
    ListView list_vegetable;
    private static Thread backgroundThread, backgroundThreadCamera;
    private static Handler backgroundThreadHandler;
    private ProgressDialog progressDialog;
    List<FeatureModel> featureModelList = new ArrayList<>();
    ArrayList<ActivityModel> activityModels = new ArrayList<>();
    protected static final int RESULT_GALLERY_IMAGE = 2;
    private static boolean isImageChanged=false;
    Point p;
    private static ProgressDialog mProgress = null;
    private TextView textViewEmpty,txtDependentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vegetable__list);
        vegetable = new ArrayList<>();
        attach = (ImageView) findViewById(R.id.imgAttachHeaderTaskDetail);
        done = (Button) findViewById(R.id.buttonVegetibleDone);
        back = (ImageView) findViewById(R.id.imgBackHeaderTaskDetail);
        list_vegetable = (ListView) findViewById(R.id.list_view);
        textViewEmpty = (TextView) findViewById(android.R.id.empty);
       // featureAdapter = new FeatureAdapter(this, vegetable);

        ViewHolder viewHolder = new ViewHolder();
         viewHolder.dependentName= (TextView)findViewById(R.id.textViewHeaderTaskDetail);

        try {

            Bundle b = getIntent().getExtras();
        /*intWhichScreen = b.getInt("WHICH_SCREEN", Config.intSimpleActivityScreen);*/

            ActivityModel act = (ActivityModel) b.getSerializable("ACTIVITY");

            lstFeatures = new ArrayList<String>(Arrays.asList(act.getFeatures()));

            featureAdapter = new FeatureAdapter(this, lstFeatures);

            Libs.log(act.getStrActivityDate(), " Date ");
            Libs.log(act.getFeatures().toString(),"Features");
        }catch (Exception e){
            e.printStackTrace();
        }

       // ac = getIntent().getSerializableExtra("ACTIVITY");

       /* String val = act.toString();
        System.out.println("INDIA : "+val);*/
/*
        try {
            byte b[] = abc.getBytes();
            ByteArrayInputStream bi = new ByteArrayInputStream(b);
            ObjectInputStream si = new ObjectInputStream(bi);
            activityModel = (ActivityModel) si.readObject();
            System.out.println("ABC : "+activityModel.getStrActivityMessage());
        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println("AAAA : "+activityModel.getStrCustomerEmail());
        System.out.println("ANOTHER : "+activityModel.getJsonArrayFeatures());
*/
        /*Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();

        ActivityModel activityModel = (ActivityModel) bundle.getSerializable("RUSHI");
        System.out.println("ACTIIII :"+activityModel);*/


        libs = new Libs(FeatureActivity.this);
        CheckBox checkBox = new CheckBox(this);

        strCustomerImagePath=getFilesDir()+"/images/"+"feature_image";
        mProgress = new ProgressDialog(FeatureActivity.this);
        progressDialog = new ProgressDialog(FeatureActivity.this);
        /*FeatureModel vegetableModel = new FeatureModel();
        vegetableModel.setVegetable(checkBox);*/

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox[] cbs;
                String arr;
                cbs = new CheckBox[20];

                for(int k=0; k<lstFeatures.size(); k++)
                {
                    arr = lstFeatures.get(k);
                    cbs[k] = (CheckBox)findViewById(R.id.checkbox22);
                    cbs[k].setOnClickListener( new View.OnClickListener()
                    {
                        public void onClick(View v)
                        {
                            CheckBox cb = (CheckBox) v ;
                            if (((CheckBox) v).isChecked()) {
                                chk = Integer.toString(v.getId());
                                selchkboxlist.add(chk);
                                Toast.makeText(FeatureActivity.this, "Selected CheckBox ID" + v.getId(), Toast.LENGTH_SHORT).show();
                                System.out.println("Katraj Bypass : "+selchkboxlist);
                            } else {
                                selchkboxlist.remove(chk);
                                Toast.makeText(FeatureActivity.this, "Not selected", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
                backgroundThreadHandler = new BackgroundThreadHandler();
            }
        });
        try {
            if (Config.jsonObject.has("activities")) {
                JSONArray jsonArrayServices = Config.jsonObject.getJSONArray("activities");
                for (int i=0;i<jsonArrayServices.length();i++){
                    json = jsonArrayServices.getJSONObject(i);
                }
                     jsonArray = json.getJSONArray("features");

                for(int j = 0 ; j < jsonArray.length(); j++) {
                    //checkBox.setTag(jsonArray.getString(j));

                    /*vegetableModel.setVegetable(jsonArray.getString(j));*/
                    veg = jsonArray.getString(j);
                    vegetable.add(veg);

                }

                for(int k = 0 ; k < json.length() ; k++){
                    System.out.println("VALUES OF DEPENDENT_NAME ARE : "+json.getString("dependent_name"));
                    FeatureModel featureModel = new FeatureModel();
                    featureModel.setDependentName(json.getString("dependent_name"));
                    viewHolder.dependentName.setText(featureModel.getDependentName());
                    featureModelList.add(featureModel);
                }
                System.out.println(jsonArray);
                featureAdapter.notifyDataSetChanged();

               /*
                for(int k =0; k < jsonArrayServices.length(); k++) {
                    JSONObject jsonObjectServices = jsonArrayServices.getJSONObject(k);
                    JSONArray jsonArrayServiceFeatures = jsonObjectServices.getJSONArray("service_features");
                    checkBox.setText(jsonArrayServiceFeatures.getString(k));
                }
                */
            }
        } catch(JSONException e){
            e.printStackTrace();
        }

        // CheckBox checkBox1 = new CheckBox(this);
        /*checkBox.setTag("Baby corn");
        FeatureModel vegetableModel1 = new FeatureModel();
        vegetableModel1.setVegetable(checkBox);

        //CheckBox checkBox2 = new CheckBox(this);
        checkBox.setTag("Beetroot");
        FeatureModel vegetableModel2 = new FeatureModel();
        vegetableModel2.setVegetable(checkBox);
        checkBox.setTag("Cauliflower");
        FeatureModel vegetableModel3 = new FeatureModel();
        vegetableModel3.setVegetable(checkBox);*/
        list_vegetable.setAdapter(featureAdapter);
        list_vegetable.setEmptyView(textViewEmpty);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FeatureActivity.this,DashboardActivity.class);
                //intent.putExtra("WHICH_SCREEN", intWhichScreen);
                Config.intSelectedMenu=Config.intDashboardScreen;
                startActivity(intent);
            }
        });


        //CheckBox checkBox3 = new CheckBox(this);
       /* done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FeatureActivity.this, TaskAttachFooter.class);
                startActivity(intent);
            }
        });*/


        attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Open popup window
                if (p != null)
                    showStatusPopup(FeatureActivity.this, p);
            }
        });


    }

        // The method that displays the popup.
    private void showStatusPopup(final Activity context, Point p) {

        // Inflate the popup_layout.xml
        LinearLayout viewGroup = (LinearLayout) context.findViewById(R.id.llStatusChangePopup);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.header_task_detail_attach, null);

        // Creating the PopupWindow
        PopupWindow changeStatusPopUp = new PopupWindow(context);
        changeStatusPopUp.setContentView(layout);
        changeStatusPopUp.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        changeStatusPopUp.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        changeStatusPopUp.setFocusable(true);

        // Some offset to align the popup a bit to the left, and a bit down, relative to button's position.
        int OFFSET_X = -20;
        int OFFSET_Y = 95;

        //Clear the default translucent background
        changeStatusPopUp.setBackgroundDrawable(new BitmapDrawable());

        // Displaying the popup at the specified location, + offsets.
        changeStatusPopUp.showAtLocation(layout, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y + OFFSET_Y);
        imageAttach = (ImageView)layout.findViewById(R.id.imageView2);
        imageCamera = (ImageView)layout.findViewById(R.id.imageView);
        imageAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 int hasReadExternalStoragePermission = ContextCompat.checkSelfPermission(FeatureActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
                int hasWriteExternalStoragePermission = ContextCompat.checkSelfPermission(FeatureActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (hasReadExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(FeatureActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
                    return;
                }
                if (hasWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(FeatureActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 124);
                    return;
                }
               /* Intent i = new Intent(Action.ACTION_MULTIPLE_PICK);
                startActivityForResult(i, RESULT_GALLERY_IMAGE);*/
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"),2);

            }
        });
        strCustomerImgNameCamera = String.valueOf(new Date().getDate() + "" + new Date().getTime()) + ".jpeg";
        imageCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                libs.selectImage(strCustomerImgNameCamera, null, FeatureActivity.this);
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_CAMERA_BUTTON);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"),1);
            }
        });
    }


    public class BackgroundThread extends Thread {
        @Override
        public void run() {

            try {
                System.out.println("URI IS : "+ uri);
                if (uri != null) {
                    Calendar calendar = new GregorianCalendar();
                    String strFileName = String.valueOf(calendar.getTimeInMillis()) + ".jpeg";
                    File galleryFile = libs.createFileInternalImage(strFileName);
                    strImageName = galleryFile.getAbsolutePath();
                    System.out.println("YOUR GALLERY PATH IS : "+ strImageName);
                    InputStream is = getContentResolver().openInputStream(uri);
                    libs.copyInputStreamToFile(is, galleryFile);

                    bitmap = libs.getBitmapFromFile(strImageName, Config.intWidth, Config.intHeight);
                }
                backgroundThreadHandler.sendEmptyMessage(0);
            } catch (IOException e) {
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class BackgroundThreadCamera extends Thread {
        @Override
        public void run() {
            try {
                if (strImageName != null && !strImageName.equalsIgnoreCase("")) {
                    bitmap = libs.getBitmapFromFile(strImageName, Config.intWidth, Config.intHeight);
                }
                backgroundThreadHandler.sendEmptyMessage(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
//https://github.com/balamurugan-adstringo/CareTaker.git
    public class BackgroundThreadHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            mProgress.dismiss();
             imageView = new ImageView(FeatureActivity.this);


            LinearLayout layout = (LinearLayout) findViewById(R.id.linear);

            if ( imageView!= null && strImageName != null && !strImageName.equalsIgnoreCase("") && bitmap != null)
                try {
                    imageView.setImageBitmap(bitmap);
                } catch (Exception e){
                    e.printStackTrace();
                }
            for (int i = 0; i < 1; i++) {
                imageView.setId(i);
                imageView.setPadding(0, 0, 10, 0);
                imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
                imageView.setImageBitmap(bitmap);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                layout.addView(imageView);
            }
            /*try {
                checkImage();
            }catch (Exception e){
                e.printStackTrace();
            }*/
        }
    }

    static class ViewHolder {
        TextView dependentName;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == Activity.RESULT_OK) { //&& data != null
            try {
                //Libs.toast(1, 1, "Getting Image...");

                mProgress.setMessage(getString(R.string.loading));
                mProgress.show();
                switch (requestCode) {
                    case Config.START_CAMERA_REQUEST_CODE:

                        backgroundThreadHandler = new BackgroundThreadHandler();
                        mProgress.setMessage(getString(R.string.loading));
                        mProgress.show();
                        strImageName = Libs.customerImageUri.getPath();
                        backgroundThreadCamera = new BackgroundThreadCamera();
                        backgroundThreadCamera.start();
                        break;

                    case Config.START_GALLERY_REQUEST_CODE:
                        backgroundThreadHandler = new BackgroundThreadHandler();
                        mProgress.setMessage(getString(R.string.loading));
                        mProgress.show();
                       // strCustomerImgName = Libs.customerImageUri.getPath();
                       // if (intent.getData() != null) {
                            uri = intent.getData();
                            backgroundThread = new BackgroundThread();
                            backgroundThread.start();
                      //  }
                        break;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
   /* public  void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                mProgress.setMessage("Loading..");
                mProgress.show();
                if (data != null) {
                    uri = data.getData();
                    System.out.println("YOUR URI IS THIS : "+ uri);
                    backgroundThreadHandler = new BackgroundThreadHandler();
                    backgroundThread = new BackgroundThread();
                    backgroundThread.start();
                    //   bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());

                    switch (requestCode) {
                        case Config.START_CAMERA_REQUEST_CODE:
                            strCustomerImgName = Libs.customerImageUri.getPath();
                            backgroundThreadCamera = new BackgroundThreadCamera();
                            backgroundThreadCamera.start();
                            break;

                        case Config.START_GALLERY_REQUEST_CODE:
                            if (data.getData() != null) {
                                uri = data.getData();
                                Thread backgroundThread = new BackgroundThread();
                                backgroundThread.start();
                            }
                            break;
                    }

                } else if (resultCode ==RESULT_CANCELED) {
                    Toast.makeText(FeatureActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }*/
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        int[] location = new int[2];
        ImageView attach = (ImageView) findViewById(R.id.imgAttachHeaderTaskDetail);

        // Get the x, y location and store it in the location[] array
        // location[0] = x, location[1] = y.
        attach.getLocationOnScreen(location);


        //Initialize the Point with x, and y positions
        p = new Point();
        p.x = location[0];
        p.y = location[1];


    }

    /*public void checkImage() {

        try {

            if (libs.isConnectingToInternet()) {

                progressDialog.setMessage(getResources().getString(R.string.uploading_image));
                progressDialog.setCancelable(false);
                progressDialog.show();

                UploadService uploadService = new UploadService(this);

                if (progressDialog.isShowing())
                    progressDialog.setProgress(1);

                uploadService.removeImage("feature_image", Config.myProfileModel.getEmail(),
                        new App42CallBack() {
                            public void onSuccess(Object response) {

                                if(response!=null){
                                    uploadImage();
                                }else{
                                    if (progressDialog.isShowing())
                                        progressDialog.dismiss();
                                    libs.toast(2, 2, getString(R.string.warning_internet));
                                }
                            }
                            @Override
                            public void onException(Exception e) {

                                if(e!=null) {

                                    App42Exception exception = (App42Exception) e;
                                    int appErrorCode = exception.getAppErrorCode();

                                    if (appErrorCode != 1401 ) {
                                        uploadImage();
                                    } else {
                                        libs.toast(2, 2, getString(R.string.error));
                                    }

                                }else{
                                    if (progressDialog.isShowing())
                                        progressDialog.dismiss();
                                    libs.toast(2, 2, getString(R.string.warning_internet));
                                }
                            }
                        });

            } else {
                libs.toast(2, 2, getString(R.string.warning_internet));
            }
        }catch (Exception e){
            e.printStackTrace();
            if (progressDialog.isShowing())
                progressDialog.dismiss();
            libs.toast(2, 2, getString(R.string.error));
        }
    }

    public void uploadImage(){

        try {

            if (libs.isConnectingToInternet()) {

                UploadService uploadService = new UploadService(this);

                Calendar c = Calendar.getInstance();
                int seconds = c.get(Calendar.SECOND);
                String sec = String.valueOf(seconds);

                long time= System.currentTimeMillis();
                String tm = String.valueOf(time);

                uploadService.uploadImageCommon(strImageName,"feature_image" , "Profile Picture", Config.myProfileModel.getEmail(),
                        UploadFileType.IMAGE, new App42CallBack() {
                            public void onSuccess(Object response) {

                                if(response!=null) {
                                    // Libs.log(response.toString(), "response");
                                    Upload upload = (Upload) response;
                                    ArrayList<Upload.File> fileList = upload.getFileList();

                                    if (fileList.size() > 0) {

                                        if (bitmap != null) {
                                            //imageView.setImageBitmap(bitmap);
                                            if (progressDialog.isShowing())
                                                progressDialog.dismiss();
                                            libs.toast(2, 2, getString(R.string.update_profile_image));
                                           // isImageChanged = false;

                                            try {

                                                File f = libs.getInternalFileImages("feature_image");

                                                if (f.exists())
                                                    f.delete();

                                                File newFile = new File(strImageName);
                                                File renameFile = new File(strCustomerImagePath);

                                                libs.moveFile(newFile, renameFile);
                                                //TODO Check Logic
                                            }catch (Exception e){
                                                e.printStackTrace();
                                            }

                                        }
                                        else {
                                            if (progressDialog.isShowing())
                                                progressDialog.dismiss();
                                            libs.toast(2, 2, getString(R.string.error));
                                        }

                                    } else {
                                        if (progressDialog.isShowing())
                                            progressDialog.dismiss();
                                        libs.toast(2, 2, ((Upload) response).getStrResponse());
                                    }
                                }else{
                                    if (progressDialog.isShowing())
                                        progressDialog.dismiss();
                                    libs.toast(2, 2, getString(R.string.warning_internet));
                                }
                            }

                            @Override
                            public void onException(Exception e) {

                                if (progressDialog.isShowing())
                                    progressDialog.dismiss();

                                if(e!=null) {
                                    Libs.log(e.toString(), "response");
                                    libs.toast(2, 2, e.getMessage());
                                }else{
                                    libs.toast(2, 2, getString(R.string.warning_internet));
                                }
                            }
                        });

            } else {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
                libs.toast(2, 2, getString(R.string.warning_internet));
            }
        }catch (Exception e){
            e.printStackTrace();
            if (progressDialog.isShowing())
                progressDialog.dismiss();
            libs.toast(2, 2, getString(R.string.error));
        }
    }*/

}
