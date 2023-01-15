package zapsolutions.zap.forwarding.listItems;

public class DateItem extends ForwardingListItem {

    public long mDate; // in milliseconds

    public DateItem(long timestamp) {

        // The timestamp for forwarding events is provided in nano seconds. The function to format the date later needs milliseconds.
        mDate = timestamp / 1000000L;

        // To get the date line to show up at the correct position in the sorted list, we have to set its timestamp correctly.
        // We set it to 1 nanosecond before the day ends.
        // 86400000000000 = Nanoseconds of one day (60 * 60 * 24 * 1000 * 1000 * 1000)
        mTimestamp = (timestamp - (timestamp % 86400000000000L) + (86400000000000L - 1));
    }

    @Override
    public int getType() {
        return TYPE_DATE;
    }
}
