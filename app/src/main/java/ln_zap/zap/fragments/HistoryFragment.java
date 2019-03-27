package ln_zap.zap.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.lightningnetwork.lnd.lnrpc.Invoice;
import com.github.lightningnetwork.lnd.lnrpc.Payment;
import com.github.lightningnetwork.lnd.lnrpc.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ln_zap.zap.R;
import ln_zap.zap.historyList.DateItem;
import ln_zap.zap.historyList.HistoryItemAdapter;
import ln_zap.zap.historyList.HistoryListItem;
import ln_zap.zap.historyList.LnInvoiceItem;
import ln_zap.zap.historyList.LnPaymentItem;
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
           // Wallet.getInstance().fetchTransactionsFromLND();
           // Wallet.getInstance().fetchInvoicesFromLND();
            updateHistoryDisplayList();
        }



        return view;
    }

    private void updateHistoryDisplayList(){


        // Add all payment relevant items

        if (Wallet.getInstance().mOnChainTransactionList != null) {
            for (Transaction t : Wallet.getInstance().mOnChainTransactionList) {
                TransactionItem transactionItem = new TransactionItem(t);
                mHistoryItems.add(transactionItem);
            }
        }

        if (Wallet.getInstance().mInvoiceList != null) {
            for (Invoice i : Wallet.getInstance().mInvoiceList) {
                LnInvoiceItem lnInvoiceItem = new LnInvoiceItem(i);

                mHistoryItems.add(lnInvoiceItem);
            }
        }

        if (Wallet.getInstance().mPaymentsList != null) {
            for (Payment p : Wallet.getInstance().mPaymentsList) {
                LnPaymentItem lnPaymentItem = new LnPaymentItem(p);
                mHistoryItems.add(lnPaymentItem);
            }
        }


        // Sort by Date
        Collections.sort(mHistoryItems, Collections.<HistoryListItem>reverseOrder());


        // Add the Date Lines
        // Our start date is tomorrow to make sure the first date is set even if it is today.
        String tempDateText = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .format(new Date(System.currentTimeMillis()+24*60*60*1000));
        for (int i = 0; i < mHistoryItems.size(); i++) {

            String currDateText = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    .format(new Date(mHistoryItems.get(i).mCreationDate * 1000L));
            if (!tempDateText.equals(currDateText)){
                DateItem dateItem = new DateItem(mHistoryItems.get(i).mCreationDate);
                mHistoryItems.add(i, dateItem);
                i++;
                tempDateText = currDateText;
            }
        }


        // show the list

        mAdapter = new HistoryItemAdapter(mHistoryItems);
        mRecyclerView.setAdapter(mAdapter);

    }
}
