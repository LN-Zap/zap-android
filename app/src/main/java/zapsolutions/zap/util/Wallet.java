package zapsolutions.zap.util;


import android.content.Context;
import android.os.Handler;

import com.github.lightningnetwork.lnd.lnrpc.ChanBackupSnapshot;
import com.github.lightningnetwork.lnd.lnrpc.Channel;
import com.github.lightningnetwork.lnd.lnrpc.ChannelBackupSubscription;
import com.github.lightningnetwork.lnd.lnrpc.ChannelBalanceRequest;
import com.github.lightningnetwork.lnd.lnrpc.ChannelBalanceResponse;
import com.github.lightningnetwork.lnd.lnrpc.ChannelCloseSummary;
import com.github.lightningnetwork.lnd.lnrpc.ChannelEventSubscription;
import com.github.lightningnetwork.lnd.lnrpc.ChannelEventUpdate;
import com.github.lightningnetwork.lnd.lnrpc.ChannelPoint;
import com.github.lightningnetwork.lnd.lnrpc.CloseChannelRequest;
import com.github.lightningnetwork.lnd.lnrpc.ClosedChannelsRequest;
import com.github.lightningnetwork.lnd.lnrpc.ClosedChannelsResponse;
import com.github.lightningnetwork.lnd.lnrpc.ConnectPeerRequest;
import com.github.lightningnetwork.lnd.lnrpc.GetInfoRequest;
import com.github.lightningnetwork.lnd.lnrpc.GetTransactionsRequest;
import com.github.lightningnetwork.lnd.lnrpc.Invoice;
import com.github.lightningnetwork.lnd.lnrpc.InvoiceSubscription;
import com.github.lightningnetwork.lnd.lnrpc.LightningAddress;
import com.github.lightningnetwork.lnd.lnrpc.ListChannelsRequest;
import com.github.lightningnetwork.lnd.lnrpc.ListChannelsResponse;
import com.github.lightningnetwork.lnd.lnrpc.ListInvoiceRequest;
import com.github.lightningnetwork.lnd.lnrpc.ListPaymentsRequest;
import com.github.lightningnetwork.lnd.lnrpc.ListPeersRequest;
import com.github.lightningnetwork.lnd.lnrpc.NodeInfo;
import com.github.lightningnetwork.lnd.lnrpc.NodeInfoRequest;
import com.github.lightningnetwork.lnd.lnrpc.OpenChannelRequest;
import com.github.lightningnetwork.lnd.lnrpc.Payment;
import com.github.lightningnetwork.lnd.lnrpc.Peer;
import com.github.lightningnetwork.lnd.lnrpc.PendingChannelsRequest;
import com.github.lightningnetwork.lnd.lnrpc.PendingChannelsResponse;
import com.github.lightningnetwork.lnd.lnrpc.Transaction;
import com.github.lightningnetwork.lnd.lnrpc.UnlockWalletRequest;
import com.github.lightningnetwork.lnd.lnrpc.WalletBalanceRequest;
import com.github.lightningnetwork.lnd.lnrpc.WalletBalanceResponse;
import com.github.lightningnetwork.lnd.routerrpc.HtlcEvent;
import com.github.lightningnetwork.lnd.routerrpc.SubscribeHtlcEventsRequest;
import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import zapsolutions.zap.R;
import zapsolutions.zap.baseClasses.App;
import zapsolutions.zap.connection.lndConnection.LndConnection;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfigsManager;
import zapsolutions.zap.lightning.LightningNodeUri;
import zapsolutions.zap.lightning.LightningParser;
import zapsolutions.zap.tor.TorManager;

import static zapsolutions.zap.util.UtilFunctions.hexStringToByteArray;

public class Wallet {

    private static final String LOG_TAG = Wallet.class.getName();

    private static Wallet mInstance = null;
    private final Set<BalanceListener> mBalanceListeners = new HashSet<>();
    private final Set<InfoListener> mInfoListeners = new HashSet<>();
    private final Set<HistoryListener> mHistoryListeners = new HashSet<>();
    private final Set<LndConnectionTestListener> mLndConnectionTestListeners = new HashSet<>();
    private final Set<WalletLoadedListener> mWalletLoadedListeners = new HashSet<>();
    private final Set<InvoiceSubscriptionListener> mInvoiceSubscriptionListeners = new HashSet<>();
    private final Set<TransactionSubscriptionListener> mTransactionSubscriptionListeners = new HashSet<>();
    private final Set<ChannelEventSubscriptionListener> mChannelEventSubscriptionListeners = new HashSet<>();
    private final Set<ChannelsUpdatedSubscriptionListener> mChannelsUpdatedSubscriptionListeners = new HashSet<>();
    private final Set<ChannelBackupSubscriptionListener> mChannelBackupSubscriptionListeners = new HashSet<>();
    private final Set<ChannelCloseUpdateListener> mChannelCloseUpdateListeners = new HashSet<>();
    private final Set<ChannelOpenUpdateListener> mChannelOpenUpdateListeners = new HashSet<>();
    private final Set<HtlcSubscriptionListener> mHtlcSubscriptionListeners = new HashSet<>();

    public List<Transaction> mOnChainTransactionList;
    public List<Invoice> mInvoiceList;
    public List<Invoice> mTempInvoiceUpdateList;
    public List<Payment> mPaymentsList;
    public List<Channel> mOpenChannelsList;
    public List<PendingChannelsResponse.PendingOpenChannel> mPendingOpenChannelsList;
    public List<PendingChannelsResponse.ClosedChannel> mPendingClosedChannelsList;
    public List<PendingChannelsResponse.ForceClosedChannel> mPendingForceClosedChannelsList;
    public List<PendingChannelsResponse.WaitingCloseChannel> mPendingWaitingCloseChannelsList;
    public List<ChannelCloseSummary> mClosedChannelsList;
    public List<NodeInfo> mNodeInfos = new LinkedList<>();

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private long mOnChainBalanceTotal = 0;
    private long mOnChainBalanceConfirmed = 0;
    private long mOnChainBalanceUnconfirmed = 0;
    private long mChannelBalance = 0;
    private long mChannelBalancePendingOpen = 0;
    private long mChannelBalanceLimbo = 0;
    private String mIdentityPubKey;
    private LightningNodeUri[] mNodeUris;
    private boolean mConnectedToLND = false;
    private boolean mInfoFetched = false;
    private boolean mBalancesFetched = false;
    private boolean mChannelsFetched = false;
    private boolean mIsWalletReady = false;
    private boolean mSyncedToChain = false;
    private boolean mTransactionUpdated = false;
    private boolean mInvoicesUpdated = false;
    private boolean mPaymentsUpdated = false;
    private boolean mUpdatingHistory = false;
    private Network mNetwork = Network.MAINNET;
    private String mLNDVersionString;
    private Handler mHandler = new Handler();
    private DebounceHandler mChannelsUpdateDebounceHandler = new DebounceHandler();
    private DebounceHandler mBalancesDebounceHandler = new DebounceHandler();

    private Wallet() {
        ;
    }

    public static Wallet getInstance() {

        if (mInstance == null) {
            mInstance = new Wallet();
        }

        return mInstance;
    }

    /**
     * Use this to reset the wallet information when the wallet was switched.
     */
    public void reset() {
        compositeDisposable.clear();
        mOnChainBalanceTotal = 0;
        mOnChainBalanceConfirmed = 0;
        mOnChainBalanceUnconfirmed = 0;
        mChannelBalance = 0;
        mChannelBalancePendingOpen = 0;
        mChannelBalanceLimbo = 0;

        mConnectedToLND = false;
        mOnChainTransactionList = null;
        mInvoiceList = null;
        mTempInvoiceUpdateList = null;
        mPaymentsList = null;
        mOpenChannelsList = null;
        mPendingOpenChannelsList = null;
        mPendingClosedChannelsList = null;
        mPendingForceClosedChannelsList = null;
        mPendingWaitingCloseChannelsList = null;

        mTransactionUpdated = false;
        mInvoicesUpdated = false;
        mPaymentsUpdated = false;
        mUpdatingHistory = false;

        mInfoFetched = false;
        mBalancesFetched = false;
        mChannelsFetched = false;
        mIsWalletReady = false;
        mSyncedToChain = false;
        mNetwork = Network.MAINNET;
        mIdentityPubKey = null;
        mNodeUris = null;
        mHandler.removeCallbacksAndMessages(null);
        App.getAppContext().connectionToLNDEstablished = false;
        mChannelsUpdateDebounceHandler.shutdown();
        mBalancesDebounceHandler.shutdown();
    }

    /**
     * This will be used on loading. If this request finishes without an error, our connection to LND is established.
     * All listeners registered to LndConnectionTestListener will be informed about the result.
     */
    public void testLndConnectionAndLoadWallet() {
        // Retrieve info from LND with gRPC (async)

        mIsWalletReady = false;
        mBalancesFetched = false;
        mChannelsFetched = false;

        ZapLog.d(LOG_TAG, "LND connection test.");

        broadcastLndConnectionTestStarted();

        compositeDisposable.add(LndConnection.getInstance().getLightningService().getInfo(GetInfoRequest.newBuilder().build())
                .timeout(RefConstants.TIMEOUT_LONG * TorManager.getInstance().getTorTimeoutMultiplier(), TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe(infoResponse -> {
                    ZapLog.d(LOG_TAG, "LND is reachable.");
                    // Save the received data.
                    mSyncedToChain = infoResponse.getSyncedToChain();

                    for (int i = 0; i < infoResponse.getChainsCount(); i++) {
                        if (infoResponse.getChains(i).getChain().equals("bitcoin")) {
                            mNetwork = Network.parseFromString(infoResponse.getChains(i).getNetwork());
                            break;
                        }
                    }

                    mLNDVersionString = infoResponse.getVersion();
                    mInfoFetched = true;
                    mConnectedToLND = true;
                    mIdentityPubKey = infoResponse.getIdentityPubkey();
                    if (mNodeUris == null) {
                        mNodeUris = new LightningNodeUri[infoResponse.getUrisCount()];
                        for (int i = 0; i < infoResponse.getUrisCount(); i++) {
                            mNodeUris[i] = LightningParser.parseNodeUri(infoResponse.getUris(i));
                        }
                    }
                    broadcastLndConnectionTestResult(true, -1);
                }, throwable -> {

                    if (throwable.getMessage().toLowerCase().contains("unavailable") && !throwable.getMessage().toLowerCase().contains(".onion")) {
                        ZapLog.e(LOG_TAG, "LND Service unavailable");
                        if (throwable.getCause() != null) {
                            if (throwable.getCause().getMessage().toLowerCase().contains("cannot verify hostname")) {
                                // This is the case if:
                                // - The hostname used to initiate the lnd connection (the hostname from the lndconnect string) does not match with the hostname in the provided certificate.
                                broadcastLndConnectionTestResult(false, LndConnectionTestListener.ERROR_HOST_VERIFICATION);
                            } else if (throwable.getCause().getMessage().toLowerCase().contains("unable to resolve host")) {
                                // This is the case if:
                                // - We have an internet or network connection, but the desired host is not resolvable.
                                broadcastLndConnectionTestResult(false, LndConnectionTestListener.ERROR_HOST_UNRESOLVABLE);
                            } else if (throwable.getCause().getMessage().toLowerCase().contains("enetunreach")) {
                                // This is the case if:
                                // - We have no internet or network connection at all.
                                broadcastLndConnectionTestResult(false, LndConnectionTestListener.ERROR_NETWORK_UNREACHABLE);
                            } else if (throwable.getCause().getMessage().toLowerCase().contains("econnrefused")) {
                                // This is the case if:
                                // - LND daemon is not running
                                // - An incorrect port is used
                                broadcastLndConnectionTestResult(false, LndConnectionTestListener.ERROR_UNAVAILABLE);
                            } else if (throwable.getCause().getMessage().toLowerCase().contains("trust anchor")) {
                                // This is the case if:
                                // - tor is not used and no certificate is provided or a wrong certificate is provided
                                broadcastLndConnectionTestResult(false, LndConnectionTestListener.ERROR_CERTIFICATE_NOT_TRUSTED);
                            } else {
                                // Unknown error. Print what gets returned directly, always english.
                                broadcastLndConnectionTestResult(throwable.getCause().getMessage());
                            }
                        } else if (throwable.getMessage().toLowerCase().contains("404") && PrefsUtil.isTorEnabled()) {
                            // This is the case if:
                            // - Tor is turned on, but the host cannot be resolved
                            broadcastLndConnectionTestResult(false, LndConnectionTestListener.ERROR_HOST_UNRESOLVABLE);
                        } else if (throwable.getMessage().toLowerCase().contains("500") && PrefsUtil.isTorEnabled()) {
                            if (LndConnection.getInstance().getConnectionConfig().isTor()) {
                                // This is the case if:
                                // - Tor is turned on and an incorrect port is used.
                                broadcastLndConnectionTestResult(false, LndConnectionTestListener.ERROR_INTERNAL);
                            } else {
                                // This is the case if:
                                // - happened for a user that used wireguard and connected to a clearnet node. Disabling tor solved connection issues.
                                broadcastLndConnectionTestResult(false, LndConnectionTestListener.ERROR_INTERNAL_CLEARNET);
                            }
                        } else {
                            // Unknown error. Print what gets returned directly, always english.
                            broadcastLndConnectionTestResult(throwable.getMessage());
                        }
                    } else if (throwable.getMessage().toLowerCase().contains("terminated")) {
                        // This is the case if:
                        // - The server is not reachable at all. (e.g. wrong IP Address or server offline)
                        ZapLog.e(LOG_TAG, "Cannot reach remote");
                        broadcastLndConnectionTestResult(false, LndConnectionTestListener.ERROR_TIMEOUT);
                    } else if (throwable.getMessage().toLowerCase().contains("unimplemented")) {
                        // This is the case if:
                        // - The wallet is locked
                        ZapLog.e(LOG_TAG, "Wallet is locked!");
                        broadcastLndConnectionTestResult(false, LndConnectionTestListener.ERROR_LOCKED);
                    } else if (throwable.getMessage().toLowerCase().contains("verification failed")) {
                        // This is the case if:
                        // - The macaroon is invalid
                        ZapLog.e(LOG_TAG, "Macaroon is invalid!");
                        broadcastLndConnectionTestResult(false, LndConnectionTestListener.ERROR_AUTHENTICATION);
                    } else if (throwable.getMessage().contains("UNKNOWN")) {
                        // This is the case if:
                        // - The macaroon has wrong encoding
                        ZapLog.e(LOG_TAG, "Macaroon is invalid!");
                        broadcastLndConnectionTestResult(false, LndConnectionTestListener.ERROR_AUTHENTICATION);
                    } else if (throwable.getMessage().contains(".onion")) {
                        // This is the case if:
                        // - Tor is not active in the settings and the user tries to connect to a tor node.
                        ZapLog.e(LOG_TAG, "Cannot resolve onion address!");
                        broadcastLndConnectionTestResult(false, LndConnectionTestListener.ERROR_TOR);
                    } else if (throwable.getMessage().toLowerCase().contains("interrupted")) {
                        ZapLog.e(LOG_TAG, "Test if LND is reachable was interrupted.");
                        broadcastLndConnectionTestResult(false, LndConnectionTestListener.ERROR_INTERRUPTED);
                    } else {
                        // Unknown error. Print what gets returned directly, always english.
                        ZapLog.e(LOG_TAG, "Unknown connection error..");
                        broadcastLndConnectionTestResult(throwable.getMessage());
                    }
                    ZapLog.e(LOG_TAG, throwable.getMessage());
                    if (throwable.getCause() != null) {
                        ZapLog.e(LOG_TAG, throwable.getCause().getMessage());
                        throwable.getCause().printStackTrace();
                    }
                }));

    }

    /**
     * Call this if the daemon is running, but the wallet is not unlocked yet.
     *
     * @param password
     */
    public void unlockWallet(String password) {
        UnlockWalletRequest unlockRequest = UnlockWalletRequest.newBuilder()
                .setWalletPassword(ByteString.copyFrom(password.getBytes()))
                .build();

        compositeDisposable.add(LndConnection.getInstance().getWalletUnlockerService().unlockWallet(unlockRequest)
                .subscribe(unlockWalletResponse -> {
                    ZapLog.d(LOG_TAG, "successfully unlocked");

                    // We have to reset the connection, because until you unlock the wallet, there is no Lightning rpc service available.
                    // Thus we could not connect to it with previous channel, so we reset the connection and connect to all services when unlocked.
                    LndConnection.getInstance().closeConnection();
                    LndConnection.getInstance().openConnection();

                    mHandler.postDelayed(() -> {
                        // We have to call this delayed, as without it, it will show as unconnected until the wallet button is hit again.
                        // ToDo: Create a routine that retries this until successful
                        testLndConnectionAndLoadWallet();
                    }, 10000);

                    mHandler.postDelayed(() -> {
                        // The channels are already fetched before, but they are all showed and saved as offline right after unlocking.
                        // That's why we update it again 10 seconds later.
                        // ToDo: Create a routine that retries this until successful
                        Wallet.getInstance().fetchChannelsFromLND();
                    }, 12000);
                }, throwable -> {
                    ZapLog.e(LOG_TAG, throwable.getMessage());
                    // Show password prompt again after error
                    broadcastLndConnectionTestResult(false, LndConnectionTestListener.ERROR_LOCKED);
                }));
    }

    /**
     * This will return a Balance object that contains all types of balances.
     * Please note that this might be different from the actual balance on LND.
     * To update what this function returns call fetchBalanceFromLND()
     *
     * @return
     */
    public Balances getBalances() {
        return new Balances(mOnChainBalanceTotal, mOnChainBalanceConfirmed,
                mOnChainBalanceUnconfirmed, mChannelBalance, mChannelBalancePendingOpen, mChannelBalanceLimbo);
    }

    /**
     * This will return a Balance object that contains all types of balances.
     * Use this only when wallet is not yet setup. The balances are not real and will always be the same.
     * If desired, these values can be set to specific values for demonstration purposes.
     *
     * @return
     */
    public Balances getDemoBalances() {
        return new Balances(0, 0,
                0, 0, 0, 0);
    }

    public void fetchBalancesWithDebounce() {
        ZapLog.d(LOG_TAG, "Fetch balance from LND. (debounce)");

        mBalancesDebounceHandler.attempt(this::fetchBalanceFromLND, DebounceHandler.DEBOUNCE_1_SECOND);
    }

    /**
     * This will fetch the current balance from LND.
     * All Listeners registered to BalanceListener will be informed about any changes.
     */
    public void fetchBalanceFromLND() {

        Single<WalletBalanceResponse> walletBalance = LndConnection.getInstance().getLightningService().walletBalance(WalletBalanceRequest.newBuilder().build());
        Single<ChannelBalanceResponse> channelBalance = LndConnection.getInstance().getLightningService().channelBalance(ChannelBalanceRequest.newBuilder().build());
        Single<PendingChannelsResponse> pendingChannels = LndConnection.getInstance().getLightningService().pendingChannels(PendingChannelsRequest.newBuilder().build());

        compositeDisposable.add(Single.zip(walletBalance, channelBalance, pendingChannels, (walletBalanceResponse, channelBalanceResponse, pendingChannelsResponse) -> {

            setOnChainBalance(walletBalanceResponse.getTotalBalance(), walletBalanceResponse.getConfirmedBalance(), walletBalanceResponse.getUnconfirmedBalance());
            setChannelBalance(channelBalanceResponse.getBalance(), channelBalanceResponse.getPendingOpenBalance());
            setChannelBalanceLimbo(pendingChannelsResponse.getTotalLimboBalance());

            return true;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(aBoolean -> {
            // Zip executed without error
            ZapLog.d(LOG_TAG, "Balances Fetched!");
            broadcastBalanceUpdate();

            if (!mIsWalletReady) {
                mBalancesFetched = true;
                if (mChannelsFetched) {
                    mIsWalletReady = true;
                    broadcastWalletLoadedUpdate();
                }
            }
        }, throwable -> ZapLog.e(LOG_TAG, "Exception in fetch balance task: " + throwable.getMessage())));
    }

    /**
     * This will fetch the current info from LND.
     * All Listeners registered to InfoListener will be informed about any changes.
     */
    public void fetchInfoFromLND() {
        // Retrieve info from LND with gRPC (async)
        if (LndConnection.getInstance().getLightningService() != null) {
            compositeDisposable.add(LndConnection.getInstance().getLightningService().getInfo(GetInfoRequest.newBuilder().build())
                    .subscribe(infoResponse -> {

                        // Save the received data.
                        mSyncedToChain = infoResponse.getSyncedToChain();
                        mLNDVersionString = infoResponse.getVersion();
                        mIdentityPubKey = infoResponse.getIdentityPubkey();

                        for (int i = 0; i < infoResponse.getChainsCount(); i++) {
                            if (infoResponse.getChains(i).getChain().equals("bitcoin")) {
                                mNetwork = Network.parseFromString(infoResponse.getChains(i).getNetwork());
                                break;
                            }
                        }

                        if (mNodeUris == null) {
                            mNodeUris = new LightningNodeUri[infoResponse.getUrisCount()];
                            for (int i = 0; i < infoResponse.getUrisCount(); i++) {
                                mNodeUris[i] = LightningParser.parseNodeUri(infoResponse.getUris(i));
                            }
                        }
                        mInfoFetched = true;
                        mConnectedToLND = true;

                        broadcastInfoUpdate(true);
                    }, throwable -> {
                        if (throwable.getMessage().toLowerCase().contains("unavailable")) {
                            mConnectedToLND = false;
                            broadcastInfoUpdate(false);
                        }
                        ZapLog.w(LOG_TAG, "Exception in fetch info task: " + throwable.getMessage());
                    }));
        }
    }

    public void simulateFetchInfoForDemo(boolean connected) {
        mConnectedToLND = connected;
        broadcastInfoUpdate(connected);
    }

    /**
     * This will fetch all transaction history from LND.
     * After that the history is provided in lists that can be handled in a synchronized way.
     */
    public void fetchLNDTransactionHistory() {
        // Set all updated flags to false. This way we can determine later, when update is finished.

        if (!mUpdatingHistory) {
            mUpdatingHistory = true;
            mTransactionUpdated = false;
            mInvoicesUpdated = false;
            mPaymentsUpdated = false;

            fetchTransactionsFromLND();
            fetchInvoicesFromLND();
            fetchPaymentsFromLND();
        }
    }

    /**
     * This will fetch all lightning payment history from LND.
     * After that the history is provided in lists that can be handled in a synchronized way.
     * <p>
     * This will need less bandwidth than updating all history and can be called when a lightning
     * payment was successful.
     */
    public void updateLightningPaymentHistory() {
        // Set payment update flags to false. This way we can determine later, when update is finished.

        if (!mUpdatingHistory) {
            mUpdatingHistory = true;
            mPaymentsUpdated = false;

            fetchPaymentsFromLND();
        }
    }

    /**
     * This will fetch all on-chain transaction history from LND.
     * After that the history is provided in lists that can be handled in a synchronized way.
     * <p>
     * This will need less bandwidth than updating all history and can be called when a lightning
     * payment was successful.
     */
    public void updateOnChainTransactionHistory() {
        // Set payment update flags to false. This way we can determine later, when update is finished.

        if (!mUpdatingHistory) {
            mUpdatingHistory = true;
            mTransactionUpdated = false;

            fetchTransactionsFromLND();
        }
    }

    /**
     * checks if the history update is finished and then broadcast an update to all registered classes.
     */
    private void isHistoryUpdateFinished() {
        if (mTransactionUpdated && mInvoicesUpdated && mPaymentsUpdated) {
            mUpdatingHistory = false;
            broadcastHistoryUpdate();
        }
    }

    /**
     * This will fetch all On-Chain transactions involved with the current wallet from LND.
     */
    public void fetchTransactionsFromLND() {
        // fetch on-chain transactions
        compositeDisposable.add(LndConnection.getInstance().getLightningService().getTransactions(GetTransactionsRequest.newBuilder().build())
                .subscribe(transactionDetails -> {
                    mOnChainTransactionList = Lists.reverse(transactionDetails.getTransactionsList());

                    mTransactionUpdated = true;
                    isHistoryUpdateFinished();
                }, throwable -> ZapLog.e(LOG_TAG, "Exception in transaction request task: " + throwable.getMessage())));
    }

    /**
     * This will fetch all lightning invoices from LND.
     */
    public void fetchInvoicesFromLND() {

        mTempInvoiceUpdateList = new LinkedList<>();

        fetchInvoicesFromLND(100);
    }

    private void fetchInvoicesFromLND(long lastIndex) {
        // Fetch lightning invoices
        ListInvoiceRequest invoiceRequest = ListInvoiceRequest.newBuilder()
                .setNumMaxInvoices(lastIndex)
                .build();

        compositeDisposable.add(LndConnection.getInstance().getLightningService().listInvoices(invoiceRequest)
                .subscribe(listInvoiceResponse -> {
                    mTempInvoiceUpdateList.addAll(listInvoiceResponse.getInvoicesList());

                    if (listInvoiceResponse.getLastIndexOffset() < lastIndex) {
                        // we have fetched all available invoices!
                        mInvoiceList = Lists.reverse(mTempInvoiceUpdateList);
                        mTempInvoiceUpdateList = null;
                        mInvoicesUpdated = true;
                        isHistoryUpdateFinished();
                    } else {
                        // there are still invoices to fetch, get the next batch!
                        fetchInvoicesFromLND(lastIndex + 100);
                    }
                }, throwable -> ZapLog.e(LOG_TAG, "Exception in invoice request task: " + throwable.getMessage())));
    }

    /**
     * This will fetch lightning payments from LND.
     */
    public void fetchPaymentsFromLND() {
        // Fetch lightning payments
        ListPaymentsRequest paymentsRequest = ListPaymentsRequest.newBuilder()
                .setIncludeIncomplete(false)
                .build();

        compositeDisposable.add(LndConnection.getInstance().getLightningService().listPayments(paymentsRequest)
                .subscribe(listPaymentsResponse -> {
                    mPaymentsList = Lists.reverse(listPaymentsResponse.getPaymentsList());
                    mPaymentsUpdated = true;
                    isHistoryUpdateFinished();
                }, throwable -> ZapLog.e(LOG_TAG, "Exception in payment request task: " + throwable.getMessage())));
    }

    public void openChannel(LightningNodeUri nodeUri, long amount, int targetConf, boolean isPrivate) {
        compositeDisposable.add(LndConnection.getInstance().getLightningService().listPeers(ListPeersRequest.newBuilder().build())
                .timeout(RefConstants.TIMEOUT_LONG * TorManager.getInstance().getTorTimeoutMultiplier(), TimeUnit.SECONDS)
                .subscribe(listPeersResponse -> {
                    boolean connected = false;
                    for (Peer node : listPeersResponse.getPeersList()) {
                        if (node.getPubKey().equals(nodeUri.getPubKey())) {
                            connected = true;
                            break;
                        }
                    }

                    if (connected) {
                        ZapLog.d(LOG_TAG, "Already connected to peer, trying to open channel...");
                        openChannelConnected(nodeUri, amount, targetConf, isPrivate);
                    } else {
                        ZapLog.d(LOG_TAG, "Not connected to peer, trying to connect...");
                        connectPeer(nodeUri, amount, targetConf, isPrivate);
                    }
                }, throwable -> {
                    ZapLog.e(LOG_TAG, "Error listing peers request: " + throwable.getMessage());
                    if (throwable.getMessage().toLowerCase().contains("terminated")) {
                        broadcastChannelOpenUpdate(nodeUri, ChannelOpenUpdateListener.ERROR_GET_PEERS_TIMEOUT, throwable.getMessage());
                    } else {
                        broadcastChannelOpenUpdate(nodeUri, ChannelOpenUpdateListener.ERROR_GET_PEERS, throwable.getMessage());
                    }
                }));
    }

    private void connectPeer(LightningNodeUri nodeUri, long amount, int targetConf, boolean isPrivate) {
        if (nodeUri.getHost() == null || nodeUri.getHost().isEmpty()) {
            ZapLog.d(LOG_TAG, "Host info missing. Trying to fetch host info to connect peer...");
            fetchNodeInfoToConnectPeer(nodeUri, amount, targetConf, isPrivate);
            return;
        }

        LightningAddress lightningAddress = LightningAddress.newBuilder()
                .setHostBytes(ByteString.copyFrom(nodeUri.getHost().getBytes(StandardCharsets.UTF_8)))
                .setPubkeyBytes(ByteString.copyFrom(nodeUri.getPubKey().getBytes(StandardCharsets.UTF_8))).build();
        ConnectPeerRequest connectPeerRequest = ConnectPeerRequest.newBuilder().setAddr(lightningAddress).build();

        compositeDisposable.add(LndConnection.getInstance().getLightningService().connectPeer(connectPeerRequest)
                .timeout(RefConstants.TIMEOUT_LONG * TorManager.getInstance().getTorTimeoutMultiplier(), TimeUnit.SECONDS)
                .subscribe(connectPeerResponse -> {
                    ZapLog.d(LOG_TAG, "Successfully connected to peer, trying to open channel...");
                    openChannelConnected(nodeUri, amount, targetConf, isPrivate);
                }, throwable -> {
                    ZapLog.e(LOG_TAG, "Error connecting to peer: " + throwable.getMessage());

                    if (throwable.getMessage().toLowerCase().contains("refused")) {
                        broadcastChannelOpenUpdate(nodeUri, ChannelOpenUpdateListener.ERROR_CONNECTION_REFUSED, throwable.getMessage());
                    } else if (throwable.getMessage().toLowerCase().contains("self")) {
                        broadcastChannelOpenUpdate(nodeUri, ChannelOpenUpdateListener.ERROR_CONNECTION_SELF, throwable.getMessage());
                    } else if (throwable.getMessage().toLowerCase().contains("terminated")) {
                        broadcastChannelOpenUpdate(nodeUri, ChannelOpenUpdateListener.ERROR_CONNECTION_TIMEOUT, throwable.getMessage());
                    } else {
                        broadcastChannelOpenUpdate(nodeUri, ChannelOpenUpdateListener.ERROR_CONNECTION, throwable.getMessage());
                    }
                }));
    }

    public void fetchNodeInfoToConnectPeer(LightningNodeUri nodeUri, long amount, int targetConf, boolean isPrivate) {
        NodeInfoRequest nodeInfoRequest = NodeInfoRequest.newBuilder()
                .setPubKey(nodeUri.getPubKey())
                .build();

        compositeDisposable.add(LndConnection.getInstance().getLightningService().getNodeInfo(nodeInfoRequest)
                .timeout(RefConstants.TIMEOUT_LONG * TorManager.getInstance().getTorTimeoutMultiplier(), TimeUnit.SECONDS)
                .subscribe(nodeInfo -> {
                    if (nodeInfo.getNode().getAddressesCount() > 0) {
                        String tempUri = nodeUri.getPubKey() + "@" + nodeInfo.getNode().getAddresses(0).getAddr();
                        LightningNodeUri nodeUriWithHost = LightningParser.parseNodeUri(tempUri);
                        if (nodeUriWithHost != null) {
                            ZapLog.d(LOG_TAG, "Host info successfully fetched. NodeUriWithHost: " + nodeUriWithHost.getAsString());
                            connectPeer(nodeUriWithHost, amount, targetConf, isPrivate);
                        } else {
                            ZapLog.d(LOG_TAG, "Failed to parse nodeUri");
                            broadcastChannelOpenUpdate(nodeUri, ChannelOpenUpdateListener.ERROR_CONNECTION_NO_HOST, null);
                        }
                    } else {
                        ZapLog.d(LOG_TAG, "Node Info does not contain any addresses.");
                        broadcastChannelOpenUpdate(nodeUri, ChannelOpenUpdateListener.ERROR_CONNECTION_NO_HOST, null);
                    }
                }, throwable -> {
                    ZapLog.w(LOG_TAG, "Fetching host info failed. Exception in get node info (" + nodeUri.getPubKey() + ") request task: " + throwable.getMessage());
                    broadcastChannelOpenUpdate(nodeUri, ChannelOpenUpdateListener.ERROR_CONNECTION_NO_HOST, null);
                }));
    }

    private void openChannelConnected(LightningNodeUri nodeUri, long amount, int targetConf, boolean isPrivate) {
        byte[] nodeKeyBytes = hexStringToByteArray(nodeUri.getPubKey());
        OpenChannelRequest openChannelRequest = OpenChannelRequest.newBuilder()
                .setNodePubkey(ByteString.copyFrom(nodeKeyBytes))
                .setTargetConf(targetConf)
                .setPrivate(isPrivate)
                .setLocalFundingAmount(amount)
                .build();

        compositeDisposable.add(LndConnection.getInstance().getLightningService().openChannel(openChannelRequest)
                .timeout(RefConstants.TIMEOUT_LONG * TorManager.getInstance().getTorTimeoutMultiplier(), TimeUnit.SECONDS)
                .firstOrError()
                .subscribe(openStatusUpdate -> {
                    ZapLog.d(LOG_TAG, "Open channel update: " + openStatusUpdate.getUpdateCase().getNumber());
                    broadcastChannelOpenUpdate(nodeUri, ChannelOpenUpdateListener.SUCCESS, null);
                }, throwable -> {
                    ZapLog.e(LOG_TAG, "Error opening channel: " + throwable.getMessage());

                    if (throwable.getMessage().toLowerCase().contains("pending channels exceed maximum")) {
                        broadcastChannelOpenUpdate(nodeUri, ChannelOpenUpdateListener.ERROR_CHANNEL_PENDING_MAX, throwable.getMessage());
                    } else if (throwable.getMessage().toLowerCase().contains("terminated")) {
                        broadcastChannelOpenUpdate(nodeUri, ChannelOpenUpdateListener.ERROR_CHANNEL_TIMEOUT, throwable.getMessage());
                    } else {
                        broadcastChannelOpenUpdate(nodeUri, ChannelOpenUpdateListener.ERROR_CHANNEL_OPEN, throwable.getMessage());
                    }
                }));
    }

    public void closeChannel(String channelPoint, boolean force) {
        ChannelPoint point = ChannelPoint.newBuilder()
                .setFundingTxidStr(channelPoint.substring(0, channelPoint.indexOf(':')))
                .setOutputIndex(Character.getNumericValue(channelPoint.charAt(channelPoint.length() - 1)))
                .build();

        CloseChannelRequest closeChannelRequest = CloseChannelRequest.newBuilder()
                .setChannelPoint(point)
                .setForce(force)
                .build();

        compositeDisposable.add(LndConnection.getInstance().getLightningService().closeChannel(closeChannelRequest)
                .timeout(RefConstants.TIMEOUT_LONG * TorManager.getInstance().getTorTimeoutMultiplier(), TimeUnit.SECONDS)
                .firstOrError()
                .subscribe(closeStatusUpdate -> {
                    ZapLog.d(LOG_TAG, "Closing channel update: " + closeStatusUpdate.getUpdateCase().getNumber());
                    broadcastChannelCloseUpdate(channelPoint, ChannelCloseUpdateListener.SUCCESS, null);
                }, throwable -> {
                    ZapLog.e(LOG_TAG, "Error closing channel: " + throwable.getMessage());
                    if (throwable.getMessage().toLowerCase().contains("offline")) {
                        broadcastChannelCloseUpdate(channelPoint, ChannelCloseUpdateListener.ERROR_PEER_OFFLINE, throwable.getMessage());
                    } else if (throwable.getMessage().toLowerCase().contains("terminated")) {
                        broadcastChannelCloseUpdate(channelPoint, ChannelCloseUpdateListener.ERROR_CHANNEL_TIMEOUT, throwable.getMessage());
                    } else {
                        broadcastChannelCloseUpdate(channelPoint, ChannelCloseUpdateListener.ERROR_CHANNEL_CLOSE, throwable.getMessage());
                    }
                }));
    }

    public void fetchChannelsFromLND() {
        Single<ListChannelsResponse> listChannelsObservable = LndConnection.getInstance().getLightningService().listChannels(ListChannelsRequest.newBuilder().build());
        Single<PendingChannelsResponse> pendingChannelsObservable = LndConnection.getInstance().getLightningService().pendingChannels(PendingChannelsRequest.newBuilder().build());
        Single<ClosedChannelsResponse> closedChannelsObservable = LndConnection.getInstance().getLightningService().closedChannels(ClosedChannelsRequest.newBuilder().build());

        compositeDisposable.add(Single.zip(listChannelsObservable, pendingChannelsObservable, closedChannelsObservable, (listChannelsResponse, pendingChannelsResponse, closedChannelsResponse) -> {

            mOpenChannelsList = listChannelsResponse.getChannelsList();
            mClosedChannelsList = closedChannelsResponse.getChannelsList();
            mPendingOpenChannelsList = pendingChannelsResponse.getPendingOpenChannelsList();
            mPendingClosedChannelsList = pendingChannelsResponse.getPendingClosingChannelsList();
            mPendingForceClosedChannelsList = pendingChannelsResponse.getPendingForceClosingChannelsList();
            mPendingWaitingCloseChannelsList = pendingChannelsResponse.getWaitingCloseChannelsList();


            // Load NodeInfos for all involved nodes. This allows us to display aliases later.
            Set<String> channelNodes = new HashSet<>();

            for (Channel c : mOpenChannelsList) {
                boolean alreadyFetched = false;
                for (NodeInfo i : mNodeInfos) {
                    if (i.getNode().getPubKey().equals(c.getRemotePubkey())) {
                        alreadyFetched = true;
                        break;
                    }
                }
                if (!alreadyFetched) {
                    channelNodes.add(c.getRemotePubkey());
                }
            }
            for (ChannelCloseSummary c : mClosedChannelsList) {
                boolean alreadyFetched = false;
                for (NodeInfo i : mNodeInfos) {
                    if (i.getNode().getPubKey().equals(c.getRemotePubkey())) {
                        alreadyFetched = true;
                        break;
                    }
                }
                if (!alreadyFetched) {
                    channelNodes.add(c.getRemotePubkey());
                }
            }
            for (PendingChannelsResponse.PendingOpenChannel c : mPendingOpenChannelsList) {
                boolean alreadyFetched = false;
                for (NodeInfo i : mNodeInfos) {
                    if (i.getNode().getPubKey().equals(c.getChannel().getRemoteNodePub())) {
                        alreadyFetched = true;
                        break;
                    }
                }
                if (!alreadyFetched) {
                    channelNodes.add(c.getChannel().getRemoteNodePub());
                }
            }
            for (PendingChannelsResponse.ClosedChannel c : mPendingClosedChannelsList) {
                boolean alreadyFetched = false;
                for (NodeInfo i : mNodeInfos) {
                    if (i.getNode().getPubKey().equals(c.getChannel().getRemoteNodePub())) {
                        alreadyFetched = true;
                        break;
                    }
                }
                if (!alreadyFetched) {
                    channelNodes.add(c.getChannel().getRemoteNodePub());
                }
            }
            for (PendingChannelsResponse.ForceClosedChannel c : mPendingForceClosedChannelsList) {
                boolean alreadyFetched = false;
                for (NodeInfo i : mNodeInfos) {
                    if (i.getNode().getPubKey().equals(c.getChannel().getRemoteNodePub())) {
                        alreadyFetched = true;
                        break;
                    }
                }
                if (!alreadyFetched) {
                    channelNodes.add(c.getChannel().getRemoteNodePub());
                }
            }
            for (PendingChannelsResponse.WaitingCloseChannel c : mPendingWaitingCloseChannelsList) {
                boolean alreadyFetched = false;
                for (NodeInfo i : mNodeInfos) {
                    if (i.getNode().getPubKey().equals(c.getChannel().getRemoteNodePub())) {
                        alreadyFetched = true;
                        break;
                    }
                }
                if (!alreadyFetched) {
                    channelNodes.add(c.getChannel().getRemoteNodePub());
                }
            }

            // Delay each NodeInfo request for 100ms to not stress LND
            ArrayList<String> channelNodesList = new ArrayList<>(channelNodes);
            ZapLog.d(LOG_TAG, "Fetching node info for " + channelNodesList.size() + " nodes.");

            compositeDisposable.add(Observable.range(0, channelNodesList.size())
                    .concatMap(i -> Observable.just(i).delay(100, TimeUnit.MILLISECONDS))
                    .doOnNext(integer -> fetchNodeInfoFromLND(channelNodesList.get(integer), integer == channelNodesList.size() - 1))
                    .subscribe());

            if (channelNodesList.size() == 0) {
                broadcastChannelsUpdated();
            }

            return true;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(aBoolean -> {
            // Zip executed without error
            ZapLog.d(LOG_TAG, "Channels fetched!");
            if (!mIsWalletReady) {
                mChannelsFetched = true;
                if (mBalancesFetched) {
                    mIsWalletReady = true;
                    broadcastWalletLoadedUpdate();
                }
            }
        }, throwable -> ZapLog.e(LOG_TAG, "Exception in get channels info request task: " + throwable.getMessage())));
    }

    /**
     * This will fetch the NodeInfo according to the supplied pubkey.
     * The NodeInfo will then be added to the mNodeInfos list (no duplicates) which can then
     * be used for non async tasks, such as getting the aliases for channels.
     *
     * @param pubkey
     */
    public void fetchNodeInfoFromLND(String pubkey, boolean broadcastChannelUpdate) {
        NodeInfoRequest nodeInfoRequest = NodeInfoRequest.newBuilder()
                .setPubKey(pubkey)
                .build();

        compositeDisposable.add(LndConnection.getInstance().getLightningService().getNodeInfo(nodeInfoRequest)
                .timeout(RefConstants.TIMEOUT_LONG * TorManager.getInstance().getTorTimeoutMultiplier(), TimeUnit.SECONDS)
                .subscribe(nodeInfo -> {
                    ZapLog.v(LOG_TAG, "Fetched Node info from " + nodeInfo.getNode().getAlias());
                    mNodeInfos.add(nodeInfo);

                    if (broadcastChannelUpdate) {
                        broadcastChannelsUpdated();
                    }
                }, throwable -> {
                    if (broadcastChannelUpdate) {
                        broadcastChannelsUpdated();
                    }
                    ZapLog.w(LOG_TAG, "Exception in get node info (" + pubkey + ") request task: " + throwable.getMessage());
                }));
    }


    /**
     * Use this to subscribe the wallet to transaction events that happen on LND.
     * The events will be captured and forwarded to the TransactionSubscriptionListener.
     * All parts of the App that want to react on transaction events have to subscribe to the
     * TransactionSubscriptionListener.
     */
    public void subscribeToTransactions() {
        compositeDisposable.add(LndConnection.getInstance().getLightningService().subscribeTransactions(GetTransactionsRequest.newBuilder().build())
                .subscribe(transaction -> {
                    ZapLog.d(LOG_TAG, "Received transaction subscription event.");
                    fetchTransactionsFromLND(); // update internal transaction list
                    fetchBalancesWithDebounce(); // Always update balances if a transaction event occurs.
                    broadcastTransactionUpdate(transaction);
                }));
    }

    /**
     * Use this to subscribe the wallet to htlc events that happen on LND.
     * The events will be captured and forwarded to the HtlcSubscriptionListener.
     * All parts of the App that want to react on transaction events have to subscribe to the
     * HtlcSubscriptionListener.
     */
    public void subscribeToHtlcEvents() {
        compositeDisposable.add(LndConnection.getInstance().getRouterService().subscribeHtlcEvents(SubscribeHtlcEventsRequest.newBuilder().build())
                .subscribe(htlcEvent -> {
                    ZapLog.d(LOG_TAG, "Received htlc subscription event. Type: " + htlcEvent.getEventType().toString());
                    fetchBalancesWithDebounce(); // Always update balances if a htlc event occurs.
                    updateLNDChannelsWithDebounce(); // Always update channels if a htlc event occurs.
                    broadcastHtlcEvent(htlcEvent);
                }));
    }

    public void cancelSubscriptions() {
        compositeDisposable.clear();
    }

    /**
     * Use this to subscribe the wallet to invoice events that happen on LND.
     * The events will be captured and forwarded to the InvoiceSubscriptionListener.
     * All parts of the App that want to react on invoice events have to subscribe to the
     * InvoiceSubscriptionListener.
     */
    public void subscribeToInvoices() {
        compositeDisposable.add(LndConnection.getInstance().getLightningService().subscribeInvoices(InvoiceSubscription.newBuilder().build())
                .subscribe(invoice -> {
                    ZapLog.d(LOG_TAG, "Received invoice subscription event.");

                    // is this a new invoice or is an old one updated?
                    if (mInvoiceList != null) {
                        if (invoice.getAddIndex() > mInvoiceList.get(0).getAddIndex()) {
                            // this is a new one
                            mInvoiceList.add(0, invoice);
                            broadcastInvoiceAdded(invoice);
                        } else {
                            // this is an update

                            // Find out which element has to be replaced
                            int changeIndex = -1;
                            for (int i = 0; i < mInvoiceList.size() - 1; i++) {
                                if (mInvoiceList.get(i).getAddIndex() == invoice.getAddIndex()) {
                                    changeIndex = i;
                                    break;
                                }
                            }

                            // Replace it
                            if (changeIndex >= 0) {
                                mInvoiceList.set(changeIndex, invoice);
                            }

                            // Inform all subscribers
                            broadcastInvoiceUpdated(invoice);
                        }
                    } else {
                        // this is a new one
                        mInvoiceList = new LinkedList<>();
                        mInvoiceList.add(invoice);
                        broadcastInvoiceAdded(invoice);
                    }
                }));
    }

    /**
     * Use this to subscribe the wallet to channel events that happen on LND.
     * The events will be captured and forwarded to the ChannelEventSubscriptionListener.
     * All parts of the App that want to react on channel events have to subscribe to the
     * ChannelEventSubscriptionListener.
     */
    public void subscribeToChannelEvents() {
        compositeDisposable.add(LndConnection.getInstance().getLightningService().subscribeChannelEvents(ChannelEventSubscription.newBuilder().build())
                .subscribe(channelEventUpdate -> {
                    ZapLog.d(LOG_TAG, "Received channel update event");
                    switch (channelEventUpdate.getChannelCase()) {
                        case OPEN_CHANNEL:
                            ZapLog.d(LOG_TAG, "Channel has been opened");
                            break;
                        case CLOSED_CHANNEL:
                            ZapLog.d(LOG_TAG, "Channel has been closed");
                            break;
                        case ACTIVE_CHANNEL:
                            ZapLog.d(LOG_TAG, "Channel went active");
                            break;
                        case INACTIVE_CHANNEL:
                            ZapLog.d(LOG_TAG, "Open channel went to inactive");
                            break;
                        case CHANNEL_NOT_SET:
                            ZapLog.d(LOG_TAG, "Received channel event update case: not set Channel");
                            break;
                        default:
                            ZapLog.d(LOG_TAG, "Unknown channel event: " + channelEventUpdate.getChannelCase());
                            break;
                    }

                    updateLNDChannelsWithDebounce();
                    broadcastChannelEvent(channelEventUpdate);
                }));
    }

    public void updateLNDChannelsWithDebounce() {
        ZapLog.d(LOG_TAG, "Fetch channels from LND. (debounce)");

        mChannelsUpdateDebounceHandler.attempt(this::fetchChannelsFromLND, DebounceHandler.DEBOUNCE_1_SECOND);
    }

    /**
     * Use this to subscribe the wallet to channel backup events that happen on LND.
     * The events will be captured and forwarded to the ChannelBackupSubscriptionListener.
     * All parts of the App that want to react on channel backups have to subscribe to the
     * ChannelBackupSubscriptionListener.
     */
    public void subscribeToChannelBackup() {
        compositeDisposable.add(LndConnection.getInstance().getLightningService().subscribeChannelBackups(ChannelBackupSubscription.newBuilder().build())
                .subscribe(chanBackupSnapshot -> {
                    ZapLog.d(LOG_TAG, "Received channel backup event.");
                    broadcastChannelBackup(chanBackupSnapshot);
                }));
    }

    /**
     * Returns if the invoice has been payed already.
     *
     * @param invoice
     * @return
     */
    public boolean isInvoicePayed(Invoice invoice) {
        boolean payed;
        if (invoice.getValue() == 0) {
            payed = invoice.getAmtPaidSat() != 0;
        } else {
            payed = invoice.getValue() <= invoice.getAmtPaidSat();
        }
        return payed;
    }

    /**
     * Returns if the invoice has been expired. This function just checks if the expiration date is in the past.
     * It will also return expired for already payed invoices.
     *
     * @param invoice
     * @return
     */
    public boolean isInvoiceExpired(Invoice invoice) {
        return invoice.getCreationDate() + invoice.getExpiry() < System.currentTimeMillis() / 1000;
    }


    /**
     * This function determines if we put the given on-chain transaction into the internal group.
     *
     * @param transaction
     * @return
     */
    public boolean isTransactionInternal(Transaction transaction) {

        // This is faster especially for nodes with lots of channels
        if (hasInternalTransactionLabel(transaction)) {
            return true;
        }

        // ToDo: looping through all channels can be removed later. But this is still necessary for Nodes that run since LND versions prior to LND 0.11
        // open channels
        if (mOpenChannelsList != null) {
            for (Channel c : mOpenChannelsList) {
                String[] parts = c.getChannelPoint().split(":");
                if (transaction.getTxHash().equals(parts[0])) {
                    return true;
                }
            }
        }

        // pending open channels
        if (mPendingOpenChannelsList != null) {
            for (PendingChannelsResponse.PendingOpenChannel c : mPendingOpenChannelsList) {
                String[] parts = c.getChannel().getChannelPoint().split(":");
                if (transaction.getTxHash().equals(parts[0])) {
                    return true;
                }
            }
        }

        // pending closed channels
        if (mPendingClosedChannelsList != null) {
            for (PendingChannelsResponse.ClosedChannel c : mPendingClosedChannelsList) {
                String[] parts = c.getChannel().getChannelPoint().split(":");
                if (transaction.getTxHash().equals(parts[0])) {
                    return true;
                }
            }
        }

        // pending force closed channels
        if (mPendingForceClosedChannelsList != null) {
            for (PendingChannelsResponse.ForceClosedChannel c : mPendingForceClosedChannelsList) {
                String[] parts = c.getChannel().getChannelPoint().split(":");
                if (transaction.getTxHash().equals(parts[0])) {
                    return true;
                }
            }
        }

        // pending waiting for close channels
        if (mPendingWaitingCloseChannelsList != null) {
            for (PendingChannelsResponse.WaitingCloseChannel c : mPendingWaitingCloseChannelsList) {
                String[] parts = c.getChannel().getChannelPoint().split(":");
                if (transaction.getTxHash().equals(parts[0])) {
                    return true;
                }
            }
        }

        // closed channels
        if (mClosedChannelsList != null) {
            for (ChannelCloseSummary c : mClosedChannelsList) {
                String[] parts = c.getChannelPoint().split(":");
                if (transaction.getTxHash().equals(parts[0]) || transaction.getTxHash().equals(c.getClosingTxHash())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * This function determines if according to the label this is a internal transaction.
     * The labelTypes are derived from: https://github.com/lightningnetwork/lnd/blob/master/labels/labels.go
     *
     * @param transaction
     * @return
     */
    public boolean hasInternalTransactionLabel(Transaction transaction) {
        String[] labelType = {":openchannel", ":closechannel", ":justicetx", ":sweep"};
        if (transaction.getLabel() != null && !transaction.getLabel().isEmpty()) {
            for (String label : labelType) {
                if (transaction.getLabel().toLowerCase().contains(label)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This functions helps us to link on-chain channel transaction with the corresponding channel's public node alias.
     *
     * @return pubKey of the Node the channel is linked to
     */
    public String getNodePubKeyFromChannelTransaction(Transaction transaction) {

        // open channels
        if (mOpenChannelsList != null) {
            for (Channel c : mOpenChannelsList) {
                String[] parts = c.getChannelPoint().split(":");
                if (transaction.getTxHash().equals(parts[0])) {
                    return c.getRemotePubkey();
                }
            }
        }

        // pending open channels
        if (mPendingOpenChannelsList != null) {
            for (PendingChannelsResponse.PendingOpenChannel c : mPendingOpenChannelsList) {
                String[] parts = c.getChannel().getChannelPoint().split(":");
                if (transaction.getTxHash().equals(parts[0])) {
                    return c.getChannel().getRemoteNodePub();
                }
            }
        }

        // pending closed channels
        if (mPendingClosedChannelsList != null) {
            for (PendingChannelsResponse.ClosedChannel c : mPendingClosedChannelsList) {
                String[] parts = c.getChannel().getChannelPoint().split(":");
                if (transaction.getTxHash().equals(parts[0])) {
                    return c.getChannel().getRemoteNodePub();
                }
            }
        }

        // pending force closed channels
        if (mPendingForceClosedChannelsList != null) {
            for (PendingChannelsResponse.ForceClosedChannel c : mPendingForceClosedChannelsList) {
                String[] parts = c.getChannel().getChannelPoint().split(":");
                if (transaction.getTxHash().equals(parts[0])) {
                    return c.getChannel().getRemoteNodePub();
                }
            }
        }

        // pending waiting for close channels
        if (mPendingWaitingCloseChannelsList != null) {
            for (PendingChannelsResponse.WaitingCloseChannel c : mPendingWaitingCloseChannelsList) {
                String[] parts = c.getChannel().getChannelPoint().split(":");
                if (transaction.getTxHash().equals(parts[0])) {
                    return c.getChannel().getRemoteNodePub();
                }
            }
        }

        // closed channels
        if (mClosedChannelsList != null) {
            for (ChannelCloseSummary c : mClosedChannelsList) {
                String[] parts = c.getChannelPoint().split(":");
                if (transaction.getTxHash().equals(parts[0]) || transaction.getTxHash().equals(c.getClosingTxHash())) {
                    return c.getRemotePubkey();
                }
            }
        }

        return "";
    }

    /**
     * This functions helps us to link on-chain channel transaction with the corresponding channel's public node alias.
     *
     * @return alias
     */
    public String getNodeAliasFromChannelTransaction(Transaction transaction, Context mContext) {
        String pubKey = getNodePubKeyFromChannelTransaction(transaction);
        return getNodeAliasFromPubKey(pubKey, mContext);
    }

    /**
     * Returns the alias of the node based on the provided pubKey.
     * If no alias is found, `Unnamed` is returned.
     *
     * @param pubKey   the pubKey of the node
     * @param mContext context to get translation
     * @return alias
     */
    public String getNodeAliasFromPubKey(String pubKey, Context mContext) {
        String alias = "";
        for (NodeInfo i : Wallet.getInstance().mNodeInfos) {
            if (i.getNode().getPubKey().equals(pubKey)) {
                if (i.getNode().getAlias().startsWith(i.getNode().getPubKey().substring(0, 8)) || i.getNode().getAlias().isEmpty()) {
                    String unnamed = mContext.getResources().getString(R.string.channel_no_alias);
                    alias = unnamed + " (" + i.getNode().getPubKey().substring(0, 5) + "...)";
                } else {
                    alias = i.getNode().getAlias();
                }
                break;
            }
        }

        if (alias.equals("")) {
            return mContext.getResources().getString(R.string.channel_no_alias);
        } else {
            return alias;
        }
    }

    /**
     * Returns if the wallet has at least one online channel.
     *
     * @return
     */
    public boolean hasOpenActiveChannels() {
        if (mOpenChannelsList != null) {
            if (mOpenChannelsList.size() != 0) {
                for (Channel c : mOpenChannelsList) {
                    if (c.getActive()) {
                        return true;
                    }
                }
                return false;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Get the maximum amount that can be received over Lightning Channels.
     *
     * @return amount in satoshis
     */
    public long getMaxLightningReceiveAmount() {

        if (!WalletConfigsManager.getInstance().hasAnyConfigs()) {
            return 0;
        }

        // Mpp is supported. Use the sum of the remote balances of all channels as maximum.
        long tempMax = 0L;
        if (mOpenChannelsList != null) {
            for (Channel c : mOpenChannelsList) {
                if (c.getActive()) {
                    tempMax = tempMax + Math.max(c.getRemoteBalance() - c.getRemoteConstraints().getChanReserveSat(), 0);
                }
            }
        }
        return tempMax;
    }

    /**
     * Get the maximum amount that can be send over Lightning Channels.
     *
     * @return amount in satoshis
     */
    public long getMaxLightningSendAmount() {

        if (!WalletConfigsManager.getInstance().hasAnyConfigs()) {
            return 0;
        }

        // Mpp is supported. Use the sum of the local balances of all channels as maximum.
        long tempMax = 0L;
        if (mOpenChannelsList != null) {
            for (Channel c : mOpenChannelsList) {
                if (c.getActive()) {
                    tempMax = tempMax + Math.max(c.getLocalBalance() - c.getLocalConstraints().getChanReserveSat(), 0);
                }
            }
        }
        return tempMax;
    }

    public boolean isSyncedToChain() {
        return mSyncedToChain;
    }

    public Network getNetwork() {
        return mNetwork;
    }

    public String getLNDVersionString() {
        if (isConnectedToLND() && mLNDVersionString != null) {
            return mLNDVersionString;
        } else {
            return App.getAppContext().getString(R.string.notConnected);
        }
    }

    public Version getLNDVersion() {
        return new Version(mLNDVersionString.split("-")[0]);
    }

    public boolean isInfoFetched() {
        return mInfoFetched;
    }

    public boolean isConnectedToLND() {
        return mConnectedToLND;
    }

    public void setLNDAsDisconnected() {
        mConnectedToLND = false;
    }

    public String getIdentityPubKey() {
        return mIdentityPubKey;
    }

    public LightningNodeUri[] getNodeUris() {
        return mNodeUris;
    }

    private void setOnChainBalance(long total, long confirmed, long unconfirmed) {
        mOnChainBalanceTotal = total;
        mOnChainBalanceConfirmed = confirmed;
        mOnChainBalanceUnconfirmed = unconfirmed;
    }

    private void setChannelBalance(long balance, long pendingOpen) {
        mChannelBalance = balance;
        mChannelBalancePendingOpen = pendingOpen;
    }

    private void setChannelBalanceLimbo(long balanceLimbo) {
        mChannelBalanceLimbo = balanceLimbo;
    }

    /**
     * Notify all listeners about the lnd connection test result.
     *
     * @param success true if successful
     * @param error   one of LndConnectionTestListener errors
     */
    public void broadcastLndConnectionTestResult(boolean success, int error) {
        if (success) {
            for (LndConnectionTestListener listener : mLndConnectionTestListeners) {
                listener.onLndConnectSuccess();
            }
        } else {
            for (LndConnectionTestListener listener : mLndConnectionTestListeners) {
                listener.onLndConnectError(error);
            }
        }
    }

    public void broadcastLndConnectionTestResult(String errorMessage) {
        for (LndConnectionTestListener listener : mLndConnectionTestListeners) {
            listener.onLndConnectError(errorMessage);
        }
    }

    public void broadcastLndConnectionTestStarted() {
        for (LndConnectionTestListener listener : mLndConnectionTestListeners) {
            listener.onLndConnectionTestStarted();
        }
    }

    public void registerLndConnectionTestListener(LndConnectionTestListener listener) {
        mLndConnectionTestListeners.add(listener);
    }

    public void unregisterLndConnectionTestListener(LndConnectionTestListener listener) {
        mLndConnectionTestListeners.remove(listener);
    }

    /**
     * Notify all listeners to balance updates.
     */
    private void broadcastBalanceUpdate() {
        for (BalanceListener listener : mBalanceListeners) {
            listener.onBalanceUpdated();
        }
    }

    public void registerBalanceListener(BalanceListener listener) {
        mBalanceListeners.add(listener);
    }

    public void unregisterBalanceListener(BalanceListener listener) {
        mBalanceListeners.remove(listener);
    }

    /**
     * Notify all listeners to info updates.
     *
     * @param connected true if connected to wallet
     */
    private void broadcastInfoUpdate(boolean connected) {
        for (InfoListener listener : mInfoListeners) {
            listener.onInfoUpdated(connected);
        }
    }

    public void registerInfoListener(InfoListener listener) {
        mInfoListeners.add(listener);
    }

    public void unregisterInfoListener(InfoListener listener) {
        mInfoListeners.remove(listener);
    }

    /**
     * Notify all listeners to history updates.
     */
    private void broadcastHistoryUpdate() {
        for (HistoryListener listener : mHistoryListeners) {
            listener.onHistoryUpdated();
        }
    }

    public void registerHistoryListener(HistoryListener listener) {
        mHistoryListeners.add(listener);
    }

    public void unregisterHistoryListener(HistoryListener listener) {
        mHistoryListeners.remove(listener);
    }


    /**
     * Notify all listeners about new invoice.
     *
     * @param invoice the new invoice
     */
    private void broadcastInvoiceAdded(Invoice invoice) {
        for (InvoiceSubscriptionListener listener : mInvoiceSubscriptionListeners) {
            listener.onNewInvoiceAdded(invoice);
        }
    }

    /**
     * Notify all listeners about updated invoice.
     *
     * @param invoice the updated invoice
     */
    private void broadcastInvoiceUpdated(Invoice invoice) {
        for (InvoiceSubscriptionListener listener : mInvoiceSubscriptionListeners) {
            listener.onExistingInvoiceUpdated(invoice);
        }
    }

    public void registerInvoiceSubscriptionListener(InvoiceSubscriptionListener listener) {
        mInvoiceSubscriptionListeners.add(listener);
    }

    public void unregisterInvoiceSubscriptionListener(InvoiceSubscriptionListener listener) {
        mInvoiceSubscriptionListeners.remove(listener);
    }


    /**
     * Notify all listeners to transaction update.
     *
     * @param transaction the details about the transaction update
     */
    private void broadcastTransactionUpdate(Transaction transaction) {
        for (TransactionSubscriptionListener listener : mTransactionSubscriptionListeners) {
            listener.onTransactionEvent(transaction);
        }
    }

    public void registerTransactionSubscriptionListener(TransactionSubscriptionListener listener) {
        mTransactionSubscriptionListeners.add(listener);
    }

    public void unregisterTransactionSubscriptionListener(TransactionSubscriptionListener listener) {
        mTransactionSubscriptionListeners.remove(listener);
    }

    /**
     * Notify all listeners to htlc update.
     *
     * @param htlcEvent the htlc event that occured
     */
    private void broadcastHtlcEvent(HtlcEvent htlcEvent) {
        for (HtlcSubscriptionListener listener : mHtlcSubscriptionListeners) {
            listener.onHtlcEvent(htlcEvent);
        }
    }

    public void registerHtlcSubscriptionListener(HtlcSubscriptionListener listener) {
        mHtlcSubscriptionListeners.add(listener);
    }

    public void unregisterHtlcSubscriptionListener(HtlcSubscriptionListener listener) {
        mHtlcSubscriptionListeners.remove(listener);
    }

    /**
     * Notify all listeners to channel event updates.
     *
     * @param channelEventUpdate the channel update
     */
    private void broadcastChannelEvent(ChannelEventUpdate channelEventUpdate) {
        for (ChannelEventSubscriptionListener listener : mChannelEventSubscriptionListeners) {
            listener.onChannelEvent(channelEventUpdate);
        }
    }

    public void registerChannelEventSubscriptionListener(ChannelEventSubscriptionListener listener) {
        mChannelEventSubscriptionListeners.add(listener);
    }

    public void unregisterChannelEventSubscriptionListener(ChannelEventSubscriptionListener listener) {
        mChannelEventSubscriptionListeners.remove(listener);
    }


    /**
     * Notify all listeners that channels have been updated.
     */
    private void broadcastChannelsUpdated() {
        for (ChannelsUpdatedSubscriptionListener listener : mChannelsUpdatedSubscriptionListeners) {
            listener.onChannelsUpdated();
        }
    }

    public void registerChannelsUpdatedSubscriptionListener(ChannelsUpdatedSubscriptionListener listener) {
        mChannelsUpdatedSubscriptionListeners.add(listener);
    }

    public void unregisterChannelsUpdatedSubscriptionListener(ChannelsUpdatedSubscriptionListener listener) {
        mChannelsUpdatedSubscriptionListeners.remove(listener);
    }

    /**
     * Notify all listeners to channel backup updates.
     *
     * @param chanBackupSnapshot snapshot of channel backup
     */
    private void broadcastChannelBackup(ChanBackupSnapshot chanBackupSnapshot) {
        for (ChannelBackupSubscriptionListener listener : mChannelBackupSubscriptionListeners) {
            listener.onChannelBackupEvent(chanBackupSnapshot);
        }
    }

    public void registerChannelBackupSubscriptionListener(ChannelBackupSubscriptionListener listener) {
        mChannelBackupSubscriptionListeners.add(listener);
    }

    public void unregisterChannelBackuptSubscriptionListener(ChannelBackupSubscriptionListener listener) {
        mChannelBackupSubscriptionListeners.remove(listener);
    }

    /**
     * Notify all listeners to channel close updates
     */
    private void broadcastChannelCloseUpdate(String channelPoint, int status, String message) {
        for (ChannelCloseUpdateListener listener : mChannelCloseUpdateListeners) {
            listener.onChannelCloseUpdate(channelPoint, status, message);
        }
    }

    public void registerChannelCloseUpdateListener(ChannelCloseUpdateListener listener) {
        mChannelCloseUpdateListeners.add(listener);
    }

    public void unregisterChannelCloseUpdateListener(ChannelCloseUpdateListener listener) {
        mChannelCloseUpdateListeners.remove(listener);
    }

    private void broadcastChannelOpenUpdate(LightningNodeUri lightningNodeUri, int status, String message) {
        for (ChannelOpenUpdateListener listener : mChannelOpenUpdateListeners) {
            listener.onChannelOpenUpdate(lightningNodeUri, status, message);
        }
    }

    public void registerChannelOpenUpdateListener(ChannelOpenUpdateListener listener) {
        mChannelOpenUpdateListeners.add(listener);
    }

    public void unregisterChannelOpenUpdateListener(ChannelOpenUpdateListener listener) {
        mChannelOpenUpdateListeners.remove(listener);
    }

    private void broadcastWalletLoadedUpdate() {
        for (WalletLoadedListener listener : mWalletLoadedListeners) {
            listener.onWalletLoaded();
        }
    }

    public void registerWalletLoadedListener(WalletLoadedListener listener) {
        mWalletLoadedListeners.add(listener);
    }

    public void unregisterWalletLoadedListener(WalletLoadedListener listener) {
        mWalletLoadedListeners.remove(listener);
    }

    public interface LndConnectionTestListener {

        int ERROR_LOCKED = 0;
        int ERROR_INTERRUPTED = 1;
        int ERROR_TIMEOUT = 2;
        int ERROR_UNAVAILABLE = 3;
        int ERROR_AUTHENTICATION = 4;
        int ERROR_TOR = 5;
        int ERROR_HOST_VERIFICATION = 6;
        int ERROR_HOST_UNRESOLVABLE = 7;
        int ERROR_NETWORK_UNREACHABLE = 8;
        int ERROR_CERTIFICATE_NOT_TRUSTED = 9;
        int ERROR_INTERNAL = 10;
        int ERROR_INTERNAL_CLEARNET = 11;

        void onLndConnectError(int error);

        void onLndConnectError(String error);

        void onLndConnectSuccess();

        void onLndConnectionTestStarted();
    }

    public interface WalletLoadedListener {
        void onWalletLoaded();
    }

    public interface BalanceListener {
        void onBalanceUpdated();
    }

    public interface InfoListener {
        void onInfoUpdated(boolean connected);
    }

    public interface HistoryListener {
        void onHistoryUpdated();
    }

    public interface InvoiceSubscriptionListener {
        void onNewInvoiceAdded(Invoice invoice);

        void onExistingInvoiceUpdated(Invoice invoice);
    }

    public interface TransactionSubscriptionListener {
        void onTransactionEvent(Transaction transaction);
    }

    public interface HtlcSubscriptionListener {
        void onHtlcEvent(HtlcEvent htlcEvent);
    }

    public interface ChannelEventSubscriptionListener {
        void onChannelEvent(ChannelEventUpdate channelEventUpdate);
    }

    public interface ChannelCloseUpdateListener {

        int SUCCESS = -1;
        int ERROR_PEER_OFFLINE = 0;
        int ERROR_CHANNEL_TIMEOUT = 1;
        int ERROR_CHANNEL_CLOSE = 2;

        void onChannelCloseUpdate(String channelPoint, int status, String message);
    }

    public interface ChannelBackupSubscriptionListener {
        void onChannelBackupEvent(ChanBackupSnapshot chanBackupSnapshot);
    }

    public interface ChannelsUpdatedSubscriptionListener {
        void onChannelsUpdated();
    }

    public interface ChannelOpenUpdateListener {

        int SUCCESS = -1;
        int ERROR_GET_PEERS_TIMEOUT = 0;
        int ERROR_GET_PEERS = 1;
        int ERROR_CONNECTION_TIMEOUT = 2;
        int ERROR_CONNECTION_REFUSED = 3;
        int ERROR_CONNECTION_SELF = 4;
        int ERROR_CONNECTION_NO_HOST = 5;
        int ERROR_CONNECTION = 6;
        int ERROR_CHANNEL_TIMEOUT = 7;
        int ERROR_CHANNEL_PENDING_MAX = 8;
        int ERROR_CHANNEL_OPEN = 9;

        void onChannelOpenUpdate(LightningNodeUri lightningNodeUri, int status, String message);
    }

    public enum Network {
        MAINNET,
        TESTNET,
        REGTEST;

        public static Wallet.Network parseFromString(String enumAsString) {
            try {
                return valueOf(enumAsString.toUpperCase());
            } catch (Exception ex) {
                return MAINNET;
            }
        }
    }
}




