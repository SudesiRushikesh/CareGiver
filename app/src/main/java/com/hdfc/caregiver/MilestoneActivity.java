package com.hdfc.caregiver;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.jjobes.slidedatetimepicker.SlideDateTimeListener;
import com.github.jjobes.slidedatetimepicker.SlideDateTimePicker;
import com.hdfc.app42service.App42GCMService;
import com.hdfc.app42service.PushNotificationService;
import com.hdfc.app42service.StorageService;
import com.hdfc.app42service.UploadService;
import com.hdfc.config.Config;
import com.hdfc.libs.AsyncApp42ServiceApi;
import com.hdfc.libs.Utils;
import com.hdfc.models.ActivityModel;
import com.hdfc.models.FieldModel;
import com.hdfc.models.FileModel;
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
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class MilestoneActivity extends AppCompatActivity {

    //public static Dialog dialog;
    private static int IMAGE_COUNT = 0;
    private static ArrayList<FileModel> arrayListFileModel = new ArrayList<>();
    //private static ArrayList<FileModel> fileModels = new ArrayList<>();
    private static ArrayList<String> imagePaths = new ArrayList<>();
    private static ArrayList<Bitmap> bitmaps = new ArrayList<>();
    private static RelativeLayout loadingPanel;
    /* private static boolean bLoad;
     private static boolean bViewLoaded;*/
    private static String strActivityStatus = "inprocess";
    private static String strActivityDoneDate = "";
    private static String strAlert;
    private static JSONObject jsonObject;
    private static ActivityModel act;
    //private static String strName;
    private static String strDependentMail;
    private static String strName1;
    private static String strImageName1 = "";
    private static StorageService storageService;
    private static Handler backgroundThreadHandler;
    private static LinearLayout layout;
    private static int iActivityPosition;
    private static boolean bEnabled;
    private final Context context = this;
    private Utils utils;
    private MilestoneModel milestoneModelObject;
    private JSONArray jsonArrayImagesAdded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_milestone);

        loadingPanel = (RelativeLayout) findViewById(R.id.loadingPanel);


        Bundle b = getIntent().getExtras();

        act = (ActivityModel) b.getSerializable("Act");

        bitmaps.clear();



        milestoneModelObject = (MilestoneModel) b.getSerializable("Milestone");

        utils = new Utils(MilestoneActivity.this);
        storageService = new StorageService(MilestoneActivity.this);
        jsonArrayImagesAdded = new JSONArray();

        bEnabled = !milestoneModelObject.getStrMilestoneStatus().equalsIgnoreCase("completed");

        //int i = 0;

        // View view = getLayoutInflater().inflate(R.layout.dialog_view, null, false);

        layout = (LinearLayout) findViewById(R.id.dialogLinear);

        final LinearLayout layoutDialog = (LinearLayout) findViewById(R.id.linearLayoutDialog);


        Button button = (Button) findViewById(R.id.dialogButtonOK);
        Button buttonCancel = (Button) findViewById(R.id.buttonCancel);
        Button buttonDone = (Button) findViewById(R.id.buttonDone);
        Button buttonUpload = (Button) findViewById(R.id.dialogButtonUpload);
        Button buttonAttach = (Button) findViewById(R.id.dialogButtonAttach);

        int iPosition = Config.strActivityIds.indexOf(act.getStrActivityID());

        //Config.activityModels.get(iPosition).clearMilestoneModel();


        if (iPosition > -1) {
            iActivityPosition = iPosition;
        }


        /*if (buttonUpload != null) {
            buttonUpload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IMAGE_COUNT = 0;

                    if (arrayListFileModel.size() > 0) {

                        if (utils.isConnectingToInternet()) {
                            loadingPanel.setVisibility(View.VISIBLE);
                            uploadImage();
                        } else {
                            utils.toast(2, 2, getString(R.string.warning_internet));
                        }
                    } else utils.toast(2, 2, "Select a Image");
                }
            });
        }*/


        if (buttonAttach != null) {
            buttonAttach.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    strName1 = String.valueOf(new Date().getDate() + "" + new Date().getTime());
                    strImageName1 = strName1 + ".jpeg";

                    utils.selectImage(strImageName1, null, MilestoneActivity.this, false);


                }
            });
        }

        if (button != null) {
            button.setTag(milestoneModelObject.getiMilestoneId());
        }
        if (buttonDone != null) {
            buttonDone.setTag(milestoneModelObject.getiMilestoneId());
        }

        if (!milestoneModelObject.getStrMilestoneDate().equalsIgnoreCase("")) {
            if (button != null) {
                button.setText(getString(R.string.update));
            }
            if (buttonDone != null) {
                buttonDone.setVisibility(View.VISIBLE);
            }
        }

        if (milestoneModelObject.getiMilestoneId() == act.getMilestoneModels().size()) {
            if (button != null) {
                button.setVisibility(View.GONE);
            }
            if (buttonDone != null) {
                buttonDone.setVisibility(View.VISIBLE);
            }
        }


        if (!bEnabled) {
            if (button != null) {
                button.setVisibility(View.GONE);
            }
            if (buttonDone != null) {
                buttonDone.setVisibility(View.GONE);
            }
            if (buttonAttach != null) {
                buttonAttach.setVisibility(View.GONE);
            }
        }

        TextView milestoneName = (TextView) findViewById(R.id.milestoneName);
        if (milestoneName != null) {
            milestoneName.setText(milestoneModelObject.getStrMilestoneName());
        }


        if (!milestoneModelObject.getStrMilestoneDate().equalsIgnoreCase("")
                && milestoneModelObject.getStrMilestoneScheduledDate() != null
                && !milestoneModelObject.getStrMilestoneScheduledDate().equalsIgnoreCase("")) {
            if (button != null) {
                button.setText(getString(R.string.reschedule));
            }
        }

        for (FieldModel fieldModel : milestoneModelObject.getFieldModels()) {

            final FieldModel finalFieldModel = fieldModel;

            //i++;

            LinearLayout linearLayout1 = new LinearLayout(MilestoneActivity.this);
            linearLayout1.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(10, 10, 10, 10);
            linearLayout1.setLayoutParams(layoutParams);

            TextView textView = new TextView(MilestoneActivity.this);
            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 3));
            textView.setText(fieldModel.getStrFieldLabel());

            linearLayout1.addView(textView);

            if (fieldModel.getStrFieldType().equalsIgnoreCase("text")
                    || fieldModel.getStrFieldType().equalsIgnoreCase("number")
                    || fieldModel.getStrFieldType().equalsIgnoreCase("datetime")
                    || fieldModel.getStrFieldType().equalsIgnoreCase("date")
                    || fieldModel.getStrFieldType().equalsIgnoreCase("time")) {

                final EditText editText = new EditText(MilestoneActivity.this);

                editText.setId(fieldModel.getiFieldID());
                editText.setTag(R.id.one, fieldModel.isFieldRequired());
                editText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 2));
                editText.setText(fieldModel.getStrFieldData());

                if (fieldModel.isFieldView())
                    editText.setEnabled(false);

                if (fieldModel.getStrFieldType().equalsIgnoreCase("text"))
                    editText.setInputType(InputType.TYPE_CLASS_TEXT);

                if (fieldModel.getStrFieldType().equalsIgnoreCase("number"))
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);

                try {
                    if (fieldModel.getStrFieldType().equalsIgnoreCase("datetime")
                            || fieldModel.getStrFieldType().equalsIgnoreCase("date")
                            || fieldModel.getStrFieldType().equalsIgnoreCase("time")) {

                        editText.setCompoundDrawables(getResources().getDrawable(R.drawable.calender_date_picked),
                                null, getResources().getDrawable(R.drawable.calender_date_picked), null);

                        //editText.setInputType(InputType.TYPE_CLASS_DATETIME);

                        final SlideDateTimeListener listener = new SlideDateTimeListener() {

                            @Override
                            public void onDateTimeSet(Date date) {
                                // selectedDateTime = date.getDate()+"-"+date.getMonth()+"-"+date.getYear()+" "+
                                // date.getTime();
                                // Do something with the date. This Date object contains
                                // the date and time that the user has selected.

                                String strDate = "";

                                if (finalFieldModel.getStrFieldType().equalsIgnoreCase("datetime"))
                                    strDate = Utils.writeFormat.format(date);

                                if (finalFieldModel.getStrFieldType().equalsIgnoreCase("time"))
                                    strDate = Utils.writeFormatTime.format(date);

                                if (finalFieldModel.getStrFieldType().equalsIgnoreCase("date"))
                                    strDate = Utils.writeFormatDate.format(date);

                                editText.setTag(R.id.two, Utils.readFormat.format(date));
                                editText.setText(strDate);
                            }

                            @Override
                            public void onDateTimeCancel() {
                                // Overriding onDateTimeCancel() is optional.
                            }

                        };

                        editText.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new SlideDateTimePicker.Builder(getSupportFragmentManager())
                                        .setListener(listener)
                                        .setInitialDate(new Date())
                                        .build()
                                        .show();
                            }
                        });
                    }

                    editText.setEnabled(bEnabled);

                    linearLayout1.addView(editText);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (fieldModel.getStrFieldType().equalsIgnoreCase("radio")
                    || fieldModel.getStrFieldType().equalsIgnoreCase("dropdown")) {

                final Spinner spinner = new Spinner(MilestoneActivity.this);

                spinner.setId(fieldModel.getiFieldID());
                spinner.setTag(fieldModel.isFieldRequired());
                spinner.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 2));
                //spinner.setText(fieldModel.getStrFieldData());

                ArrayAdapter<String> adapter = new ArrayAdapter<>(MilestoneActivity.this, android.R.layout.select_dialog_item, fieldModel.getStrFieldValues());

                spinner.setAdapter(adapter);

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        try {

                            if (finalFieldModel.isChild()) {

                                for (int i = 0; i < finalFieldModel.getiChildfieldID().length; i++) {

                                    if (finalFieldModel.getStrChildType()[i].equalsIgnoreCase("text")) {

                                        EditText editTextChild = (EditText) layoutDialog.findViewById(finalFieldModel.getiChildfieldID()[i]);

                                        if (editTextChild != null) {

                                            String strValue = spinner.getSelectedItem().toString();

                                            if (finalFieldModel.getStrChildCondition()[i].equalsIgnoreCase("equals")) {

                                                if (strValue.equalsIgnoreCase(finalFieldModel.getStrChildValue()[i])) {
                                                    //editText.setVisibility(View.VISIBLE);
                                                    editTextChild.setEnabled(true);
                                                    break;
                                                } else {
                                                    editTextChild.setEnabled(false);
                                                }
                                            } else
                                                editTextChild.setEnabled(false);
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

                if (fieldModel.getStrFieldData() != null && !fieldModel.getStrFieldData().equalsIgnoreCase("")) {

                    int iSelected = adapter.getPosition(fieldModel.getStrFieldData());
                    spinner.setSelection(iSelected);
                }

                spinner.setEnabled(bEnabled);

                linearLayout1.addView(spinner);
            }

            if (fieldModel.getStrFieldType().equalsIgnoreCase("array")) {

                final LinearLayout linearLayoutParent = new LinearLayout(MilestoneActivity.this);
                linearLayoutParent.setOrientation(LinearLayout.VERTICAL);
                linearLayoutParent.setTag(R.id.linearparent);

                LinearLayout.LayoutParams layoutParentParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParentParams.setMargins(10, 10, 10, 10);
                linearLayoutParent.setLayoutParams(layoutParentParams);

                //
                try {

                    JSONObject jsonObjectMedicines = new JSONObject(fieldModel.getStrArrayData());

                    JSONArray jsonArrayMedicines = jsonObjectMedicines.getJSONArray("array_data");

                    for (int i = 0; i < jsonArrayMedicines.length(); i++) {

                        JSONObject jsonObjectMedicine =
                                jsonArrayMedicines.getJSONObject(i);

                        final LinearLayout linearLayoutArrayExist = new LinearLayout(MilestoneActivity.this);
                        linearLayoutArrayExist.setOrientation(LinearLayout.HORIZONTAL);
                        //linearLayoutArrayExist.setId(R.id.actionBarNotification);

                        LinearLayout.LayoutParams layoutArrayExistParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        layoutArrayExistParams.setMargins(10, 10, 10, 10);
                        linearLayoutArrayExist.setLayoutParams(layoutArrayExistParams);


                        EditText editMedicineName = new EditText(MilestoneActivity.this);
                        editMedicineName.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2));
                        editMedicineName.setHint(getString(R.string.medicine_name));
                        editMedicineName.setTag("medicine_name");
                        editMedicineName.setInputType(InputType.TYPE_CLASS_TEXT);
                        editMedicineName.setText(jsonObjectMedicine.getString("medicine_name"));
                        editMedicineName.setEnabled(bEnabled);
                        linearLayoutArrayExist.addView(editMedicineName);

                        EditText editMedicineQty = new EditText(MilestoneActivity.this);
                        editMedicineQty.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                        editMedicineQty.setHint(getString(R.string.qunatity));
                        editMedicineQty.setTag("medicine_qty");
                        editMedicineQty.setInputType(InputType.TYPE_CLASS_NUMBER);
                        editMedicineQty.setText(String.valueOf(jsonObjectMedicine.getInt("medicine_qty")));
                        editMedicineQty.setEnabled(bEnabled);
                        linearLayoutArrayExist.addView(editMedicineQty);


                        //
                        Button buttonDel = new Button(MilestoneActivity.this);
                        buttonDel.setText("X");
                        buttonDel.setTextColor(Color.BLACK);
                        buttonDel.setEnabled(bEnabled);
                        buttonDel.setBackgroundDrawable(getResources().getDrawable(R.drawable.circle));
                        buttonDel.setLayoutParams(new LinearLayout.LayoutParams(64, 64, 0));
                        buttonDel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {


                                try {
                                    if (bEnabled) {
                                        LinearLayout linearLayout = (LinearLayout) v.getParent();
                                        linearLayout.removeAllViews();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        linearLayoutArrayExist.addView(buttonDel);

                        linearLayoutParent.addView(linearLayoutArrayExist);
                        ///
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                //

                final LinearLayout linearLayoutArray = new LinearLayout(MilestoneActivity.this);
                linearLayoutArray.setOrientation(LinearLayout.HORIZONTAL);

                LinearLayout.LayoutParams layoutArrayParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutArrayParams.setMargins(10, 10, 10, 10);
                linearLayoutArray.setLayoutParams(layoutArrayParams);


                for (int j = 0; j < finalFieldModel.getiArrayCount(); j++) {

                    EditText editTextArray = new EditText(MilestoneActivity.this);

                    if (j == 0) {
                        editTextArray.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2));
                        editTextArray.setHint(getString(R.string.medicine_name));
                        editTextArray.setTag("medicine_name");
                        editTextArray.setInputType(InputType.TYPE_CLASS_TEXT);
                    }

                    if (j == 1) {
                        editTextArray.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                        editTextArray.setHint(getString(R.string.qunatity));
                        editTextArray.setTag("medicine_qty");
                        editTextArray.setInputType(InputType.TYPE_CLASS_NUMBER);
                    }

                                           /* if (fieldModel.getStrFieldType().equalsIgnoreCase("text"))


                                            if (fieldModel.getStrFieldType().equalsIgnoreCase("number"))*/

                    editTextArray.setEnabled(bEnabled);

                    linearLayoutArray.addView(editTextArray);
                }

                final boolean finalBEnabled = bEnabled;

                Button buttonAdd = new Button(MilestoneActivity.this);
                buttonAdd.setBackgroundDrawable(getResources().getDrawable(R.drawable.add_icon));
                buttonAdd.setLayoutParams(new LinearLayout.LayoutParams(64, 64, 0));
                buttonAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        try {

                            if (bEnabled) {

                                final LinearLayout linearLayoutArrayInner = new LinearLayout(MilestoneActivity.this);
                                linearLayoutArrayInner.setOrientation(LinearLayout.HORIZONTAL);

                                LinearLayout.LayoutParams layoutArrayInnerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                layoutArrayInnerParams.setMargins(10, 10, 10, 10);
                                linearLayoutArrayInner.setLayoutParams(layoutArrayInnerParams);

                                for (int j = 0; j < finalFieldModel.getiArrayCount(); j++) {

                                    EditText editTextArray = new EditText(MilestoneActivity.this);

                                    if (j == 0) {
                                        editTextArray.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2));
                                        editTextArray.setHint(getString(R.string.medicine_name));
                                        editTextArray.setTag("medicine_name");
                                        editTextArray.setInputType(InputType.TYPE_CLASS_TEXT);
                                    }

                                    if (j == 1) {
                                        editTextArray.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                                        editTextArray.setHint(getString(R.string.qunatity));
                                        editTextArray.setTag("medicine_qty");
                                        editTextArray.setInputType(InputType.TYPE_CLASS_NUMBER);
                                    }

                                                        /*if (finalFieldModel.getStrFieldType().equalsIgnoreCase("text"))


                                                        if (finalFieldModel.getStrFieldType().equalsIgnoreCase("number"))*/

                                    editTextArray.setEnabled(finalBEnabled);
                                    linearLayoutArrayInner.addView(editTextArray);
                                }

                                //
                                Button buttonDel = new Button(MilestoneActivity.this);
                                buttonDel.setText("X");
                                buttonDel.setTextColor(Color.BLACK);
                                buttonDel.setEnabled(finalBEnabled);
                                buttonDel.setBackgroundDrawable(getResources().getDrawable(R.drawable.circle));
                                buttonDel.setLayoutParams(new LinearLayout.LayoutParams(64, 64, 0));
                                buttonDel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        try {
                                            LinearLayout linearLayout = (LinearLayout) v.getParent();
                                            linearLayout.removeAllViews();

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                                linearLayoutArrayInner.addView(buttonDel);
                                ///

                                linearLayoutParent.addView(linearLayoutArrayInner);
                            }
                            //
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                linearLayoutArray.addView(buttonAdd);

                linearLayoutParent.addView(linearLayoutArray);

                linearLayout1.addView(linearLayoutParent);
            }
            if (layoutDialog != null) {
                layoutDialog.addView(linearLayout1);
            }
        }

        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int id = (int) v.getTag();
                    LinearLayout linearLayout = null;
                    if (layoutDialog != null) {
                        linearLayout = (LinearLayout) layoutDialog.findViewWithTag(R.id.linearparent);
                    }
                    traverseEditTexts(layoutDialog, id, linearLayout, 1);


                }
            });
        }

        if (buttonDone != null) {
            buttonDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int id = (int) v.getTag();
                    LinearLayout linearLayout = null;
                    if (layoutDialog != null) {
                        linearLayout = (LinearLayout) layoutDialog.findViewWithTag(R.id.linearparent);
                    }
                    traverseEditTexts(layoutDialog, id, linearLayout, 2);
                }
            });
        }

        if (buttonCancel != null) {
            buttonCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   /* Bundle args = new Bundle();
                    Intent intent = new Intent(MilestoneActivity.this, FeatureActivity.class);
                    args.putSerializable("ACTIVITY", act);
                    intent.putExtras(args);
                    startActivity(intent);*/

                    Bundle args = new Bundle();
                    Intent intent = new Intent(MilestoneActivity.this, FeatureActivity.class);
                    args.putSerializable("ACTIVITY", act);
                    args.putInt("ACTIVITY_POSITION", iActivityPosition);
                    intent.putExtras(args);
                    startActivity(intent);
                    finish();
                }
            });
        }

    /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setView(view);
    dialog = builder.create();

    dialog.show();
*/
        //}

        backgroundThreadHandler = new BackgroundThreadHandler();
        Thread backgroundThreadImages = new BackgroundThreadImages();
        backgroundThreadImages.start();
    }

    private void traverseEditTexts(ViewGroup v, int iMileStoneId, ViewGroup viewGroup, int iFlag) {

        boolean b = true;

        try {

            Calendar calendar = Calendar.getInstance();

            String strScheduledDate = "";

            //int iPosition = Config.strActivityIds.indexOf(act.getStrActivityID());

            if (iActivityPosition > -1)
                Config.activityModels.get(iActivityPosition).clearMilestoneModel();

            int iIndex = 0;

            int iClose = 0;

            for (MilestoneModel milestoneModel : act.getMilestoneModels()) {

                iIndex++;

                if (milestoneModel.getiMilestoneId() == iMileStoneId) {

                    strScheduledDate = "";

                    Date date = calendar.getTime();
                    milestoneModel.setStrMilestoneDate(utils.convertDateToString(date));

                    //if(iFlag==1)
                    //milestoneModel.setStrMilestoneStatus("opened");


                    for (FieldModel fieldModel : milestoneModel.getFieldModels()) {

                        //text
                        if (fieldModel.getStrFieldType().equalsIgnoreCase("text")
                                || fieldModel.getStrFieldType().equalsIgnoreCase("number")
                                || fieldModel.getStrFieldType().equalsIgnoreCase("datetime")
                                || fieldModel.getStrFieldType().equalsIgnoreCase("date")
                                || fieldModel.getStrFieldType().equalsIgnoreCase("time")) {

                            EditText editText = (EditText) v.findViewById(fieldModel.getiFieldID());

                            boolean b1 = (Boolean) editText.getTag(R.id.one);
                            String data = editText.getText().toString().trim();

                            if (editText.isEnabled()) {
                                if (b1 && !data.equalsIgnoreCase("")) {

                                    if (fieldModel.getStrFieldType().equalsIgnoreCase("datetime")
                                            || fieldModel.getStrFieldType().equalsIgnoreCase("date")
                                            || fieldModel.getStrFieldType().equalsIgnoreCase("time")) {

                                        boolean bFuture = true;

                                        /////////////////////////////
                                        Date dateNow = null;
                                        String strdateCopy;
                                        Date enteredDate = null;

                                        try {
                                            strdateCopy = Utils.writeFormat.format(calendar.getTime());
                                            dateNow = Utils.writeFormat.parse(strdateCopy);
                                            enteredDate = Utils.writeFormat.parse(data);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }

                                        if (dateNow != null && enteredDate != null) {

                                            //Utils.log(String.valueOf(date + " ! " + enteredDate), " NOW ");

                                            if (enteredDate.before(dateNow)) {
                                                bFuture = false;
                                            }
                                        }
                                        /////////////////////////////

                                        if (bFuture) {

                                            fieldModel.setStrFieldData(data);

                                            String strDate = (String) editText.getTag(R.id.two);

                                            if ((milestoneModel.isReschedule() || !milestoneModel.isReschedule())
                                                    && milestoneModel.getStrMilestoneScheduledDate() != null
                                                    && (!milestoneModel.getStrMilestoneScheduledDate().equalsIgnoreCase("")
                                                    || milestoneModel.getStrMilestoneScheduledDate().equalsIgnoreCase(""))
                                                    && fieldModel.getStrFieldType().equalsIgnoreCase("datetime")
                                                    ) {

                                                if (milestoneModel.getStrMilestoneScheduledDate() != null
                                                        && !milestoneModel.getStrMilestoneScheduledDate().
                                                        equalsIgnoreCase(""))
                                                    milestoneModel.setReschedule(true);


                                                milestoneModel.setStrMilestoneScheduledDate(strDate);

                                                strScheduledDate = strDate;
                                            }

                                            if (iIndex == act.getMilestoneModels().size() && iFlag == 2) {
                                                strActivityDoneDate = strDate;
                                            }

                                        } else {
                                            editText.setError(context.getString(R.string.invalid_date));
                                            b = false;
                                        }
                                    } else {
                                        fieldModel.setStrFieldData(data);
                                    }

                                } else {
                                    editText.setError(context.getString(R.string.error_field_required));
                                    b = false;
                                }
                            }
                        }

                        //radio or dropdown
                        if (fieldModel.getStrFieldType().equalsIgnoreCase("radio")
                                || fieldModel.getStrFieldType().equalsIgnoreCase("dropdown")) {

                            Spinner spinner = (Spinner) v.findViewById(fieldModel.getiFieldID());

                            boolean b1 = (Boolean) spinner.getTag();

                            String data = fieldModel.getStrFieldValues()[spinner.getSelectedItemPosition()];

                            if (b1 && !data.equalsIgnoreCase("")) {
                                fieldModel.setStrFieldData(data);
                            } else {
                                utils.toast(2, 2, getString(R.string.error_field_required));
                                spinner.setBackgroundDrawable(getResources().getDrawable(R.drawable.edit_text));
                                b = false;
                            }
                        }
                        //

                        //array
                        if (fieldModel.getStrFieldType().equalsIgnoreCase("array")) {

                            ArrayList<String> strMedicineNames = utils.getEditTextValueByTag(viewGroup, "medicine_name");
                            ArrayList<String> strMedicineQty = utils.getEditTextValueByTag(viewGroup, "medicine_qty");

                            int j = 0;

                            if (strMedicineNames.size() > 0) {

                                JSONObject jsonObject = new JSONObject();

                                JSONArray jsonArray = new JSONArray();

                                for (int i = 0; i < strMedicineNames.size(); i++) {

                                    if (!strMedicineNames.get(i).equalsIgnoreCase("") && !strMedicineQty.get(i).equalsIgnoreCase("")) {
                                        j++;
                                        JSONObject jsonObjectMedicine = new JSONObject();

                                        jsonObjectMedicine.put("medicine_name", strMedicineNames.get(i));
                                        jsonObjectMedicine.put("medicine_qty", Integer.parseInt(strMedicineQty.get(i)));

                                        jsonArray.put(jsonObjectMedicine);
                                    }
                                }
                                jsonObject.put("array_data", jsonArray);

                                fieldModel.setStrArrayData(jsonObject.toString());
                            }

                            if (j <= 0) {
                                b = false;
                                utils.toast(2, 2, getString(R.string.error_medicines));
                            }
                        }
                    }

                    if (b) {
                        if (iFlag == 2)
                            milestoneModel.setStrMilestoneStatus("completed");

                        if (iIndex == act.getMilestoneModels().size() && iFlag == 2) {
                            strActivityStatus = "completed";
                        }

                        //
                        //String strDate = milestoneModel.getStrMilestoneDate();

                        String strPushMessage = Config.providerModel.getStrName()
                                + getString(R.string.has_updated)
                          /*  + getString(R.string.activity)
                            + getString(R.string.space)*/
                                + getString(R.string.space)
                                + act.getStrActivityName()
                                + getString(R.string.hyphen)
                           /* + getString(R.string.milestone)*/
                                + getString(R.string.space)
                                + milestoneModel.getStrMilestoneName();


                        if (strScheduledDate != null && !strScheduledDate.equalsIgnoreCase("")) {
                            strPushMessage += getString(R.string.space) + getString(R.string.scheduled_to)
                                    + getString(R.string.space) + strScheduledDate + getString(R.string.space);
                        }

                        jsonObject = new JSONObject();

                        try {

                            String strDateNow = "";
                            Date dateNow = calendar.getTime();
                            strDateNow = utils.convertDateToString(dateNow);

                            int iCustomerPosition = Config.customerIdsAdded.indexOf(act.getStrCustomerID());

                            if (iCustomerPosition > -1)
                                strDependentMail = Config.customerModels.get(iCustomerPosition).getStrEmail();

                            jsonObject.put("created_by", Config.providerModel.getStrProviderId());
                            jsonObject.put("time", strDateNow);
                            jsonObject.put("user_type", "dependent");
                            jsonObject.put("user_id", act.getStrDependentID());
                            jsonObject.put("activity_id", act.getStrActivityID());//todo add to care taker
                            jsonObject.put("created_by_type", "provider");
                            jsonObject.put(App42GCMService.ExtraMessage, strPushMessage);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    //
                }

                if (!milestoneModel.getStrMilestoneStatus().equalsIgnoreCase("completed") && iIndex < act.getMilestoneModels().size()) {
                    iClose++;
                }

                if (iFlag == 2 && strActivityStatus.equalsIgnoreCase("completed") && iClose > 0) {
                    b = false;
                    utils.toast(2, 2, getString(R.string.close_error));
                    strActivityStatus = "inprocess";
                    milestoneModel.setStrMilestoneStatus("inactive");
                }

                //
                if (iActivityPosition > -1) {
                    //Config.activityModels.get(iPosition).removeMilestoneModel(milestoneModel);
                    Config.activityModels.get(iActivityPosition).setMilestoneModel(milestoneModel);
                }
                //
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (b) {

            uploadImage(v, iMileStoneId, viewGroup, iFlag);

        }
    }

    private void uploadJson(int iMilestoneId) {
        ///////////////////////
        JSONObject jsonObjectMileStone = new JSONObject();

        try {
            JSONArray jsonArrayMilestones = new JSONArray();

            for (MilestoneModel milestoneModel : act.getMilestoneModels()) {

                JSONObject jsonObjectMilestone = new JSONObject();

                jsonObjectMilestone.put("id", milestoneModel.getiMilestoneId());

                // if (milestoneModel.getiMilestoneId() == iMilestoneId) {
                    //JSONObject jsonObjectFiles =
                    //JSONObject jsonObjectFiles =
                // jsonObjectMilestone.put("files", jsonArrayImagesAdded);

                //}

                jsonObjectMilestone.put("status", milestoneModel.getStrMilestoneStatus());

                jsonObjectMilestone.put("name", milestoneModel.getStrMilestoneName());
                jsonObjectMilestone.put("date", milestoneModel.getStrMilestoneDate());

                jsonObjectMilestone.put("show", milestoneModel.isVisible());
                jsonObjectMilestone.put("reschedule", milestoneModel.isReschedule());
                jsonObjectMilestone.put("scheduled_date", milestoneModel.getStrMilestoneScheduledDate());

                JSONArray jsonArrayFiles = new JSONArray();

                for (FileModel fileModel : milestoneModel.getFileModels()) {

                    JSONObject jsonObjectFile = new JSONObject();

                    jsonObjectFile.put("file_name", fileModel.getStrFileName());
                    jsonObjectFile.put("file_url", fileModel.getStrFileUrl());
                    jsonObjectFile.put("file_type", fileModel.getStrFileType());
                    jsonObjectFile.put("file_desc", fileModel.getStrFileDescription());
                    jsonObjectFile.put("file_path", fileModel.getStrFilePath());
                    jsonObjectFile.put("file_time", fileModel.getStrFileUploadTime());

                    jsonArrayFiles.put(jsonObjectFile);
                }

                jsonObjectMilestone.put("files", jsonArrayFiles);
                //todo add to corresponding milestone

                JSONArray jsonArrayFields = new JSONArray();

                for (FieldModel fieldModel : milestoneModel.getFieldModels()) {

                    JSONObject jsonObjectField = new JSONObject();

                    jsonObjectField.put("id", fieldModel.getiFieldID());

                    if (fieldModel.isFieldView())
                        jsonObjectField.put("hide", fieldModel.isFieldView());

                    jsonObjectField.put("required", fieldModel.isFieldRequired());
                    jsonObjectField.put("data", fieldModel.getStrFieldData());
                    jsonObjectField.put("label", fieldModel.getStrFieldLabel());
                    jsonObjectField.put("type", fieldModel.getStrFieldType());

                    if (fieldModel.getStrFieldValues() != null && fieldModel.getStrFieldValues().length > 0)
                        jsonObjectField.put("values", utils.stringToJsonArray(fieldModel.getStrFieldValues()));

                    if (fieldModel.isChild()) {

                        jsonObjectField.put("child", fieldModel.isChild());

                        if (fieldModel.getStrChildType() != null && fieldModel.getStrChildType().length > 0)
                            jsonObjectField.put("child_type", utils.stringToJsonArray(fieldModel.getStrChildType()));

                        if (fieldModel.getStrChildValue() != null && fieldModel.getStrChildValue().length > 0)
                            jsonObjectField.put("child_value", utils.stringToJsonArray(fieldModel.getStrChildValue()));

                        if (fieldModel.getStrChildCondition() != null && fieldModel.getStrChildCondition().length > 0)
                            jsonObjectField.put("child_condition", utils.stringToJsonArray(fieldModel.getStrChildCondition()));

                        if (fieldModel.getiChildfieldID() != null && fieldModel.getiChildfieldID().length > 0)
                            jsonObjectField.put("child_field", utils.intToJsonArray(fieldModel.getiChildfieldID()));
                    }

                    //
                    if (fieldModel.getiArrayCount() > 0) {
                        jsonObjectField.put("array_fields", fieldModel.getiArrayCount());
                        jsonObjectField.put("array_type", utils.stringToJsonArray(fieldModel.getStrArrayType()));
                        jsonObjectField.put("array_data", fieldModel.getStrArrayData());
                    }
                    //

                    jsonArrayFields.put(jsonObjectField);

                    jsonObjectMilestone.put("fields", jsonArrayFields);
                }
                jsonArrayMilestones.put(jsonObjectMilestone);
            }
            ////////////////////

            jsonObjectMileStone.put("milestones", jsonArrayMilestones);

            jsonObjectMileStone.put("status", strActivityStatus);
            jsonObjectMileStone.put("activity_done_date", strActivityDoneDate);

            updateMileStones(jsonObjectMileStone);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateMileStones(JSONObject jsonToUpdate) {

        if (utils.isConnectingToInternet()) {

            //  dialog.dismiss();

            loadingPanel.setVisibility(View.VISIBLE);

            //bLoad = true;

            Utils.log(jsonToUpdate.toString(), " JSON ");

            storageService.updateDocs(jsonToUpdate,
                    act.getStrActivityID(),
                    Config.collectionActivity, new App42CallBack() {
                        @Override
                        public void onSuccess(Object o) {
                            insertNotification();
                        }

                        @Override
                        public void onException(Exception e) {
                            loadingPanel.setVisibility(View.GONE);
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

        if (resultCode == Activity.RESULT_OK) { //&& data != null
            try {

                loadingPanel.setVisibility(View.VISIBLE);
                switch (requestCode) {
                    case Config.START_CAMERA_REQUEST_CODE:

                        backgroundThreadHandler = new BackgroundThreadHandler();
                        //  backgroundThreadHandlerDialog = new BackgroundThreadHandlerDialog();
                        strImageName1 = Utils.customerImageUri.getPath();
                        // Thread backgroundTreadDialog1 = new BackgroundThreadDialog1();
                        Thread backgroundThreadCamera = new BackgroundThreadCamera();
                        backgroundThreadCamera.start();
                        //  backgroundTreadDialog1.start();
                        break;

                    case Config.START_GALLERY_REQUEST_CODE:
                        backgroundThreadHandler = new BackgroundThreadHandler();
                        //  backgroundThreadHandlerDialog = new BackgroundThreadHandlerDialog();
                        imagePaths.clear();

                        String[] all_path = intent.getStringArrayExtra("all_path");

                       /* if (all_path.length + IMAGE_COUNT > 20) {

                            for (int i = 0; i < (20 - IMAGE_COUNT); i++) {
                                imagePaths.add(all_path[i]);
                            }

                        } else {*/

                            for (String string : all_path) {
                                imagePaths.add(string);
                            }
                        //}
                        // Thread backgroundTreadDialog2 = new BackgroundThreadDialog2();
                        Thread backgroundThread = new BackgroundThread();
                        backgroundThread.start();
                        //  backgroundTreadDialog2.start();


                        break;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void addImages() {

        layout.removeAllViews();

        for (int i = 0; i < bitmaps.size(); i++) {
            try {

                //
                ImageView imageView = new ImageView(MilestoneActivity.this);
                imageView.setPadding(0, 0, 3, 0);

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                layoutParams.setMargins(10, 10, 10, 10);
                // linearLayout1.setLayoutParams(layoutParams);

                imageView.setLayoutParams(layoutParams);
                imageView.setImageBitmap(bitmaps.get(i));
                imageView.setTag(bitmaps.get(i));
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);


                Utils.log(" 2 " + String.valueOf(i), " IN ");

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Dialog dialog = new Dialog(MilestoneActivity.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                        dialog.setContentView(R.layout.image_dialog_layout);

                        TouchImageView mOriginal = (TouchImageView) dialog.findViewById(R.id.imgOriginal);
                        try {
                            mOriginal.setImageBitmap((Bitmap) v.getTag());
                        } catch (OutOfMemoryError oOm) {
                            oOm.printStackTrace();
                        }
                        dialog.setCancelable(true);

                        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT); //Controlling width and height.
                        dialog.show();

                    }
                });

                layout.addView(imageView);
            } catch (Exception | OutOfMemoryError e) {
                //bitmap.recycle();
                e.printStackTrace();
            }
        }

    }

    private void uploadImage(final ViewGroup v, final int iMileStoneId, final ViewGroup viewGroup, final int iFlag) {

        loadingPanel.setVisibility(View.VISIBLE);

        if (arrayListFileModel.size() > 0) {

            final FileModel fileModel = arrayListFileModel.get(0);

            UploadService uploadService = new UploadService(this);

            uploadService.uploadImageCommon(fileModel.getStrFilePath(),
                    fileModel.getStrFileName(), fileModel.getStrFileDescription(),
                    Config.providerModel.getStrEmail(), UploadFileType.IMAGE,
                    new App42CallBack() {
                        public void onSuccess(Object response) {

                            if (response != null) {

                                //Utils.log(response.toString(), " Success ");

                                Upload upload = (Upload) response;
                                ArrayList<Upload.File> fileList = upload.getFileList();

                                if (fileList.size() > 0) {

                                    Upload.File file = fileList.get(0);

                                    JSONObject jsonObjectImages = new JSONObject();

                                    try {

                                        jsonObjectImages.put("file_name", fileModel.getStrFileName());
                                        jsonObjectImages.put("file_url", file.getUrl());
                                        jsonObjectImages.put("file_type", fileModel.getStrFileType());
                                        jsonObjectImages.put("file_desc", fileModel.getStrFileDescription());
                                        jsonObjectImages.put("file_path", fileModel.getStrFilePath());
                                        jsonObjectImages.put("file_time", fileModel.getStrFileUploadTime());

                                       /* FileModel fileModel1 = new FileModel(fileModel.getStrFileName(),
                                                file.getUrl(), fileModel.getStrFileType(),
                                                fileModel.getStrFileUploadTime(),
                                                fileModel.getStrFileDescription(),
                                                fileModel.getStrFilePath()

                                        );

                                        fileModels.add(fileModel1);
*/
                                        jsonArrayImagesAdded.put(jsonObjectImages);


                                        //
                                        File newFile = new File(fileModel.getStrFilePath());
                                        File renameFile = utils.getInternalFileImages(fileModel.getStrFileName());

                                        try {
                                            utils.moveFile(newFile, renameFile);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }


                                        arrayListFileModel.remove(fileModel);

                                        if (arrayListFileModel.size() <= 0) {
                                            updateImages(iMileStoneId);
                                        } else {
                                            uploadImage(v, iMileStoneId, viewGroup, iFlag);
                                        }
                                        //

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                } else {
                                    if (arrayListFileModel.size() <= 0) {
                                        updateImages(iMileStoneId);
                                    } else {
                                        uploadImage(v, iMileStoneId, viewGroup, iFlag);
                                    }
                                }
                            } else {
                                loadingPanel.setVisibility(View.GONE);
                                utils.toast(2, 2, getString(R.string.warning_internet));
                            }
                        }

                        @Override
                        public void onException(Exception e) {



                            if (e != null) {
                                Utils.log(e.getMessage(), " Failure ");
                                if (arrayListFileModel.size() <= 0) {
                                    updateImages(iMileStoneId);
                                } else {
                                    uploadImage(v, iMileStoneId, viewGroup, iFlag);
                                }
                            } else {
                                loadingPanel.setVisibility(View.GONE);
                                utils.toast(2, 2, getString(R.string.warning_internet));
                            }
                        }
                    });
        } else {
            if (arrayListFileModel.size() <= 0) {
                updateImages(iMileStoneId);
            }
        }

    }

    private void updateImages(int iMilestoneId) {


        if (utils.isConnectingToInternet()) {

            //int iIndex = 0;
            if (iActivityPosition > -1)
                Config.activityModels.get(iActivityPosition).clearMilestoneModel();

            try {

                for (MilestoneModel milestoneModel : act.getMilestoneModels()) {

                    //iIndex++;

                    if (milestoneModel.getiMilestoneId() == iMilestoneId) {

                        milestoneModel.clearFileModel();

                        for (int i = 0; i < jsonArrayImagesAdded.length(); i++) {

                            JSONObject jsonObjectFiles = jsonArrayImagesAdded.getJSONObject(i);

                            FileModel fileModel = new FileModel(
                                    jsonObjectFiles.getString("file_name"),
                                    jsonObjectFiles.getString("file_url"),
                                    jsonObjectFiles.getString("file_type"),
                                    jsonObjectFiles.getString("file_time"),
                                    jsonObjectFiles.getString("file_desc"),
                                    jsonObjectFiles.getString("file_time")
                            );

                            milestoneModel.setFileModel(fileModel);
                        }

                    }

                    if (iActivityPosition > -1) {
                        //Config.activityModels.get(iPosition).removeMilestoneModel(milestoneModel);
                        Config.activityModels.get(iActivityPosition).setMilestoneModel(milestoneModel);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

       /*     if (iPosition > -1) {
                //Config.activityModels.get(iPosition).removeMilestoneModel(milestoneModel);
                Config.activityModels.get(iPosition).setMilestoneModel(milestoneModel);
            }*/
            //////////////////////////////

          /*  for (MilestoneModel milestoneModel : act.getMilestoneModels()) {
                if(milestoneModel.getiMilestoneId()==iMilestoneId)
                    milestoneModel.setFileModels(arrayListFileModel);
            }

            for (MilestoneModel milestoneModel : act.getMilestoneModels()) {
                if(milestoneModel.getiMilestoneId()==iMilestoneId)
                    Utils.log(String.valueOf(milestoneModel.getFileModels().size()), " SIZE ");
            }
*/
          /*  JSONObject jsonToUpdate = null;

            try {
                jsonToUpdate = new JSONObject();
                jsonToUpdate.put("files", jsonArrayImagesAdded);
            } catch (Exception e) {
                e.printStackTrace();
            }*/

            uploadJson(iMilestoneId);

            /*storageService.updateDocs(jsonToUpdate,
                    act.getStrActivityID(),
                    Config.collectionActivity, new App42CallBack() {
                        @Override
                        public void onSuccess(Object o) {
                            //insertNotification()
                            goToActivityList(getString(R.string.image_upload));
                        }

                        @Override
                        public void onException(Exception e) {
                            loadingPanel.setVisibility(View.GONE);
                            utils.toast(2, 2, getString(R.string.warning_internet));
                        }
                    });*/
        } else {
            loadingPanel.setVisibility(View.GONE);
            utils.toast(2, 2, getString(R.string.warning_internet));
        }
    }


    private void insertNotification() {

        if (utils.isConnectingToInternet()) {

            storageService.insertDocs(Config.collectionNotification, jsonObject,
                    new AsyncApp42ServiceApi.App42StorageServiceListener() {

                        @Override
                        public void onDocumentInserted(Storage response) {
                            try {
                                if (response.isResponseSuccess()) {
                                    sendPushToProvider();


                                } else {
                                    strAlert = getString(R.string.no_push_actiity_updated);
                                    goToActivityList(strAlert);

                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                goToActivityList(strAlert);
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
                            strAlert = getString(R.string.no_push_actiity_updated);
                            goToActivityList(strAlert);
                        }

                        @Override
                        public void onFindDocFailed(App42Exception ex) {
                        }

                        @Override
                        public void onUpdateDocFailed(App42Exception ex) {
                        }
                    });
        } else {
            strAlert = getString(R.string.no_push_actiity_updated);

            goToActivityList(strAlert);
        }
    }

    private void sendPushToProvider() {

        if (utils.isConnectingToInternet()) {

            PushNotificationService pushNotificationService = new PushNotificationService(MilestoneActivity.this);

            pushNotificationService.sendPushToUser(strDependentMail, jsonObject.toString(),
                    new App42CallBack() {

                        @Override
                        public void onSuccess(Object o) {

                            strAlert = getString(R.string.activity_updated);

                            if (o == null)
                                strAlert = getString(R.string.no_push_actiity_updated);

                            goToActivityList(strAlert);
                        }

                        @Override
                        public void onException(Exception ex) {
                            if (ex != null)
                                Utils.log(ex.getMessage(), " PUSH ");
                            strAlert = getString(R.string.no_push_actiity_updated);
                            goToActivityList(strAlert);
                        }
                    });
        } else {
            strAlert = getString(R.string.no_push_actiity_updated);

            goToActivityList(strAlert);
        }
    }

    private void goToActivityList(String strAlert) {

        loadingPanel.setVisibility(View.GONE);

        //utils.toast(2, 2, getString(R.string.milestone_updated));

        utils.toast(2, 2, strAlert);

        //  reloadMilestones();

        // Intent intent = new Intent(MilestoneActivity.this, FeatureActivity.class);
        // Config.intSelectedMenu = Config.intDashboardScreen;
        //startActivity(intent);

        //ActivityModel obj = activityModels.get(itemPosition);
        Bundle args = new Bundle();
        Intent intent = new Intent(MilestoneActivity.this, FeatureActivity.class);
        args.putSerializable("ACTIVITY", act);
        args.putInt("ACTIVITY_POSITION", iActivityPosition);
        intent.putExtras(args);
        startActivity(intent);
        finish();
    }

    private class BackgroundThreadHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            addImages();
            loadingPanel.setVisibility(View.GONE);
        }
    }

    private class BackgroundThread extends Thread {
        @Override
        public void run() {

            try {
                for (int i = 0; i < imagePaths.size(); i++) {
                    Calendar calendar = new GregorianCalendar();
                    String strTime = String.valueOf(calendar.getTimeInMillis());
                    String strFileName = strTime + ".jpeg";
                    File galleryFile = utils.createFileInternalImage(strFileName);
                    strImageName1 = galleryFile.getAbsolutePath();
                    Date date = new Date();

                    FileModel fileModel = new FileModel(strTime, "", "IMAGE", utils.convertDateToString(date), "DESC", strImageName1);
                    arrayListFileModel.add(fileModel);

                    utils.copyFile(new File(imagePaths.get(i)), galleryFile);
                    //
                    utils.compressImageFromPath(strImageName1, Config.intCompressWidth, Config.intCompressHeight, Config.iQuality);

                    bitmaps.add(utils.getBitmapFromFile(strImageName1, Config.intWidth, Config.intHeight));

                    IMAGE_COUNT++;
                }
                backgroundThreadHandler.sendEmptyMessage(0);
            } catch (IOException | OutOfMemoryError e) {
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }

    private class BackgroundThreadCamera extends Thread {
        @Override
        public void run() {
            try {
                if (strImageName1 != null && !strImageName1.equalsIgnoreCase("")) {

                    utils.compressImageFromPath(strImageName1, Config.intCompressWidth, Config.intCompressHeight, Config.iQuality);
                    Date date = new Date();
                    FileModel fileModel = new FileModel(strName1, "", "IMAGE", utils.convertDateToString(date), "DESC", strImageName1);
                    arrayListFileModel.add(fileModel);
                    bitmaps.add(utils.getBitmapFromFile(strImageName1, Config.intWidth, Config.intHeight));

                }

                IMAGE_COUNT++;

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
                //


                jsonArrayImagesAdded = new JSONArray();

                for (FileModel fileModel : milestoneModelObject.getFileModels()) {
                    if (fileModel.getStrFileName() != null && !fileModel.getStrFileName().equalsIgnoreCase("")) {
                        bitmaps.add(utils.getBitmapFromFile(utils.getInternalFileImages(fileModel.getStrFileName()).getAbsolutePath(), Config.intWidth, Config.intHeight));
                        IMAGE_COUNT++;

                        JSONObject jsonObjectImages = new JSONObject();

                        jsonObjectImages.put("file_name", fileModel.getStrFileName());
                        jsonObjectImages.put("file_url", fileModel.getStrFileUrl());
                        jsonObjectImages.put("file_type", fileModel.getStrFileType());
                        jsonObjectImages.put("file_desc", fileModel.getStrFileDescription());
                        jsonObjectImages.put("file_path", fileModel.getStrFilePath());
                        jsonObjectImages.put("file_time", fileModel.getStrFileUploadTime());

                        jsonArrayImagesAdded.put(jsonObjectImages);
                    }
                }
                //
            } catch (Exception | OutOfMemoryError e) {
                e.printStackTrace();
            }
            backgroundThreadHandler.sendEmptyMessage(0);
        }
    }

}