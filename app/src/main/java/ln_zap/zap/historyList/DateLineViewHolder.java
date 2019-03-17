package ln_zap.zap.historyList;

import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.recyclerview.widget.RecyclerView;
import ln_zap.zap.R;

public class DateLineViewHolder extends RecyclerView.ViewHolder{
    public TextView mTvDate;
    public DateLineViewHolder(View v) {
        super(v);
        mTvDate = v.findViewById(R.id.date);
    }

    public void bindDateItem(DateItem dateItem){

        // ToDo: correct locale!!!!!!!!
        String dateAsText = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .format(new Date(dateItem.mDate * 1000L));
        mTvDate.setText(dateAsText);
    }
}
