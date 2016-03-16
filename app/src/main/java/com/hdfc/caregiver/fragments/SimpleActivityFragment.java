package com.hdfc.caregiver.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hdfc.caregiver.CreatingTaskActivity;
import com.hdfc.caregiver.FeatureActivity;
import com.hdfc.caregiver.R;
import com.hdfc.config.Config;
import com.hdfc.libs.Libs;
import com.hdfc.libs.MultiBitmapLoader;
import com.hdfc.models.ActivityModel;
import com.hdfc.models.ClientModel;
import com.hdfc.models.Model;
import com.yydcdut.sdlv.Menu;
import com.yydcdut.sdlv.MenuItem;
import com.yydcdut.sdlv.SlideAndDragListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class SimpleActivityFragment extends Fragment implements SlideAndDragListView.OnListItemLongClickListener,
        SlideAndDragListView.OnDragListener, SlideAndDragListView.OnSlideListener,
         SlideAndDragListView.OnMenuItemClickListener, SlideAndDragListView.OnListItemClickListener,
        SlideAndDragListView.OnItemDeleteListener {
    private static final String TAG = "";
    private MultiBitmapLoader multiBitmapLoader;

    private Menu mMenu;
    private List<ApplicationInfo> mAppList;
    private SlideAndDragListView<ApplicationInfo> mListView;
    private List<Model> models;
    private List<Integer> images;
    Model obj = new Model();
    String[] MsgSubjet = null;
    String[] MsgDescr;
    String[] strTimes;
    private Libs libs;
    private  TextView textViewEmpty;

    public ArrayList<ActivityModel> activityModels = new ArrayList<>();

    ImageView mytask,clients,feedback,next;
    LinearLayout vegetable;
    ImageButton add;
    private static int intWhichScreen;

    public static SimpleActivityFragment newInstance() {
        SimpleActivityFragment fragment = new SimpleActivityFragment();
        Bundle args = new Bundle();
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

        View view = inflater.inflate(R.layout.fragment_simple_activity_, container, false);

        initData();
        initMenu();

        libs =new Libs(getActivity());
        multiBitmapLoader = new MultiBitmapLoader(getActivity());

        add = (ImageButton)view.findViewById(R.id.add_button);
        mytask = (ImageView)view.findViewById(R.id.buttonMyTasks);
        clients = (ImageView)view.findViewById(R.id.buttonClients);
        feedback = (ImageView)view.findViewById(R.id.buttonFeedback);
        vegetable = (LinearLayout)view.findViewById(R.id.llFirst);
        next = (ImageView)view.findViewById(R.id.imgNext);
        textViewEmpty = (TextView) view.findViewById(android.R.id.empty);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),FeatureActivity.class);
                intent.putExtra("WHICH_SCREEN", intWhichScreen);
                startActivity(intent);
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CreatingTaskActivity.class);
                //intent.putExtra("WHICH_SCREEN1",intWhichScreen);
                Config.intSelectedMenu=Config.intDashboardScreen;
                startActivity(intent);
                Toast.makeText(getContext(),"Click",Toast.LENGTH_LONG).show();
            }
        });

            mListView = (SlideAndDragListView)view.findViewById(R.id.listViewEdit);
            mListView.setMenu(mMenu);

            mListView.setEmptyView(textViewEmpty);
            mListView.setOnListItemLongClickListener(this);
            mListView.setOnDragListener(this, mAppList);
            //mListView.setOnListItemClickListener(this);
            mListView.setOnSlideListener(this);
            mListView.setOnMenuItemClickListener(this);
            mListView.setOnItemDeleteListener(this);

           /* mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                  *//*  intent.putExtra("WHICH_SCREEN",intWhichScreen);
                    System.out.println("Data contained in model class is : "+activityModels.get(position)+" Parent item is "+parent.getItemAtPosition(position));
                    intent.putExtra("WHICH_SCREEN",activityModels.get(position));
                    startActivity(intent);*//*

                }
            });*/

            parseData();


        return view;
    }
/*
    @Override
    public void onListItemClick(View v, int position) {
            Intent intent = new Intent(getActivity(), FeatureActivity.class);
            *//*intent.putExtra("WHICH_SCREEN", intWhichScreen);
            Bundle bundle = new Bundle();
            bundle.putSerializable("RUSHI", activityModels.get(position));
            intent.putExtras(bundle);
            startActivity(intent);*//*
            intent.putStringArrayListExtra("WHICH_SCREEN",activityModels.get(position));
            startActivity(intent);
    }*/
    public void parseData(){

        try {

            activityModels.clear();

            if (Config.jsonObject.has("activities")) {

                JSONArray jsonArrayFeedback = Config.jsonObject.getJSONArray("activities");

                int iArraySize = jsonArrayFeedback.length();

                MsgSubjet = new String[iArraySize];
                MsgDescr = new String[iArraySize];
                strTimes = new String[iArraySize];

                for (int k = 0; k < iArraySize; k++) {

                    JSONObject jsonObject = jsonArrayFeedback.getJSONObject(k);

                    ActivityModel activityModel = new ActivityModel();

                    //jsonObjectActCarla.put("videos", jsonArray);
                    //jsonObjectActCarla.put("images", jsonArray);

                    activityModel.setStrActivityMessage(jsonObject.getString("activity_message"));
                    activityModel.setStrActivityName(jsonObject.getString("activity_name"));
                    activityModel.setStrActivityDesc(jsonObject.getString("activity_description"));
                    activityModel.setiServiceId(jsonObject.getInt("service_id"));
                    activityModel.setStrActivityDate(jsonObject.getString("activity_date"));
                    activityModel.setStrActivityDoneDate(jsonObject.getString("activity_done_date"));
                    activityModel.setStrActivityStatus(jsonObject.getString("status"));
                    activityModel.setStrActivityDependentName(jsonObject.getString("dependent_name"));

                    String features[] = new String[jsonObject.getJSONArray("features").length()];
                    String featuresDone[] = new String[jsonObject.getJSONArray("features_done").length()];

                    for(int i=0;i< jsonObject.getJSONArray("features").length();i++){
                        features[i]= jsonObject.getJSONArray("features").getString(i);
                    }

                    for (int i=0;i<jsonObject.getJSONArray("features_done").length();i++){
                        featuresDone[i] = jsonObject.getJSONArray("features_done").getString(i);
                    }

                    activityModel.setFeatures(features);
                    activityModel.setDoneFeatures(featuresDone);


                    activityModel.setStrCustomerEmail(jsonObject.getString("customer_email"));
                    System.out.println("cust mail is :"+jsonObject.getString("customer_email"));
                    activityModels.add(activityModel);
                }
            }

            mListView.setAdapter(mAdapter);
            //mAdapter.notifyDataSetChanged();

        }catch (Exception e){
            e.printStackTrace();
        }
    }



    public void initData() {
        mAppList = getActivity().getPackageManager().getInstalledApplications(0);

       /* images = new ArrayList<Integer>();
        images.add(R.drawable.hungal_circle);
        images.add(R.drawable.mrs_hungal_circle);
        images.add(R.drawable.hungal_circle);
        images.add(R.drawable.carla1);
        images.add(R.drawable.carla2);
        images.add(R.drawable.mrs_hungal_circle);
        images.add(R.drawable.hungal_circle);
        images.add(R.drawable.carla1);;*/
    }

    public void initMenu() {

        mMenu = new Menu(new ColorDrawable(Color.LTGRAY), true);
        mMenu.addItem(new MenuItem.Builder().setWidth((int) getResources().getDimension(R.dimen.slv_item_bg_btn_width_img))
                .setBackground(getActivity().getResources().getDrawable(R.color.polygonViewCircleStrokeColor))
                .setIcon(getActivity().getResources().getDrawable(R.drawable.done))
                .build());
        mMenu.addItem(new MenuItem.Builder().setWidth((int) getResources().getDimension(R.dimen.slv_item_bg_btn_width) * 2)
                .setBackground(getActivity().getResources().getDrawable(R.color.polygonViewCircleStrokeColor))
                .setText("Done")
                .setTextColor(Color.WHITE)
                .setTextSize((int) getResources().getDimension(R.dimen.txt_size))
                .build());
        mMenu.addItem(new MenuItem.Builder().setWidth((int) getResources().getDimension(R.dimen.slv_item_bg_btn_width) * 2)
                .setBackground(getActivity().getResources().getDrawable(R.color.polygonViewCircleStrokeColor))
                .setText("Undo")
                .setTextColor(Color.WHITE)
                .setTextSize((int) getResources().getDimension(R.dimen.txt_size))
                .build());
        mMenu.addItem(new MenuItem.Builder()
                .setWidth((int) getResources().getDimension(R.dimen.slv_item_bg_btn_width_img)+50 )
                .setBackground(getActivity().getResources().getDrawable(R.color.polygonViewCircleStrokeColor))
                .setIcon(getActivity().getResources().getDrawable(R.drawable.circle))
                .build());

        mMenu.addItem(new MenuItem.Builder().setWidth((int) getResources().getDimension(R.dimen.slv_item_bg_btn_width_img)-30)
                .setBackground(getActivity().getResources().getDrawable(R.color.polygonViewCircleStrokeColor))
                .setDirection(MenuItem.DIRECTION_RIGHT)
                .setIcon(getResources().getDrawable(R.mipmap.map))
                .build());
        mMenu.addItem(new MenuItem.Builder().setWidth((int) getResources().getDimension(R.dimen.slv_item_bg_btn_width_img) - 30)
                .setBackground(getActivity().getResources().getDrawable(R.color.polygonViewCircleStrokeColor))
                .setDirection(MenuItem.DIRECTION_RIGHT)
                .setIcon(getResources().getDrawable(R.mipmap.message))
                .build());
        mMenu.addItem(new MenuItem.Builder().setWidth((int) getResources().getDimension(R.dimen.slv_item_bg_btn_width_img)-30)
                .setBackground(getActivity().getResources().getDrawable(R.color.polygonViewCircleStrokeColor))
                .setDirection(MenuItem.DIRECTION_RIGHT)
                .setIcon(getResources().getDrawable(R.mipmap.call1))
                .build());
        mMenu.addItem(new MenuItem.Builder().setWidth((int) getResources().getDimension(R.dimen.slv_item_bg_btn_width_img) - 30)
                .setBackground(getActivity().getResources().getDrawable(R.color.polygonViewCircleStrokeColor))
                .setDirection(MenuItem.DIRECTION_RIGHT)
                .setIcon(getResources().getDrawable(R.drawable.hungal_circle2))
                .build());
    }



    private BaseAdapter mAdapter = new BaseAdapter() {

        @Override
        public int getCount() {

            Libs.log(String.valueOf(activityModels.size()), " size ");
            return activityModels.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CustomViewHolder cvh;

            if (convertView == null) {

                cvh = new CustomViewHolder();

                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.my_tasks_item, null);
                cvh.imageTiming = (ImageView) convertView.findViewById(R.id.imageTiming);
                cvh.textMessage = (TextView) convertView.findViewById(R.id.task_message);
                cvh.textSubject = (TextView) convertView.findViewById(R.id.task_subject);
                cvh.textTime = (TextView) convertView.findViewById(R.id.task_time);
                cvh.imagePerson = (ImageView) convertView.findViewById(R.id.imagePerson);
                convertView.setTag(cvh);

            } else {
                cvh = (CustomViewHolder) convertView.getTag();
            }


            if(activityModels.size()>0) {

                Libs.log(String.valueOf(activityModels.size()), " size ");

                ActivityModel activityModel = activityModels.get(position);

                cvh.textSubject.setText(activityModel.getStrActivityMessage());
                cvh.textMessage.setText(activityModel.getStrActivityName());

                String strDisplayDateTime = libs.formatDate(activityModel.getStrActivityDate());

                cvh.textTime.setText(strDisplayDateTime);

                File fileImage = libs.createFileInternal("images/" + libs.replaceSpace(activityModel.getStrActivityDependentName()));

                if(fileImage.exists()) {
                    String filename = fileImage.getAbsolutePath();
                    multiBitmapLoader.loadBitmap(filename, cvh.imagePerson);
                }else{
                    cvh.imagePerson.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.mrs_hungal_circle2));
                }
                //cvh.imagePerson.setImageDrawable(getActivity().getResources().getDrawable(images.get(position)));
            }
            return convertView;
        }

        class CustomViewHolder {
            public ImageView imageTiming;
            public TextView textSubject;
            public ImageView imagePerson;
            public TextView textMessage;
            public TextView textTime;
        }
    };

    @Override
    public void onListItemLongClick(View view, int position) {

        Log.i(TAG, "onListItemLongClick   " + position);
    }

    @Override
    public void onDragViewStart(int position) {

        Log.i(TAG, "onDragViewStart   " + position);
    }

    @Override
    public void onDragViewMoving(int position) {
//        Toast.makeText(DemoActivity.this, "onDragViewMoving   position--->" + position, Toast.LENGTH_SHORT).show();
        Log.i("yuyidong", "onDragViewMoving   " + position);
    }

    @Override
    public void onDragViewDown(int position) {

        Log.i(TAG, "onDragViewDown   " + position);
    }

    /*@Override
    public void onListItemClick(View v, int position) {
        Intent intent4 = new Intent(getActivity(), FeatureActivity.class);
        intent4.putExtra("WHICH_SCREEN",intWhichScreen);
        startActivity(intent4);
        Log.i(TAG, "onListItemClick   " + position);
    }*/

   /* @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getContext(),"You are done clicking",Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getActivity(),FeatureActivity.class);
        intent.putExtra("WHICH_SCREEN",(ActivityModel)parent.getItemAtPosition(position));
        startActivity(intent);
    }*/

    @Override
    public void onSlideOpen(View view, View parentView, int position, int direction) {
        Log.i(TAG, "onSlideOpen   " + position);
    }

    @Override
    public void onSlideClose(View view, View parentView, int position, int direction) {

        Log.i(TAG, "onSlideClose   " + position);
    }

    @Override
    public int onMenuItemClick(View v, int itemPosition, int buttonPosition, int direction) {
        Log.e(TAG, "onMenuItemClick   " + itemPosition + "   " + buttonPosition + "   " + direction);
        switch (direction) {
            case MenuItem.DIRECTION_LEFT:
                switch (buttonPosition) {
                    case 0:
                        Toast.makeText(getContext(),"You click on "+buttonPosition,Toast.LENGTH_SHORT).show();
                        return Menu.ITEM_NOTHING;
                    case 1:
                        Toast.makeText(getContext(),"You click on "+buttonPosition,Toast.LENGTH_SHORT).show();

                        if(activityModels.size()>0) {

                            Bundle args = new Bundle();
                            args.putSerializable("ACTIVITY", activityModels.get(itemPosition));

                           // fragment.setArguments(args);

                            //Libs.log(String.valueOf(activityModels.size()), " size ");
                          //  ActivityModel activityModel = activityModels.get(position);
                            Intent intent = new Intent(getActivity(), FeatureActivity.class);
                            /*intent.putExtra("WHICH_SCREEN", activityModels.get(itemPosition));*/

                           // System.out.println("MODEL CLASS VAL :: " + activityModels.get(itemPosition).toString());
                            intent.putExtras(args);
                            startActivity(intent);

                        }
                        return Menu.ITEM_SCROLL_BACK;
                    case 2:
                        Intent intent1 = new Intent(getActivity(),CreatingTaskActivity.class);
                        //intent.putExtra("WHICH_SCREEN1",intWhichScreen);
                        Config.intSelectedMenu=Config.intDashboardScreen;
                        startActivity(intent1);
                        Toast.makeText(getContext(),"You click on "+buttonPosition,Toast.LENGTH_SHORT).show();
                        return Menu.ITEM_NOTHING;
                    case 3:
                        Toast.makeText(getContext(),"You click on "+buttonPosition,Toast.LENGTH_SHORT).show();
                        return Menu.ITEM_NOTHING;
                }
                break;
            case MenuItem.DIRECTION_RIGHT:
                switch (buttonPosition) {
                    case 0:
                        Toast.makeText(getContext(),"You click on "+buttonPosition,Toast.LENGTH_SHORT).show();
                        return Menu.ITEM_SCROLL_BACK;
                    case 1:
                        Toast.makeText(getContext(),"You click on "+buttonPosition,Toast.LENGTH_SHORT).show();
                        return Menu.ITEM_NOTHING;
                    case 2:
                        Toast.makeText(getContext(),"You click on "+buttonPosition,Toast.LENGTH_SHORT).show();
                        return Menu.ITEM_NOTHING;
                    case 3:
                        Toast.makeText(getContext(),"You click on "+buttonPosition,Toast.LENGTH_SHORT).show();
                        return Menu.ITEM_NOTHING;
                }
        }
        return Menu.ITEM_NOTHING;
    }

    @Override
    public void onItemDelete(View view, int position) {
        mAppList.remove(position - mListView.getHeaderViewsCount());
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(View v, int position) {
        Toast.makeText(getContext(),"You are done clicking",Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getActivity(), FeatureActivity.class);
        intent.putExtra("WHICH_SCREEN",activityModels.get(position));
        System.out.println("RUSHAAAA :: "+ activityModels.get(position));
        startActivity(intent);
    }
}
