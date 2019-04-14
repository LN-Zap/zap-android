package ln_zap.zap.historyList;

public abstract class HistoryListItem implements Comparable<HistoryListItem> {

    public static final int TYPE_DATE = 0;
    public static final int TYPE_TRANSACTION = 1;
    public static final int TYPE_LN_INVOICE = 2;
    public static final int TYPE_LN_PAYMENT = 3;
    public static final int TYPE_DEMO = 4;

    public long mCreationDate = 0;

    abstract public int getType();

    @Override
    public int compareTo(HistoryListItem o) {
        HistoryListItem other = (HistoryListItem) o;
        return Long.compare(this.mCreationDate, other.mCreationDate);
    }
}
