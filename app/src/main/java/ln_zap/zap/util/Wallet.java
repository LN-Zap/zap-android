package ln_zap.zap.util;


import com.github.lightningnetwork.lnd.lnrpc.ChannelBalanceRequest;
import com.github.lightningnetwork.lnd.lnrpc.ChannelBalanceResponse;
import com.github.lightningnetwork.lnd.lnrpc.GetInfoRequest;
import com.github.lightningnetwork.lnd.lnrpc.GetInfoResponse;
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
    public PayReq mPaymentRequest = null;
    private long mOnChainBalanceTotal = 0;
    private long mOnChainBalanceConfirmed = 0;
    private long mOnChainBalanceUnconfirmed = 0;
    private long mChannelBalance = 0;
    private long mChannelBalancePending = 0;

    private boolean mInfoFetched = false;
    private boolean mSyncedToChain = false;
    private boolean mTestnet = false;
    private String mLNDVersion = "not connected";

    private final Set<BalanceListener> mBalanceListeners = new HashSet<>();
    private final Set<InfoListener> mInfoListeners = new HashSet<>();



    private Wallet() { ; }

    public static Wallet getInstance() {

        if(mInstance == null) {
            mInstance = new Wallet();
        }

        return mInstance;
    }

    /**
     * Use this to reset the wallet information when the connection type was changed.
     */
    public void reset(){
        mPaymentRequest = null;
        mOnChainBalanceTotal = 0;
        mOnChainBalanceConfirmed = 0;
        mOnChainBalanceUnconfirmed = 0;
        mChannelBalance = 0;
        mChannelBalancePending = 0;

        mInfoFetched = false;
        mSyncedToChain = false;
        mTestnet = false;
        mLNDVersion = "not connected";
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
     * This will return a Balance object that contains all types of balances.
     * Use this only for the demo mode. The balances are not real and will always be the same.
     *
     * @return
     */
    public Balances getDemoBalances(){
        return new Balances(2637452, 2637452,
                0, 200000, 0);
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

        WalletBalanceRequest asyncBalanceRequest = WalletBalanceRequest.newBuilder().build();
        final ListenableFuture<WalletBalanceResponse> balanceFuture = asyncBalanceClient.walletBalance(asyncBalanceRequest);

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

    public void fetchInfoFromLND(){
        // Retrieve info from LND with gRPC (async)

        LightningGrpc.LightningFutureStub asyncInfoClient = LightningGrpc
                .newFutureStub(LndConnection.getInstance().getSecureChannel())
                .withCallCredentials(LndConnection.getInstance().getMacaroon());

        GetInfoRequest asyncInfoRequest = GetInfoRequest.newBuilder().build();
        final ListenableFuture<GetInfoResponse> infoFuture = asyncInfoClient.getInfo(asyncInfoRequest);

        infoFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    GetInfoResponse infoResponse = infoFuture.get();
                    ZapLog.debug(LOG_TAG,infoResponse.toString());
                    // Save the received data.
                    mSyncedToChain = infoResponse.getSyncedToChain();
                    mTestnet = infoResponse.getTestnet();
                    mLNDVersion = infoResponse.getVersion();
                    mInfoFetched = true;
                    broadcastInfoUpdate();
                } catch (InterruptedException e) {
                    ZapLog.debug(LOG_TAG,"Info request interrupted.");
                } catch (ExecutionException e) {
                    ZapLog.debug(LOG_TAG,"Exception in info request task.");
                }
            }
        },new ExecuteOnCaller());
    }

    public boolean isSyncedToChain() {
        return mSyncedToChain;
    }

    public boolean isTestnet() {
        return mTestnet;
    }

    public String getLNDVersion() {
        return mLNDVersion;
    }

    public boolean isInfoFetched() {
        return mInfoFetched;
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



    // Event handling to notify all registered listeners to a balance update.

    private void broadcastBalanceUpdate() {
        for( BalanceListener listener : mBalanceListeners) {
            listener.onBalanceUpdated();
        }
    }

    public void registerBalanceListener(BalanceListener listener) {
        mBalanceListeners.add(listener);
    }

    public void unregisterBalanceListener(BalanceListener listener) {
        mBalanceListeners.remove(listener);
    }

    public interface BalanceListener {
        void onBalanceUpdated();
    }


    // Event handling to notify all registeredlisteners to an info update.

    private void broadcastInfoUpdate() {
        for( InfoListener listener : mInfoListeners) {
            listener.onInfoUpdated();
        }
    }

    public void registerInfoListener(InfoListener listener) {
        mInfoListeners.add(listener);
    }

    public void unregisterInfoListener(InfoListener listener) {
        mInfoListeners.remove(listener);
    }

    public interface InfoListener {
        void onInfoUpdated();
    }
}




