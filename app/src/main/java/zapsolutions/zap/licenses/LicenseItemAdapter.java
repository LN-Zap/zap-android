package zapsolutions.zap.licenses;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import java.util.List;

import zapsolutions.zap.R;


public class LicenseItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final SortedList<LicenseListItem> mSortedList = new SortedList<>(LicenseListItem.class, new SortedList.Callback<LicenseListItem>() {
        @Override
        public int compare(LicenseListItem i1, LicenseListItem i2) {
            return i1.compareTo(i2);
        }

        @Override
        public void onChanged(int position, int count) {
            notifyItemRangeChanged(position, count);
        }

        @Override
        public boolean areContentsTheSame(LicenseListItem oldItem, LicenseListItem newItem) {
            return oldItem.equalsWithSameContent(newItem);
        }

        @Override
        public boolean areItemsTheSame(LicenseListItem item1, LicenseListItem item2) {
            return item1.equals(item2);
        }

        @Override
        public void onInserted(int position, int count) {
            notifyItemRangeInserted(position, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            notifyItemRangeRemoved(position, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            notifyItemMoved(fromPosition, toPosition);
        }
    });


    // Construct the adapter with a data list
    public LicenseItemAdapter() {
    }

    @Override
    public int getItemViewType(int position) {
        return mSortedList.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View licenseView = inflater.inflate(R.layout.license_list_element, parent, false);
        return new LicenseViewHolder(licenseView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        LicenseViewHolder licenseHolder = (LicenseViewHolder) holder;
        LicenseListItem licenseItem = (LicenseListItem) mSortedList.get(position);
        licenseHolder.bindLicenseListItem(licenseItem);
    }

    public void replaceAll(List<LicenseListItem> items) {
        mSortedList.replaceAll(items);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mSortedList.size();
    }
}
