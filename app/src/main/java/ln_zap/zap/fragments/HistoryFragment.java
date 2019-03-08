package ln_zap.zap.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.lightningnetwork.lnd.lnrpc.Transaction;

import java.util.ArrayList;
import java.util.List;

import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ln_zap.zap.R;
import ln_zap.zap.historyList.HistoryItemAdapter;
import ln_zap.zap.historyList.HistoryListItem;
import ln_zap.zap.historyList.TransactionItem;
import ln_zap.zap.util.Wallet;

/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment {

    private static final String LOG_TAG = "History Fragment";

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SharedPreferences mPrefs;

    private List<HistoryListItem> mHistoryItems;

    public HistoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mRecyclerView = view.findViewById(R.id.historyList);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);


        mHistoryItems = new ArrayList<>();

        if (mPrefs.getBoolean("isWalletSetup", false)) {
            Wallet.getInstance().fetchTransactionsFromLND();
            Wallet.getInstance().fetchInvoicesFromLND();
            updateHistoryDisplayList();
        }



        return view;
    }

    private void updateHistoryDisplayList(){


        if (Wallet.getInstance().mOnChainTransactionList != null) {
            for (Transaction t : Wallet.getInstance().mOnChainTransactionList) {
                TransactionItem transactionItem = new TransactionItem(t);
                mHistoryItems.add(transactionItem);
            }
        }

        // show the list

        mAdapter = new HistoryItemAdapter(mHistoryItems);
        mRecyclerView.setAdapter(mAdapter);


    }
}
