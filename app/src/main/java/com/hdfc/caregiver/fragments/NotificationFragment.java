package com.hdfc.caregiver.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.hdfc.adapters.NotificationAdapter;
import com.hdfc.app42service.StorageService;
import com.hdfc.caregiver.DashboardActivity;
import com.hdfc.caregiver.FeatureActivity;
import com.hdfc.caregiver.R;
import com.hdfc.config.CareGiver;
import com.hdfc.config.Config;
import com.hdfc.dbconfig.DbHelper;
import com.hdfc.libs.AppUtils;
import com.hdfc.libs.Utils;
import com.hdfc.models.ActivityModel;
import com.shephertz.app42.paas.sdk.android.App42CallBack;
import com.shephertz.app42.paas.sdk.android.storage.Query;
import com.shephertz.app42.paas.sdk.android.storage.QueryBuilder;
import com.shephertz.app42.paas.sdk.android.storage.Storage;

import net.sqlcipher.Cursor;

import java.util.ArrayList;


public class NotificationFragment extends Fragment {

    private NotificationAdapter notificationAdapter;
    //private static ProgressDialog progressDialog;
    private Utils utils;
    private AppUtils appUtils;

    public NotificationFragment() {
        // Required empty public constructor
    }

    public static NotificationFragment newInstance(boolean b) {
        NotificationFragment fragment = new NotificationFragment();
        Bundle args = new Bundle();
        args.putBoolean("RELOAD", b);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        ListView listViewActivities = (ListView) view.findViewById(R.id.listViewActivity);
        TextView emptyTextView = (TextView) view.findViewById(android.R.id.empty);
        listViewActivities.setEmptyView(emptyTextView);
        utils = new Utils(getActivity());
        appUtils = new AppUtils(getActivity());
     //   loadingPanel = (RelativeLayout) view.findViewById(R.id.loadingPanel);

        Bundle bundle = this.getArguments();
        boolean b = bundle.getBoolean("RELOAD", false);

        appUtils.createNotificationModel();

        notificationAdapter = new NotificationAdapter(getActivity(), Config.notificationModels);
        listViewActivities.setAdapter(notificationAdapter);

        listViewActivities.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                findActivities(Config.notificationModels.get(position).getStrActivityId());
            }
        });

        if (Config.notificationModels.size() <= 0 || b) {

          /*  boolean isBackground=false;

            if(b)*/


            loadNotifications(b);
        }

        return view;
    }

    private void loadNotifications(final boolean isBackground) {

        if (utils.isConnectingToInternet()) {

            if (!isBackground)
                DashboardActivity.loadingPanel.setVisibility(View.VISIBLE);

            String strDate = DbHelper.DEFAULT_DB_DATE;

            Cursor cursor = CareGiver.getDbCon().getMaxDate(Config.collectionNotification);

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                strDate = cursor.getString(0);
            }

            CareGiver.getDbCon().closeCursor(cursor);

            StorageService storageService = new StorageService(getContext());

            Query q1 = QueryBuilder.build("user_id", Config.providerModel.getStrProviderId(),
                    QueryBuilder.Operator.EQUALS);

            Query finalQuery;

            if (strDate != null && !strDate.equalsIgnoreCase("")) {
                Query q12 = QueryBuilder.build("_$updatedAt", strDate, QueryBuilder.Operator.GREATER_THAN_EQUALTO);

                finalQuery = QueryBuilder.compoundOperator(q1, QueryBuilder.Operator.AND, q12);
            } else {
                finalQuery = q1;
            }

          /*  try {
                Utils.log(finalQuery.get(), " QUERY ");
            } catch (Exception e) {
                e.printStackTrace();
            }*/


            storageService.findDocsByQueryOrderBy(Config.collectionNotification, finalQuery, 30000,
                    0, "time", 1, new App42CallBack() {

                        @Override
                        public void onSuccess(Object o) {
                            if (o != null) {

                                Storage storage = (Storage) o;

                                Utils.log(storage.toString(), "not ");

                                if (storage.getJsonDocList().size() > 0) {

                                    ArrayList<Storage.JSONDocument> jsonDocList = storage.
                                            getJsonDocList();

                                    try {

                                        CareGiver.getDbCon().beginDBTransaction();

                                        for (int i = 0; i < jsonDocList.size(); i++) {

                                            String values[] = {jsonDocList.get(i).getDocId(),
                                                    jsonDocList.get(i).getUpdatedAt(),
                                                    jsonDocList.get(i).getJsonDoc(), Config.collectionNotification,
                                                    "1", ""};

                                            //String selection = DbHelper.COLUMN_OBJECT_ID + " = ?";

                                            // WHERE clause arguments
                                            //String[] selectionArgs = {jsonDocList.get(i).getDocId()};
                                            CareGiver.getDbCon().insert(DbHelper.strTableNameCollection,
                                                    values,
                                                    DbHelper.COLLECTION_FIELDS);

                                        }
                                        CareGiver.getDbCon().dbTransactionSuccessFull();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    } finally {
                                        CareGiver.getDbCon().endDBTransaction();
                                    }
                                }
                                refreshNotifications();
                            } else {
                                if (!isBackground)
                                    utils.toast(2, 2, getString(R.string.warning_internet));
                            }

                            if (!isBackground)
                                DashboardActivity.loadingPanel.setVisibility(View.GONE);
                        }

                        @Override
                        public void onException(Exception e) {

                            if (!isBackground) {

                                DashboardActivity.loadingPanel.setVisibility(View.GONE);

                                if (e == null)
                                    utils.toast(2, 2, getString(R.string.warning_internet));
                                else
                                    utils.toast(1, 1, getString(R.string.error));
                            }
                        }
                    });
        }
    }

    private void refreshNotifications() {
        appUtils.createNotificationModel();
        notificationAdapter.notifyDataSetChanged();
    }

    private void findActivities(final String strActivityId) {

        int iPosition = Config.strActivityIdsNotifications.indexOf(strActivityId);

        ActivityModel activityModel;

        if(iPosition>-1) {
            activityModel = Config.activityModelsNotifications.get(iPosition);

            Bundle args = new Bundle();
            //
            Intent intent = new Intent(getActivity(), FeatureActivity.class);
            args.putSerializable("ACTIVITY", activityModel);
            args.putBoolean("WHICH_SCREEN", true);
            intent.putExtras(args);
            startActivity(intent);

        }else {

           /* StorageService storageService = new StorageService(getContext());

            storageService.findDocsById(strActivityId, Config.collectionActivity,
                    new AsyncApp42ServiceApi.App42StorageServiceListener() {
                @Override
                public void onDocumentInserted(Storage response) {

                }

                @Override
                public void onUpdateDocSuccess(Storage response) {

                }

                @Override
                public void onFindDocSuccess(Storage storage) throws JSONException {
                    if (storage != null) {

                        if (storage.getJsonDocList().size() > 0) {

                            ArrayList<Storage.JSONDocument> jsonDocList = storage.getJsonDocList();

                            for (int i = 0; i < jsonDocList.size(); i++) {
//                                    utils.createNotificationModel(jsonDocList.get(i).getDocId(), jsonDocList.get(i).getJsonDoc());
                                appUtils.createActivityModel(jsonDocList.get(i).getDocId(),
                                        jsonDocList.get(i).getJsonDoc(), 2);
                            }

                            int iPosition = Config.strNotificationIds.indexOf(strActivityId);
                            if (iPosition > -1) {
                                ActivityModel activityModel = Config.activityModelsNotifications.
                                        get(iPosition);
                               *//* Bundle args = new Bundle();
                                Intent intent = new Intent(getActivity(), FeatureActivity.class);
                                args.putSerializable("ACTIVITY", activityModel);
                                intent.putExtras(args);
                                startActivity(intent);*//*

                                Bundle args = new Bundle();
                                //
                                Intent intent = new Intent(getActivity(), FeatureActivity.class);
                                args.putSerializable("ACTIVITY", activityModel);
                                args.putBoolean("WHICH_SCREEN", true);
                                intent.putExtras(args);
                                startActivity(intent);
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
            });*/
        }
    }
}
