package com.hdfc.libs;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.widget.RelativeLayout;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.hdfc.app42service.App42GCMController;
import com.hdfc.app42service.App42GCMService;
import com.hdfc.app42service.PushNotificationService;
import com.hdfc.app42service.StorageService;
import com.hdfc.app42service.UploadService;
import com.hdfc.caregiver.DashboardActivity;
import com.hdfc.caregiver.R;
import com.hdfc.caregiver.fragments.ActivityFragment;
import com.hdfc.caregiver.fragments.DashboardFragment;
import com.hdfc.caregiver.fragments.NotificationFragment;
import com.hdfc.config.CareGiver;
import com.hdfc.config.Config;
import com.hdfc.dbconfig.DbHelper;
import com.hdfc.models.ActivityModel;
import com.hdfc.models.CheckInCareActivityModel;
import com.hdfc.models.CheckInCareModel;
import com.hdfc.models.ClientModel;
import com.hdfc.models.ClientNameModel;
import com.hdfc.models.CustomerModel;
import com.hdfc.models.DependentModel;
import com.hdfc.models.FeedBackModel;
import com.hdfc.models.FieldModel;
import com.hdfc.models.FileModel;
import com.hdfc.models.ImageModel;
import com.hdfc.models.MilestoneModel;
import com.hdfc.models.NotificationModel;
import com.hdfc.models.PictureModel;
import com.hdfc.models.ProviderModel;
import com.hdfc.models.ServiceModel;
import com.hdfc.models.SubActivityModel;
import com.shephertz.app42.paas.sdk.android.App42CallBack;
import com.shephertz.app42.paas.sdk.android.storage.Query;
import com.shephertz.app42.paas.sdk.android.storage.QueryBuilder;
import com.shephertz.app42.paas.sdk.android.storage.Storage;
import com.shephertz.app42.paas.sdk.android.upload.Upload;
import com.shephertz.app42.paas.sdk.android.upload.UploadFileType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

/**
 * Created by Admin on 4/25/2016.
 */
public class AppUtils {
    public static Set<String> categorySet = new HashSet<>();
    private static StorageService storageService;
    //private static Date startDate, endDate;
    //private Context context;
    //private Utils utils;

    public AppUtils(Context context) {

        //utils = new Utils(context);
        //progressDialog = new ProgressDialog(context);
        storageService = new StorageService(context);
       /* sharedPreferences = context.getSharedPreferences(Config.strPreferenceName, Context.MODE_PRIVATE);

        strProviderId = sharedPreferences.getString("PROVIDER_ID", "");*/

        //this.context = context;

        /*Calendar calendar = Calendar.getInstance();

        String strStartDateCopy, strEndDateCopy;*/

        /*try {
            Date dateNow = calendar.getTime();
            strEndDateCopy = Utils.writeFormatDateDB.format(dateNow) + "T23:59:59.999Z";
            strStartDateCopy = Utils.writeFormatDateDB.format(dateNow) + "T00:00:00.000Z";

            endDate = Utils.convertStringToDate(strEndDateCopy);
            startDate = Utils.convertStringToDate(strStartDateCopy);

        } catch (Exception e) {
            e.printStackTrace();
        }*/


    }

    public static void logout(Context _context) {
        try {
            Config.jsonObject = null;
            Config.notificationModels.clear();
            Config.intSelectedMenu = 0;

            Config.iRatings = 0;
            Config.feedBackModels.clear();
            Config.iRatingCount = 0;

            Config.strCustomerNames.clear();
            Config.customerIdsCopy.clear();
            Config.clientModels.clear();
            Config.clientNameModels.clear();
            Config.customerModels.clear();
            Config.customerIdsAdded.clear();

            Config.dependentModels.clear();
            Config.strDependentNames.clear();
            Config.dependentIdsAdded.clear();

            Config.activityModels.clear();

            Config.checkInCareModels.clear();

            //
            Config.strServcieIds.clear();
            Config.serviceModels.clear();
            Config.servicelist.clear();
            Config.serviceCategorylist.clear();
            Config.serviceNameModels.clear();

            //

            //Config.fileModels.clear();
            if (CareGiver.getDbCon() != null)
                CareGiver.getDbCon().truncateDatabase();

            unregisterGcm(_context);

            SessionManager sessionManager = new SessionManager(_context);

            PushNotificationService pushNotificationService = new PushNotificationService(
                    _context);
            pushNotificationService.removeUserDevice(sessionManager.getEmail(),
                    sessionManager.getDeviceToken(), new App42CallBack() {

                        @Override
                        public void onSuccess(Object o) {
                        }

                        @Override
                        public void onException(Exception ex) {
                        }
                    });

            sessionManager.logoutUser();
            App42GCMController.clearPref(_context);
            File fileImage = Utils.createFileInternal("images/", _context);
            Utils.deleteAllFiles(fileImage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void unregisterGcm(final Context _context) {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(_context);

                    try {
                        if (gcm != null)
                            gcm.unregister();
                        Utils.log(" removed ", " Device Token");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } catch (Exception bug) {
                    bug.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public static void loadNotifications(final Context context) {

        final String currentDate;
        Date myDate = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTime(myDate);
        Date time = calendar.getTime();
        SimpleDateFormat outputFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS zz");
        currentDate = outputFmt.format(time);
        System.out.println("Current Date =>" + currentDate);

        //todo optimize data fetch and multiple pushes
        if (Utils.isConnectingToInternet(context)) {

            String strDate = "";

            Cursor cursor = CareGiver.getDbCon().getMaxDate(Config.collectionNotification);

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                strDate = cursor.getString(0);
            }

            if (strDate == null || strDate.equalsIgnoreCase(""))
                strDate = DbHelper.DEFAULT_DB_DATE;

            CareGiver.getDbCon().closeCursor(cursor);

            final SessionManager sessionManager = new SessionManager(context);

            StorageService storageService = new StorageService(context);

            Query q1 = QueryBuilder.build("user_id", sessionManager.getProviderId(),
                    QueryBuilder.Operator.EQUALS);

            Query finalQuery;

            if (!strDate.equalsIgnoreCase("")) {
                Query q12 = QueryBuilder.build("_$updatedAt", strDate,
                        QueryBuilder.Operator.GREATER_THAN);

                finalQuery = QueryBuilder.compoundOperator(q1, QueryBuilder.Operator.AND, q12);
            } else {
                finalQuery = q1;
            }

            storageService.findDocsByQueryOrderBy(Config.collectionNotification, finalQuery, 30000,
                    0, "time", 1, new App42CallBack() {

                        @Override
                        public void onSuccess(Object o) {
                            if (o != null) {

                                Storage storage = (Storage) o;

                                Utils.log(storage.toString(), " Notifications ");

                                if (storage.getJsonDocList().size() > 0) {

                                    ArrayList<Storage.JSONDocument> jsonDocList = storage.
                                            getJsonDocList();

                                    try {

                                        CareGiver.getDbCon().beginDBTransaction();

                                        for (int i = 0; i < jsonDocList.size(); i++) {

                                            String values[] = {jsonDocList.get(i).getDocId(),
                                                    jsonDocList.get(i).getUpdatedAt(),
                                                    jsonDocList.get(i).getJsonDoc(),
                                                    Config.collectionNotification, "", "1",
                                                    sessionManager.getProviderId()};

                                            CareGiver.getDbCon().insert(
                                                    DbHelper.strTableNameCollection,
                                                    values,
                                                    DbHelper.COLLECTION_FIELDS);

                                        }
                                        if (!((Activity) context).isFinishing()) {

                                            if (Config.intSelectedMenu !=
                                                    Config.intNotificationScreen) {
                                                Utils.toast(
                                                        1, 1,
                                                        context.getString(R.string.new_notification),
                                                        context);
                                            } else {
                                                createNotificationModel(currentDate);
                                                if (NotificationFragment.notificationAdapter !=
                                                        null) {
                                                    NotificationFragment.notificationAdapter.
                                                            notifyDataSetChanged();
                                                }

                                            }
                                        }
                                        CareGiver.getDbCon().dbTransactionSuccessFull();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    } finally {
                                        CareGiver.getDbCon().endDBTransaction();
                                    }
                                }
                            }
                        }

                        @Override
                        public void onException(Exception e) {
                            Utils.log(e.getMessage(), " Notifications ");
                        }
                    });
        }
    }

    public static void fetchActivitiesSync(final Context context, final RelativeLayout
            loadingPanel) {

        String strDate = "";

        final SessionManager sessionManager = new SessionManager(context);

        boolean b = sessionManager.getActivitySync();

        if (b) {
            Cursor cursor = CareGiver.getDbCon().getMaxDate(Config.collectionActivity);

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                strDate = cursor.getString(0);
            }

            if (strDate == null || strDate.equalsIgnoreCase(""))
                strDate = DbHelper.DEFAULT_DB_DATE;

            CareGiver.getDbCon().closeCursor(cursor);
        }

        Query q1 = QueryBuilder.build("provider_id", sessionManager.getProviderId(),
                QueryBuilder.Operator.EQUALS);

        Query finalQuery;

        if (!strDate.equalsIgnoreCase("") && b) {
            Query q12 = QueryBuilder.build("_$updatedAt", strDate, QueryBuilder.Operator.
                    GREATER_THAN);

            finalQuery = QueryBuilder.compoundOperator(q1, QueryBuilder.Operator.AND, q12);
        } else {
            finalQuery = q1;
        }

        StorageService storageService = new StorageService(context);

        storageService.findDocsByQueryOrderBy(Config.collectionActivity, finalQuery, 30000, 0,
                "milestones.scheduled_date", 1,
                new App42CallBack() {

                    @Override
                    public void onSuccess(Object o) {
                        if (o != null) {

                            Utils.log(o.toString(), " Activity All ");

                            Storage storage = (Storage) o;

                            ArrayList<Storage.JSONDocument> jsonDocList = storage.getJsonDocList();

                            try {
                                CareGiver.getDbCon().beginDBTransaction();
                                for (int i = 0; i < jsonDocList.size(); i++) {

                                    Storage.JSONDocument jsonDocument = jsonDocList.get(i);

                                    String values[] = {jsonDocument.getDocId(),
                                            jsonDocument.getUpdatedAt(),
                                            jsonDocument.getJsonDoc(),
                                            Config.collectionActivity, "", "1",
                                            sessionManager.getProviderId()};

                                    String selection = DbHelper.COLUMN_OBJECT_ID + " = ? and "
                                            + DbHelper.COLUMN_COLLECTION_NAME + "=?";

                                    // WHERE clause arguments
                                    String[] selectionArgs = {jsonDocument.getDocId(),
                                            Config.collectionActivity};
                                    CareGiver.getDbCon().updateInsert(
                                            DbHelper.strTableNameCollection,
                                            selection, values, DbHelper.COLLECTION_FIELDS,
                                            selectionArgs);

                                    insertActivityDate(jsonDocument.getDocId(),
                                            jsonDocument.getJsonDoc());
                                }
                                sessionManager.setActivitySync(true);

                                //
                                if (!((Activity) context).isFinishing()) {

                                    if (Config.intSelectedMenu !=
                                            Config.intDashboardScreen) {
                                        Utils.toast(
                                                1, 1,
                                                context.getString(R.string.new_activity),
                                                context);
                                    } else {

                                        if (Config.intSelectedMenu == Config.intDashboardScreen
                                                && DashboardFragment._strDate != null) {

                                            String strStartDate = DashboardFragment._strDate
                                                    + " 00:00:00.000";

                                            //todo check time value
                                            String strEndDate = DashboardFragment._strDate
                                                    + " 24:59:59.999"; //+ "  23:59:59.999";

                                            if (ActivityFragment.mAdapter != null) {
                                                createActivityModel(strStartDate, strEndDate);
                                                ActivityFragment.activityModels = Config.
                                                        activityModels;
                                                ActivityFragment.mAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    }
                                }
                                //
                                CareGiver.getDbCon().dbTransactionSuccessFull();
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                CareGiver.getDbCon().endDBTransaction();
                            }
                        }
                        fetchClients(1, context, loadingPanel);
                        /*if (DashboardActivity.isLoaded)
                            DashboardActivity.gotoSimpleActivityMenu(false);*/
                    }

                    @Override
                    public void onException(Exception ex) {
                        Utils.log(ex.getMessage(), " Sync Activity Failure ");
                        fetchClients(1, context, loadingPanel);
                        /*if (DashboardActivity.isLoaded)
                            DashboardActivity.gotoSimpleActivityMenu(false);*/
                    }
                });
    }

    ///////////////////////////insert into DB
    public static void insertActivityDate(String strDocumentId, final String strDocument) {

        try {

            JSONObject jsonObject = new JSONObject(strDocument);

            if (jsonObject.has("dependent_id")) {

                String selectionActivity = DbHelper.COLUMN_OBJECT_ID + " = ? and "
                        + DbHelper.COLUMN_MILESTONE_ID + "=? ";

                String[] selectionArgsActivity = {strDocumentId,
                        "0"
                };

                Utils.log(jsonObject.getString("activity_date"), " Actual Date 0");

                String strActivityDate = Utils.formatDateInsertActivity(jsonObject.
                        getString("activity_date"));

                Utils.log(strActivityDate, " Converted Date 0 ");

                String valuesActivity[] = {strDocumentId, "0", strActivityDate,
                        jsonObject.optString("provider_id"), jsonObject.optString("status")};

                CareGiver.getDbCon().updateInsert(
                        DbHelper.strTableNameMilestone,
                        selectionActivity, valuesActivity, DbHelper.MILESTONE_FIELDS,
                        selectionArgsActivity);

                insertClientIds(jsonObject.optString("customer_id"),
                        jsonObject.optString("dependent_id"),
                        jsonObject.optString("provider_id"));

                //feedback
                if (jsonObject.has("feedbacks")) {

                    JSONArray jsonArrayFeedback = jsonObject.getJSONArray("feedbacks");

                    for (int k = 0; k < jsonArrayFeedback.length(); k++) {

                        JSONObject jsonObjectFeedback = jsonArrayFeedback.getJSONObject(k);

                        if (jsonObjectFeedback.has("feedback_message")) {

                            String strCustomerId = "", strDependentId = "";

                            if (jsonObjectFeedback.getString("feedback_by_type").
                                    equalsIgnoreCase("customer")) {
                                strCustomerId = jsonObjectFeedback.optString("feedback_by");
                            }

                            if (jsonObjectFeedback.getString("feedback_by_type").
                                    equalsIgnoreCase("dependent")) {
                                strDependentId = jsonObjectFeedback.optString("feedback_by");
                            }
                            insertClientIds(strCustomerId, strDependentId,
                                    jsonObject.optString("provider_id"));
                        }
                    }

                }
                //feedback

                if (jsonObject.has("milestones")) {

                    JSONArray jsonArrayMilestones = jsonObject.
                            getJSONArray("milestones");

                    for (int k = 0; k < jsonArrayMilestones.length(); k++) {

                        JSONObject jsonObjectMilestone =
                                jsonArrayMilestones.getJSONObject(k);

                        if (jsonObjectMilestone.has("scheduled_date")
                                ) {
                            /*&& */
                            String strMilestoneDate = "";

                            if (!jsonObjectMilestone.getString("scheduled_date")
                                    .equalsIgnoreCase("")) {
                                strMilestoneDate =
                                        Utils.formatDateInsertActivity(jsonObjectMilestone.
                                                getString("scheduled_date"));
                            }

                            String values[] = {strDocumentId, jsonObjectMilestone.getString("id"),
                                    strMilestoneDate, jsonObject.optString("provider_id"),
                                    jsonObjectMilestone.optString("status")};

                            String selection = DbHelper.COLUMN_OBJECT_ID + " = ? and "
                                    + DbHelper.COLUMN_MILESTONE_ID + "=? ";

                            String[] selectionArgs = {strDocumentId,
                                    jsonObjectMilestone.getString("id")
                            };

                            CareGiver.getDbCon().updateInsert(
                                    DbHelper.strTableNameMilestone,
                                    selection, values, DbHelper.MILESTONE_FIELDS,
                                    selectionArgs);
                        }
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void fetchDependents(final int iFlag, final Context context,
                                        final RelativeLayout loadingPanel) {

        if (Utils.isConnectingToInternet(context)) {

            final SessionManager sessionManager = new SessionManager(context);

            String strDate = "";

            Cursor cursor = CareGiver.getDbCon().getMaxDate(Config.collectionDependent);

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                strDate = cursor.getString(0);
            }

            if (strDate == null || strDate.equalsIgnoreCase(""))
                strDate = DbHelper.DEFAULT_DB_DATE;

            CareGiver.getDbCon().closeCursor(cursor);

            Cursor cursor1 = CareGiver.getDbCon().fetch(
                    DbHelper.strTableNameCollection, new String[]{DbHelper.COLUMN_OBJECT_ID},
                    DbHelper.COLUMN_COLLECTION_NAME + "=? and " + DbHelper.COLUMN_PROVIDER_ID + "=?",
                    new String[]{Config.collectionDependent, sessionManager.getProviderId()},
                    null, null, true, null, null
            );

            ArrayList<String> strDependentIds = new ArrayList<>();

            if (cursor1.getCount() > 0) {
                cursor1.moveToFirst();

                while (!cursor1.isAfterLast()) {
                    strDependentIds.add(cursor1.getString(0));
                    cursor1.moveToNext();
                }
            }
            CareGiver.getDbCon().closeCursor(cursor1);

            Query finalQuery = QueryBuilder.build("_$updatedAt", strDate, QueryBuilder.Operator.
                    GREATER_THAN);

            Query mQuery1 = null;
            if (strDependentIds.size() > 0) {
                mQuery1 = QueryBuilder.build("_id", strDependentIds,
                        QueryBuilder.Operator.INLIST);
            }

            if (mQuery1 != null) { //strDate != null && !strDate.equalsIgnoreCase("")
                finalQuery = QueryBuilder.compoundOperator(mQuery1, QueryBuilder.Operator.AND,
                        finalQuery);
            }

            storageService.findDocsByQuery(Config.collectionDependent, finalQuery,
                    new App42CallBack() {

                        @Override
                        public void onSuccess(Object o) {
                            try {
                                if (o != null) {

                                    Storage storage = (Storage) o;

                                    Utils.log(o.toString(), " Dependent");

                                    if (storage.getJsonDocList().size() > 0) {

                                        for (int i = 0; i < storage.getJsonDocList().size(); i++) {

                                            Storage.JSONDocument jsonDocument = storage.
                                                    getJsonDocList().get(i);

                                            //fetch name
                                            JSONObject jsonObject;
                                            String strName = "";
                                            try {
                                                jsonObject = new JSONObject(jsonDocument.
                                                        getJsonDoc());
                                                strName = jsonObject.optString("dependent_name");
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                            String values[] = {jsonDocument.getDocId(),
                                                    jsonDocument.getUpdatedAt(),
                                                    jsonDocument.getJsonDoc(),
                                                    Config.collectionDependent,
                                                    sessionManager.getProviderId(),
                                                    "1", strName
                                            };

                                            String selection = DbHelper.COLUMN_OBJECT_ID
                                                    + " = ? and "
                                                    + DbHelper.COLUMN_COLLECTION_NAME + "=?";

                                            // WHERE clause arguments
                                            String[] selectionArgs = {jsonDocument.getDocId(),
                                                    Config.collectionDependent};
                                            CareGiver.getDbCon().updateInsert(
                                                    DbHelper.strTableNameCollection,
                                                    selection, values,
                                                    DbHelper.COLLECTION_FIELDS_CD,
                                                    selectionArgs);

                                        }
                                    }
                                }
                                if (DashboardActivity.isLoaded) {
                                    if (iFlag == 2)
                                        DashboardActivity.gotoSimpleActivityMenu(true, context,
                                                loadingPanel);
                                    else
                                        DashboardActivity.gotoSimpleActivityMenu(false, context,
                                                loadingPanel);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onException(Exception e) {
                            Utils.log(e.getMessage(), " Dependent Failure");
                            if (DashboardActivity.isLoaded) {
                                if (iFlag == 2)
                                    DashboardActivity.gotoSimpleActivityMenu(true, context,
                                            loadingPanel);
                                else
                                    DashboardActivity.gotoSimpleActivityMenu(false, context,
                                            loadingPanel);
                            }
                        }
                    });
            /*} else {
                if (DashboardActivity.isLoaded) {
                    if (iFlag == 2)
                        DashboardActivity.gotoSimpleActivityMenu(true);
                    else
                        DashboardActivity.gotoSimpleActivityMenu(false);
                }
            }*/
        }
    }

    private static void fetchCustomers(final int iFlag, final Context context,
                                       final RelativeLayout loadingPanel) {

        if (Utils.isConnectingToInternet(context)) {

            final SessionManager sessionManager = new SessionManager(context);

            String strDate = "";

            Cursor cursor = CareGiver.getDbCon().getMaxDate(Config.collectionCustomer);

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                strDate = cursor.getString(0);
            }

            if (strDate == null || strDate.equalsIgnoreCase(""))
                strDate = DbHelper.DEFAULT_DB_DATE;

            CareGiver.getDbCon().closeCursor(cursor);

            Cursor cursor1 = CareGiver.getDbCon().fetch(
                    DbHelper.strTableNameCollection, new String[]{DbHelper.COLUMN_OBJECT_ID},
                    DbHelper.COLUMN_COLLECTION_NAME + "=? and " + DbHelper.COLUMN_PROVIDER_ID + "=?",
                    new String[]{Config.collectionCustomer, sessionManager.getProviderId()},
                    null, null, true, null, null
            );

            ArrayList<String> strCustomerIds = new ArrayList<>();

            if (cursor1.getCount() > 0) {
                cursor1.moveToFirst();

                while (!cursor1.isAfterLast()) {
                    strCustomerIds.add(cursor1.getString(0));
                    cursor1.moveToNext();
                }
            }
            CareGiver.getDbCon().closeCursor(cursor1);

            Query finalQuery = QueryBuilder.build("_$updatedAt", strDate, QueryBuilder.Operator.
                    GREATER_THAN);

            Query mQuery1 = null;
            if (strCustomerIds.size() > 0) {
                mQuery1 = QueryBuilder.build("_id", strCustomerIds,
                        QueryBuilder.Operator.INLIST);
            }

            if (mQuery1 != null) {
                Query q1 = QueryBuilder.compoundOperator(mQuery1, QueryBuilder.Operator.AND,
                        finalQuery);

                /*Query q2 = QueryBuilder.build("customer_register", "true", QueryBuilder.Operator.
                    EQUALS);*/

                //fetch registered customers
                //Query q3 = QueryBuilder.compoundOperator(q1, QueryBuilder.Operator.AND, q2);
                finalQuery = QueryBuilder.compoundOperator(finalQuery, QueryBuilder.Operator.AND,
                        q1);
            }

            storageService.findDocsByQuery(Config.collectionCustomer, finalQuery,
                    new App42CallBack() {

                        @Override
                        public void onSuccess(Object o) {
                            try {

                                if (o != null) {

                                    Utils.log(o.toString(), " fetchCustomers ");
                                    Storage storage = (Storage) o;

                                    if (storage.getJsonDocList().size() > 0) {

                                        for (int i = 0; i < storage.getJsonDocList().size();
                                             i++) {

                                            Storage.JSONDocument jsonDocument = storage.
                                                    getJsonDocList().get(i);

                                            //fetch name
                                            JSONObject jsonObject;
                                            String strName = "";
                                            try {
                                                jsonObject = new JSONObject(jsonDocument.getJsonDoc());
                                                strName = jsonObject.optString("customer_name");
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                            String values[] = {jsonDocument.getDocId(),
                                                    jsonDocument.getUpdatedAt(),
                                                    jsonDocument.getJsonDoc(),
                                                    Config.collectionCustomer,
                                                    sessionManager.getProviderId(),
                                                    "1", strName
                                            };

                                            String selection = DbHelper.COLUMN_OBJECT_ID
                                                    + " = ? and "
                                                    + DbHelper.COLUMN_COLLECTION_NAME + "=?";

                                            // WHERE clause arguments
                                            String[] selectionArgs = {jsonDocument.getDocId(),
                                                    Config.collectionCustomer};
                                            CareGiver.getDbCon().updateInsert(
                                                    DbHelper.strTableNameCollection,
                                                    selection, values,
                                                    DbHelper.COLLECTION_FIELDS_CD,
                                                    selectionArgs);
                                        }
                                    }
                                    fetchDependents(iFlag, context, loadingPanel);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onException(Exception e) {
                            Utils.log(e.getMessage(), " MESS 0");
                            fetchDependents(iFlag, context, loadingPanel);
                        }

                    });
        }
    }

    //refresh Clients = Customer->Dependents
    public static void fetchClients(final int iFlag, final Context context,
                                    final RelativeLayout loadingPanel) {

        if (Utils.isConnectingToInternet(context)) {

            Query finalQuery;
            Calendar calendar = Calendar.getInstance();
            Date date = calendar.getTime();
            final String strDateNow = Utils.readFormat.format(date);

            final SessionManager sessionManager = new SessionManager(context);

            String strDate = "";

            if (sessionManager.getClientDate() != null
                    && !sessionManager.getClientDate().equalsIgnoreCase("")) {
                strDate = sessionManager.getClientDate();
            }

            if (strDate == null || strDate.equalsIgnoreCase(""))
                strDate = DbHelper.DEFAULT_DB_DATE;

            Query q1 = QueryBuilder.build("provider_id", sessionManager.getProviderId(),
                    QueryBuilder.Operator.EQUALS);

            if (!strDate.equalsIgnoreCase("")) {
                Query q12 = QueryBuilder.build("_$updatedAt", strDate, QueryBuilder.Operator.
                        GREATER_THAN);
                finalQuery = QueryBuilder.compoundOperator(q1, QueryBuilder.Operator.AND, q12);
            } else {
                finalQuery = q1;
            }

            StorageService storageService = new StorageService(context);

            storageService.findDocsByQueryOrderBy(Config.collectionProviderDependent, finalQuery,
                    30000, 0,
                    "provider_id", 1, new App42CallBack() {
                        @Override
                        public void onSuccess(Object o) {
                            try {
                                if (o != null) {

                                    Storage storage = (Storage) o;

                                    Utils.log(o.toString(), " MESS ");

                                    if (storage.getJsonDocList().size() > 0) {

                                        ArrayList<Storage.JSONDocument> jsonDocList = storage.
                                                getJsonDocList();

                                        for (int i = 0; i < jsonDocList.size(); i++) {

                                            Storage.JSONDocument jsonDocument = storage.
                                                    getJsonDocList().get(i);

                                            String strDocument = jsonDocument.getJsonDoc();

                                            try {
                                                JSONObject jsonObject = new JSONObject(strDocument);

                                                insertClientIds(jsonObject.getString("customer_id"),
                                                        jsonObject.getString("dependent_id"),
                                                        jsonObject.optInt("removed"),
                                                        sessionManager.getProviderId());

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        sessionManager.saveClientDate(strDateNow);

                                        if (!((Activity) context).isFinishing()) {

                                            Utils.toast(1, 1,
                                                    context.getString(R.string.new_clients),
                                                    context);
                                        }
                                    }
                                }

                                fetchCustomers(iFlag, context, loadingPanel);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                                //fetchCustomers(iFlag);
                            }
                        }

                        @Override
                        public void onException(Exception e) {
                            Utils.log(e.getMessage(), " Clients Failure ");
                            fetchCustomers(iFlag, context, loadingPanel);
                        }
                    });
        }
    }

    private static void insertClientIds(String strCustomerId, String strDependentId,
                                        String strProviderId) {

        try {
            //customer
            if (!strCustomerId.equalsIgnoreCase("")) {
                Cursor cursor = CareGiver.getDbCon().fetch(
                        DbHelper.strTableNameCollection, new String[]{DbHelper.COLUMN_DOCUMENT},
                        DbHelper.COLUMN_COLLECTION_NAME + "=? and " + DbHelper.COLUMN_OBJECT_ID
                                + "=? and " + DbHelper.COLUMN_PROVIDER_ID + "=?",
                        new String[]{Config.collectionCustomer, strCustomerId, strProviderId},
                        null, "0, 1", true, null, null
                );

                if (cursor.getCount() <= 0) {
                    String values[] = {strCustomerId, Config.collectionCustomer, strProviderId};

                    CareGiver.getDbCon().insert(DbHelper.strTableNameCollection, values,
                            DbHelper.COLLECTION_FIELDS_CLIENTS);
                }

                CareGiver.getDbCon().closeCursor(cursor);
            }

            //dependent
            if (!strDependentId.equalsIgnoreCase("")) {
                Cursor cursor1 = CareGiver.getDbCon().fetch(
                        DbHelper.strTableNameCollection, new String[]{DbHelper.COLUMN_DOCUMENT},
                        DbHelper.COLUMN_COLLECTION_NAME + "=? and " + DbHelper.COLUMN_OBJECT_ID
                                + "=? and " + DbHelper.COLUMN_PROVIDER_ID + "=?",
                        new String[]{Config.collectionDependent, strDependentId, strProviderId},
                        null, "0, 1", true, null, null
                );

                if (cursor1.getCount() <= 0) {

                    String values1[] = {strDependentId, Config.collectionDependent, strProviderId};

                    CareGiver.getDbCon().insert(DbHelper.strTableNameCollection, values1,
                            DbHelper.COLLECTION_FIELDS_CLIENTS);
                }

                CareGiver.getDbCon().closeCursor(cursor1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void insertClientIds(String strCustomerId, String strDependentId, int iRemoved,
                                        String strProviderId) {

        try {

            if (!strCustomerId.equalsIgnoreCase("")) {
                Cursor cursor = CareGiver.getDbCon().fetch(
                        DbHelper.strTableNameCollection, new String[]{DbHelper.COLUMN_DOCUMENT},
                        DbHelper.COLUMN_COLLECTION_NAME + "=? and " + DbHelper.COLUMN_OBJECT_ID
                                + "=? and " + DbHelper.COLUMN_PROVIDER_ID + "=?",
                        new String[]{Config.collectionCustomer, strCustomerId, strProviderId},
                        null, "0, 1", true,
                        null, null
                );

                if (cursor.getCount() <= 0) {
                    String values[] = {strCustomerId, "", "", Config.collectionCustomer,
                            String.valueOf(iRemoved), "1", strProviderId};

                    CareGiver.getDbCon().insert(DbHelper.strTableNameCollection, values,
                            DbHelper.COLLECTION_FIELDS);
                } else {
                    String values[] = {String.valueOf(iRemoved), "1"};
                    String selection = DbHelper.COLUMN_OBJECT_ID + "=? and "
                            + DbHelper.COLUMN_COLLECTION_NAME + "=?";

                    // WHERE clause arguments
                    String[] selectionArgs = {strCustomerId, Config.collectionCustomer};
                    CareGiver.getDbCon().update(DbHelper.strTableNameCollection, selection, values,
                            new String[]{DbHelper.COLUMN_CLIENT_FLAG, DbHelper.COLUMN_NEW_UPDATED},
                            selectionArgs);
                }

                CareGiver.getDbCon().closeCursor(cursor);
            }

            //dependent
            if (!strDependentId.equalsIgnoreCase("")) {
                Cursor cursor1 = CareGiver.getDbCon().fetch(
                        DbHelper.strTableNameCollection, new String[]{DbHelper.COLUMN_DOCUMENT},
                        DbHelper.COLUMN_COLLECTION_NAME + "=? and "
                                + DbHelper.COLUMN_OBJECT_ID + "=? and "
                                + DbHelper.COLUMN_PROVIDER_ID + "=?",
                        new String[]{Config.collectionDependent, strDependentId, strProviderId},
                        null, "0, 1", true,
                        null, null
                );

                if (cursor1.getCount() <= 0) {

                    String values1[] = {strDependentId, "", "", Config.collectionDependent,
                            String.valueOf(iRemoved), "1", strProviderId};

                    CareGiver.getDbCon().insert(DbHelper.strTableNameCollection, values1,
                            DbHelper.COLLECTION_FIELDS);
                } else {
                    String values[] = {String.valueOf(iRemoved), "1"};
                    String selection = DbHelper.COLUMN_OBJECT_ID + "=? and "
                            + DbHelper.COLUMN_COLLECTION_NAME + "=?";

                    // WHERE clause arguments
                    String[] selectionArgs = {strDependentId, Config.collectionDependent};
                    CareGiver.getDbCon().update(DbHelper.strTableNameCollection, selection, values,
                            new String[]{DbHelper.COLUMN_CLIENT_FLAG, DbHelper.COLUMN_NEW_UPDATED},
                            selectionArgs);
                }

                CareGiver.getDbCon().closeCursor(cursor1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createProviderModel(String strProviderId) {

        try {

            Cursor cursor = CareGiver.getDbCon().fetch(
                    DbHelper.strTableNameCollection, new String[]{DbHelper.COLUMN_DOCUMENT},
                    DbHelper.COLUMN_COLLECTION_NAME + "=? and " + DbHelper.COLUMN_OBJECT_ID + "=?",
                    new String[]{Config.collectionProvider, strProviderId}, null, "0, 1", true,
                    null, null
            );

            String strDocument = "";

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                strDocument = cursor.getString(0);
            }
            CareGiver.getDbCon().closeCursor(cursor);

            if (!strDocument.equalsIgnoreCase("")) {

                JSONObject jsonObject = new JSONObject(strDocument);

                if (jsonObject.has("provider_email")) {

                    Config.providerModel = new ProviderModel(
                            jsonObject.getString("provider_name"),
                            jsonObject.getString("provider_profile_url"),
                            //"",
                            jsonObject.getString("provider_address"),
                            jsonObject.getString("provider_contact_no"),
                            jsonObject.getString("provider_email"),
                            strProviderId);

                    Config.providerModel.setStrCountry(jsonObject.getString("provider_country"));
                    Config.providerModel.setStrState(jsonObject.getString("provider_state"));
                    Config.providerModel.setStrCity(jsonObject.getString("provider_city"));
                    Config.providerModel.setStrPinCode(jsonObject.getString("provider_pin_code"));

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*public void loadAllFiles() {

        Cursor cur = null;

        try {

            cur = CareGiver.getDbCon().fetch(
                    DbHelper.strTableNameFiles, new String[]{"name", "url"}, null,
                    null, null, null, true, null, null
            );

            if (cur.getCount() > 0) {
                cur.moveToFirst();
                while (!cur.isAfterLast()) {
                    utils.loadImageFromWeb(cur.getString(0), cur.getString(1));
                    cur.moveToNext();
                }
            }

            CareGiver.getDbCon().closeCursor(cur);

        } catch (Exception e) {
            e.printStackTrace();
            CareGiver.getDbCon().closeCursor(cur);
        }

        *//*for (int i = 0; i < Config.fileModels.size(); i++) {
            FileModel fileModel = Config.fileModels.get(i);

            if (fileModel != null && fileModel.getStrFileUrl() != null &&
                    !fileModel.getStrFileUrl().equalsIgnoreCase("")) {
                utils.loadImageFromWeb(fileModel.getStrFileName(),
                        fileModel.getStrFileUrl());
            }
        }*//*
    }*/

    public static void refreshProvider(Context context) {

        if (Utils.isConnectingToInternet(context)) {

            String strDate = "";

            final SessionManager sessionManager = new SessionManager(context);

            boolean b = sessionManager.getActivitySync();

            if (b) {
                Cursor cursor = CareGiver.getDbCon().getMaxDate(Config.collectionProvider);

                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    strDate = cursor.getString(0);
                }

                if (strDate == null || strDate.equalsIgnoreCase(""))
                    strDate = DbHelper.DEFAULT_DB_DATE;

                CareGiver.getDbCon().closeCursor(cursor);
            }

            Query q1 = QueryBuilder.build("provider_email", sessionManager.getEmail().toLowerCase(),
                    QueryBuilder.Operator.EQUALS);

            Query finalQuery;

            if (!strDate.equalsIgnoreCase("") && b) {
                Query q2 = QueryBuilder.build("_$updatedAt", strDate, QueryBuilder.Operator.
                        GREATER_THAN);

                finalQuery = QueryBuilder.compoundOperator(q1, QueryBuilder.Operator.AND, q2);
            } else {
                finalQuery = q1;
            }

            StorageService storageService = new StorageService(context);

            storageService.findDocsByQueryOrderBy(Config.collectionProvider, finalQuery, 1, 0,
                    "updated_date", 1, new App42CallBack() {
                        @Override
                        public void onSuccess(Object o) {
                            try {
                                Storage storage = (Storage) o;

                                if (storage.isResponseSuccess() && storage.getJsonDocList().
                                        size() > 0) {

                                    Storage.JSONDocument jsonDocument = storage.getJsonDocList().
                                            get(0);

                                    String values[] = {jsonDocument.getDocId(),
                                            jsonDocument.getUpdatedAt(),
                                            jsonDocument.getJsonDoc(),
                                            Config.collectionProvider, "", "1",
                                            sessionManager.getProviderId()
                                    };

                                    String selection = DbHelper.COLUMN_OBJECT_ID +
                                            " = ? and " + DbHelper.COLUMN_COLLECTION_NAME
                                            + " = ? ";

                                    String[] selectionArgs = {jsonDocument.getDocId(),
                                            Config.collectionProvider};

                                    CareGiver.getDbCon().updateInsert(
                                            DbHelper.strTableNameCollection,
                                            selection, values, DbHelper.COLLECTION_FIELDS,
                                            selectionArgs);

                                    createProviderModel(jsonDocument.getDocId());
                                }
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }

                        @Override
                        public void onException(Exception e) {
                        }
                    });
        }
    }

    public static void syncAll(Context context, RelativeLayout loadingPanel) {

        //todo add to Intent Service
        Utils.log(" 1 ", " syncAll ");
        fetchActivitiesSync(context, loadingPanel);
        Utils.log(" 2 ", " syncAll ");
        loadNotifications(context);
        Utils.log(" 3 ", " syncAll ");
        refreshProvider(context);
        Utils.log(" 4 ", " syncAll ");

        //todo optional fetch check in cares,services
        fetchCheckInCareSync(context);
        Utils.log(" 5 ", " syncAll ");
        fetchServicesSync(context);
        Utils.log(" 6 ", " syncAll ");
    }

    public static void createActivityModel(String strStartDate, String strEndDate) {

        try {

            Config.strActivityIds.clear();
            Config.activityModels.clear();

            String strQuery = "SELECT a." + DbHelper.COLUMN_DOCUMENT + " AS C1 , b."
                    + DbHelper.COLUMN_MILESTONE_ID + " AS C2, b." + DbHelper.COLUMN_OBJECT_ID
                    + " AS C3, b." + DbHelper.COLUMN_MILESTONE_DATE + " AS C4 "
                    + " FROM " + DbHelper.strTableNameCollection + " AS a INNER JOIN "
                    + DbHelper.strTableNameMilestone + " AS b ON a.object_id=b.object_id  WHERE b."
                    + DbHelper.COLUMN_MILESTONE_DATE + ">= Datetime('" + strStartDate + "') AND b."
                    + DbHelper.COLUMN_MILESTONE_DATE + "<= Datetime('" + strEndDate + "')"
                    + " AND a." + DbHelper.COLUMN_COLLECTION_NAME + "='" + Config.collectionActivity
                    + "' AND a." + DbHelper.COLUMN_PROVIDER_ID + "='"
                    + Config.providerModel.getStrProviderId()
                    + "' AND b." + DbHelper.COLUMN_MILESTONE_ID + "!=-1 ORDER BY"
                    + " b." + DbHelper.COLUMN_MILESTONE_DATE + " DESC";

            Utils.log(strQuery, " QUERY ");

            Cursor newCursor = CareGiver.getDbCon().rawQuery(strQuery);

            if (newCursor.getCount() > 0) {

                newCursor.moveToFirst();

                Boolean isActivity = true;

                while (!newCursor.isAfterLast()) {

                    if (!Config.strActivityIds.contains(newCursor.getString(2))) {
                        Config.strActivityIds.add(newCursor.getString(2));

                        if (!newCursor.getString(1).equalsIgnoreCase("0"))
                            isActivity = false;

                        JSONObject jsonObject = new JSONObject(newCursor.getString(0));

                        createActivityModel(jsonObject, newCursor.getString(2), isActivity,
                                newCursor.getString(3));
                    }
                    newCursor.moveToNext();
                }
            }
            CareGiver.getDbCon().closeCursor(newCursor);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void createActivityModel(JSONObject jsonObject, String strDocumentId,
                                            boolean isActivity, String strDate) {
        try {

            if (jsonObject.has("dependent_id")) {

                ActivityModel activityModel = new ActivityModel();

                activityModel.setStrActivityName(jsonObject.optString("activity_name"));
                activityModel.setStrActivityID(strDocumentId);
                activityModel.setStrProviderID(jsonObject.optString("provider_id"));
                activityModel.setStrDependentID(jsonObject.optString("dependent_id"));
                activityModel.setStrCustomerID(jsonObject.optString("customer_id"));
                activityModel.setStrActivityStatus(jsonObject.optString("status"));
                activityModel.setStrActivityDesc(jsonObject.optString("activity_desc"));

                activityModel.setStrDisplayDate(Utils.displayActivityDate(strDate));
                activityModel.setStrDisplayTime(Utils.displayActivityTime(strDate));

                activityModel.setStrCreatedBy(jsonObject.optString("created_by"));

                activityModel.setStrServcieID(jsonObject.optString("service_id"));
                activityModel.setStrServiceName(jsonObject.optString("service_name"));
                activityModel.setmServiceNo(jsonObject.optInt("service_no"));

                if (jsonObject.has("activity_date")) {
                    activityModel.setStrActivityDate(jsonObject.getString("activity_date"));
                }

                activityModel.setStrActivityDoneDate(jsonObject.
                        optString("activity_done_date"));

                activityModel.setStrActivityProviderStatus(jsonObject.
                        optString("provider_status"));

                activityModel.setStrActivityProviderMessage(jsonObject.
                        optString("provider_message"));

                ArrayList<FeedBackModel> feedBackModels = new ArrayList<>();
                //ArrayList<VideoModel> videoModels = new ArrayList<>();
                ArrayList<ImageModel> imageModels = new ArrayList<>();

               /* if (jsonObject.has("videos")) {

                    JSONArray jsonArrayVideos = jsonObject.
                            getJSONArray("videos");

                    for (int k = 0; k < jsonArrayVideos.length(); k++) {

                        JSONObject jsonObjectVideo = jsonArrayVideos.
                                getJSONObject(k);

                        if (jsonObjectVideo.has("video_name")) {

                            VideoModel videoModel = new VideoModel(
                                    jsonObjectVideo.optString("video_name"),
                                    jsonObjectVideo.optString("video_url"),
                                    jsonObjectVideo.optString("video_description"),
                                    jsonObjectVideo.optString("video_taken"));

                            videoModels.add(videoModel);
                        }
                    }
                    activityModel.setVideoModels(videoModels);
                }*/

                if (jsonObject.has("images")) {

                    JSONArray jsonArrayVideos = jsonObject.
                            getJSONArray("images");

                    for (int k = 0; k < jsonArrayVideos.length(); k++) {

                        JSONObject jsonObjectImage = jsonArrayVideos.
                                getJSONObject(k);

                        if (jsonObjectImage.has("image_name")) {

                            ImageModel imageModel = new ImageModel(
                                    jsonObjectImage.optString("image_name"),
                                    jsonObjectImage.optString("image_url"),
                                    jsonObjectImage.optString("image_description"),
                                    jsonObjectImage.optString("image_taken"),
                                    "");
                            imageModels.add(imageModel);
                        }
                    }
                    activityModel.setImageModels(imageModels);
                }

                if (jsonObject.has("feedbacks")) {

                    JSONArray jsonArrayFeedback = jsonObject.getJSONArray("feedbacks");

                    for (int k = 0; k < jsonArrayFeedback.length(); k++) {

                        JSONObject jsonObjectFeedback = jsonArrayFeedback.getJSONObject(k);

                        if (jsonObjectFeedback.has("feedback_message")) {
                            //todo check time
                            FeedBackModel feedBackModel = new FeedBackModel(
                                    jsonObjectFeedback.optString("feedback_message"),
                                    jsonObjectFeedback.optString("feedback_by"),
                                    jsonObjectFeedback.getInt("feedback_rating"),
                                    Utils.writeFormat.format(Utils.convertStringToDate(
                                            jsonObjectFeedback.optString("feedback_time"))),
                                    jsonObjectFeedback.optString("feedback_by_type"));

                            try {
                                feedBackModel.setbFeedBackReport(jsonObjectFeedback.
                                        getBoolean("feedback_report"));
                            } catch (Exception e) {
                                try {
                                    String strTemp = jsonObjectFeedback.
                                            optString("feedback_report");
                                    boolean b = false;
                                    if (strTemp.equalsIgnoreCase("1"))
                                        b = true;

                                    feedBackModel.setbFeedBackReport(b);
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            }

                            feedBackModels.add(feedBackModel);

                          /*  if (jsonObjectFeedback.getString("feedback_by_type").
                                    equalsIgnoreCase("customer")) {
                                if (!Config.customerIds.contains(jsonObjectFeedback.
                                        optString("feedback_by")))
                                    Config.customerIds.add(jsonObjectFeedback.
                                            optString("feedback_by"));
                            }

                            if (jsonObjectFeedback.getString("feedback_by_type").
                                    equalsIgnoreCase("dependent")) {
                                if (!Config.dependentIds.contains(jsonObjectFeedback.
                                        optString("feedback_by")))
                                    Config.dependentIds.add(jsonObjectFeedback.
                                            optString("feedback_by"));
                            }*/
                        }
                    }
                    activityModel.setFeedBackModels(feedBackModels);
                }

                if (jsonObject.has("milestones")) {

                    JSONArray jsonArrayMilestones = jsonObject.
                            getJSONArray("milestones");

                    for (int k = 0; k < jsonArrayMilestones.length(); k++) {

                        JSONObject jsonObjectMilestone =
                                jsonArrayMilestones.getJSONObject(k);

                        MilestoneModel milestoneModel = new MilestoneModel();

                        milestoneModel.setiMilestoneId(jsonObjectMilestone.getInt("id"));
                        milestoneModel.setStrMilestoneStatus(jsonObjectMilestone.
                                optString("status"));
                        milestoneModel.setStrMilestoneName(jsonObjectMilestone.optString("name"));
                        milestoneModel.setStrMilestoneDate(jsonObjectMilestone.optString("date"));

                        if (jsonObjectMilestone.has("files")) {

                            JSONArray jsonArrayMsFiles = jsonObjectMilestone.
                                    getJSONArray("files");

                            for (int m = 0; m < jsonArrayMsFiles.length(); m++) {

                                JSONObject jsonObjectMsFile = jsonArrayMsFiles.
                                        getJSONObject(m);

                                if (jsonObjectMsFile.has("file_name")) {

                                    FileModel fileModel = new FileModel(
                                            jsonObjectMsFile.optString("file_name"),
                                            jsonObjectMsFile.optString("file_url"),
                                            jsonObjectMsFile.optString("file_type"),
                                            jsonObjectMsFile.optString("file_time"),
                                            jsonObjectMsFile.optString("file_desc"),
                                            jsonObjectMsFile.optString("file_path"));

                                    milestoneModel.setFileModel(fileModel);
                                }
                            }
                        }

                        if (jsonObjectMilestone.has("show")) {

                            try {
                                milestoneModel.setVisible(jsonObjectMilestone.getBoolean("show"));
                            } catch (Exception e) {
                                boolean b = true;
                                try {
                                    if (jsonObjectMilestone.getInt("show") == 0)
                                        b = false;
                                    milestoneModel.setVisible(b);
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }

                        if (jsonObjectMilestone.has("reschedule")) {

                            try {
                                milestoneModel.setReschedule(jsonObjectMilestone.
                                        getBoolean("reschedule"));
                            } catch (Exception e) {
                                boolean b = true;
                                try {
                                    if (jsonObjectMilestone.getInt("reschedule") == 0)
                                        b = false;
                                    milestoneModel.setReschedule(b);
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }

                        if (jsonObjectMilestone.has("scheduled_date")) {
                            milestoneModel.setStrMilestoneScheduledDate(jsonObjectMilestone.
                                    optString("scheduled_date"));
                        }

                        if (jsonObjectMilestone.has("fields")) {

                            JSONArray jsonArrayFields = jsonObjectMilestone.
                                    getJSONArray("fields");

                            for (int l = 0; l < jsonArrayFields.length(); l++) {

                                JSONObject jsonObjectField =
                                        jsonArrayFields.getJSONObject(l);

                                FieldModel fieldModel = new FieldModel();

                                fieldModel.setiFieldID(jsonObjectField.getInt("id"));

                                if (jsonObjectField.has("hide")) {

                                    try {
                                        fieldModel.setFieldView(jsonObjectField.getBoolean("hide"));
                                    } catch (Exception e) {
                                        boolean b = true;
                                        try {
                                            if (jsonObjectField.getInt("hide") == 0)
                                                b = false;
                                            fieldModel.setFieldView(b);
                                        } catch (Exception e1) {
                                            e1.printStackTrace();
                                        }
                                    }
                                }

                                if (jsonObjectField.has("required")) {

                                    try {
                                        fieldModel.setFieldRequired(jsonObjectField.
                                                getBoolean("required"));
                                    } catch (Exception e) {
                                        boolean b = true;
                                        try {
                                            if (jsonObjectField.getInt("required") == 0)
                                                b = false;
                                            fieldModel.setFieldRequired(b);
                                        } catch (Exception e1) {
                                            e1.printStackTrace();
                                        }
                                    }
                                }

                                fieldModel.setStrFieldData(jsonObjectField.optString("data"));
                                fieldModel.setStrFieldLabel(jsonObjectField.optString("label"));
                                fieldModel.setStrFieldType(jsonObjectField.optString("type"));

                                if (jsonObjectField.has("values")) {

                                    fieldModel.setStrFieldValues(Utils.jsonToStringArray(
                                            jsonObjectField.getJSONArray("values")));
                                }

                                if (jsonObjectField.has("child")) {

                                    try {
                                        fieldModel.setChild(jsonObjectField.getBoolean("child"));
                                    } catch (Exception e) {
                                        boolean b = true;
                                        try {
                                            if (jsonObjectField.getInt("child") == 0)
                                                b = false;
                                            fieldModel.setChild(b);
                                        } catch (Exception e1) {
                                            e1.printStackTrace();
                                        }
                                    }


                                    if (jsonObjectField.has("child_type"))
                                        fieldModel.setStrChildType(Utils.jsonToStringArray(
                                                jsonObjectField.getJSONArray("child_type")));

                                    if (jsonObjectField.has("child_value"))
                                        fieldModel.setStrChildValue(Utils.jsonToStringArray(
                                                jsonObjectField.getJSONArray("child_value")));

                                    if (jsonObjectField.has("child_condition"))
                                        fieldModel.setStrChildCondition(Utils.jsonToStringArray(
                                                jsonObjectField.getJSONArray("child_condition")));

                                    if (jsonObjectField.has("child_field"))
                                        fieldModel.setiChildfieldID(Utils.jsonToIntArray(
                                                jsonObjectField.getJSONArray("child_field")));
                                }

                                if (jsonObjectField.has("array_fields")) {

                                    try {
                                        fieldModel.setiArrayCount(jsonObjectField.
                                                getInt("array_fields"));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        int i;
                                        try {
                                            i = Integer.parseInt(jsonObjectField.
                                                    optString("array_fields"));
                                            fieldModel.setiArrayCount(i);
                                        } catch (Exception e1) {
                                            e1.printStackTrace();
                                        }
                                    }

                                    if (jsonObjectField.has("array_type"))
                                        fieldModel.setStrArrayType(Utils.jsonToStringArray(
                                                jsonObjectField.getJSONArray("array_type")));

                                    if (jsonObjectField.has("array_data"))
                                        fieldModel.setStrArrayData(jsonObjectField.
                                                optString("array_data"));

                                }

                                milestoneModel.setFieldModel(fieldModel);
                            }
                        }
                        activityModel.setiActivityDisplayFlag(isActivity);
                        activityModel.setMilestoneModel(milestoneModel);
                    }
                }
                Config.activityModels.add(activityModel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void fetchCheckInCareSync(final Context context) {

        String strDate = "";

        final SessionManager sessionManager = new SessionManager(context);

        Cursor cursor = CareGiver.getDbCon().getMaxDate(Config.collectionCheckInCare);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            strDate = cursor.getString(0);
        }

        if (strDate == null || strDate.equalsIgnoreCase(""))
            strDate = DbHelper.DEFAULT_DB_DATE;

        CareGiver.getDbCon().closeCursor(cursor);

        Query q1 = QueryBuilder.build("provider_id", sessionManager.getProviderId(),
                QueryBuilder.Operator.EQUALS);

        Query finalQuery;

        if (!strDate.equalsIgnoreCase("")) {
            Query q12 = QueryBuilder.build("_$updatedAt", strDate, QueryBuilder.Operator.
                    GREATER_THAN);

            finalQuery = QueryBuilder.compoundOperator(q1, QueryBuilder.Operator.AND, q12);
        } else {
            finalQuery = q1;
        }

        StorageService storageService = new StorageService(context);

        storageService.findDocsByQueryOrderBy(Config.collectionCheckInCare, finalQuery, 30000, 0,
                "created_date", 1,
                new App42CallBack() {

                    @Override
                    public void onSuccess(Object o) {
                        if (o != null) {

                            Utils.log(o.toString(), " Check In Care All ");

                            Storage storage = (Storage) o;

                            ArrayList<Storage.JSONDocument> jsonDocList = storage.getJsonDocList();

                            try {
                                CareGiver.getDbCon().beginDBTransaction();
                                for (int i = 0; i < jsonDocList.size(); i++) {

                                    Storage.JSONDocument jsonDocument = jsonDocList.get(i);

                                    String values[] = {jsonDocument.getDocId(),
                                            jsonDocument.getUpdatedAt(),
                                            jsonDocument.getJsonDoc(),
                                            Config.collectionCheckInCare, "", "1",
                                            sessionManager.getProviderId()
                                    };

                                    String selection = DbHelper.COLUMN_OBJECT_ID + " = ? and "
                                            + DbHelper.COLUMN_COLLECTION_NAME + "=? and "
                                            + DbHelper.COLUMN_PROVIDER_ID + "=?";

                                    // WHERE clause arguments
                                    String[] selectionArgs = {jsonDocument.getDocId(),
                                            Config.collectionCheckInCare,
                                            Config.providerModel.getStrProviderId()};

                                    CareGiver.getDbCon().updateInsert(
                                            DbHelper.strTableNameCollection,
                                            selection, values, DbHelper.COLLECTION_FIELDS,
                                            selectionArgs);

                                    try {
                                        JSONObject jsonObject = new JSONObject(jsonDocument.
                                                getJsonDoc());

                                        if (jsonObject.has("customer_id")
                                                && jsonObject.has("month")) {

                                            String values1[] = {jsonDocument.getDocId(),
                                                    "-1",
                                                    "",
                                                    jsonObject.optString("customer_id"),
                                                    jsonObject.optString("provider_id"),
                                                    jsonObject.optString("month"),
                                                    jsonObject.optString("year")
                                            };

                                            String selection1 = DbHelper.COLUMN_OBJECT_ID
                                                    + " = ? and "
                                                    + DbHelper.COLUMN_MILESTONE_ID + "=? ";

                                            String[] selectionArgs1 = {jsonDocument.getDocId(),
                                                    "-1"
                                            };

                                            CareGiver.getDbCon().updateInsert(
                                                    DbHelper.strTableNameMilestone,
                                                    selection1, values1, DbHelper.CCARE_FIELDS,
                                                    selectionArgs1);
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                CareGiver.getDbCon().dbTransactionSuccessFull();
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                CareGiver.getDbCon().endDBTransaction();
                            }
                        }
                    }

                    @Override
                    public void onException(Exception ex) {
                        Utils.log(ex.getMessage(), " Sync Check in care Failure ");
                    }
                });
    }

    public static void fetchServicesSync(Context context) {

        String strDate = "";

        Cursor cursor = CareGiver.getDbCon().getMaxDate(Config.collectionService);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            strDate = cursor.getString(0);
        }

        if (strDate == null || strDate.equalsIgnoreCase(""))
            strDate = DbHelper.DEFAULT_DB_DATE;

        CareGiver.getDbCon().closeCursor(cursor);

        Query q1 = QueryBuilder.build("_$updatedAt", strDate, QueryBuilder.Operator.
                GREATER_THAN);

        final SessionManager sessionManager = new SessionManager(context);

        StorageService storageService = new StorageService(context);

        storageService.findDocsByQueryOrderBy(Config.collectionService, q1, 30000, 0,
                "_$updatedAt", 1,
                new App42CallBack() {
                    @Override
                    public void onSuccess(Object o) {

                        if (o != null) {

                            Utils.log(o.toString(), " Service S");

                            Storage storage = (Storage) o;

                            ArrayList<Storage.JSONDocument> jsonDocList = storage.
                                    getJsonDocList();

                            for (int i = 0; i < jsonDocList.size(); i++) {

                                Storage.JSONDocument jsonDocument = jsonDocList.get(i);

                                try {
                                    String values[] = {jsonDocument.getDocId(),
                                            jsonDocument.getUpdatedAt(),
                                            jsonDocument.getJsonDoc(),
                                            Config.collectionService, "", "1",
                                            sessionManager.getProviderId()
                                    };

                                    String selection = DbHelper.COLUMN_OBJECT_ID + "=? and "
                                            + DbHelper.COLUMN_COLLECTION_NAME + "=?";

                                    // WHERE clause arguments
                                    String[] selectionArgs = {jsonDocument.getDocId(),
                                            Config.collectionService};
                                    CareGiver.getDbCon().updateInsert(
                                            DbHelper.strTableNameCollection,
                                            selection, values, DbHelper.COLLECTION_FIELDS,
                                            selectionArgs);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                    }

                    @Override
                    public void onException(Exception e) {
                        if (e != null)
                            Utils.log(e.getMessage(), " Service");
                    }
                });
    }
    //////////////////////////

    public static void createNotificationModel(String currentDate) {

        Cursor cursor = null;
        try {

            String strDocument;
            int iNew;
            String strDocId;

            // +  " AND updated_date = '"+currentDate+"'"
            // select * from collections  where  collection_name = 'notification' and updated_date >= date('now','-15 day') and updated_date <= date('now') and updated_date like '2017-01-11%'
            cursor = CareGiver.getDbCon().fetch(
                    DbHelper.strTableNameCollection, new String[]{DbHelper.COLUMN_OBJECT_ID, DbHelper.COLUMN_DOCUMENT
                            , DbHelper.COLUMN_NEW_UPDATED},
                    DbHelper.COLUMN_COLLECTION_NAME + "=? AND updated_date >= date('now','-15 day') and updated_date <= date('now')" +
                            " OR updated_date like '" + currentDate + "%'",
                    new String[]{Config.collectionNotification}, DbHelper.COLUMN_UPDATE_DATE
                            + " desc", null, true, null, null);

            if (cursor != null && cursor.getCount() > 0) {
                Config.notificationModels.clear();
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    strDocId = cursor.getString(0);
                    strDocument = cursor.getString(1);
                    iNew = cursor.getInt(2);


                    JSONObject jsonObjectProvider = new JSONObject(strDocument);

                    if (jsonObjectProvider.has(App42GCMService.ExtraMessage)) {

                        NotificationModel notificationModel = new NotificationModel(
                                jsonObjectProvider.getString(App42GCMService.ExtraMessage),
                                jsonObjectProvider.getString("time"),
                                jsonObjectProvider.getString("user_type"),
                                jsonObjectProvider.getString("created_by_type"),
                                jsonObjectProvider.getString("user_id"),
                                jsonObjectProvider.getString("created_by"), "");

                        notificationModel.setiNew(iNew);
                        notificationModel.setStrDocId(strDocId);

                        if (jsonObjectProvider.has("activity_id"))
                            notificationModel.setStrActivityId(jsonObjectProvider.
                                    getString("activity_id"));

                        if (jsonObjectProvider.getString("created_by_type").
                                equalsIgnoreCase("customer")) {
                            if (!Config.customerIdsAdded.contains(jsonObjectProvider.
                                    getString("created_by")))
                                Config.customerIds.add(jsonObjectProvider.getString("created_by"));
                        }

                        if (jsonObjectProvider.getString("created_by_type").
                                equalsIgnoreCase("dependent")) {
                            if (!Config.dependentIdsAdded.contains(jsonObjectProvider.
                                    getString("created_by")))
                                Config.dependentIds.add(jsonObjectProvider.getString("created_by"));
                        }

                        Config.notificationModels.add(notificationModel);
                    }
                    cursor.moveToNext();
                }
            }
            CareGiver.getDbCon().closeCursor(cursor);

            //
            CareGiver.getDbCon().update(
                    DbHelper.strTableNameCollection,
                    DbHelper.COLUMN_COLLECTION_NAME + "=?",
                    new String[]{"0"},
                    new String[]{DbHelper.COLUMN_NEW_UPDATED},
                    new String[]{Config.collectionNotification});
            //

        } catch (JSONException e) {
            e.printStackTrace();
            CareGiver.getDbCon().closeCursor(cursor);
        }
    }

    public static void updateProviderJson(String strProviderId, boolean bWhich) {

        try {
            Cursor cursor = CareGiver.getDbCon().fetch(
                    DbHelper.strTableNameCollection, new String[]{DbHelper.COLUMN_DOCUMENT},
                    DbHelper.COLUMN_COLLECTION_NAME + "=? and " + DbHelper.COLUMN_OBJECT_ID + "=?",
                    new String[]{Config.collectionProvider, strProviderId}, null, "0, 1", true,
                    null, null
            );

            String strDocument = "";

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                strDocument = cursor.getString(0);
            }
            CareGiver.getDbCon().closeCursor(cursor);

            if (!strDocument.equalsIgnoreCase("")) {

                JSONObject jsonObject = new JSONObject(strDocument);

                if (jsonObject.has("provider_email")) {

                    if (bWhich) {
                        jsonObject.put("provider_contact_no", Config.providerModel.getStrContacts());
                        jsonObject.put("provider_address", Config.providerModel.getStrAddress());
                        jsonObject.put("provider_name", Config.providerModel.getStrName());
                    } else {
                        jsonObject.put("provider_profile_url", Config.providerModel.getStrImgUrl());
                    }

                    CareGiver.getDbCon().updateProvider(
                            new String[]{"", jsonObject.toString(), "3"},
                            new String[]{"updated_date", "document", "new_updated"},
                            new String[]{Config.providerModel.getStrProviderId(),
                                    Config.collectionProvider});
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //offline update
    public static void checkImage(final Context context, final SessionManager sessionManager) {

        try {

            UploadService uploadService = new UploadService(context);

            Utils.log(" 1 ", " Check Image ");

            uploadService.removeImageByUserService(Config.strCustomerImageName,
                    sessionManager.getEmail(),
                    new App42CallBack() {
                        public void onSuccess(Object response) {
                            Utils.log(" 1- ", " Check Image ");
                            if (response != null)
                                // Utils.log(response.toString(), " Check Image ");
                                uploadImage(context, sessionManager);
                        }

                        @Override
                        public void onException(Exception e) {
                            Utils.log(" 2- ", " Check Image ");
                            if (e != null) {
                                Utils.log(e.getMessage(), " Check Image ");
                               /* App42Exception exception = (App42Exception)e;
                                int appErrorCode = exception.getAppErrorCode();
                                if(appErrorCode == 2102){
                                    // Handle here for Not found (Files for the User 'Nick' does not Exist )
                                }
                                else if(appErrorCode == 1401){
                                    // handle here for Client is not authorized
                                }
                                else if(appErrorCode == 1500){
                                    // handle here for Internal Server Error
                                }*/
                                uploadImage(context, sessionManager);
                            }

                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void uploadImage(final Context context, final SessionManager sessionManager) {

        try {
            if (Utils.isConnectingToInternet(context)) {

                UploadService uploadService = new UploadService(context);

                Utils.log(" 2 ", " Upload Image ");

                uploadService.uploadImageCommon(sessionManager.getProfileImage(),
                        Config.strCustomerImageName,
                        "Profile Picture", sessionManager.getEmail(),
                        UploadFileType.IMAGE, new App42CallBack() {

                            public void onSuccess(Object response) {

                                Utils.log(" 2- ", " Upload Image ");

                                if (response != null) {
                                    //Utils.log(response.toString(), " Upload Image ");

                                    Upload upload = (Upload) response;
                                    ArrayList<Upload.File> fileList = upload.getFileList();

                                    if (fileList.size() > 0) {
                                        createProviderModel(sessionManager.getProviderId());
                                        Config.providerModel.setStrImgUrl(fileList.get(0).getUrl());
                                        updateProviderJson(sessionManager.getProviderId(), false);
                                    }
                                }
                            }

                            @Override
                            public void onException(Exception e) {
                                Utils.log(" -2 ", " Upload Image ");
                                if (e != null)
                                    Utils.log(e.getMessage(), " Upload Image ");
                            }
                        });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateAllDocs(Context context, final SessionManager sessionManager) {

        Cursor cursor = null;

        try {

            cursor = CareGiver.getDbCon().fetch(
                    DbHelper.strTableNameCollection, new String[]{DbHelper.COLUMN_DOCUMENT,
                            DbHelper.COLUMN_COLLECTION_NAME, DbHelper.COLUMN_OBJECT_ID},
                    "(" + DbHelper.COLUMN_PROVIDER_ID + "=? OR " + DbHelper.COLUMN_OBJECT_ID
                            + "=?)", // AND " + DbHelper.COLUMN_NEW_UPDATED + "=?
                    new String[]{sessionManager.getProviderId(), sessionManager.getProviderId()},
                    //, "3"
                    null, null, true, null, null
            );

            Utils.log(String.valueOf(cursor.getCount()), " Count ");

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                JSONObject jsonObject;

                while (!cursor.isAfterLast()) {

                    if (!cursor.getString(0).equalsIgnoreCase("")
                            && !cursor.getString(1).equalsIgnoreCase("")
                            && !cursor.getString(2).equalsIgnoreCase("")) {

                        Utils.log(cursor.getString(1)
                                + " ~ " + cursor.getString(2), " Docs ");

                        jsonObject = new JSONObject(cursor.getString(0));

                        if (Utils.isConnectingToInternet(context)) {

                            final String strCollectionName = cursor.getString(1);
                            final String strObjectId = cursor.getString(2);

                            Utils.log(" 1 ", " Call Docs ");

                            StorageService storageService = new StorageService(context);

                            storageService.updateDocsService(jsonObject, strObjectId,
                                    strCollectionName,
                                    new App42CallBack() {
                                        @Override
                                        public void onSuccess(Object o) {

                                            if (o != null)
                                                Utils.log(o.toString(), " Update Docs ");

                                            String selection = DbHelper.COLUMN_OBJECT_ID + "=? and "
                                                    + DbHelper.COLUMN_COLLECTION_NAME + "=? ";

                                            String[] selectionArgs;

                                            if (!strCollectionName.equalsIgnoreCase(
                                                    Config.collectionProvider)) {
                                                selection += "and " + DbHelper.COLUMN_PROVIDER_ID
                                                        + "=?";

                                                // WHERE clause arguments
                                                selectionArgs = new String[]{strObjectId,
                                                        strCollectionName,
                                                        sessionManager.getProviderId()};
                                            } else {
                                                selectionArgs = new String[]{strObjectId,
                                                        strCollectionName};

                                                sessionManager.saveProfileImage("N");
                                            }

                                            CareGiver.getDbCon().update(
                                                    DbHelper.strTableNameCollection,
                                                    selection, new String[]{"5"},
                                                    new String[]{DbHelper.COLUMN_NEW_UPDATED},
                                                    selectionArgs);
                                        }

                                        @Override
                                        public void onException(Exception e) {
                                            if (e != null)
                                                Utils.log(e.getMessage(), " Update Docs ");
                                        }
                                    });
                        }
                    }

                    cursor.moveToNext();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CareGiver.getDbCon().closeCursor(cursor);
        }
    }

    public void createCustomerModel() {

        Config.strCustomerNames.clear();
        Config.customerIdsCopy.clear();
        Config.clientModels.clear();
        Config.clientNameModels.clear();
        Config.customerModels.clear();
        Config.customerIdsAdded.clear();

        Cursor newCursor = CareGiver.getDbCon().fetch(
                DbHelper.strTableNameCollection,
                new String[]{DbHelper.COLUMN_OBJECT_ID, DbHelper.COLUMN_DOCUMENT,
                        DbHelper.COLUMN_CLIENT_FLAG},
                DbHelper.COLUMN_COLLECTION_NAME
                        + "=?",
                new String[]{Config.collectionCustomer},
                "user_name ASC", null, true,
                null, null
        );

        if (newCursor.getCount() > 0) {

            newCursor.moveToFirst();

            try {

                int iRemoved;

                while (!newCursor.isAfterLast()) {

                    if (newCursor.getString(1) != null
                            && !newCursor.getString(1).equalsIgnoreCase("")) {

                        if (!newCursor.isNull(2))
                            iRemoved = newCursor.getInt(2);
                        else
                            iRemoved = -1;

                        //Utils.log(" Removed ", String.valueOf(iRemoved));

                        JSONObject jsonObject = new JSONObject(newCursor.getString(1));

                        if (jsonObject.has("customer_name")) {

                            CustomerModel customerModel = new CustomerModel(
                                    jsonObject.optString("customer_name"),
                                    jsonObject.optString("paytm_account"),
                                    jsonObject.optString("customer_profile_url"), "",
                                    jsonObject.optString("customer_address"),
                                    jsonObject.optString("customer_city"),
                                    jsonObject.optString("customer_state"),
                                    jsonObject.optString("customer_contact_no"),
                                    jsonObject.optString("customer_email"),
                                    jsonObject.optString("customer_dob"),
                                    jsonObject.optString("customer_country"),
                                    jsonObject.optString("customer_country_code"),
                                    jsonObject.optString("customer_area_code"),
                                    jsonObject.optString("customer_contact_no"),
                                    newCursor.getString(0));


                            if (iRemoved == 0) {

                                Config.strCustomerNames.add(jsonObject.getString("customer_name"));

                                Config.customerIdsCopy.add(newCursor.getString(0));

                                ClientModel clientModel = new ClientModel();
                                clientModel.setCustomerModel(customerModel);
                                Config.clientModels.add(clientModel);

                                ClientNameModel clientNameModel = new ClientNameModel();
                                clientNameModel.setStrCustomerName(jsonObject.
                                        getString("customer_name"));

                                Config.clientNameModels.add(clientNameModel);

                                Config.customerModels.add(customerModel);
                                Config.customerIdsAdded.add(newCursor.getString(0));
                            }
                        }
                    }
                    newCursor.moveToNext();
                }

            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        CareGiver.getDbCon().closeCursor(newCursor);
        createDependentModel();
    }

    private void createDependentModel() {

        Config.dependentModels.clear();
        Config.strDependentNames.clear();
        Config.dependentIdsAdded.clear();

        Cursor newCursor = CareGiver.getDbCon().fetch(
                DbHelper.strTableNameCollection,
                new String[]{
                        DbHelper.COLUMN_OBJECT_ID, DbHelper.COLUMN_DOCUMENT,
                        DbHelper.COLUMN_CLIENT_FLAG
                },
                DbHelper.COLUMN_COLLECTION_NAME + "=?", new String[]{Config.collectionDependent},
                "user_name ASC", null, true, null, null
        );

        if (newCursor.getCount() > 0) {

            newCursor.moveToFirst();

            int iRemoved;

            try {

                while (!newCursor.isAfterLast()) {

                    if (!newCursor.isNull(2))
                        iRemoved = newCursor.getInt(2);
                    else
                        iRemoved = -1;

                    //Utils.log(" Removed 0 ", String.valueOf(iRemoved));

                    if (newCursor.getString(1) != null && !newCursor.getString(1).
                            equalsIgnoreCase("")) {
                        JSONObject jsonObjectDependent = new JSONObject(newCursor.getString(1));

                        DependentModel dependentModel = new DependentModel(
                                jsonObjectDependent.optString("dependent_name"),
                                jsonObjectDependent.optString("dependent_relation"),
                                jsonObjectDependent.optString("dependent_notes"),
                                jsonObjectDependent.optString("dependent_address"),
                                jsonObjectDependent.optString("dependent_contact_no"),
                                jsonObjectDependent.optString("dependent_email"),
                                jsonObjectDependent.optString("dependent_illness"),
                                "",
                                "",
                                newCursor.getString(0),
                                jsonObjectDependent.optString("customer_id"));

                        if (jsonObjectDependent.has("dependent_profile_url"))
                            dependentModel.setStrImageUrl(jsonObjectDependent.
                                    optString("dependent_profile_url"));

                        dependentModel.setStrDob(jsonObjectDependent.optString("dependent_dob"));


                        if (jsonObjectDependent.has("dependent_age")) {

                            try {
                                dependentModel.setIntAge(jsonObjectDependent.
                                        getInt("dependent_age"));
                            } catch (Exception e) {
                                int iAge = 0;
                                try {
                                    String strAge = jsonObjectDependent.optString("dependent_age");

                                    if (!strAge.equalsIgnoreCase(""))
                                        iAge = Integer.parseInt(strAge);

                                    dependentModel.setIntAge(iAge);
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                    // iAge = 0;
                                }
                            }
                        }

                        if (jsonObjectDependent.has("health_bp")) {

                            try {
                                dependentModel.setIntHealthBp(jsonObjectDependent.
                                        getInt("health_bp"));
                            } catch (Exception e) {
                                try {
                                    String strBp = jsonObjectDependent.optString("health_bp");

                                    int iBp = 0;
                                    if (!strBp.equalsIgnoreCase(""))
                                        iBp = Integer.parseInt(strBp);

                                    dependentModel.setIntHealthBp(iBp);
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }

                        if (jsonObjectDependent.has("health_heart_rate")) {

                            try {
                                dependentModel.setIntHealthHeartRate(jsonObjectDependent.
                                        getInt("health_heart_rate"));
                            } catch (Exception e) {
                                try {

                                    String strPulse = jsonObjectDependent.
                                            optString("health_heart_rate");

                                    int iPulse = 0;
                                    if (!strPulse.equalsIgnoreCase(""))
                                        iPulse = Integer.parseInt(strPulse);

                                    dependentModel.setIntHealthHeartRate(iPulse);
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }

                        if (iRemoved == 0) {

                            Config.dependentModels.add(dependentModel);
                            Config.dependentIdsAdded.add(newCursor.getString(0));

                            if (iRemoved == 0) {
                                Config.strDependentNames.add(jsonObjectDependent.
                                        optString("dependent_name"));

                                int iPosition = Config.customerIdsCopy.indexOf(jsonObjectDependent.
                                        optString("customer_id"));

                                if (Config.clientModels.size() > 0) {
                                    if (iPosition > -1 && iPosition < Config.clientModels.size())
                                        Config.clientModels.get(iPosition).setDependentModel(
                                                dependentModel);
                                }

                                if (Config.clientNameModels.size() > 0) {
                                    if (iPosition > -1 && iPosition < Config.clientNameModels.
                                            size()) {
                                        Config.clientNameModels.get(iPosition).
                                                removeStrDependentName(
                                                        jsonObjectDependent.optString("dependent_name"));
                                        Config.clientNameModels.get(iPosition).setStrDependentName(
                                                jsonObjectDependent.optString("dependent_name"));
                                    }
                                }
                            }
                        }
                    }

                    newCursor.moveToNext();
                }

            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        CareGiver.getDbCon().closeCursor(newCursor);
    }

    public void fetchActivities(final String strProviderId, final Context context,
                                final RelativeLayout loadingPanel) {

        Query q1 = QueryBuilder.build("provider_id", strProviderId,
                QueryBuilder.Operator.EQUALS);

        Query q2 = QueryBuilder.build("activity_date", DashboardFragment.strEndDate, QueryBuilder.
                Operator.LESS_THAN_EQUALTO);

        Query q3 = QueryBuilder.build("activity_date", DashboardFragment.strStartDate, QueryBuilder.
                Operator.GREATER_THAN_EQUALTO);

        Query q4 = QueryBuilder.compoundOperator(q2, QueryBuilder.Operator.AND, q3);

        Query q7 = QueryBuilder.build("milestones.scheduled_date", DashboardFragment.strEndDate,
                QueryBuilder.Operator.LESS_THAN_EQUALTO);

        Query q8 = QueryBuilder.build("milestones.scheduled_date", DashboardFragment.strStartDate,
                QueryBuilder.Operator.GREATER_THAN_EQUALTO);

        Query q10 = QueryBuilder.build("milestones.status", Config.MilestoneStatus.COMPLETED,
                QueryBuilder.Operator.NOT_EQUALS);

        Query q9 = QueryBuilder.compoundOperator(q7, QueryBuilder.Operator.AND, q8);

        Query q11 = QueryBuilder.compoundOperator(q9, QueryBuilder.Operator.AND, q10);

        Query q5 = QueryBuilder.compoundOperator(q4, QueryBuilder.Operator.OR, q11);

        Query q6 = QueryBuilder.compoundOperator(q1, QueryBuilder.Operator.AND, q5);


        storageService.findDocsByQueryOrderBy(Config.collectionActivity, q6, 30000, 0,
                "milestones.scheduled_date", 1,
                new App42CallBack() {

                    @Override
                    public void onSuccess(Object o) {
                        if (o != null) {

                            Storage storage = (Storage) o;

                            ArrayList<Storage.JSONDocument> jsonDocList = storage.getJsonDocList();

                            try {
                                CareGiver.getDbCon().beginDBTransaction();
                                for (int i = 0; i < jsonDocList.size(); i++) {

                                    Storage.JSONDocument jsonDocument = jsonDocList.get(i);

                                    String values[] = {jsonDocument.getDocId(),
                                            jsonDocument.getUpdatedAt(),
                                            jsonDocument.getJsonDoc(), Config.collectionActivity,
                                            "", "1", strProviderId};

                                    String selection = DbHelper.COLUMN_OBJECT_ID + " = ? and "
                                            + DbHelper.COLUMN_COLLECTION_NAME + "=?";

                                    // WHERE clause arguments
                                    String[] selectionArgs = {jsonDocument.getDocId(),
                                            Config.collectionActivity};
                                    CareGiver.getDbCon().updateInsert(
                                            DbHelper.strTableNameCollection,
                                            selection, values, DbHelper.COLLECTION_FIELDS,
                                            selectionArgs);

                                    insertActivityDate(jsonDocument.getDocId(),
                                            jsonDocument.getJsonDoc());
                                }
                                CareGiver.getDbCon().dbTransactionSuccessFull();
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                CareGiver.getDbCon().endDBTransaction();
                            }
                        }
                        //fetchCustomers(1);
                        //DashboardActivity.reloadActivities();
                        DashboardActivity.refreshClientsData(context, loadingPanel);
                    }

                    @Override
                    public void onException(Exception ex) {
                        Utils.log(ex.getMessage(), " f1 ");
                        //fetchCustomers(1);
                        //DashboardActivity.reloadActivities();
                        DashboardActivity.refreshClientsData(context, loadingPanel);
                    }
                });
    }

    public void createFeedbackModel() {

        try {

            Config.iRatings = 0;
            Config.feedBackModels.clear();
            Config.iRatingCount = 0;

            Cursor cursor = CareGiver.getDbCon().fetch(
                    DbHelper.strTableNameCollection,
                    new String[]{DbHelper.COLUMN_DOCUMENT, DbHelper.COLUMN_OBJECT_ID},
                    DbHelper.COLUMN_COLLECTION_NAME + "=?",
                    new String[]{Config.collectionActivity},
                    DbHelper.COLUMN_UPDATE_DATE + " desc",
                    null, true, null, null);

            if (cursor.getCount() > 0) {

                cursor.moveToFirst();

                while (!cursor.isAfterLast()) {

                    JSONObject jsonObject = new JSONObject(cursor.getString(0));

                    if (jsonObject.has("dependent_id")) {

                        if (jsonObject.has("feedbacks")) {

                            JSONArray jsonArrayFeedback = jsonObject.getJSONArray("feedbacks");

                            for (int k = 0; k < jsonArrayFeedback.length(); k++) {

                                JSONObject jsonObjectFeedback = jsonArrayFeedback.getJSONObject(k);

                                if (jsonObjectFeedback.has("feedback_message")) {

                                    FeedBackModel feedBackModel = new FeedBackModel(
                                            jsonObjectFeedback.optString("feedback_message"),
                                            jsonObjectFeedback.optString("feedback_by"),
                                            jsonObjectFeedback.getInt("feedback_rating"),
                                            Utils.writeFormat.format(Utils.convertStringToDate(
                                                    jsonObjectFeedback.optString("feedback_time"))),
                                            jsonObjectFeedback.optString("feedback_by_type"));

                                    feedBackModel.setStrActivityName(
                                            jsonObject.optString("activity_name"));
                                    feedBackModel.setStrActivityDate(
                                            Utils.formatDate(jsonObject.optString("activity_date")));
                                    feedBackModel.setStrDependentId(
                                            jsonObject.optString("dependent_id"));

                                    feedBackModel.setStrActivityId(cursor.getString(1));

                                    try {
                                        feedBackModel.setbFeedBackReport(jsonObjectFeedback.
                                                getBoolean("feedback_report"));
                                    } catch (Exception e) {
                                        try {
                                            String strTemp = jsonObjectFeedback.
                                                    optString("feedback_report");
                                            boolean b = false;
                                            if (strTemp.equalsIgnoreCase("1"))
                                                b = true;

                                            feedBackModel.setbFeedBackReport(b);
                                        } catch (Exception e1) {
                                            e1.printStackTrace();
                                        }
                                    }

                                    //feedBackModels.add(feedBackModel);

                                    if (jsonObjectFeedback.getString("feedback_by_type").
                                            equalsIgnoreCase("customer")) {
                                        if (!Config.customerIds.contains(jsonObjectFeedback.
                                                optString("feedback_by")))
                                            Config.customerIds.add(jsonObjectFeedback.
                                                    optString("feedback_by"));
                                    }

                                    if (jsonObjectFeedback.getString("feedback_by_type").
                                            equalsIgnoreCase("dependent")) {
                                        if (!Config.dependentIds.contains(jsonObjectFeedback.
                                                optString("feedback_by")))
                                            Config.dependentIds.add(jsonObjectFeedback.
                                                    optString("feedback_by"));
                                    }

                                    Config.iRatings += jsonObjectFeedback.getInt("feedback_rating");

                                    Config.iRatingCount += 1;

                                    Config.feedBackModels.add(feedBackModel);
                                }
                            }

                        }
                    }
                    cursor.moveToNext();
                }
            }
            CareGiver.getDbCon().closeCursor(cursor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ActivityModel createActivityModelNotification(JSONObject jsonObject,
                                                         String strDocumentId) {

        ActivityModel activityModel = null;

        try {

            if (jsonObject.has("dependent_id")) {

                activityModel = new ActivityModel();

                activityModel.setStrActivityName(jsonObject.optString("activity_name"));
                activityModel.setStrActivityID(strDocumentId);
                activityModel.setStrProviderID(jsonObject.optString("provider_id"));
                activityModel.setStrDependentID(jsonObject.optString("dependent_id"));
                activityModel.setStrCustomerID(jsonObject.optString("customer_id"));
                activityModel.setStrActivityStatus(jsonObject.optString("status"));
                activityModel.setStrActivityDesc(jsonObject.optString("activity_desc"));

                activityModel.setStrCreatedBy(jsonObject.optString("created_by"));

                activityModel.setStrServcieID(jsonObject.optString("service_id"));
                activityModel.setStrServiceName(jsonObject.optString("service_name"));
                activityModel.setmServiceNo(jsonObject.optInt("service_no"));

                if (jsonObject.has("activity_date")) {
                    activityModel.setStrActivityDate(jsonObject.getString("activity_date"));
                }

                activityModel.setStrActivityDoneDate(jsonObject.
                        optString("activity_done_date"));

                activityModel.setStrActivityProviderStatus(jsonObject.
                        optString("provider_status"));

                activityModel.setStrActivityProviderMessage(jsonObject.
                        optString("provider_message"));

                ArrayList<FeedBackModel> feedBackModels = new ArrayList<>();
                //ArrayList<VideoModel> videoModels = new ArrayList<>();
                ArrayList<ImageModel> imageModels = new ArrayList<>();

                /*if (jsonObject.has("videos")) {

                    JSONArray jsonArrayVideos = jsonObject.
                            getJSONArray("videos");

                    for (int k = 0; k < jsonArrayVideos.length(); k++) {

                        JSONObject jsonObjectVideo = jsonArrayVideos.
                                getJSONObject(k);

                        if (jsonObjectVideo.has("video_name")) {

                            VideoModel videoModel = new VideoModel(
                                    jsonObjectVideo.optString("video_name"),
                                    jsonObjectVideo.optString("video_url"),
                                    jsonObjectVideo.optString("video_description"),
                                    jsonObjectVideo.optString("video_taken"));

                            videoModels.add(videoModel);
                        }
                    }
                    activityModel.setVideoModels(videoModels);
                }*/

                if (jsonObject.has("images")) {

                    JSONArray jsonArrayVideos = jsonObject.
                            getJSONArray("images");

                    for (int k = 0; k < jsonArrayVideos.length(); k++) {

                        JSONObject jsonObjectImage = jsonArrayVideos.
                                getJSONObject(k);

                        if (jsonObjectImage.has("image_name")) {

                            ImageModel imageModel = new ImageModel(
                                    jsonObjectImage.optString("image_name"),
                                    jsonObjectImage.optString("image_url"),
                                    jsonObjectImage.optString("image_description"),
                                    jsonObjectImage.optString("image_taken"),
                                    "");
                            imageModels.add(imageModel);
                        }
                    }
                    activityModel.setImageModels(imageModels);
                }

                if (jsonObject.has("feedbacks")) {

                    JSONArray jsonArrayFeedback = jsonObject.getJSONArray("feedbacks");

                    for (int k = 0; k < jsonArrayFeedback.length(); k++) {

                        JSONObject jsonObjectFeedback = jsonArrayFeedback.getJSONObject(k);

                        if (jsonObjectFeedback.has("feedback_message")) {

                            FeedBackModel feedBackModel = new FeedBackModel(
                                    jsonObjectFeedback.optString("feedback_message"),
                                    jsonObjectFeedback.optString("feedback_by"),
                                    jsonObjectFeedback.getInt("feedback_rating"),
                                    Utils.writeFormat.format(Utils.convertStringToDate(
                                            jsonObjectFeedback.optString("feedback_time"))),
                                    jsonObjectFeedback.optString("feedback_by_type"));

                            try {
                                feedBackModel.setbFeedBackReport(jsonObjectFeedback.
                                        getBoolean("feedback_report"));
                            } catch (Exception e) {
                                try {
                                    String strTemp = jsonObjectFeedback.
                                            optString("feedback_report");
                                    boolean b = false;
                                    if (strTemp.equalsIgnoreCase("1"))
                                        b = true;

                                    feedBackModel.setbFeedBackReport(b);
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            }

                            feedBackModels.add(feedBackModel);

                            /*if (jsonObjectFeedback.getString("feedback_by_type").
                                    equalsIgnoreCase("customer")) {
                                if (!Config.customerIds.contains(jsonObjectFeedback.
                                        optString("feedback_by")))
                                    Config.customerIds.add(jsonObjectFeedback.
                                            optString("feedback_by"));
                            }

                            if (jsonObjectFeedback.getString("feedback_by_type").
                                    equalsIgnoreCase("dependent")) {
                                if (!Config.dependentIds.contains(jsonObjectFeedback.
                                        optString("feedback_by")))
                                    Config.dependentIds.add(jsonObjectFeedback.
                                            optString("feedback_by"));
                            }
*/
                           /* Config.iRatings += jsonObjectFeedback.getInt("feedback_rating");

                            Config.iRatingCount += 1;

                            Config.feedBackModels.add(feedBackModel);
*/
                        }
                    }
                    activityModel.setFeedBackModels(feedBackModels);
                }

                if (jsonObject.has("milestones")) {

                    JSONArray jsonArrayMilestones = jsonObject.
                            getJSONArray("milestones");

                    for (int k = 0; k < jsonArrayMilestones.length(); k++) {

                        JSONObject jsonObjectMilestone =
                                jsonArrayMilestones.getJSONObject(k);

                        MilestoneModel milestoneModel = new MilestoneModel();

                        milestoneModel.setiMilestoneId(jsonObjectMilestone.getInt("id"));
                        milestoneModel.setStrMilestoneStatus(jsonObjectMilestone.
                                optString("status"));
                        milestoneModel.setStrMilestoneName(jsonObjectMilestone.optString("name"));
                        milestoneModel.setStrMilestoneDate(jsonObjectMilestone.optString("date"));

                        if (jsonObjectMilestone.has("files")) {

                            JSONArray jsonArrayMsFiles = jsonObjectMilestone.
                                    getJSONArray("files");

                            for (int m = 0; m < jsonArrayMsFiles.length(); m++) {

                                JSONObject jsonObjectMsFile = jsonArrayMsFiles.
                                        getJSONObject(m);

                                if (jsonObjectMsFile.has("file_name")) {

                                    FileModel fileModel = new FileModel(
                                            jsonObjectMsFile.optString("file_name"),
                                            jsonObjectMsFile.optString("file_url"),
                                            jsonObjectMsFile.optString("file_type"),
                                            jsonObjectMsFile.optString("file_time"),
                                            jsonObjectMsFile.optString("file_desc"),
                                            jsonObjectMsFile.optString("file_path"));

                                    milestoneModel.setFileModel(fileModel);
                                }
                            }
                        }

                        if (jsonObjectMilestone.has("show")) {

                            try {
                                milestoneModel.setVisible(jsonObjectMilestone.getBoolean("show"));
                            } catch (Exception e) {
                                boolean b = true;
                                try {
                                    if (jsonObjectMilestone.getInt("show") == 0)
                                        b = false;
                                    milestoneModel.setVisible(b);
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }

                        if (jsonObjectMilestone.has("reschedule")) {

                            try {
                                milestoneModel.setReschedule(jsonObjectMilestone.
                                        getBoolean("reschedule"));
                            } catch (Exception e) {
                                boolean b = true;
                                try {
                                    if (jsonObjectMilestone.getInt("reschedule") == 0)
                                        b = false;
                                    milestoneModel.setReschedule(b);
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }

                        if (jsonObjectMilestone.has("scheduled_date")) {
                            milestoneModel.setStrMilestoneScheduledDate(jsonObjectMilestone.
                                    optString("scheduled_date"));
                        }

                        if (jsonObjectMilestone.has("fields")) {

                            JSONArray jsonArrayFields = jsonObjectMilestone.
                                    getJSONArray("fields");

                            for (int l = 0; l < jsonArrayFields.length(); l++) {

                                JSONObject jsonObjectField =
                                        jsonArrayFields.getJSONObject(l);

                                FieldModel fieldModel = new FieldModel();

                                fieldModel.setiFieldID(jsonObjectField.getInt("id"));

                                if (jsonObjectField.has("hide")) {

                                    try {
                                        fieldModel.setFieldView(jsonObjectField.getBoolean("hide"));
                                    } catch (Exception e) {
                                        boolean b = true;
                                        try {
                                            if (jsonObjectField.getInt("hide") == 0)
                                                b = false;
                                            fieldModel.setFieldView(b);
                                        } catch (Exception e1) {
                                            e1.printStackTrace();
                                        }
                                    }
                                }

                                if (jsonObjectField.has("required")) {

                                    try {
                                        fieldModel.setFieldRequired(jsonObjectField.
                                                getBoolean("required"));
                                    } catch (Exception e) {
                                        boolean b = true;
                                        try {
                                            if (jsonObjectField.getInt("required") == 0)
                                                b = false;
                                            fieldModel.setFieldRequired(b);
                                        } catch (Exception e1) {
                                            e1.printStackTrace();
                                        }
                                    }
                                }

                                fieldModel.setStrFieldData(jsonObjectField.optString("data"));
                                fieldModel.setStrFieldLabel(jsonObjectField.optString("label"));
                                fieldModel.setStrFieldType(jsonObjectField.optString("type"));

                                if (jsonObjectField.has("values")) {

                                    fieldModel.setStrFieldValues(Utils.jsonToStringArray(
                                            jsonObjectField.getJSONArray("values")));
                                }

                                if (jsonObjectField.has("child")) {

                                    try {
                                        fieldModel.setChild(jsonObjectField.getBoolean("child"));
                                    } catch (Exception e) {
                                        boolean b = true;
                                        try {
                                            if (jsonObjectField.getInt("child") == 0)
                                                b = false;
                                            fieldModel.setChild(b);
                                        } catch (Exception e1) {
                                            e1.printStackTrace();
                                        }
                                    }


                                    if (jsonObjectField.has("child_type"))
                                        fieldModel.setStrChildType(Utils.jsonToStringArray(
                                                jsonObjectField.getJSONArray("child_type")));

                                    if (jsonObjectField.has("child_value"))
                                        fieldModel.setStrChildValue(Utils.jsonToStringArray(
                                                jsonObjectField.getJSONArray("child_value")));

                                    if (jsonObjectField.has("child_condition"))
                                        fieldModel.setStrChildCondition(Utils.jsonToStringArray(
                                                jsonObjectField.getJSONArray("child_condition")));

                                    if (jsonObjectField.has("child_field"))
                                        fieldModel.setiChildfieldID(Utils.jsonToIntArray(
                                                jsonObjectField.getJSONArray("child_field")));
                                }

                                if (jsonObjectField.has("array_fields")) {

                                    try {
                                        fieldModel.setiArrayCount(jsonObjectField.
                                                getInt("array_fields"));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        int i;
                                        try {
                                            i = Integer.parseInt(jsonObjectField.
                                                    optString("array_fields"));
                                            fieldModel.setiArrayCount(i);
                                        } catch (Exception e1) {
                                            e1.printStackTrace();
                                        }
                                    }

                                    if (jsonObjectField.has("array_type"))
                                        fieldModel.setStrArrayType(Utils.jsonToStringArray(
                                                jsonObjectField.getJSONArray("array_type")));

                                    if (jsonObjectField.has("array_data"))
                                        fieldModel.setStrArrayData(jsonObjectField.
                                                optString("array_data"));

                                }

                                milestoneModel.setFieldModel(fieldModel);
                            }
                        }

                        //activityModel.setiActivityDisplayFlag(isActivity);
                        activityModel.setMilestoneModel(milestoneModel);
                    }
                }
                //Config.activityModels.add(activityModel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return activityModel;
    }

    public void createCheckInCareModel(String strDocumentId, String strDocument) {

        try {
            JSONObject jsonObjectCheck = new JSONObject(strDocument);


            CheckInCareModel checkInCareModel = new CheckInCareModel();
            //
            checkInCareModel.setStrName(jsonObjectCheck.optString("check_in_care_name"));
            checkInCareModel.setStrDate(jsonObjectCheck.optString("date"));
            checkInCareModel.setStrDocumentID(strDocumentId);
            checkInCareModel.setStrCreatedDate(jsonObjectCheck.optString("created_date"));
            checkInCareModel.setStrMediaComment(jsonObjectCheck.optString("media_comment"));
            checkInCareModel.setStrProviderID(jsonObjectCheck.optString("provider_id"));
            checkInCareModel.setStrDependentID(jsonObjectCheck.optString("dependent_id"));
            checkInCareModel.setStrCurrentDate(jsonObjectCheck.optString("current_date"));
            checkInCareModel.setStrMonth(jsonObjectCheck.optString("month"));
            checkInCareModel.setStrYear(jsonObjectCheck.optString("year"));
            checkInCareModel.setStrCustomerID(jsonObjectCheck.optString("customer_id"));
            checkInCareModel.setStrCreatedActualDate(jsonObjectCheck.
                    optString("created_date_actual"));

            JSONArray subMainactivities = jsonObjectCheck.optJSONArray("activities");
            JSONArray picture = jsonObjectCheck.optJSONArray("picture");

            try {
                if (picture != null && picture.length() > 0) {

                    for (int m = 0; m < picture.length(); m++) {
                        JSONObject jsonObject = picture.getJSONObject(m);


                        if (jsonObject != null && jsonObject.length() > 0) {

                            PictureModel pictureModel = new PictureModel();

                            pictureModel.setStrStatus(jsonObject.getString("status"));
                            pictureModel.setStrRoomName(jsonObject.getString("room_name"));
                            ArrayList<ImageModel> imageModels = new ArrayList<>();

                            JSONArray imageDetails = jsonObject.optJSONArray("pictures_details");

                            for (int k = 0; k < imageDetails.length(); k++) {

                                JSONObject jsonObjectImage = imageDetails.getJSONObject(k);

                                if (jsonObjectImage.has("image_url")) {

                                    ImageModel imageModel = new ImageModel(jsonObjectImage.
                                            optString("image_url"),
                                            jsonObjectImage.optString("description"),
                                            jsonObjectImage.optString("date_time"));
                                    imageModels.add(imageModel);
                                }
                            }

                            pictureModel.setImageModels(imageModels);

                            checkInCareModel.setPictureModel(pictureModel);

                            Config.roomtypeName.add(pictureModel);

                        }

                    }

                }


            } catch (Exception e) {
                e.printStackTrace();
            }

            ArrayList<CheckInCareActivityModel> checkInCareActivityModels = new ArrayList<CheckInCareActivityModel>();

            try {
                if (subMainactivities != null && subMainactivities.length() > 0) {
                    for (int i = 0; i < subMainactivities.length(); i++) {


                        JSONObject jsonObjectsubactivitites = subMainactivities.getJSONObject(i);
                        jsonObjectsubactivitites.optString("activity_name");
                        if (jsonObjectsubactivitites.length() > 0) {
                            ArrayList<SubActivityModel> subActivityModels = new ArrayList<>();

                            JSONArray subactivities = jsonObjectsubactivitites.
                                    optJSONArray("sub_activities");
                            for (int j = 0; j < subactivities.length(); j++) {
                                JSONObject jsonObjectsubactivity = subactivities.getJSONObject(j);

                                SubActivityModel subActivityModel = new SubActivityModel(
                                        jsonObjectsubactivity.optString("sub_activity_name"),
                                        jsonObjectsubactivity.optString("status"),
                                        jsonObjectsubactivity.optString("due_status"),
                                        jsonObjectsubactivity.optString("due_date"),
                                        jsonObjectsubactivity.optString("utility_name"),
                                        jsonObjectsubactivity.optString("checkbox_status"));
                                subActivityModels.add(subActivityModel);
                            }
                            CheckInCareActivityModel checkInCareActivityModel =
                                    new CheckInCareActivityModel(jsonObjectsubactivitites.
                                            optString("activity_name"), subActivityModels);
                            checkInCareActivityModels.add(checkInCareActivityModel);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            checkInCareModel.setCheckInCareActivityModels(checkInCareActivityModels);

            Config.checkInCareModels.add(checkInCareModel);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void createServiceModel(String strDocumentId, JSONObject jsonObject) {

        try {

            ServiceModel serviceModel = new ServiceModel();

            serviceModel.setDoubleCost(jsonObject.getDouble("cost"));
            serviceModel.setStrServiceName(jsonObject.getString("service_name"));
            serviceModel.setiServiceNo(jsonObject.getInt("service_no"));
            serviceModel.setStrCategoryName(jsonObject.getString("category_name"));
            serviceModel.setiUnit(jsonObject.getInt("unit"));
            serviceModel.setiUnitValue(jsonObject.getInt("unit_value"));

            if (jsonObject.has("milestones")) {

                JSONArray jsonArrayMilestones = jsonObject.
                        getJSONArray("milestones");

                for (int k = 0; k < jsonArrayMilestones.length(); k++) {

                    JSONObject jsonObjectMilestone =
                            jsonArrayMilestones.getJSONObject(k);

                    MilestoneModel milestoneModel = new MilestoneModel();

                    milestoneModel.setiMilestoneId(jsonObjectMilestone.getInt("id"));
                    milestoneModel.setStrMilestoneStatus(jsonObjectMilestone.getString("status"));
                    milestoneModel.setStrMilestoneName(jsonObjectMilestone.getString("name"));
                    milestoneModel.setStrMilestoneDate(jsonObjectMilestone.getString("date"));

                    if (jsonObjectMilestone.has("show"))
                        milestoneModel.setVisible(jsonObjectMilestone.getBoolean("show"));

                    if (jsonObjectMilestone.has("reschedule"))
                        milestoneModel.setReschedule(jsonObjectMilestone.getBoolean("reschedule"));

                    if (jsonObjectMilestone.has("scheduled_date"))
                        milestoneModel.setStrMilestoneScheduledDate(jsonObjectMilestone.
                                getString("scheduled_date"));


                    if (jsonObjectMilestone.has("fields")) {

                        JSONArray jsonArrayFields = jsonObjectMilestone.
                                getJSONArray("fields");

                        for (int l = 0; l < jsonArrayFields.length(); l++) {

                            JSONObject jsonObjectField =
                                    jsonArrayFields.getJSONObject(l);

                            FieldModel fieldModel = new FieldModel();

                            fieldModel.setiFieldID(jsonObjectField.getInt("id"));

                            if (jsonObjectField.has("hide"))
                                fieldModel.setFieldView(jsonObjectField.getBoolean("hide"));

                            fieldModel.setFieldRequired(jsonObjectField.getBoolean("required"));
                            fieldModel.setStrFieldData(jsonObjectField.getString("data"));
                            fieldModel.setStrFieldLabel(jsonObjectField.getString("label"));
                            fieldModel.setStrFieldType(jsonObjectField.getString("type"));

                            if (jsonObjectField.has("values")) {
                                fieldModel.setStrFieldValues(Utils.jsonToStringArray(
                                        jsonObjectField.getJSONArray("values")));
                            }

                            if (jsonObjectField.has("child")) {

                                try {
                                    fieldModel.setChild(jsonObjectField.getBoolean("child"));
                                } catch (Exception e) {
                                    boolean b = true;
                                    try {
                                        if (jsonObjectField.getInt("child") == 0)
                                            b = false;
                                        fieldModel.setChild(b);
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                }

                                if (jsonObjectField.has("child_type"))
                                    fieldModel.setStrChildType(Utils.jsonToStringArray(
                                            jsonObjectField.getJSONArray("child_type")));

                                if (jsonObjectField.has("child_value"))
                                    fieldModel.setStrChildValue(
                                            Utils.jsonToStringArray(jsonObjectField.
                                                    getJSONArray("child_value")));

                                if (jsonObjectField.has("child_condition"))
                                    fieldModel.setStrChildCondition(Utils.
                                            jsonToStringArray(jsonObjectField.
                                                    getJSONArray("child_condition")));

                                if (jsonObjectField.has("child_field"))
                                    fieldModel.setiChildfieldID(Utils.
                                            jsonToIntArray(jsonObjectField.
                                                    getJSONArray("child_field")));
                            }

                            if (jsonObjectField.has("array_fields")) {

                                try {
                                    fieldModel.setiArrayCount(jsonObjectField.
                                            getInt("array_fields"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    int i;
                                    try {
                                        i = Integer.parseInt(jsonObjectField.
                                                getString("array_fields"));
                                        fieldModel.setiArrayCount(i);
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                }

                                if (jsonObjectField.has("array_type"))
                                    fieldModel.setStrArrayType(Utils.jsonToStringArray(
                                            jsonObjectField.getJSONArray("array_type")));

                                if (jsonObjectField.has("array_data"))
                                    fieldModel.setStrArrayData(jsonObjectField.
                                            getString("array_data"));
                            }
                            milestoneModel.setFieldModel(fieldModel);
                        }
                    }
                    serviceModel.setMilestoneModels(milestoneModel);
                }
            }

            serviceModel.setStrServiceId(strDocumentId);

            if (!Config.strServcieIds.contains(strDocumentId)) {

                Config.serviceModels.add(serviceModel);
                Config.strServcieIds.add(strDocumentId);

                //if (!jsonObject.getString("category_name").equalsIgnoreCase("Check-In Care")) {

                Config.servicelist.add(jsonObject.getString("service_name"));
                categorySet.add(jsonObject.getString("category_name"));
                List<String> serviceNameList = new ArrayList<>();

                if (Config.serviceNameModels != null && Config.serviceNameModels.size() > 0) {
                    if (Config.serviceNameModels.containsKey(jsonObject.
                            getString("category_name"))) {
                        serviceNameList.addAll(Config.serviceNameModels.get(jsonObject.
                                getString("category_name")));
                        serviceNameList.add(jsonObject.getString("service_name"));
                    } else {
                        serviceNameList.add(jsonObject.getString("service_name"));
                    }
                } else {
                    serviceNameList.add(jsonObject.getString("service_name"));
                }
                Config.serviceNameModels.put(jsonObject.getString("category_name"),
                        serviceNameList);
                //}
            }

            Config.serviceCategorylist.clear();
            List<String> serviceNameList = new ArrayList<>(categorySet);
            Config.serviceCategorylist.addAll(serviceNameList);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}