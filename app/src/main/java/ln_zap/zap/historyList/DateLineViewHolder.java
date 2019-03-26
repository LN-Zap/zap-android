package ln_zap.zap.historyList;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import java.text.DateFormat;

import java.util.Date;

import androidx.recyclerview.widget.RecyclerView;
import ln_zap.zap.R;

public class DateLineViewHolder extends RecyclerView.ViewHolder{
    private TextView mTvDate;
    private Context mContext;
    public DateLineViewHolder(View v) {
        super(v);
        mTvDate = v.findViewById(R.id.date);
        mContext = v.getContext();
    }

    public void bindDateItem(DateItem dateItem){
        DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, mContext.getResources().getConfiguration().locale);
        String formattedDate = df.format(new Date(dateItem.mDate * 1000L));
        mTvDate.setText(formattedDate);
    }
}
