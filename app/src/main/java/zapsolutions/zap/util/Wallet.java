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
import com.github.lightningnetwork.lnd.lnrpc.CloseStatusUpdate;
import com.github.lightningnetwork.lnd.lnrpc.ClosedChannelsRequest;
import com.github.lightningnetwork.lnd.lnrpc.ClosedChannelsResponse;
import com.github.lightningnetwork.lnd.lnrpc.ConnectPeerRequest;
import com.github.lightningnetwork.lnd.lnrpc.ConnectPeerResponse;
import com.github.lightningnetwork.lnd.lnrpc.GetInfoRequest;
import com.github.lightningnetwork.lnd.lnrpc.GetInfoResponse;
import com.github.lightningnetwork.lnd.lnrpc.GetTransactionsRequest;
import com.github.lightningnetwork.lnd.lnrpc.Invoice;
import com.github.lightningnetwork.lnd.lnrpc.InvoiceSubscription;
import com.github.lightningnetwork.lnd.lnrpc.LightningAddress;
import com.github.lightningnetwork.lnd.lnrpc.LightningGrpc;
import com.github.lightningnetwork.lnd.lnrpc.ListChannelsRequest;
import com.github.lightningnetwork.lnd.lnrpc.ListChannelsResponse;
import com.github.lightningnetwork.lnd.lnrpc.ListInvoiceRequest;
import com.github.lightningnetwork.lnd.lnrpc.ListInvoiceResponse;
import com.github.lightningnetwork.lnd.lnrpc.ListPaymentsRequest;
import com.github.lightningnetwork.lnd.lnrpc.ListPaymentsResponse;
import com.github.lightningnetwork.lnd.lnrpc.ListPeersRequest;
import com.github.lightningnetwork.lnd.lnrpc.ListPeersResponse;
import com.github.lightningnetwork.lnd.lnrpc.NodeInfo;
import com.github.lightningnetwork.lnd.lnrpc.NodeInfoRequest;
import com.github.lightningnetwork.lnd.lnrpc.OpenChannelRequest;
import com.github.lightningnetwork.lnd.lnrpc.OpenStatusUpdate;
import com.github.lightningnetwork.lnd.lnrpc.PayReq;
import com.github.lightningnetwork.lnd.lnrpc.Payment;
import com.github.lightningnetwork.lnd.lnrpc.Peer;
import com.github.lightningnetwork.lnd.lnrpc.PendingChannelsRequest;
import com.github.lightningnetwork.lnd.lnrpc.PendingChannelsResponse;
import com.github.lightningnetwork.lnd.lnrpc.Transaction;
import com.github.lightningnetwork.lnd.lnrpc.TransactionDetails;
import com.github.lightningnetwork.lnd.lnrpc.UnlockWalletRequest;
import com.github.lightningnetwork.lnd.lnrpc.UnlockWalletResponse;
import com.github.lightningnetwork.lnd.lnrpc.WalletBalanceRequest;
import com.github.lightningnetwork.lnd.lnrpc.WalletBalanceResponse;
import com.github.lightningnetwork.lnd.lnrpc.WalletUnlockerGrpc;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.StreamObserver;
import zapsolutions.zap.R;
import zapsolutions.zap.connection.establishConnectionToLnd.LndConnection;
import zapsolutions.zap.lightning.LightningNodeUri;

import static zapsolutions.zap.util.UtilFunctions.hexStringToByteArray;


public class Wallet {

    private static final String LOG_TAG = Wallet.class.getName();

    private static Wallet mInstance = null;
    private final Set<BalanceListener> mBalanceListeners = new HashSet<>();
    private final Set<InfoListener> mInfoListeners = new HashSet<>();
    private final Set<HistoryListener> mHistoryListeners = new HashSet<>();
    private final Set<WalletLoadedListener> mWalletLoadedListeners = new HashSet<>();
    private final Set<InvoiceSubscriptionListener> mInvoiceSubscriptionListeners = new HashSet<>();
    private final Set<TransactionSubscriptionListener> mTransactionSubscriptionListeners = new HashSet<>();
    private final Set<ChannelEventSubscriptionListener> mChannelEventSubscriptionListeners = new HashSet<>();
    private final Set<ChannelsUpdatedSubscriptionListener> mChannelsUpdatedSubscriptionListeners = new HashSet<>();
    private final Set<ChannelBackupSubscriptionListener> mChannelBackupSubscriptionListeners = new HashSet<>();
    private final Set<ChannelCloseUpdateListener> mChannelCloseUpdateListeners = new HashSet<>();
    private final Set<ChannelOpenUpdateListener> mChannelOpenUpdateListeners = new HashSet<>();

    public PayReq mPaymentRequest = null;
    public String mPaymentRequestString = "";
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

    private long mOnChainBalanceTotal = 0;
    private long mOnChainBalanceConfirmed = 0;
    private long mOnChainBalanceUnconfirmed = 0;
    private long mChannelBalance = 0;
    private long mChannelBalancePendingOpen = 0;
    private long mChannelBalanceLimbo = 0;
    private boolean mConnectedToLND = false;
    private boolean mInfoFetched = false;
    private boolean mSyncedToChain = false;
    private boolean mTransactionUpdated = false;
    private boolean mInvoicesUpdated = false;
    private boolean mPaymentsUpdated = false;
    private boolean mUpdatingHistory = false;
    private boolean mTestnet = false;
    private boolean mConnectionCheckInProgress = false;
    private String mLNDVersion = "not connected";
    private ClientCallStreamObserver<Invoice> mInvoiceStreamObserver;
    private ClientCallStreamObserver<TransactionDetails> mTransactionStreamObserver;
    private ClientCallStreamObserver<ChannelEventUpdate> mChannelEventStreamObserver;
    private ClientCallStreamObserver<ChanBackupSnapshot> mChannelBackupStreamObserver;
    private Handler mHandler = new Handler();
    private DebounceHandler mChannelsUpdateDebounceHandler = new DebounceHandler();

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
     * Use this to reset the wallet information when the connection type was changed.
     */
    public void reset() {
        mPaymentRequest = null;
        mOnChainBalanceTotal = 0;
        mOnChainBalanceConfirmed = 0;
        mOnChainBalanceUnconfirmed = 0;
        mChannelBalance = 0;
        mChannelBalancePendingOpen = 0;
        mChannelBalanceLimbo = 0;

        mInfoFetched = false;
        mSyncedToChain = false;
        mTestnet = false;
        mLNDVersion = "not connected";
        mHandler.removeCallbacksAndMessages(null);
        mChannelsUpdateDebounceHandler.shutdown();
    }

    /**
     * This will be used on loading. If this request finishes without an error, our connection to LND is established.
     * All Listeners registered to WalletLoadedListener will be informed about any changes.
     */
    public void checkIfLndIsReachableAndTriggerWalletLoadedInterface() {
        // Retrieve info from LND with gRPC (async)


        if (!mConnectionCheckInProgress) {

            mConnectionCheckInProgress = true;

            LightningGrpc.LightningFutureStub asyncInfoClient = LightningGrpc
                    .newFutureStub(LndConnection.getInstance().getSecureChannel())
                    .withDeadlineAfter(5, TimeUnit.SECONDS)
                    .withCallCredentials(LndConnection.getInstance().getMacaroon());

            GetInfoRequest asyncInfoRequest = GetInfoRequest.newBuilder().build();
            final ListenableFuture<GetInfoResponse> infoFuture = asyncInfoClient.getInfo(asyncInfoRequest);

            ZapLog.debug(LOG_TAG, "Test if LND is reachable.");

            infoFuture.addListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        GetInfoResponse infoResponse = infoFuture.get();

                        // Save the received data.
                        mSyncedToChain = infoResponse.getSyncedToChain();
                        mTestnet = infoResponse.getTestnet();
                        mLNDVersion = infoResponse.getVersion();
                        mInfoFetched = true;
                        mConnectedToLND = true;

                        mConnectionCheckInProgress = false;
                        broadcastWalletLoadedUpdate(true, -1);
                    } catch (InterruptedException e) {
                        ZapLog.debug(LOG_TAG, "Test if LND is reachable was interrupted.");
                        mConnectionCheckInProgress = false;
                        broadcastWalletLoadedUpdate(false, WalletLoadedListener.ERROR_INTERRUPTED);
                    } catch (ExecutionException e) {
                        mConnectionCheckInProgress = false;
                        if (e.getMessage().toLowerCase().contains("unavailable")) {
                            // This is the case if:
                            // - LND deamon is not running
                            // - An incorrect port is used
                            // - A wrong certificate is used (When the certificate creation failed due to an error)
                            broadcastWalletLoadedUpdate(false, WalletLoadedListener.ERROR_UNAVAILABLE);
                        } else if (e.getMessage().toLowerCase().contains("deadline_exceeded")) {
                            // This is the case if:
                            // - The server is not reachable at all. (e.g. wrong IP Address or server offline)
                            ZapLog.debug(LOG_TAG, "Cannot reach remote");
                            broadcastWalletLoadedUpdate(false, WalletLoadedListener.ERROR_TIMEOUT);
                        } else if (e.getMessage().toLowerCase().contains("unimplemented")) {
                            // This is the case if:
                            // - The wallet is locked
                            broadcastWalletLoadedUpdate(false, WalletLoadedListener.ERROR_LOCKED);
                            ZapLog.debug(LOG_TAG, "Wallet is locked!");
                        } else if (e.getMessage().toLowerCase().contains("verification failed")) {
                            // This is the case if:
                            // - The macaroon is invalid
                            broadcastWalletLoadedUpdate(false, WalletLoadedListener.ERROR_AUTHENTICATION);
                            ZapLog.debug(LOG_TAG, "Macaroon is invalid!");
                        } else if (e.getMessage().contains("UNKNOWN")) {
                            // This is the case if:
                            // - The macaroon has wrong encoding
                            broadcastWalletLoadedUpdate(false, WalletLoadedListener.ERROR_AUTHENTICATION);
                            ZapLog.debug(LOG_TAG, "Macaroon is invalid!");
                        }
                        ZapLog.debug(LOG_TAG, e.getMessage());
                    }
                }
            }, new ExecuteOnCaller());
        }
    }

    /**
     * Call this if the deamon is running, but the wallet is not unlocked yet.
     *
     * @param password
     */
    public void unlockWallet(String password) {
        //UnlockWallet
        WalletUnlockerGrpc.WalletUnlockerFutureStub asyncUnlockClient = WalletUnlockerGrpc
                .newFutureStub(LndConnection.getInstance().getSecureChannel())
                .withCallCredentials(LndConnection.getInstance().getMacaroon());

        UnlockWalletRequest asyncUnlockRequest = UnlockWalletRequest.newBuilder()
                .setWalletPassword(ByteString.copyFrom(password, Charset.defaultCharset()))
                .build();

        final ListenableFuture<UnlockWalletResponse> unlockFuture = asyncUnlockClient.unlockWallet(asyncUnlockRequest);

        unlockFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    UnlockWalletResponse unlockResponse = unlockFuture.get();
                    ZapLog.debug(LOG_TAG, "successfully unlocked");

                    if (PrefsUtil.isWalletSetup()) {
                        LndConnection.getInstance().stopBackgroundTasks();
                    }
                    LndConnection.getInstance().restartBackgroundTasks();

                    mHandler.postDelayed(() -> {
                        // We have to call this delayed, as without it, it will show as unconnected until the wallet button is hit again.
                        // ToDo: Create a routine that retries this until successful
                        checkIfLndIsReachableAndTriggerWalletLoadedInterface();
                    }, 10000);

                    mHandler.postDelayed(() -> {
                        // The channels are already fetched before, but they are all showed and saved as offline right after unlocking.
                        // That's why we update it again 10 seconds later.
                        // ToDo: Create a routine that retries this until successful
                        Wallet.getInstance().fetchChannelsFromLND();
                    }, 12000);


                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {

                    ZapLog.debug(LOG_TAG, e.getMessage());

                    // Show password prompt again after error
                    broadcastWalletLoadedUpdate(false, WalletLoadedListener.ERROR_LOCKED);

                }
            }
        }, new ExecuteOnCaller());

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

    /**
     * This will fetch the current balance from LND.
     * All Listeners registered to BalanceListener will be informed about any changes.
     */
    public void fetchBalanceFromLND() {

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

                    // ZapLog.debug(LOG_TAG,balanceResponse.toString());

                    // Update the on-chain balances of our wallet util to the fetched values
                    setOnChainBalance(balanceResponse.getTotalBalance(),
                            balanceResponse.getConfirmedBalance(),
                            balanceResponse.getUnconfirmedBalance());
                } catch (InterruptedException e) {
                    ZapLog.debug(LOG_TAG, "Wallet balance request interrupted.");
                } catch (ExecutionException e) {
                    ZapLog.debug(LOG_TAG, "Exception in wallet balance request task.");
                }
            }
        }, new ExecuteOnCaller());


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

                    // ZapLog.debug(LOG_TAG,channelBalanceResponse.toString());

                    // Update the channel balances of our wallet util to the fetched values
                    setChannelBalance(channelBalanceResponse.getBalance(),
                            channelBalanceResponse.getPendingOpenBalance());
                } catch (InterruptedException e) {
                    ZapLog.debug(LOG_TAG, "Channel balance request interrupted.");
                } catch (ExecutionException e) {
                    ZapLog.debug(LOG_TAG, "Exception in channel balance request task.");
                }
            }
        }, new ExecuteOnCaller());

        // fetch pending channels balance
        LightningGrpc.LightningFutureStub asyncPendingChannelsClient = LightningGrpc
                .newFutureStub(LndConnection.getInstance().getSecureChannel())
                .withCallCredentials(LndConnection.getInstance().getMacaroon());

        PendingChannelsRequest asyncPendingChannelsRequest = PendingChannelsRequest.newBuilder().build();
        final ListenableFuture<PendingChannelsResponse> pendingChannelsFuture = asyncPendingChannelsClient.pendingChannels(asyncPendingChannelsRequest);

        pendingChannelsFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    PendingChannelsResponse pendingChannelsResponse = pendingChannelsFuture.get();

                    /* Update balance to include limbo channels. The limbo balance does not account for open pending channels.
                    Those are handled via the `channelbalance` request. */
                    setChannelBalanceLimbo(pendingChannelsResponse.getTotalLimboBalance());

                    // ZapLog.debug(LOG_TAG, pendingChannelsResponse.getTotalLimboBalance());
                } catch (InterruptedException e) {
                    ZapLog.debug(LOG_TAG, "List pending channels request interrupted.");
                } catch (ExecutionException e) {
                    ZapLog.debug(LOG_TAG, "Exception in list pending channels request task.");
                }
            }
        }, new ExecuteOnCaller());

    }

    /**
     * This will fetch the current info from LND.
     * All Listeners registered to InfoListener will be informed about any changes.
     */
    public void fetchInfoFromLND() {
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

                    // Save the received data.
                    mSyncedToChain = infoResponse.getSyncedToChain();
                    mTestnet = infoResponse.getTestnet();
                    mLNDVersion = infoResponse.getVersion();
                    mInfoFetched = true;
                    mConnectedToLND = true;

                    // ZapLog.debug(LOG_TAG,infoResponse.toString());

                    broadcastInfoUpdate(true);
                } catch (InterruptedException e) {
                    ZapLog.debug(LOG_TAG, "Info request interrupted.");
                } catch (ExecutionException e) {
                    if (e.getMessage().toLowerCase().contains("unavailable")) {
                        mConnectedToLND = false;
                        broadcastInfoUpdate(false);
                    }
                    ZapLog.debug(LOG_TAG, e.getMessage());
                }
            }
        }, new ExecuteOnCaller());
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

                    mOnChainTransactionList = Lists.reverse(transactionResponse.getTransactionsList());

                    mTransactionUpdated = true;
                    isHistoryUpdateFinished();

                    // ZapLog.debug(LOG_TAG, transactionResponse.toString());
                } catch (InterruptedException e) {
                    ZapLog.debug(LOG_TAG, "Transaction request interrupted.");
                } catch (ExecutionException e) {
                    ZapLog.debug(LOG_TAG, "Exception in transaction request task.");
                }
            }
        }, new ExecuteOnCaller());
    }

    /**
     * This will fetch all lightning invoices from LND.
     */
    public void fetchInvoicesFromLND() {

        mTempInvoiceUpdateList = new LinkedList<>();

        fetchInvoicesFromLND(100);
    }

    public void fetchInvoicesFromLND(long lastIndex) {
        // Fetch lightning invoices
        LightningGrpc.LightningFutureStub asyncInvoiceClient = LightningGrpc
                .newFutureStub(LndConnection.getInstance().getSecureChannel())
                .withCallCredentials(LndConnection.getInstance().getMacaroon());

        ListInvoiceRequest asyncInvoiceRequest = ListInvoiceRequest.newBuilder()
                //.setReversed(true)
                //.setPendingOnly(true)
                .setNumMaxInvoices(lastIndex)
                .build();
        final ListenableFuture<ListInvoiceResponse> invoiceFuture = asyncInvoiceClient.listInvoices(asyncInvoiceRequest);

        invoiceFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ListInvoiceResponse invoiceResponse = invoiceFuture.get();

                    mTempInvoiceUpdateList.addAll(invoiceResponse.getInvoicesList());

                    if (invoiceResponse.getLastIndexOffset() < lastIndex) {
                        // we have fetched all available invoices!
                        mInvoiceList = Lists.reverse(mTempInvoiceUpdateList);
                        mTempInvoiceUpdateList = null;
                        mInvoicesUpdated = true;
                        isHistoryUpdateFinished();
                    } else {
                        // there are still invoices to fetch, get the next batch!
                        fetchInvoicesFromLND(lastIndex + 100);
                    }


                    // ZapLog.debug(LOG_TAG, String.valueOf(invoiceResponse.toString()));
                } catch (InterruptedException e) {
                    ZapLog.debug(LOG_TAG, "Invoice request interrupted.");
                } catch (ExecutionException e) {
                    ZapLog.debug(LOG_TAG, "Exception in invoice request task.");
                }
            }
        }, new ExecuteOnCaller());
    }

    /**
     * This will fetch lightning payments from LND.
     */
    public void fetchPaymentsFromLND() {
        // Fetch lightning payments
        LightningGrpc.LightningFutureStub asyncPaymentsClient = LightningGrpc
                .newFutureStub(LndConnection.getInstance().getSecureChannel())
                .withCallCredentials(LndConnection.getInstance().getMacaroon());

        ListPaymentsRequest asyncPaymentsRequest = ListPaymentsRequest.newBuilder()
                .setIncludeIncomplete(false)
                .build();
        final ListenableFuture<ListPaymentsResponse> paymentsFuture = asyncPaymentsClient.listPayments(asyncPaymentsRequest);

        paymentsFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ListPaymentsResponse paymentsResponse = paymentsFuture.get();

                    mPaymentsList = Lists.reverse(paymentsResponse.getPaymentsList());
                    mPaymentsUpdated = true;
                    isHistoryUpdateFinished();

                    /*
                    // Load invoices for all involved payments. This allows us to display memos later.
                    if (mPaymentsList != null) {
                        for (Payment p : mPaymentsList) {
                            lookupInvoiceWithLND(p.getPaymentHashBytes());
                        }
                    }
                    */

                    // ZapLog.debug(LOG_TAG, String.valueOf(paymentsResponse.toString()));
                } catch (InterruptedException e) {
                    ZapLog.debug(LOG_TAG, "Payment request interrupted.");
                } catch (ExecutionException e) {
                    ZapLog.debug(LOG_TAG, "Exception in payment request task.");
                }
            }
        }, new ExecuteOnCaller());
    }

    public void openChannel(LightningNodeUri nodeUri, long amount) {
        LightningGrpc.LightningStub lightningStub = LightningGrpc.newStub(LndConnection.getInstance().getSecureChannel())
                .withCallCredentials(LndConnection.getInstance().getMacaroon())
                .withDeadlineAfter(15, TimeUnit.SECONDS);

        ListPeersRequest listPeersRequest = ListPeersRequest.newBuilder().build();
        lightningStub.listPeers(listPeersRequest, new StreamObserver<ListPeersResponse>() {
            @Override
            public void onNext(ListPeersResponse value) {
                boolean connected = false;
                for (Peer node: value.getPeersList()) {
                    if(node.getPubKey().equals(nodeUri.getPubKey())) {
                        connected = true;
                        break;
                    }
                }

                if(connected) {
                    ZapLog.debug(LOG_TAG, "Already connected to peer, trying to open channel...");
                    openChannelConnected(nodeUri,amount);
                } else {
                    ZapLog.debug(LOG_TAG, "Not connected to peer, trying to connect...");

                    LightningAddress lightningAddress = LightningAddress.newBuilder()
                            .setHostBytes(ByteString.copyFrom(nodeUri.getHost().getBytes(StandardCharsets.UTF_8)))
                            .setPubkeyBytes(ByteString.copyFrom(nodeUri.getPubKey().getBytes(StandardCharsets.UTF_8))).build();
                    ConnectPeerRequest connectPeerRequest = ConnectPeerRequest.newBuilder().setAddr(lightningAddress).build();

                    lightningStub.connectPeer(connectPeerRequest, new StreamObserver<ConnectPeerResponse>() {
                        @Override
                        public void onNext(ConnectPeerResponse value) {
                            ZapLog.debug(LOG_TAG, "Successfully connected to peer, trying to open channel...");
                            openChannelConnected(nodeUri,amount);
                        }

                        @Override
                        public void onError(Throwable t) {
                            ZapLog.debug(LOG_TAG, "Error opening channel:" + t.getLocalizedMessage());
                            broadcastChannelOpenUpdate(nodeUri, false);
                        }

                        @Override
                        public void onCompleted() {

                        }
                    });
                }
            }

            @Override
            public void onError(Throwable t) {
                ZapLog.debug(LOG_TAG, "Error connecting to peer:" + t.getLocalizedMessage());
                broadcastChannelOpenUpdate(nodeUri, false);
            }

            @Override
            public void onCompleted() {

            }
        });
    }

    private void openChannelConnected(LightningNodeUri nodeUri, long amount) {
        LightningGrpc.LightningStub lightningStub = LightningGrpc.newStub(LndConnection.getInstance().getSecureChannel())
                .withCallCredentials(LndConnection.getInstance().getMacaroon());

        byte[] nodeKeyBytes = hexStringToByteArray(nodeUri.getPubKey());
        OpenChannelRequest openChannelRequest = OpenChannelRequest.newBuilder()
                .setNodePubkey(ByteString.copyFrom(nodeKeyBytes))
                .setLocalFundingAmount(amount).build();

        lightningStub.openChannel(openChannelRequest, new StreamObserver<OpenStatusUpdate>() {
            @Override
            public void onNext(OpenStatusUpdate value) {
                ZapLog.debug(LOG_TAG, "Open channel update: " + value.getUpdateCase().getNumber());
                broadcastChannelOpenUpdate(nodeUri, true);
            }

            @Override
            public void onError(Throwable t) {
                ZapLog.debug(LOG_TAG, "Error opening channel:" + t.getLocalizedMessage());
                broadcastChannelOpenUpdate(nodeUri, false);
            }

            @Override
            public void onCompleted() {

            }
        });
    }

    public void closeChannel(String channelPoint, boolean force) {
        ChannelPoint point = ChannelPoint.newBuilder()
                .setFundingTxidStr(channelPoint.substring(0, channelPoint.indexOf(':')))
                .setOutputIndex(Character.getNumericValue(channelPoint.charAt(channelPoint.length() - 1))).build();

        CloseChannelRequest closeChannelRequest = CloseChannelRequest.newBuilder().setChannelPoint(point).setForce(force).build();

        LightningGrpc.newStub(LndConnection.getInstance().getSecureChannel())
                .withCallCredentials(LndConnection.getInstance().getMacaroon())
                .closeChannel(closeChannelRequest, new StreamObserver<CloseStatusUpdate>() {
                    @Override
                    public void onNext(CloseStatusUpdate value) {
                        ZapLog.debug(LOG_TAG, "Closing channel update: " + value.getUpdateCase().getNumber());
                        broadcastChannelCloseUpdate(channelPoint, true);
                    }

                    @Override
                    public void onError(Throwable t) {
                        ZapLog.debug(LOG_TAG, "Error closing channel");
                        broadcastChannelCloseUpdate(channelPoint, false);
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    public void fetchChannelsFromLND() {
        ZapLog.debug(LOG_TAG, "Fetch channels from LND.");
        LightningGrpc.LightningFutureStub client = LightningGrpc
                .newFutureStub(LndConnection.getInstance().getSecureChannel())
                .withCallCredentials(LndConnection.getInstance().getMacaroon());

        ListChannelsRequest asyncOpenChannelsRequest = ListChannelsRequest.newBuilder().build();
        final ListenableFuture<ListChannelsResponse> openChannelsFuture = client.listChannels(asyncOpenChannelsRequest);

        PendingChannelsRequest asyncPendingChannelsRequest = PendingChannelsRequest.newBuilder().build();
        final ListenableFuture<PendingChannelsResponse> pendingChannelsFuture = client.pendingChannels(asyncPendingChannelsRequest);

        ClosedChannelsRequest asyncClosedChannelsRequest = ClosedChannelsRequest.newBuilder().build();
        final ListenableFuture<ClosedChannelsResponse> closedChannelsFuture = client.closedChannels(asyncClosedChannelsRequest);

        Futures.whenAllSucceed(openChannelsFuture, pendingChannelsFuture, closedChannelsFuture).run(() -> {
            try {
                ZapLog.debug(LOG_TAG, "Fetched channels from LND.");

                // open channels
                ListChannelsResponse openChannelsResponse = openChannelsFuture.get();
                mOpenChannelsList = openChannelsResponse.getChannelsList();

                // Load NodeInfos for all involved nodes. This allows us to display aliases later.
                for (Channel c : mOpenChannelsList) {
                    fetchNodeInfoFromLND(c.getRemotePubkey());
                }

                // closed channels
                ClosedChannelsResponse closedChannelsResponse = closedChannelsFuture.get();
                mClosedChannelsList = closedChannelsResponse.getChannelsList();
                // Load NodeInfos for all involved nodes. This allows us to display aliases later.
                for (ChannelCloseSummary c : mClosedChannelsList) {
                    fetchNodeInfoFromLND(c.getRemotePubkey());
                }

                // pending channels
                PendingChannelsResponse pendingChannelsResponse = pendingChannelsFuture.get();

                mPendingOpenChannelsList = pendingChannelsResponse.getPendingOpenChannelsList();
                mPendingClosedChannelsList = pendingChannelsResponse.getPendingClosingChannelsList();
                mPendingForceClosedChannelsList = pendingChannelsResponse.getPendingForceClosingChannelsList();
                mPendingWaitingCloseChannelsList = pendingChannelsResponse.getWaitingCloseChannelsList();

                // Load NodeInfos for all involved nodes. This allows us to display aliases later.
                for (PendingChannelsResponse.PendingOpenChannel c : mPendingOpenChannelsList) {
                    fetchNodeInfoFromLND(c.getChannel().getRemoteNodePub());
                }
                for (PendingChannelsResponse.ClosedChannel c : mPendingClosedChannelsList) {
                    fetchNodeInfoFromLND(c.getChannel().getRemoteNodePub());
                }
                for (PendingChannelsResponse.ForceClosedChannel c : mPendingForceClosedChannelsList) {
                    fetchNodeInfoFromLND(c.getChannel().getRemoteNodePub());
                }
                for (PendingChannelsResponse.WaitingCloseChannel c : mPendingWaitingCloseChannelsList) {
                    fetchNodeInfoFromLND(c.getChannel().getRemoteNodePub());
                }

                broadcastChannelsUpdated();

            } catch (InterruptedException e) {
                ZapLog.debug(LOG_TAG, "Get channels request interrupted.");
            } catch (ExecutionException e) {
                ZapLog.debug(LOG_TAG, "Exception in get channels info request task.");
            }
        }, Executors.newSingleThreadExecutor());
    }

    /**
     * This will fetch the NodeInfo according to the supplied pubkey.
     * The NodeInfo will then be added to the mNodeInfos list (no duplicates) which can then
     * be used for non async tasks, such as getting the aliases for channels.
     *
     * @param pubkey
     */
    public void fetchNodeInfoFromLND(String pubkey) {
        // fetch node info
        LightningGrpc.LightningFutureStub asyncNodeInfoClient = LightningGrpc
                .newFutureStub(LndConnection.getInstance().getSecureChannel())
                .withCallCredentials(LndConnection.getInstance().getMacaroon());

        NodeInfoRequest asyncNodeInfoRequest = NodeInfoRequest.newBuilder()
                .setPubKey(pubkey)
                .build();
        final ListenableFuture<NodeInfo> nodeInfoFuture = asyncNodeInfoClient.getNodeInfo(asyncNodeInfoRequest);

        nodeInfoFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    NodeInfo nodeInfoResponse = nodeInfoFuture.get();

                    // Add the nodeInfo to our list, if it is not already a member of the list.
                    boolean nodeInfoAlreadyExists = false;
                    for (NodeInfo i : mNodeInfos) {
                        if (i.getNode().getPubKey().equals(nodeInfoResponse.getNode().getPubKey())) {
                            nodeInfoAlreadyExists = true;
                        }
                    }
                    if (!nodeInfoAlreadyExists) {
                        mNodeInfos.add(nodeInfoResponse);
                    }

                    // ZapLog.debug(LOG_TAG, nodeInfoResponse.toString());
                } catch (InterruptedException e) {
                    ZapLog.debug(LOG_TAG, "Get node info request interrupted.");
                } catch (ExecutionException e) {
                    ZapLog.debug(LOG_TAG, "Exception in get node info request task.");
                    // ZapLog.debug(LOG_TAG, e.getMessage());
                }
            }
        }, new ExecuteOnCaller());
    }


    /**
     * Use this to subscribe the wallet to transaction events that happen on LND.
     * The events will be captured and forwarded to the TransactionSubscriptionListener.
     * All parts of the App that want to react on transaction events have to subscribe to the
     * TransactionSubscriptionListener.
     */
    public void subscribeToTransactions() {

        LightningGrpc.LightningStub streamingTransactionClient = LightningGrpc
                .newStub(LndConnection.getInstance().getSecureChannel())
                .withCallCredentials(LndConnection.getInstance().getMacaroon());

        GetTransactionsRequest streamingTransactionRequest = GetTransactionsRequest.newBuilder()
                .build();

        mTransactionStreamObserver = new ClientCallStreamObserver<TransactionDetails>() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setOnReadyHandler(Runnable onReadyHandler) {

            }

            @Override
            public void disableAutoInboundFlowControl() {

            }

            @Override
            public void request(int count) {

            }

            @Override
            public void setMessageCompression(boolean enable) {

            }

            @Override
            public void cancel(@Nullable String message, @Nullable Throwable cause) {

            }

            @Override
            public void onNext(TransactionDetails transactionDetails) {

                ZapLog.debug(LOG_TAG, "Received transaction subscription event.");

                broadcastTransactionUpdate(transactionDetails);

            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        };

        streamingTransactionClient.getTransactions(streamingTransactionRequest, mTransactionStreamObserver);

    }

    public void cancelTransactionSubscription() {
        if (mTransactionStreamObserver != null) {
            mTransactionStreamObserver.cancel(null, null);
        }
    }


    /**
     * Use this to subscribe the wallet to invoice events that happen on LND.
     * The events will be captured and forwarded to the InvoiceSubscriptionListener.
     * All parts of the App that want to react on invoice events have to subscribe to the
     * InvoiceSubscriptionListener.
     */
    public void subscribeToInvoices() {

        LightningGrpc.LightningStub streamingInvoiceClient = LightningGrpc
                .newStub(LndConnection.getInstance().getSecureChannel())
                .withCallCredentials(LndConnection.getInstance().getMacaroon());

        InvoiceSubscription streamingInvoiceRequest = InvoiceSubscription.newBuilder()
                .build();

        mInvoiceStreamObserver = new ClientCallStreamObserver<Invoice>() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setOnReadyHandler(Runnable onReadyHandler) {

            }

            @Override
            public void disableAutoInboundFlowControl() {

            }

            @Override
            public void request(int count) {

            }

            @Override
            public void setMessageCompression(boolean enable) {

            }

            @Override
            public void cancel(@Nullable String message, @Nullable Throwable cause) {

            }

            @Override
            public void onNext(Invoice invoice) {

                ZapLog.debug(LOG_TAG, "Received invoice subscription event.");

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

            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        };

        streamingInvoiceClient.subscribeInvoices(streamingInvoiceRequest, mInvoiceStreamObserver);

    }

    public void cancelInvoiceSubscription() {
        if (mInvoiceStreamObserver != null) {
            mInvoiceStreamObserver.cancel(null, null);
        }
    }


    /**
     * Use this to subscribe the wallet to channel events that happen on LND.
     * The events will be captured and forwarded to the ChannelEventSubscriptionListener.
     * All parts of the App that want to react on channel events have to subscribe to the
     * ChannelEventSubscriptionListener.
     */
    public void subscribeToChannelEvents() {

        LightningGrpc.LightningStub streamingChannelEventClient = LightningGrpc
                .newStub(LndConnection.getInstance().getSecureChannel())
                .withCallCredentials(LndConnection.getInstance().getMacaroon());

        ChannelEventSubscription streamingChannelEventRequest = ChannelEventSubscription.newBuilder()
                .build();

        mChannelEventStreamObserver = new ClientCallStreamObserver<ChannelEventUpdate>() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setOnReadyHandler(Runnable onReadyHandler) {

            }

            @Override
            public void disableAutoInboundFlowControl() {

            }

            @Override
            public void request(int count) {

            }

            @Override
            public void setMessageCompression(boolean enable) {

            }

            @Override
            public void cancel(@Nullable String message, @Nullable Throwable cause) {

            }

            @Override
            public void onNext(ChannelEventUpdate channelEventUpdate) {
                ZapLog.debug(LOG_TAG, "Received channel update event");
                switch (channelEventUpdate.getChannelCase()) {
                    case OPEN_CHANNEL:
                        ZapLog.debug(LOG_TAG, "Channel has been opened");
                        break;
                    case CLOSED_CHANNEL:
                        ZapLog.debug(LOG_TAG, "Channel has been closed");
                        break;
                    case ACTIVE_CHANNEL:
                        ZapLog.debug(LOG_TAG, "Channel went active");
                        break;
                    case INACTIVE_CHANNEL:
                        ZapLog.debug(LOG_TAG, "Open channel went to inactive");
                        break;
                    case CHANNEL_NOT_SET:
                        ZapLog.debug(LOG_TAG, "Received channel event update case: not set Channel");
                        break;
                    default:
                        ZapLog.debug(LOG_TAG, "Unknown channel event: " + channelEventUpdate.getChannelCase());
                        break;
                }

                updateLNDChannelsWithDebounce();

                broadcastChannelEvent(channelEventUpdate);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        };

        streamingChannelEventClient.subscribeChannelEvents(streamingChannelEventRequest, mChannelEventStreamObserver);
    }

    public void updateLNDChannelsWithDebounce() {
        ZapLog.debug(LOG_TAG, "Fetch channels from LND. (debounce)");

        mChannelsUpdateDebounceHandler.attempt(this::fetchChannelsFromLND, DebounceHandler.DEBOUNCE_1_SECOND);
    }

    public void cancelChannelEventSubscription() {
        if (mChannelEventStreamObserver != null) {
            mChannelEventStreamObserver.cancel(null, null);
        }
    }


    /**
     * Use this to subscribe the wallet to channel backup events that happen on LND.
     * The events will be captured and forwarded to the ChannelBackupSubscriptionListener.
     * All parts of the App that want to react on channel backups have to subscribe to the
     * ChannelBackupSubscriptionListener.
     */
    public void subscribeToChannelBackup() {

        LightningGrpc.LightningStub streamingChannelBackupClient = LightningGrpc
                .newStub(LndConnection.getInstance().getSecureChannel())
                .withCallCredentials(LndConnection.getInstance().getMacaroon());

        ChannelBackupSubscription streamingChannelBackupRequest = ChannelBackupSubscription.newBuilder()
                .build();

        mChannelBackupStreamObserver = new ClientCallStreamObserver<ChanBackupSnapshot>() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setOnReadyHandler(Runnable onReadyHandler) {

            }

            @Override
            public void disableAutoInboundFlowControl() {

            }

            @Override
            public void request(int count) {

            }

            @Override
            public void setMessageCompression(boolean enable) {

            }

            @Override
            public void cancel(@Nullable String message, @Nullable Throwable cause) {

            }

            @Override
            public void onNext(ChanBackupSnapshot chanBackupSnapshot) {

                ZapLog.debug(LOG_TAG, "Received channel backup event.");

                broadcastChannelBackup(chanBackupSnapshot);

            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        };

        streamingChannelBackupClient.subscribeChannelBackups(streamingChannelBackupRequest, mChannelBackupStreamObserver);

    }

    public void cancelChannelBackupSubscription() {
        if (mChannelBackupStreamObserver != null) {
            mChannelBackupStreamObserver.cancel(null, null);
        }
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
            payed = invoice.getValue() == invoice.getAmtPaidSat();
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
     * This functions helps us to link on-chain channel transaction with the corresponding channel's public node alias.
     *
     * @return pubKey of the Node the channel is linked to
     */
    private String getNodePubKeyFromChannelTransaction(Transaction transaction) {

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
     * Returns the the highest remote balance of all active channels.
     * This can be used to determine maximum possible receive amount for a lightning invoice as long as there is no splicing.
     *
     * @return
     */
    public long getMaxChannelRemoteBalance() {
        long tempMax = 0L;
        if (mOpenChannelsList != null) {
            for (Channel c : mOpenChannelsList) {
                if (c.getActive()) {
                    if (c.getRemoteBalance() > tempMax) {
                        tempMax = c.getRemoteBalance();
                    }
                }
            }
        }
        return tempMax;
    }

    /**
     * Returns the the highest local balance of all active channels.
     * This can be used to determine maximum possible send amount for a lightning payment as long as there is no splicing.
     *
     * @return
     */
    public long getMaxChannelLocalBalance() {
        long tempMax = 0L;
        if (mOpenChannelsList != null) {
            for (Channel c : mOpenChannelsList) {
                if (c.getActive()) {
                    if (c.getLocalBalance() > tempMax) {
                        tempMax = c.getLocalBalance();
                    }
                }
            }
        }
        return tempMax;
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

    public boolean isConnectedToLND() {
        return mConnectedToLND;
    }

    private void setOnChainBalance(long total, long confirmed, long unconfirmed) {
        mOnChainBalanceTotal = total;
        mOnChainBalanceConfirmed = confirmed;
        mOnChainBalanceUnconfirmed = unconfirmed;
        broadcastBalanceUpdate();
    }

    private void setChannelBalance(long balance, long pendingOpen) {
        mChannelBalance = balance;
        mChannelBalancePendingOpen = pendingOpen;
        broadcastBalanceUpdate();
    }

    private void setChannelBalanceLimbo(long balanceLimbo) {
        mChannelBalanceLimbo = balanceLimbo;
        broadcastBalanceUpdate();
    }

    /**
     * Notify all listeners to finished wallet initialization.
     *
     * @param success true if successful
     * @param error   one of WalletLoadedListener errors, -1 if successful
     */
    private void broadcastWalletLoadedUpdate(boolean success, int error) {
        for (WalletLoadedListener listener : mWalletLoadedListeners) {
            listener.onWalletLoadedUpdated(success, error);
        }
    }

    public void registerWalletLoadedListener(WalletLoadedListener listener) {
        mWalletLoadedListeners.add(listener);
    }

    public void unregisterWalletLoadedListener(WalletLoadedListener listener) {
        mWalletLoadedListeners.remove(listener);
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
     * @param transactionDetails the details about the transaction update
     */
    private void broadcastTransactionUpdate(TransactionDetails transactionDetails) {
        for (TransactionSubscriptionListener listener : mTransactionSubscriptionListeners) {
            listener.onTransactionEvent(transactionDetails);
        }
    }

    public void registerTransactionSubscriptionListener(TransactionSubscriptionListener listener) {
        mTransactionSubscriptionListeners.add(listener);
    }

    public void unregisterTransactionSubscriptionListener(TransactionSubscriptionListener listener) {
        mTransactionSubscriptionListeners.remove(listener);
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
    private void broadcastChannelCloseUpdate(String channelPoint, boolean success) {
        for (ChannelCloseUpdateListener listener : mChannelCloseUpdateListeners) {
            listener.onChannelCloseUpdate(channelPoint, success);
        }
    }

    public void registerChannelCloseUpdateListener(ChannelCloseUpdateListener listener) {
        mChannelCloseUpdateListeners.add(listener);
    }

    public void unregisterChannelCloseUpdateListener(ChannelCloseUpdateListener listener) {
        mChannelCloseUpdateListeners.remove(listener);
    }

    private void broadcastChannelOpenUpdate(LightningNodeUri lightningNodeUri, boolean success) {
        for (ChannelOpenUpdateListener listener : mChannelOpenUpdateListeners) {
            listener.onChannelOpenUpdate(lightningNodeUri, success);
        }
    }

    public void registerChannelOpenUpdateListener(ChannelOpenUpdateListener listener) {
        mChannelOpenUpdateListeners.add(listener);
    }

    public void unregisterChannelOpenUpdateListener(ChannelOpenUpdateListener listener) {
        mChannelOpenUpdateListeners.remove(listener);
    }

    public interface WalletLoadedListener {

        int ERROR_LOCKED = 0;
        int ERROR_INTERRUPTED = 1;
        int ERROR_TIMEOUT = 2;
        int ERROR_UNAVAILABLE = 3;
        int ERROR_AUTHENTICATION = 4;

        void onWalletLoadedUpdated(boolean success, int error);
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
        void onTransactionEvent(TransactionDetails transactionDetails);
    }

    public interface ChannelEventSubscriptionListener {
        void onChannelEvent(ChannelEventUpdate channelEventUpdate);
    }

    public interface ChannelCloseUpdateListener {
        void onChannelCloseUpdate(String channelPoint, boolean success);
    }

    public interface ChannelBackupSubscriptionListener {
        void onChannelBackupEvent(ChanBackupSnapshot chanBackupSnapshot);
    }

    public interface ChannelsUpdatedSubscriptionListener {
        void onChannelsUpdated();
    }

    public interface ChannelOpenUpdateListener {
        void onChannelOpenUpdate(LightningNodeUri lightningNodeUri, boolean success);
    }
}




