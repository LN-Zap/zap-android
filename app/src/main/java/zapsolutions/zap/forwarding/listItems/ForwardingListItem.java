package zapsolutions.zap.forwarding.listItems;

public abstract class ForwardingListItem implements Comparable<ForwardingListItem> {

    public static final int TYPE_DATE = 0;
    public static final int TYPE_FORWARDING_EVENT = 1;

    protected long mTimestamp;

    abstract public int getType();

    public boolean equalsWithSameContent(Object o) {
        return equals(o);
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    @Override
    public int compareTo(ForwardingListItem o) {
        return Long.compare(o.mTimestamp, this.mTimestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForwardingListItem that = (ForwardingListItem) o;

        if (this.getType() != that.getType()) {
            return false;
        }
        return mTimestamp == that.mTimestamp;
    }

    @Override
    public int hashCode() {
        return Long.valueOf(mTimestamp).hashCode();
    }
}
