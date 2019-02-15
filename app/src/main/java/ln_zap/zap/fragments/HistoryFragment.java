package ln_zap.zap.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.lightningnetwork.lnd.lnrpc.GetTransactionsRequest;
import com.github.lightningnetwork.lnd.lnrpc.LightningGrpc;
import com.github.lightningnetwork.lnd.lnrpc.Transaction;
import com.github.lightningnetwork.lnd.lnrpc.TransactionDetails;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ln_zap.zap.R;
import ln_zap.zap.historyList.HistoryItemAdapter;
import ln_zap.zap.connection.LndConnection;
import ln_zap.zap.historyList.HistoryListItem;
import ln_zap.zap.historyList.TransactionItem;
import ln_zap.zap.util.ExecuteOnCaller;
import ln_zap.zap.util.ZapLog;

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
            updateHistory();
        }

        return view;
    }

    private void updateHistory(){
        // fetch on-chain transactions
        LightningGrpc.LightningFutureStub asyncTransactionsClient = LightningGrpc
                .newFutureStub(LndConnection.getInstance().getSecureChannel())
                .withCallCredentials(LndConnection.getInstance().getMacaroon());

        GetTransactionsRequest asyncTransactionRequest = GetTransactionsRequest.newBuilder().build();
        final ListenableFuture<TransactionDetails> transactionFuture = asyncTransactionsClient.getTransactions(asyncTransactionRequest);

        transactionFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    TransactionDetails transactionResponse = transactionFuture.get();

                    List<Transaction> transactionList = Lists.reverse(transactionResponse.getTransactionsList());
                    for (Transaction t : transactionList) {
                        TransactionItem transactionItem = new TransactionItem(t);
                        mHistoryItems.add(transactionItem);
                    }

                    // show the list

                    mAdapter = new HistoryItemAdapter(mHistoryItems);
                    mRecyclerView.setAdapter(mAdapter);

                    ZapLog.debug(LOG_TAG, transactionResponse.toString());
                } catch (InterruptedException e) {
                    ZapLog.debug(LOG_TAG, "Transaction request interrupted.");
                } catch (ExecutionException e) {
                    ZapLog.debug(LOG_TAG, "Exception in transaction request task.");
                }
            }
        }, new ExecuteOnCaller());
    }
}
