package zapsolutions.zap.transactionHistory.listItems;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class DateItem extends HistoryListItem {

    public long mDate;

    public DateItem(long date) {
        mDate = date;

        // To get the dateline to show up at the correct position in the sorted list, we have to set its CreationDate.
        // We set it to 1 second before the day ends.
        String tempDateText = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .format(date * 1000L);
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    .parse(tempDateText);
            mCreationDate = d.getTime() / 1000 + (60 * 60 * 24) - 1;
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getType() {
        return TYPE_DATE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DateItem dateItem = (DateItem) o;
        return mCreationDate == dateItem.mCreationDate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mCreationDate);
    }
}
