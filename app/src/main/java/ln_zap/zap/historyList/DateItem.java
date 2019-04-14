package ln_zap.zap.historyList;

public class DateItem extends HistoryListItem {

    public long mDate;

    public DateItem(long date) {
        mDate = date;
    }

    @Override
    public int getType() {
        return TYPE_DATE;
    }
}
