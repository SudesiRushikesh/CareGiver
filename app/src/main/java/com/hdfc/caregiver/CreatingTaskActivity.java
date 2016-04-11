package com.hdfc.caregiver;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.jjobes.slidedatetimepicker.SlideDateTimeListener;
import com.github.jjobes.slidedatetimepicker.SlideDateTimePicker;
import com.hdfc.app42service.StorageService;
import com.hdfc.config.Config;
import com.hdfc.libs.AsyncApp42ServiceApi;
import com.hdfc.libs.Libs;
import com.hdfc.models.ActivityModel;
import com.hdfc.models.DependentModel;
import com.shephertz.app42.paas.sdk.android.App42CallBack;
import com.shephertz.app42.paas.sdk.android.App42Exception;
import com.shephertz.app42.paas.sdk.android.storage.Storage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Admin on 28-01-2016.
 */
public class CreatingTaskActivity extends AppCompatActivity {

    public static String valDateTime, valTitle, valSearch;
    public static DependentModel sDependentModel = new DependentModel();
    private static StorageService storageService;
    private static ArrayList<DependentModel> dependentModels = new ArrayList<>();
    private static ArrayList<ActivityModel> activityModels = new ArrayList<>();
    private static String products="";
    String _strDate;
    TextView createtaskDone;
    boolean cancel = false;
    View focusView = null;
    ImageView backImage;
    AutoCompleteTextView inputSearch;
    private ProgressDialog progressDialog;
    private JSONObject jsonObjectAct, responseJSONDoc, jsonObjectActCarla, responseJSONDocCarla, jsonObjectCarla;
    private Libs libs;
    private EditText dateTime, editTextTitle;
        private SlideDateTimeListener listener = new SlideDateTimeListener() {

            @Override
            public void onDateTimeSet(Date date) {
                // selectedDateTime = date.getDate()+"-"+date.getMonth()+"-"+date.getYear()+" "+
                // date.getTime();
                // Do something with the date. This Date object contains
                // the date and time that the user has selected.

                String strDate = Libs.writeFormat.format(date);
                _strDate = Libs.readFormat.format(date);
                dateTime.setText(strDate);
            }

            @Override
            public void onDateTimeCancel() {
                // Overriding onDateTimeCancel() is optional.
            }

    };
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creating_task);
        dateTime = (EditText)findViewById(R.id.editDateTime);
        editTextTitle = (EditText)findViewById(R.id.editTextTitle);
        inputSearch = (AutoCompleteTextView) findViewById(R.id.inputSearch);

        libs = new Libs(CreatingTaskActivity.this);
        progressDialog = new ProgressDialog(CreatingTaskActivity.this);

        backImage = (ImageView)findViewById(R.id.imgBackCreatingTaskDetail);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreatingTaskActivity.this,DashboardActivity.class);
                Config.intSelectedMenu=Config.intDashboardScreen;
                startActivity(intent);
            }
        });
        /*Bundle b = getIntent().getExtras();
        intWhichScreen = b.getInt("WHICH_SCREEN", Config.intDashboardScreen);*/

        dateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SlideDateTimePicker.Builder(getSupportFragmentManager())
                        .setListener(listener)
                        .setInitialDate(new Date())
                        .build()
                        .show();
            }
        });

        createtaskDone = (TextView)findViewById(R.id.textViewDoneHeaderCreatingTask);

        createtaskDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                valTitle = editTextTitle.getText().toString().trim();
                valDateTime = dateTime.getText().toString();
                valSearch = inputSearch.getText().toString().trim();

                if (TextUtils.isEmpty(valDateTime)) {
                    dateTime.setError(getString(R.string.error_field_required));
                    focusView = dateTime;
                    cancel = true;
                }
                if (TextUtils.isEmpty(valTitle)) {
                    editTextTitle.setError(getString(R.string.error_field_required));
                    focusView = editTextTitle;
                    cancel = true;
                }
                if (TextUtils.isEmpty(valSearch)) {
                    inputSearch.setError(getString(R.string.error_field_required));
                    focusView = inputSearch;
                    cancel = true;
                }
                if (cancel) {
                    focusView.requestFocus();
                } else {
                    //// TODO: 3/13/2016
                    uploadData();
                }
            }

        });

        parseData();
      /*  try {

            dependentModels.clear();

            if (Config.jsonObject.has("dependents")) {

                JSONArray jsonArrayNotifications = Config.jsonObject.getJSONArray("dependents");

                products = new String[jsonArrayNotifications.length()];

                for (int j = 0; j < jsonArrayNotifications.length(); j++) {

                    JSONObject jsonObjectNotification = jsonArrayNotifications.getJSONObject(j);

                    DependentModel dependentModel = new DependentModel();

                  //  dependentModel.setStrCustomerEmail(jsonObjectNotification.getString("customer_email"));
                    dependentModel.setStrDependentName(jsonObjectNotification.getString("dependent_name"));

                    products[j]=jsonObjectNotification.getString("dependent_name");

                    dependentModels.add(dependentModel);
                }

            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
      */  //inputSearch = (TextView) findViewById(R.id.inputSearch);

        //Creating the instance of ArrayAdapter containing list of language names

    }

    public void parseData() {

        StorageService storageService = new StorageService(this);

        storageService.findDocsByKeyValue(Config.collectionDependent, "dependent_name",Config.dependent_name, new AsyncApp42ServiceApi.App42StorageServiceListener() {
            @Override
            public void onDocumentInserted(Storage response) {

            }

            @Override
            public void onUpdateDocSuccess(Storage response) {

            }

            @Override
            public void onFindDocSuccess(Storage response) {
                System.out.println("Jinkalo JHIKALO REEEE");
                Libs.log(String.valueOf(response.getJsonDocList().size()), " count ");
                System.out.println("Grabbed it like hell : "+response.getJsonDocList().size());
                if (response.getJsonDocList().size() > 0) {

                    Storage.JSONDocument jsonDocument = response.getJsonDocList().get(0);

                    String strDocument = jsonDocument.getJsonDoc();

                    try {

                        Config.jsonObject = new JSONObject(strDocument);
                        System.out.println("You are here now : "+Config.jsonObject);
                        Storage storage = (Storage)response;
                        ArrayList<Storage.JSONDocument> fileSize = storage.getJsonDocList();

                        activityModels.clear();
                        ActivityModel activityModel = new ActivityModel();

                        dependentModels.clear();
                        System.out.println("aananana "+Config.jsonObject.has("services"));

                        if (Config.jsonObject.has("services")) {

                            //JSONArray jsonArrayNotifications = Config.jsonObject.getJSONArray("");
//                            products = new String();

//                            for (int j = 0; j < Config.jsonOb; j++) {

                            //  JSONObject jsonObjectNotification = jsonArrayNotifications.getJSONObject(j);

                            DependentModel dependentModel = new DependentModel();
                            System.out.println("Aniket Daf");
                            System.out.println("ABCDEFG "+Config.jsonObject);
//                                dependentModel.setStrCustomerEmail(jsonObjectNotification.getString("feedback_by"));
//                                dependentModel.setStrDependentName(jsonObjectNotification.getString("dependent_name"));

                            dependentModel.setStrDependentName(Config.jsonObject.getString("dependent_name"));
                            System.out.println("Maddy Madhavan : "+Config.jsonObject.getString("dependent_name"));
                            products=Config.jsonObject.getString("dependent_name");
                            System.out.println("Product val is "+products);
                            dependentModels.add(dependentModel);
//                            }
                            String producto[] = {"komu","kovendam","rushi","aniket"};

                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(CreatingTaskActivity.this,android.R.layout.select_dialog_item,producto);
                            //Getting the instance of AutoCompleteTextView
                            //AutoCompleteTextView actv= (AutoCompleteTextView)findViewById(R.id.inputSearch);
                            inputSearch.setThreshold(1);//will start working from first character
                            inputSearch.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView*/
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
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

    public void uploadData() {

        if (libs.isConnectingToInternet()) {

            for(DependentModel dependentModelFind: dependentModels){
                if(dependentModelFind.getStrDependentName().equalsIgnoreCase(valSearch)){
                    sDependentModel = dependentModelFind;
                }
            }

            System.out.println("Ith ith bas re mora : "+storageService);
            storageService = new StorageService(CreatingTaskActivity.this);
            storageService.findDocsByKeyValue(Config.collectionActivity, "provider_id",Config.jsonDocId, new AsyncApp42ServiceApi.App42StorageServiceListener() {

                        @Override
                        public void onDocumentInserted(Storage response) {

                        }

                        @Override
                        public void onUpdateDocSuccess(Storage response) {

                        }

                        @Override
                        public void onFindDocSuccess(Storage response) {
                            Libs.log(String.valueOf(response.getJsonDocList().size()), " count ");

                            if (response.getJsonDocList().size() > 0) {

                                Storage.JSONDocument jsonDocument = response.getJsonDocList().get(0);

                                String strDocument = jsonDocument.getJsonDoc();

                                try {

                                    Config.jsonObject = new JSONObject(strDocument);
                                    jsonObjectCarla = Config.jsonObject;

                                    //if (jsonObjectCarla != null && jsonObjectCarla.has("provider_email")) {
                                    if (jsonObjectCarla != null) {
                                        //for customer
                                /*        jsonObjectAct.put("provider_email", jsonObjectCarla.getString("provider_email"));
                                        jsonObjectAct.put("provider_image_url", jsonObjectCarla.getString("provider_profile_url"));

                                        jsonObjectAct.put("provider_contact_no", jsonObjectCarla.getString("provider_contact_no"));
                                        jsonObjectAct.put("provider_description", "description");
                                        jsonObjectAct.put("provider_name", jsonObjectCarla.getString("provider_name"));*/

                                        jsonObjectAct.put("activity_message", editTextTitle.getText().toString());
                                        jsonObjectAct.put("activity_name", editTextTitle.getText().toString());
                                        jsonObjectAct.put("dependent_name", inputSearch.getText().toString());
                                        jsonObjectAct.put("activity_date",dateTime.getText().toString());
                                        System.out.println(jsonObjectAct);

                                        jsonObjectAct.put("activity_description", editTextTitle.getText().toString());
                                        jsonObjectAct.put("service_id", "14700");


                                        jsonObjectAct.put("activity_date", _strDate);
                                        jsonObjectAct.put("activity_done_date", _strDate);


                                        JSONArray jsonArray = new JSONArray();
                                        jsonObjectAct.put("features", new String[]{"corn", "potato"});
                                        jsonObjectAct.put("feedbacks", jsonArray);
                                        jsonObjectAct.put("videos", jsonArray);
                                        jsonObjectAct.put("images", jsonArray);
                                        jsonObjectAct.put("features", jsonArray);
                                        jsonObjectAct.put("features_done", jsonArray);

                                        //for carla
                                        jsonObjectActCarla.put("customer_email", sDependentModel.getStrCustomerEmail());//
                                        jsonObjectActCarla.put("activity_message", editTextTitle.getText().toString());
                                        jsonObjectActCarla.put("activity_name", editTextTitle.getText().toString());

                                        jsonObjectActCarla.put("activity_description", editTextTitle.getText().toString());
                                        jsonObjectActCarla.put("service_id", "14700");

                                        jsonObjectActCarla.put("activity_date", _strDate);
                                        jsonObjectActCarla.put("activity_done_date", _strDate);

                                        jsonObjectActCarla.put("dependent_name", sDependentModel.getStrDependentName());
                                        jsonObjectActCarla.put("status", "upcoming");

                                        jsonObjectAct.put("feedbacks", jsonArray);
                                        jsonObjectAct.put("videos", jsonArray);
                                        jsonObjectAct.put("images", jsonArray);
                                        jsonObjectAct.put("features", new String[]{"corn", "potato"});
                                        jsonObjectAct.put("features_done", jsonArray);
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
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

            jsonObjectAct = new JSONObject();
            jsonObjectActCarla = new JSONObject();
            jsonObjectCarla = Config.jsonObject;

            progressDialog.setMessage(getResources().getString(R.string.loading));
            progressDialog.setCancelable(false);
            progressDialog.show();

            /*try {

                System.out.println("Zingat");
                System.out.println("Servicve"+jsonObjectCarla);
                System.out.println("Marathi "+jsonObjectCarla.has("provider_email"));

                if (jsonObjectCarla != null && jsonObjectCarla.has("provider_email")) {

                    //for customer
                    jsonObjectAct.put("provider_email", jsonObjectCarla.getString("provider_email"));
                    System.out.println("ZING ZING ZINGAT ");
                    System.out.println("ZING ZING ZINGAT "+jsonObjectAct);
                    jsonObjectAct.put("provider_image_url", jsonObjectCarla.getString("provider_profile_url"));

                    jsonObjectAct.put("provider_contact_no", jsonObjectCarla.getString("provider_contact_no"));
                    jsonObjectAct.put("provider_description", "description");
                    jsonObjectAct.put("provider_name", jsonObjectCarla.getString("provider_name"));

                    jsonObjectAct.put("activity_message", editTextTitle.getText().toString());
                    jsonObjectAct.put("status", "upcoming");
                    jsonObjectAct.put("activity_name", editTextTitle.getText().toString());

                    jsonObjectAct.put("activity_description", editTextTitle.getText().toString());
                    jsonObjectAct.put("service_id", "14700");


                    jsonObjectAct.put("activity_date", _strDate);
                    jsonObjectAct.put("activity_done_date", _strDate);


                    JSONArray jsonArray = new JSONArray();
                    jsonObjectAct.put("features", new String[]{"corn", "potato"});
                    jsonObjectAct.put("feedbacks", jsonArray);
                    jsonObjectAct.put("videos", jsonArray);
                    jsonObjectAct.put("images", jsonArray);
                    jsonObjectAct.put("features", jsonArray);
                    jsonObjectAct.put("features_done", jsonArray);

                    //for carla
                    jsonObjectActCarla.put("customer_email", sDependentModel.getStrCustomerEmail());//
                    jsonObjectActCarla.put("activity_message", editTextTitle.getText().toString());
                    jsonObjectActCarla.put("activity_name", editTextTitle.getText().toString());

                    jsonObjectActCarla.put("activity_description", editTextTitle.getText().toString());
                    jsonObjectActCarla.put("service_id", "14700");

                    jsonObjectActCarla.put("activity_date", _strDate);
                    jsonObjectActCarla.put("activity_done_date", _strDate);

                    jsonObjectActCarla.put("dependent_name", sDependentModel.getStrDependentName());
                    jsonObjectActCarla.put("status", "upcoming");

                    jsonObjectAct.put("feedbacks", jsonArray);
                    jsonObjectAct.put("videos", jsonArray);
                    jsonObjectAct.put("images", jsonArray);
                    jsonObjectAct.put("features", new String[]{"corn", "potato"});
                    jsonObjectAct.put("features_done", jsonArray);
                }

            } catch (JSONException e) {
                e.printStackTrace();
               // jsonObjectAct = null;
            }*/

            if (jsonObjectAct != null) {

                storageService.findDocsByIdApp42CallBack(Config.jsonDocId, Config.collectionProvider, new App42CallBack() {
                    @Override
                    public void onSuccess(Object o) {

                        if (o != null) {

                            final Storage findObj = (Storage) o;
                            System.out.println("TATA"+findObj);

                            try {
                                responseJSONDoc = new JSONObject(findObj.
                                        getJsonDocList().get(0).getJsonDoc());
                                System.out.println("Response"+responseJSONDoc);
                                if (responseJSONDoc.has("activities")) {
                                    JSONArray dependantsA = responseJSONDoc.
                                            getJSONArray("activities");

                                    dependantsA.put(jsonObjectActCarla);
                                }
                            } catch (JSONException jSe) {
                                jSe.printStackTrace();
                                progressDialog.dismiss();
                            }

                            Libs.log(responseJSONDoc.toString(), " onj 1 ");


                            if (libs.isConnectingToInternet()) {//TODO check activity added

                                storageService.updateDocs(responseJSONDoc, Config.jsonDocId, Config.collectionProvider, new App42CallBack() {
                                    @Override
                                    public void onSuccess(Object o) {

                                        //
                                        Config.jsonObject = responseJSONDoc;
                                        //
                                        if (o != null) {

                                            // Config.jsonObject = responseJSONDoc;

                                            storageService.findDocsByKeyValue(Config.collectionCustomer, "customer_email", "balamscint@gmail.com", new AsyncApp42ServiceApi.App42StorageServiceListener() {
                                                @Override
                                                public void onDocumentInserted(Storage response) {
                                                }

                                                @Override
                                                public void onUpdateDocSuccess(Storage response) {
                                                }

                                                @Override
                                                public void onFindDocSuccess(Storage response) {

                                                    if (response != null) {

                                                        if (response.getJsonDocList().size() > 0) {

                                                            Storage.JSONDocument jsonDocument = response.getJsonDocList().get(0);

                                                             final String strCarlaJsonId = response.getJsonDocList().get(0).getDocId();

                                                            String strDocument = jsonDocument.getJsonDoc();

                                                            storageService.findDocsByKeyValue(Config.collectionDependent, "dependent_email", "komagan09@gmail.com", new AsyncApp42ServiceApi.App42StorageServiceListener() {
                                                                @Override
                                                                public void onDocumentInserted(Storage response) {
                                                                }

                                                                @Override
                                                                public void onUpdateDocSuccess(Storage response) {
                                                                }

                                                                @Override
                                                                public void onFindDocSuccess(Storage response) {

                                                                    if (response != null) {

                                                                        if (response.getJsonDocList().size() > 0) {

                                                                            Storage.JSONDocument jsonDocument = response.getJsonDocList().get(0);

                                                                            final String strCarlaJsonId = response.getJsonDocList().get(0).getDocId();

                                                                            String strDocument = jsonDocument.getJsonDoc();

                                                                            try {
                                                                                responseJSONDocCarla = new JSONObject(strDocument);

                                                                                System.out.println(responseJSONDocCarla);


                                                                                   // JSONArray dependantsA = responseJSONDocCarla.getJSONArray("dependents");

                                                                                    //TODO

                                                                                    //products = new String[jsonArrayNotifications.length()];
                                                                                   // for (int i = 0; i < dependantsA.length(); i++) {

                                                                                       // JSONObject jsonObjectDependent = dependantsA.getJSONObject(i);
                                                                                        JSONObject jsonObjectDependent = responseJSONDocCarla;

                                                                                        if (inputSearch.getText().toString().equalsIgnoreCase(jsonObjectDependent.getString("dependent_name"))) {

                                                                                            JSONArray jsonArrayActivities = jsonObjectDependent.getJSONArray("activities");

                                                                                            jsonArrayActivities.put(jsonObjectAct);
                                                                                            System.out.println("bye "+jsonArrayActivities);
                                                                                        }
                                                                                   // }


                                                                                Libs.log(responseJSONDocCarla.toString(), " onj 2 ");

                                                                                storageService.updateDocs(responseJSONDocCarla, strCarlaJsonId, Config.collectionCustomer, new App42CallBack() {
                                                                                    @Override
                                                                                    public void onSuccess(Object o) {
                                                                                        System.out.println("Success");

                                                                                        if (o != null) {
                                                                                            System.out.println("Success2");
                                                                                            Intent intent = new Intent(CreatingTaskActivity.this, DashboardActivity.class);
                                                                                            if (progressDialog.isShowing())
                                                                                                progressDialog.dismiss();
                                                                                            Config.intSelectedMenu = Config.intDashboardScreen;
                                                                                            startActivity(intent);
                                                                                            finish();

                                                                                        } else {
                                                                                            System.out.println("Success3");
                                                                                            if (progressDialog.isShowing())
                                                                                                progressDialog.dismiss();
                                                                                            libs.toast(2, 2, getString(R.string.warning_internet));
                                                                                        }
                                                                                    }

                                                                                    @Override
                                                                                    public void onException(Exception e) {
                                                                                        if (progressDialog.isShowing())
                                                                                            progressDialog.dismiss();
                                                                                        if (e != null) {
                                                                                            libs.toast(2, 2, e.getMessage());
                                                                                        } else {
                                                                                            libs.toast(2, 2, getString(R.string.warning_internet));
                                                                                        }
                                                                                    }
                                                                                });

                                                                            } catch (JSONException e) {
                                                                                e.printStackTrace();
                                                                            }
                                                                        }
                                                                    }
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

                                                    } else {
                                                        if (progressDialog.isShowing())
                                                            progressDialog.dismiss();
                                                        libs.toast(2, 2, getString(R.string.warning_internet));
                                                    }
                                                }

                                                @Override
                                                public void onInsertionFailed(App42Exception ex) {

                                                }

                                                @Override
                                                public void onFindDocFailed(App42Exception ex) {
                                                    if (progressDialog.isShowing())
                                                        progressDialog.dismiss();

                                                    if (ex != null) {
                                                        libs.toast(2, 2, ex.getMessage());
                                                    } else {
                                                        libs.toast(2, 2, getString(R.string.warning_internet));
                                                    }
                                                }

                                                @Override
                                                public void onUpdateDocFailed(App42Exception ex) {

                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onException(Exception e) {
                                        if (progressDialog.isShowing())
                                            progressDialog.dismiss();
                                        if (e != null) {
                                            libs.toast(2, 2, e.getMessage());
                                        } else {
                                            libs.toast(2, 2, getString(R.string.warning_internet));
                                        }
                                    }
                                });


                            } else {
                                if (progressDialog.isShowing())
                                    progressDialog.dismiss();
                                libs.toast(2, 2, getString(R.string.warning_internet));
                            }

                        } else {
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                            libs.toast(2, 2, getString(R.string.warning_internet));
                        }
                    }

                    @Override
                    public void onException(Exception e) {
                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
                        if (e != null) {
                            libs.toast(2, 2, e.getMessage());
                        } else {
                            libs.toast(2, 2, getString(R.string.warning_internet));
                        }
                    }
                });


            } else libs.toast(2, 2, getString(R.string.error));

        } else {
            libs.toast(2, 2, getString(R.string.warning_internet));
        }
    }

}