package com.hdfc.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hdfc.caregiver.R;
import com.hdfc.libs.Libs;
import com.hdfc.libs.MultiBitmapLoader;
import com.hdfc.models.ClientModel;

import java.io.File;
import java.util.List;

/**
 * Created by Admin on 24-02-2016.
 */
public class ClientsAdapter extends BaseAdapter {

    private static LayoutInflater inflater = null;
    private Context _context;
    public List<ClientModel> data ;
    private Libs libs;
    private MultiBitmapLoader multiBitmapLoader;

    public ClientsAdapter(Context context, List<ClientModel> arrayModel){
        _context = context;
        libs = new Libs(_context);
        multiBitmapLoader = new MultiBitmapLoader(_context);
        data = arrayModel;
        inflater = ( LayoutInflater )context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

               ViewHolder viewHolder;

        if (convertView == null) {

            convertView = inflater.inflate(R.layout.clients_item, null);

            viewHolder = new ViewHolder();

            viewHolder.name = (TextView) convertView.findViewById(R.id.textViewName);
            viewHolder.age = (TextView) convertView.findViewById(R.id.textViewClient_age);
            viewHolder.problem = (TextView) convertView.findViewById(R.id.textViewClient_problem);
            viewHolder.premium = (TextView) convertView.findViewById(R.id.textViewPremium);
            viewHolder.address = (TextView) convertView.findViewById(R.id.textViewAddress);
            viewHolder.client = (ImageView) convertView.findViewById(R.id.imageClients);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if(data.size()>0) {

            ClientModel clientsModel = data.get(position);

            viewHolder.name.setText(clientsModel.getName());
            viewHolder.age.setText(clientsModel.getAge());

            viewHolder.problem.setText(clientsModel.getProblem().length()>8 ? clientsModel.getProblem().substring(0,5)+"..":clientsModel.getProblem());
            viewHolder.premium.setText(clientsModel.getPremium().length()>8 ? clientsModel.getPremium().substring(0,5)+"..":clientsModel.getPremium());

            File fileImage = libs.createFileInternal("images/" + libs.replaceSpace(clientsModel.getName()));

            if(fileImage.exists()) {
                String filename = fileImage.getAbsolutePath();
                multiBitmapLoader.loadBitmap(filename, viewHolder.client);
            }else{
                viewHolder.client.setImageDrawable(_context.getResources().getDrawable(R.drawable.hungal_circle));
            }

            viewHolder.address.setText(clientsModel.getAddress());


        }

        return convertView;
    }
    public  class ViewHolder{
        TextView name,age,problem,address,premium;
        ImageView client;

    }
}
