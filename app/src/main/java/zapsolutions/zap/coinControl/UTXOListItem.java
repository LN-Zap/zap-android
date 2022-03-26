package zapsolutions.zap.coinControl;

import com.github.lightningnetwork.lnd.lnrpc.Utxo;

public class UTXOListItem implements Comparable<UTXOListItem> {

    public long mCreationDate = 0;
    private Utxo mUtxo;

    public UTXOListItem(Utxo utxo){
        mUtxo = utxo;
    }

    public Utxo getUtxo() {
        return mUtxo;
    }

    public boolean equalsWithSameContent(Object o) {
        if (!equals(o)) {
            return false;
        }

        return true;
    }

    @Override
    public int compareTo(UTXOListItem o) {
        UTXOListItem other = (UTXOListItem) o;
        return Long.compare(other.mCreationDate, this.mCreationDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UTXOListItem that = (UTXOListItem) o;

        return mCreationDate == that.mCreationDate;
    }

    @Override
    public int hashCode() {
        return Long.valueOf(mCreationDate).hashCode();
    }
}
