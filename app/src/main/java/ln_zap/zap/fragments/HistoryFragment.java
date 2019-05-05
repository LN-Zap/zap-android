package ln_zap.zap.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.github.lightningnetwork.lnd.lnrpc.Invoice;
import com.github.lightningnetwork.lnd.lnrpc.Payment;
import com.github.lightningnetwork.lnd.lnrpc.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import ln_zap.zap.R;
import ln_zap.zap.historyList.DateItem;
import ln_zap.zap.historyList.HistoryItemAdapter;
import ln_zap.zap.historyList.HistoryListItem;
import ln_zap.zap.historyList.LnInvoiceItem;
import ln_zap.zap.historyList.LnPaymentItem;
import ln_zap.zap.historyList.TransactionItem;
import ln_zap.zap.util.Wallet;
import ln_zap.zap.util.ZapLog;

/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment implements Wallet.HistoryListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String LOG_TAG = "History Fragment";

    private ImageView mListOptions;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private TextView mEmptyListText;
    private TextView mTitle;
    private SharedPreferences mPrefs;
    private SwipeRefreshLayout mSwipeRefreshLayout;


    private List<HistoryListItem> mHistoryItems;

    private boolean mHistoryChangeListenerRegistered = false;

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
        mListOptions = view.findViewById(R.id.listOptions);
        mEmptyListText = view.findViewById(R.id.listEmpty);
        mTitle = view.findViewById(R.id.heading);


        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // SwipeRefreshLayout
        mSwipeRefreshLayout = view.findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mSwipeRefreshLayout.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.seaBlueGradient3));
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.white));


        mHistoryItems = new ArrayList<>();


        updateHistoryDisplayList();


        mListOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
                LayoutInflater adbInflater = LayoutInflater.from(getActivity());
                View DialogLayout = adbInflater.inflate(R.layout.dialog_history_list_settings, null);
                Switch normalSwitch = DialogLayout.findViewById(R.id.switchNormal);
                Switch expiredSwitch = DialogLayout.findViewById(R.id.switchExpired);
                Switch internalSwitch = DialogLayout.findViewById(R.id.switchInternal);
                normalSwitch.setChecked(mPrefs.getBoolean("showNormalTransactions", true));
                expiredSwitch.setChecked(mPrefs.getBoolean("showExpiredRequests", false));
                internalSwitch.setChecked(mPrefs.getBoolean("showInternalTransactions", true));
                normalSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        SharedPreferences.Editor editor = mPrefs.edit();
                        editor.putBoolean("showNormalTransactions", isChecked);
                        editor.commit();
                        updateHistoryDisplayList();
                    }
                });
                expiredSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        SharedPreferences.Editor editor = mPrefs.edit();
                        editor.putBoolean("showExpiredRequests", isChecked);
                        editor.commit();
                        updateHistoryDisplayList();
                    }
                });
                internalSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        SharedPreferences.Editor editor = mPrefs.edit();
                        editor.putBoolean("showInternalTransactions", isChecked);
                        editor.commit();
                        updateHistoryDisplayList();
                    }
                });
                adb.setView(DialogLayout);
                adb.setTitle(R.string.filter_transactions);
                adb.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                Dialog dlg = adb.create();
                // Apply FLAG_SECURE to dialog to prevent screen recording
                if (mPrefs.getBoolean("preventScreenRecording", true)) {
                    dlg.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
                }
                dlg.show();
            }
        });


        return view;
    }

    private void updateHistoryDisplayList() {

        mHistoryItems.clear();

        List<HistoryListItem> normalPayments = new LinkedList<>();
        List<HistoryListItem> expiredRequest = new LinkedList<>();
        List<HistoryListItem> internalTransactions = new LinkedList<>();

        if (mPrefs.getBoolean("isWalletSetup", false)) {

            // Add all payment relevant items to one of the lists above

            if (Wallet.getInstance().mOnChainTransactionList != null) {
                for (Transaction t : Wallet.getInstance().mOnChainTransactionList) {
                    TransactionItem transactionItem = new TransactionItem(t);

                    if (Wallet.getInstance().isTransactionInternal(t)) {
                        internalTransactions.add(transactionItem);
                    } else {
                        normalPayments.add(transactionItem);
                    }
                }
            }

            if (Wallet.getInstance().mInvoiceList != null) {
                for (Invoice i : Wallet.getInstance().mInvoiceList) {

                    LnInvoiceItem lnInvoiceItem = new LnInvoiceItem(i);

                    // add to list according to current state of the invoice
                    if (Wallet.getInstance().isInvoicePayed(i)) {
                        normalPayments.add(lnInvoiceItem);
                    } else {
                        if (Wallet.getInstance().isInvoiceExpired(i)) {
                            expiredRequest.add(lnInvoiceItem);
                        } else {
                            normalPayments.add(lnInvoiceItem);
                        }
                    }
                }
            }

            if (Wallet.getInstance().mPaymentsList != null) {
                for (Payment p : Wallet.getInstance().mPaymentsList) {
                    LnPaymentItem lnPaymentItem = new LnPaymentItem(p);
                    normalPayments.add(lnPaymentItem);
                }
            }

        }

        // Apply filters

        if (mPrefs.getBoolean("showNormalTransactions", true)) {
            mHistoryItems.addAll(normalPayments);
        }

        if (mPrefs.getBoolean("showExpiredRequests", false)) {
            mHistoryItems.addAll(expiredRequest);
        }

        if (mPrefs.getBoolean("showInternalTransactions", true)) {
            mHistoryItems.addAll(internalTransactions);
        }


        // Sort by Date
        Collections.sort(mHistoryItems, Collections.<HistoryListItem>reverseOrder());


        // Add the Date Lines
        // Our start date is tomorrow to make sure the first date is set even if it is today.
        String tempDateText = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .format(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000));
        for (int i = 0; i < mHistoryItems.size(); i++) {

            String currDateText = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    .format(new Date(mHistoryItems.get(i).mCreationDate * 1000L));
            if (!tempDateText.equals(currDateText)) {
                DateItem dateItem = new DateItem(mHistoryItems.get(i).mCreationDate);
                mHistoryItems.add(i, dateItem);
                i++;
                tempDateText = currDateText;
            }
        }


        // Show "No transactions" if the list is empty

        if (mHistoryItems.size() == 0) {
            mEmptyListText.setVisibility(View.VISIBLE);
        } else {
            mEmptyListText.setVisibility(View.GONE);
        }


        // Show the list

        mAdapter = new HistoryItemAdapter(mHistoryItems);
        mRecyclerView.setAdapter(mAdapter);

    }

    @Override
    public void onHistoryUpdated() {
        updateHistoryDisplayList();
        mSwipeRefreshLayout.setRefreshing(false);
        ZapLog.debug(LOG_TAG, "History updated!");
    }

    @Override
    public void onResume() {
        super.onResume();

        // Register listeners
        if (!mHistoryChangeListenerRegistered) {
            Wallet.getInstance().registerHistoryListener(this);
            mHistoryChangeListenerRegistered = true;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unregister listeners
        Wallet.getInstance().unregisterHistoryListener(this);
    }

    @Override
    public void onRefresh() {
        if (mPrefs.getBoolean("isWalletSetup", false)) {
            Wallet.getInstance().fetchLNDTransactionHistory();
        } else {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }
}
