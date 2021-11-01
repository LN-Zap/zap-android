package zapsolutions.zap.contacts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import java.util.List;

import zapsolutions.zap.R;


public class ContactItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final SortedList<Contact> mSortedList = new SortedList<>(Contact.class, new SortedList.Callback<Contact>() {
        @Override
        public int compare(Contact c1, Contact c2) {
            return c1.compareTo(c2);
        }

        @Override
        public void onChanged(int position, int count) {
            notifyItemRangeChanged(position, count);
        }

        @Override
        public boolean areContentsTheSame(Contact oldContact, Contact newContact) {
            return oldContact.getAlias().equals(newContact.getContactData());
        }

        @Override
        public boolean areItemsTheSame(Contact contact1, Contact contact2) {
            return contact1.getContactData().equals(contact2.getContactData());
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

    private ContactSelectListener mContactSelectListener;

    // Construct the adapter with a data list
    public ContactItemAdapter(ContactSelectListener contactSelectListener) {
        ;
        mContactSelectListener = contactSelectListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View contactItemView = inflater.inflate(R.layout.contact_list_element, parent, false);
        return new ContactItemViewHolder(contactItemView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        ContactItemViewHolder contactItemViewHolder = (ContactItemViewHolder) holder;
        Contact contact = mSortedList.get(position);
        contactItemViewHolder.bindContactItem(contact);
        contactItemViewHolder.addOnContactSelectListener(mContactSelectListener);
    }

    public void add(Contact contact) {
        mSortedList.add(contact);
    }

    public void remove(Contact contact) {
        mSortedList.remove(contact);
    }

    public void add(List<Contact> contacts) {
        mSortedList.addAll(contacts);
    }

    public void remove(List<Contact> contacts) {
        mSortedList.beginBatchedUpdates();
        for (Contact contact : contacts) {
            mSortedList.remove(contact);
        }
        mSortedList.endBatchedUpdates();
    }

    public void replaceAll(List<Contact> contacts) {
        mSortedList.beginBatchedUpdates();
        for (int i = mSortedList.size() - 1; i >= 0; i--) {
            final Contact contact = mSortedList.get(i);
            if (!contacts.contains(contact)) {
                mSortedList.remove(contact);
            }
        }
        mSortedList.addAll(contacts);
        mSortedList.endBatchedUpdates();
    }

    // Return the size of your sorted list (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mSortedList.size();
    }
}
