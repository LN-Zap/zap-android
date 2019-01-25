package ln_zap.zap.util;


import com.github.lightningnetwork.lnd.lnrpc.ChannelBalanceRequest;
import com.github.lightningnetwork.lnd.lnrpc.ChannelBalanceResponse;
import com.github.lightningnetwork.lnd.lnrpc.LightningGrpc;
import com.github.lightningnetwork.lnd.lnrpc.PayReq;
import com.github.lightningnetwork.lnd.lnrpc.WalletBalanceRequest;
import com.github.lightningnetwork.lnd.lnrpc.WalletBalanceResponse;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import ln_zap.zap.connection.LndConnection;

public class Wallet {

    private static final String LOG_TAG = "Wallet Util";

    private static Wallet mInstance = null;
    public PayReq mPaymentRequest;
    private long mOnChainBalanceTotal = 0;
    private long mOnChainBalanceConfirmed = 0;
    private long mOnChainBalanceUnconfirmed = 0;
    private long mChannelBalance = 0;
    private long mChannelBalancePending = 0;

    private final Set<BalanceListener> listeners = new HashSet<>();



    private Wallet() { ; }

    public static Wallet getInstance() {

        if(mInstance == null) {
            mInstance = new Wallet();
        }

        return mInstance;
    }


    /**
     * This will return a Balance object that contains all types of balances.
     * Please note that this might be different from the actual balance on LND.
     * To update what this function returns call fetchBalanceFromLND()
     *
     * @return
     */
    public Balances getBalances(){
        return new Balances(mOnChainBalanceTotal, mOnChainBalanceConfirmed,
                mOnChainBalanceUnconfirmed, mChannelBalance, mChannelBalancePending);
    }

    /**
     * This will fetch the current balance from LND.
     * All Listeners registered to BalanceListener will be informed about any changes.
     */
    public void fetchBalanceFromLND(){

        // Retrieve balance with gRPC (async)

        // fetch on-chain balance
        LightningGrpc.LightningFutureStub asyncBalanceClient = LightningGrpc
                .newFutureStub(LndConnection.getInstance().getSecureChannel())
                .withCallCredentials(LndConnection.getInstance().getMacaroon());

        WalletBalanceRequest aBalanceRequest = WalletBalanceRequest.newBuilder().build();
        final ListenableFuture<WalletBalanceResponse> balanceFuture = asyncBalanceClient.walletBalance(aBalanceRequest);

        balanceFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    WalletBalanceResponse balanceResponse = balanceFuture.get();
                    ZapLog.debug(LOG_TAG,balanceResponse.toString());
                    // Update the on-chain balances of our wallet util to the fetched values
                    setOnChainBalance(balanceResponse.getTotalBalance(),
                            balanceResponse.getConfirmedBalance(),
                            balanceResponse.getConfirmedBalance());
                } catch (InterruptedException e) {
                    ZapLog.debug(LOG_TAG,"Wallet balance request interrupted.");
                } catch (ExecutionException e) {
                    ZapLog.debug(LOG_TAG,"Exception in wallet balance request task.");
                }
            }
        },new ExecuteOnCaller());


        // fetch channel balance
        LightningGrpc.LightningFutureStub asyncChannelBalanceClient = LightningGrpc
                .newFutureStub(LndConnection.getInstance().getSecureChannel())
                .withCallCredentials(LndConnection.getInstance().getMacaroon());


        ChannelBalanceRequest asyncChannelBalanceRequest = ChannelBalanceRequest.newBuilder().build();
        final ListenableFuture<ChannelBalanceResponse> channelBalanceFuture = asyncChannelBalanceClient.
                channelBalance(asyncChannelBalanceRequest);

        channelBalanceFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ChannelBalanceResponse channelBalanceResponse = channelBalanceFuture.get();
                    ZapLog.debug(LOG_TAG,channelBalanceResponse.toString());
                    // Update the channel balances of our wallet util to the fetched values
                    setChannelBalance(channelBalanceResponse.getBalance(),
                            channelBalanceResponse.getPendingOpenBalance());
                } catch (InterruptedException e) {
                    ZapLog.debug(LOG_TAG,"Channel balance request interrupted.");
                } catch (ExecutionException e) {
                    ZapLog.debug(LOG_TAG,"Exception in channel balance request task.");
                }
            }
        },new ExecuteOnCaller());
    }

    private void setOnChainBalance(long total, long confirmed, long unconfirmed){
        mOnChainBalanceTotal = total;
        mOnChainBalanceConfirmed = confirmed;
        mOnChainBalanceUnconfirmed = unconfirmed;
        broadcastBalanceUpdate();
    }

    private void setChannelBalance(long balance, long pending){
        mChannelBalance = balance;
        mChannelBalancePending = pending;
        broadcastBalanceUpdate();
    }



    // Event handling to notify all registered Listeners to a balance update.

    private void broadcastBalanceUpdate() {
        for( BalanceListener listener : listeners ) {
            listener.onBalanceUpdated();
        }
    }

    public void registerBalanceListener(BalanceListener listener) {
        listeners.add( listener );
    }

    public void unregisterBalanceListener(BalanceListener listener) {
        listeners.remove(listener);
    }

    public interface BalanceListener {
        void onBalanceUpdated();
    }
}




