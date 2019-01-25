package ln_zap.zap.historyList;

public abstract class HistoryListItem {

    public static final int TYPE_DATE = 0;
    public static final int TYPE_TRANSACTION = 1;

    abstract public int getType();
}
