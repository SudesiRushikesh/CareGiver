package com.hdfc.caregiver.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.hdfc.caregiver.R;
import com.hdfc.config.Config;
import com.hdfc.libs.AppUtils;

import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 */
public class DashboardFragment extends Fragment {

    //private static int iMenuLength=2;

    private Button buttonActivity, buttonTask;

    private AppUtils appUtils;

    public DashboardFragment() {
        // Required empty public constructor
    }

    public static DashboardFragment newInstance() {
        return new DashboardFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        buttonActivity = (Button) view.findViewById(R.id.buttonActivity);
        buttonTask = (Button) view.findViewById(R.id.buttonTask);

        appUtils = new AppUtils(getActivity());

        //final LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.dashboardlinearLayout);

        buttonActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(0);
            }
        });

        buttonTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(1);
            }
        });

        SimpleActivityFragment fragment = SimpleActivityFragment.newInstance();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayoutDashboard, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

        //
        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH); // Note: zero based!
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        TextView textView = (TextView) view.findViewById(R.id.textViewDate);

        String strDay = String.valueOf(day);

        if (day <= 9)
            strDay = "0" + strDay;

        String strDate = strDay + "-" + Config.months[month] + "-" + String.valueOf(year);

        textView.setText(strDate);

        buttonClicked(0);

        return view;
    }

    public void buttonClicked(int iPosition) {

        try {

            buttonActivity.setBackgroundResource(R.drawable.button_back_trans);
            buttonActivity.setTextColor(getActivity().getResources().getColor(R.color.colorAccentDark));

            buttonTask.setBackgroundResource(R.drawable.button_back_trans);
            buttonTask.setTextColor(getActivity().getResources().getColor(R.color.colorAccentDark));

            if (iPosition == 0) {
                buttonActivity.setBackgroundResource(R.drawable.one_side_border);
                buttonActivity.setTextColor(getActivity().getResources().getColor(R.color.colorPrimaryDark));

                SimpleActivityFragment fragment = SimpleActivityFragment.newInstance();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.frameLayoutDashboard, fragment);
                transaction.addToBackStack(null);
                transaction.commit();

                SimpleActivityFragment.activityModels = Config.activityModels;
                SimpleActivityFragment.mAdapter.notifyDataSetChanged();
            }

            if (iPosition == 1) {
                buttonTask.setBackgroundResource(R.drawable.one_side_border);
                buttonTask.setTextColor(getActivity().getResources().getColor(R.color.colorPrimaryDark));

                MileStoneFragment fragment = MileStoneFragment.newInstance();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.frameLayoutDashboard, fragment);
                transaction.addToBackStack(null);
                transaction.commit();


                MileStoneFragment.activityModels = Config.milestoneModels;
                MileStoneFragment.mAdapter.notifyDataSetChanged();

                /*DashboardActivity.loadingPanel.setVisibility(View.VISIBLE);
                appUtils.fetchMileStone(DashboardActivity.loadingPanel);*/
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
