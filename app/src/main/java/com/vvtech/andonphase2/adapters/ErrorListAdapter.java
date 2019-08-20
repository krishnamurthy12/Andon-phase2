package com.vvtech.andonphase2.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vvtech.andonphase2.R;
import com.vvtech.andonphase2.apiresponses.allerrors.ErrorList;

import java.util.List;

public class ErrorListAdapter extends RecyclerView.Adapter<ErrorListAdapter.MyHolder> {

    Context context;
    List<ErrorList> mList;
    ErrorListInterface errorListInterface=null;

    public ErrorListAdapter(Context context, List<ErrorList> mList) {
        this.context = context;
        this.mList = mList;
        errorListInterface= (ErrorListInterface) context;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.errorlist_singlerow, parent, false);
        return new ErrorListAdapter.MyHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {


        holder.mErrorTitle.setText(mList.get(position).getErrorname());
        holder.mLineName.setText(mList.get(position).getLinename());
        holder.mStationName.setText(mList.get(position).getStation());
        holder.mDate.setText(mList.get(position).getCreateddate());

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

        TextView mErrorTitle,mLineName,mStationName,mDate;
        public MyHolder(View itemView) {
            super(itemView);

            mErrorTitle=itemView.findViewById(R.id.vT_els_errorname);
            mLineName=itemView.findViewById(R.id.vT_els_linename);
            mStationName=itemView.findViewById(R.id.vT_els_stationname);
            mDate=itemView.findViewById(R.id.vT_els_date);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    errorListInterface.getErrorId(getAdapterPosition());
                }
            });
        }
    }

    public interface ErrorListInterface
    {
        public void getErrorId(int position);
    }
}
