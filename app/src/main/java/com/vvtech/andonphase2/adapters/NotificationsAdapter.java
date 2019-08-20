package com.vvtech.andonphase2.adapters;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vvtech.andonphase2.R;
import com.vvtech.andonphase2.apiresponses.allnotifications.NotificationList;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> implements Filterable {

    private final RecyclerView recyclerList;
    Context context;
    private List<NotificationList> mList;
    private List<NotificationList> mFilteredList;

    private static final int UNSELECTED = -1;
    private int selectedItem = UNSELECTED;

    ItemClickInterface itemClickInterface;

    int numOfDays , hours , minutes , seconds ;


    public NotificationsAdapter(List<NotificationList> listItems, Context ctx, RecyclerView recyclerList) {
        this.context = ctx;
        this.mList = listItems;
        this.mFilteredList=listItems;
        this.recyclerList = recyclerList;
        itemClickInterface= (ItemClickInterface) ctx;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString().trim();
                Log.d("filtercheck","entered text=>"+charString);
                if (charString.isEmpty()) {
                    mFilteredList = mList;
                }
                else {
                    List<NotificationList> filteredList = new ArrayList<>();
                    for (NotificationList row : mList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getNotificationStatus().equalsIgnoreCase(charString))
                        {
                            filteredList.add(row);
                            Log.d("filtercheck","match found in getNotificationStatus=>"+charString+" list size=>"+filteredList.size());
                        }
                        /*else if(row.getTeam().equalsIgnoreCase(charString))
                        {
                            filteredList.add(row);
                            Log.d("filtercheck","match found in getTeam=>"+charString+" list size=>"+filteredList.size());
                        }*/
                        else if(row.getAcceptedBy().equalsIgnoreCase(charString))
                        {
                            if(row.getAcceptedBy().isEmpty() || row.getAcceptedBy()==null)
                            {

                            }
                            else {
                                filteredList.add(row);
                            }

                            Log.d("filtercheck","match found in getAcceptedBy=>"+charString+" list size=>"+filteredList.size());

                        }
                       /* else if(row.getError().toLowerCase().contains(charString.toLowerCase()))
                        {
                            filteredList.add(row);
                            Log.d("filtercheck","match found in getError=>"+charString+" list size=>"+filteredList.size());

                        }*/
                        else if(row.getStation().equalsIgnoreCase(charString))
                        {
                            filteredList.add(row);
                            Log.d("filtercheck","match found in getStation=>"+charString+" list size=>"+filteredList.size());

                        }
                        else if(row.getCreatedDate().contains(charString.toLowerCase()))
                        {
                            filteredList.add(row);
                            Log.d("filtercheck","match found in getCreatedDate=>"+charString+" list size=>"+filteredList.size());

                        }
                        else if(row.getLineCode().toLowerCase().contains(charString))
                        {
                            filteredList.add(row);
                            Log.d("filtercheck","match found in getLineCode=>"+charString+" list size=>"+filteredList.size());

                        }

                        else {
                            //Log.d("filtercheck","no match found=>"+charString+" list size=>"+filteredList.size());
                        }
                    }

                    mFilteredList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mFilteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mFilteredList = (ArrayList<NotificationList>) results.values;
                notifyDataSetChanged();

            }
        };
    }


    @NonNull
    @Override
    public NotificationsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_single_item_layout, parent, false);
        return new ViewHolder(v);
    }


    @SuppressLint("WrongConstant")
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.mErrorID.setText(mList.get(position).getNotificationId()+"");


       // holder.mActionsLayout.setVisibility(View.GONE);

        holder.mErrorID.setText(String.valueOf(mList.get(position).getNotificationId()));
        holder.mErrorMessage.setText(mList.get(position).getError());
        holder.mLine.setText( mList.get(position).getLineCode().toUpperCase());
        holder.mStation.setText(mList.get(position).getStation());
        holder.mTeam.setText(mList.get(position).getTeam());

        holder.mAcceptedby.setText(mList.get(position).getAcceptedBy());
        holder.mComplaintStatus.setText(mList.get(position).getNotificationStatus());
        holder.mDateTime.setText(mList.get(position).getCreatedDate());

        if(mList.get(position).getNotificationStatus().equalsIgnoreCase("Kit Prepared"))
        {

            ObjectAnimator anim = ObjectAnimator.ofInt( holder.mainContactLayout, "backgroundColor", Color.WHITE,context.getResources().getColor(R.color.greendark),
                    Color.WHITE);
            anim.setDuration(2000);
            anim.setEvaluator(new ArgbEvaluator());
            anim.setRepeatMode(Animation.REVERSE);
            anim.setRepeatCount(Animation.INFINITE);
            anim.start();

        }
        else if(mList.get(position).getNotificationStatus().equalsIgnoreCase("Maze Request Confirmed"))
        {

            //
            holder.mComplaintStatus.setTextColor(context.getResources().getColor(R.color.greendark));

        }
        else if(mList.get(position).getNotificationStatus().equalsIgnoreCase("Maze Request Raised"))
        {

            //
            holder.mComplaintStatus.setTextColor(context.getResources().getColor(R.color.redlight));

        }

       calculateDateDifference(mList.get(position).getCreatedDate());

            if(numOfDays>=1 || hours>=1 || minutes>=20)
            {

                holder.mainContactLayout.setBackgroundResource(R.drawable.card_border);
                Log.d("dateformatting","\ndays=>"+numOfDays+"\n Hours=>"+hours+"\nminutes=>"+minutes+"\nseconds=>"+seconds);
            }


        //holder.mainContactLayout.setTag(position);
        holder.mainContactLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String notificationStatus = mList.get(position).getNotificationStatus().trim().replaceAll("^\"|\"$", "");

                boolean shouldExpandActionLayout = holder.mActionsLayout.getVisibility() == View.GONE;
                boolean shouldExpandAcceptlayout = holder.mAcceptLayout.getVisibility() == View.GONE;

                AutoTransition autoTransition = new AutoTransition();
                autoTransition.setDuration(200);

                Log.d("expansion","shouldExpandActionLayout"+shouldExpandActionLayout+" shouldExpandAcceptlayout"+shouldExpandAcceptlayout);
                if (shouldExpandActionLayout && shouldExpandAcceptlayout) {
                    // Expand the the item which is previously not expanded
                    holder.mainContactLayout.setBackground(context.getResources().getDrawable(R.drawable.rectangular_edittext_focused));

                   // notifyItemChanged(position);

                    if(notificationStatus.equalsIgnoreCase("Open"))
                    {
                        holder.mActionsLayout.setVisibility(View.GONE);
                        holder.mAcceptLayout.setVisibility(View.VISIBLE);
                    }
                    else {
                        holder.mAcceptLayout.setVisibility(View.GONE);
                        holder.mActionsLayout.setVisibility(View.VISIBLE);
                    }

                }
                else {
                    //collapse the item which is previously opened
                    holder.mAcceptLayout.setVisibility(View.GONE);
                    holder.mActionsLayout.setVisibility(View.GONE);
                    calculateDateDifference(mList.get(position).getCreatedDate());

                        if(numOfDays>=1 || hours>=1 || minutes>=20)
                        {

                            holder.mainContactLayout.setBackgroundResource(R.drawable.card_border);
                            //Log.d("dateformatting","\ndays=>"+numOfDays+"\n Hours=>"+hours+"\nminutes=>"+minutes+"\nseconds=>"+seconds);
                        }
                        else {
                            holder.mainContactLayout.setBackground(context.getResources().getDrawable(R.drawable.rectangular_edit_text));
                        }


                }

                TransitionManager.beginDelayedTransition( recyclerList, autoTransition);
                holder.mainContactLayout.setActivated(shouldExpandActionLayout);
                holder.mainContactLayout.setActivated(shouldExpandAcceptlayout);

                holder.mainContactLayout.setActivated(false);

            }
        });

       // holder.accept.setTag(position);
        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Snackbar.make(recyclerList,"accept"+position,Snackbar.LENGTH_LONG).show();
                holder.mAcceptLayout.setVisibility(View.GONE);
                holder.mActionsLayout.setVisibility(View.GONE);
                itemClickInterface.itemSelected("accept",position,mList.get(position).getNotificationId().toString(),mList.get(position).getTeam());

                calculateDateDifference(mList.get(position).getCreatedDate());
                if(numOfDays>=1 || hours>=1 || minutes>=20)
                {
                    holder.mainContactLayout.setBackgroundResource(R.drawable.card_border);
                    //Log.d("dateformatting","\ndays=>"+numOfDays+"\n Hours=>"+hours+"\nminutes=>"+minutes+"\nseconds=>"+seconds);
                }
                else {
                    holder.mainContactLayout.setBackground(context.getResources().getDrawable(R.drawable.rectangular_edit_text));
                }
            }
        });


       // holder.mGiveAction.setTag(position);
        holder.mGiveAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.mAcceptLayout.setVisibility(View.GONE);
                holder.mActionsLayout.setVisibility(View.GONE);

                itemClickInterface.itemSelected("action",position,mList.get(position).getNotificationId().toString(),mList.get(position).getTeam());

                calculateDateDifference(mList.get(position).getCreatedDate());
                if(numOfDays>=1 || hours>=1 || minutes>=20)
                {
                    holder.mainContactLayout.setBackgroundResource(R.drawable.card_border);
                    //Log.d("dateformatting","\ndays=>"+numOfDays+"\n Hours=>"+hours+"\nminutes=>"+minutes+"\nseconds=>"+seconds);
                }
                else {
                    holder.mainContactLayout.setBackground(context.getResources().getDrawable(R.drawable.rectangular_edit_text));
                }
            }
        });


        //holder.mHistory.setTag(position);
        holder.mHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.mAcceptLayout.setVisibility(View.GONE);
                holder.mActionsLayout.setVisibility(View.GONE);

                itemClickInterface.itemSelected("history",position,mList.get(position).getNotificationId().toString(),mList.get(position).getTeam());

                calculateDateDifference(mList.get(position).getCreatedDate());
                if(numOfDays>=1 || hours>=1 || minutes>=20)
                {
                    holder.mainContactLayout.setBackgroundResource(R.drawable.card_border);
                    //Log.d("dateformatting","\ndays=>"+numOfDays+"\n Hours=>"+hours+"\nminutes=>"+minutes+"\nseconds=>"+seconds);
                }
                else {
                    holder.mainContactLayout.setBackground(context.getResources().getDrawable(R.drawable.rectangular_edit_text));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFilteredList.size();
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void calculateDateDifference(String date)
    {
        try {
            Date createdTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS").parse(date);
            Date now = new Date();

            long diff =  now.getTime() - createdTime.getTime();
            numOfDays = (int) (diff / (1000 * 60 * 60 * 24));
            hours = (int) (diff / (1000 * 60 * 60));
            minutes = (int) (diff / (1000 * 60));
            seconds = (int) (diff / (1000));


        } catch (ParseException e) {
            Log.d("dateformatting",e.getMessage());
            e.printStackTrace();
        }
    }



    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout mainContactLayout;
        //LinearLayout expandCallLogs,acceptLayout;

        public TextView mErrorID, mErrorMessage, mLine,mStation, mTeam, mAcceptedby, mComplaintStatus, mDateTime;

        LinearLayout mActionsLayout,mAcceptLayout;
        LinearLayout mGiveAction,mHistory;
        TextView accept;/*decline;*/

        public ViewHolder(View rowView) {
            super(rowView);

            mainContactLayout = (LinearLayout) rowView.findViewById(R.id.mainContactLayout);

            mActionsLayout = (LinearLayout) rowView.findViewById(R.id.actions_layout);
            mGiveAction=rowView.findViewById(R.id.vL_give_action);
            mHistory=rowView.findViewById(R.id.vL_history);


            mAcceptLayout=(LinearLayout)rowView.findViewById(R.id.accept_layout);
            accept=rowView.findViewById(R.id.vT_accept);
            //decline=rowView.findViewById(R.id.vT_decline);


           // tvType = (ImageView) rowView.findViewById(R.id.tvType);

            mErrorID = itemView.findViewById(R.id.vT_error_id);
            mErrorMessage = itemView.findViewById(R.id.vT_error_message);
            mLine = itemView.findViewById(R.id.vT_line);
            mStation=itemView.findViewById(R.id.vT_station);
            mTeam = itemView.findViewById(R.id.vT_team);
            mAcceptedby = itemView.findViewById(R.id.vT_accepted_by);
            mComplaintStatus = itemView.findViewById(R.id.vT_complaint_status);
            mDateTime = itemView.findViewById(R.id.vT_date_and_time);
        }
    }

    public interface ItemClickInterface
    {
        public void itemSelected(String item,int position,String notificationId,String team);
    }

    public interface NotificationInterface
    {
        public void acceptError(String errorId,String team);
        public void giveCA(String errorId,String team);
        public void giveMOEComment(String errorId,String team);
        public void checklist(String errorId);

    }
}
