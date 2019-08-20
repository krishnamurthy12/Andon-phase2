package com.vvtech.andonphase2.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vvtech.andonphase2.R;
import com.vvtech.andonphase2.apiresponses.allusers.EmployeeStatusList;
import com.vvtech.andonphase2.pojo.EmployeeDetails;
import com.vvtech.andonphase2.utils.Utilities;

import java.util.ArrayList;
import java.util.List;

import static com.vvtech.andonphase2.utils.Utilities.getIPAddress;

public class EmployeesAdapter extends RecyclerView.Adapter<EmployeesAdapter.MyHolder> {

    Context context;
    List<EmployeeStatusList> mList=new ArrayList<>();

    private int lastPosition = -1;

    public EmployeesAdapter(Context context, List<EmployeeStatusList> mList) {
        this.context = context;
        this.mList = mList;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.employee_single_row_appearence, null, false);
        return new MyHolder(v);
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {

        holder.mEmployeeName.setText(mList.get(position).getEmployeeName());

        if(mList.get(position).getImageUrl().contains(".png")) {
            String imageUrl="http://"+ getIPAddress(context)+":8080/AndonWebservices/";
            Picasso.get()
                    .load(imageUrl+mList.get(position).getImageUrl())
                    //.placeholder(R.drawable.background_drawable)
                    //.error(R.drawable.user)
                    //.resize(150,150)
                    .into(holder.mEmployeeImage);
        }
        else
        {
            Picasso.get()
                    .load(R.drawable.empicon_red)
                    //.placeholder(R.drawable.background_drawable)
                    //.error(R.drawable.user)
                    //.resize(150,150)
                    .into(holder.mEmployeeImage);

        }
        // Here you apply the animation when the view is bound
        setAnimation(holder.itemView, position);

    }


    /**
     * Here is the key method to apply the animation
     */
    private void setAnimation(View viewToAnimate, int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }
    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        public ImageView mEmployeeImage;
        public TextView mEmployeeName;

        public MyHolder(View itemView) {
            super(itemView);

            mEmployeeImage=itemView.findViewById(R.id.vI_esa_image);
            mEmployeeName=itemView.findViewById(R.id.vT_esa_name);
        }
    }
}

