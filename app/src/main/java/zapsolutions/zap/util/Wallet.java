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
import com.github.lightningnetwork.lnd.lnrpc.ClosedChannelsRequest;
import com.github.lightningnetwork.lnd.lnrpc.ClosedChannelsResponse;
import com.github.lightningnetwork.lnd.lnrpc.GetInfoRequest;
import com.github.lightningnetwork.lnd.lnrpc.GetInfoResponse;
import com.github.lightningnetwork.lnd.lnrpc.GetTransactionsRequest;
import com.github.lightningnetwork.lnd.lnrpc.Invoice;
import com.github.lightningnetwork.lnd.lnrpc.InvoiceSubscription;
import com.github.lightningnetwork.lnd.lnrpc.LightningGrpc;
import com.github.lightningnetwork.lnd.lnrpc.ListChannelsRequest;
import com.github.lightningnetwork.lnd.lnrpc.ListChannelsResponse;
import com.github.lightningnetwork.lnd.lnrpc.ListInvoiceRequest;
import com.github.lightningnetwork.lnd.lnrpc.ListInvoiceResponse;
import com.github.lightningnetwork.lnd.lnrpc.ListPaymentsRequest;
import com.github.lightningnetwork.lnd.lnrpc.ListPaymentsResponse;
import com.github.lightningnetwork.lnd.lnrpc.NodeInfo;
import com.github.lightningnetwork.lnd.lnrpc.NodeInfoRequest;
import com.github.lightningnetwork.lnd.lnrpc.PayReq;
import com.github.lightningnetwork.lnd.lnrpc.Payment;
import com.github.lightningnetwork.lnd.lnrpc.PaymentHash;
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
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import io.grpc.stub.ClientCallStreamObserver;
import zapsolutions.zap.R;

import zapsolutions.zap.connection.LndConnection;


public class Wallet {

    private static final String LOG_TAG = "Wallet Util";

    private static Wallet mInstance = null;

    public PayReq mPaymentRequest = null;
    public String mPaymentRequestString = "";
    public List<Transaction> mOnChainTransactionList;
    public List<Invoice> mInvoiceList;
    public List<Invoice> mTempInvoiceUpdateList;
    public List<Payment> mPaymentsList;
    public List<Invoice> mPayedInvoicesList = new LinkedList<>();

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
    private long mChannelBalancePending = 0;

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

    private final Set<BalanceListener> mBalanceListeners = new HashSet<>();
    private final Set<InfoListener> mInfoListeners = new HashSet<>();
    private final Set<HistoryListener> mHistoryListeners = new HashSet<>();
    private final Set<WalletLoadedListener> mWalletLoadedListeners = new HashSet<>();
    private final Set<InvoiceSubscriptionListener> mInvoiceSubscriptionListeners = new HashSet<>();
    private final Set<TransactionSubscriptionListener> mTransactionSubscriptionListeners = new HashSet<>();
    private final Set<ChannelEventSubscriptionListener> mChannelEventSubscriptionListeners = new HashSet<>();
    private final Set<ChannelBackupSubscriptionListener> mChannelBackupSubscriptionListeners = new HashSet<>();


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
        mChannelBalancePending = 0;

        mInfoFetched = false;
        mSyncedToChain = false;
        mTestnet = false;
        mLNDVersion = "not connected";
    }


    /**
     * This will be used on loading. If this request finishes without an error, our connection to LND is established.
     * All Listeners registered to WalletLoadedListener will be informed about any changes.
     */
    public void isLNDReachable() {
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
                        broadcastWalletLoadedUpdate(true, "");
                    } catch (InterruptedException e) {
                        ZapLog.debug(LOG_TAG, "Test if LND is reachable was interrupted.");
                        mConnectionCheckInProgress = false;
                        broadcastWalletLoadedUpdate(false, "interrupted");
                    } catch (ExecutionException e) {
                        mConnectionCheckInProgress = false;
                        if (e.getMessage().toLowerCase().contains("unavailable")) {
                            // This is the case if:
                            // - LND deamon is not running
                            // - An incorrect port is used
                            // - A wrong certificate is used (When the certificate creation failed due to an error)
                            broadcastWalletLoadedUpdate(false, "unavailable");
                        } else if (e.getMessage().toLowerCase().contains("deadline_exceeded")) {
                            // This is the case if:
                            // - The server is not reachable at all. (e.g. wrong IP Address or server offline)
                            ZapLog.debug(LOG_TAG, "Cannot reach remote");
                            broadcastWalletLoadedUpdate(false, "timeout");
                        } else if (e.getMessage().toLowerCase().contains("unimplemented")) {
                            // This is the case if:
                            // - The wallet is locked
                            broadcastWalletLoadedUpdate(false, "locked");
                            ZapLog.debug(LOG_TAG, "Wallet is locked!");
                        } else if (e.getMessage().toLowerCase().contains("verification failed")) {
                            // This is the case if:
                            // - The macaroon is invalid
                            broadcastWalletLoadedUpdate(false, "authentication");
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


                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // We have to call this delayed, as otherwise it will hang up lnd. (SubscribeToChannelEvents seems to cause this.)
                            // To also get channels in an active state, this delay has to be quite high (5 seconds were not enough).
                            isLNDReachable();
                        }
                    }, 1000);

                    Handler handler2 = new Handler();
                    handler2.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // The channels are already fetched before, but they are all showed and saved as offline right after unlocking.
                            // That's why we update it again 10 seconds later.
                            Wallet.getInstance().fetchOpenChannelsFromLND();
                            Wallet.getInstance().fetchPendingChannelsFromLND();
                            Wallet.getInstance().fetchClosedChannelsFromLND();
                        }
                    }, 10000);


                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {

                    ZapLog.debug(LOG_TAG, e.getMessage());

                    // Show password prompt again after error
                    broadcastWalletLoadedUpdate(false, "locked");

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
                mOnChainBalanceUnconfirmed, mChannelBalance, mChannelBalancePending);
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
                0, 0, 0);
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


    /**
     * This will fetch all open channels for the current wallet from LND.
     */
    public void fetchOpenChannelsFromLND() {
        // fetch open channels
        LightningGrpc.LightningFutureStub asyncOpenChannelsClient = LightningGrpc
                .newFutureStub(LndConnection.getInstance().getSecureChannel())
                .withCallCredentials(LndConnection.getInstance().getMacaroon());

        ListChannelsRequest asyncOpenChannelsRequest = ListChannelsRequest.newBuilder().build();
        final ListenableFuture<ListChannelsResponse> openChannelsFuture = asyncOpenChannelsClient.listChannels(asyncOpenChannelsRequest);

        openChannelsFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ListChannelsResponse openChannelsResponse = openChannelsFuture.get();

                    mOpenChannelsList = openChannelsResponse.getChannelsList();

                    // Load NodeInfos for all involved nodes. This allows us to display aliases later.
                    for (Channel c : mOpenChannelsList) {
                        fetchNodeInfoFromLND(c.getRemotePubkey());
                    }

                    // ZapLog.debug(LOG_TAG, openChannelsResponse.toString());
                } catch (InterruptedException e) {
                    ZapLog.debug(LOG_TAG, "List open channels request interrupted.");
                } catch (ExecutionException e) {
                    ZapLog.debug(LOG_TAG, "Exception in list open channels request task.");
                }
            }
        }, new ExecuteOnCaller());
    }


    /**
     * This will fetch all pending channels for the current wallet from LND.
     */
    public void fetchPendingChannelsFromLND() {
        // fetch pending channels
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


                    // ZapLog.debug(LOG_TAG, pendingChannelsResponse.toString());
                } catch (InterruptedException e) {
                    ZapLog.debug(LOG_TAG, "List pending channels request interrupted.");
                } catch (ExecutionException e) {
                    ZapLog.debug(LOG_TAG, "Exception in list pending channels request task.");
                }
            }
        }, new ExecuteOnCaller());
    }


    /**
     * This will fetch all closed channels for the current wallet from LND.
     */
    public void fetchClosedChannelsFromLND() {
        // fetch closed channels
        LightningGrpc.LightningFutureStub asyncClosedChannelsClient = LightningGrpc
                .newFutureStub(LndConnection.getInstance().getSecureChannel())
                .withCallCredentials(LndConnection.getInstance().getMacaroon());

        ClosedChannelsRequest asyncClosedChannelsRequest = ClosedChannelsRequest.newBuilder().build();
        final ListenableFuture<ClosedChannelsResponse> closedChannelsFuture = asyncClosedChannelsClient.closedChannels(asyncClosedChannelsRequest);

        closedChannelsFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ClosedChannelsResponse closedChannelsResponse = closedChannelsFuture.get();

                    mClosedChannelsList = closedChannelsResponse.getChannelsList();

                    // Load NodeInfos for all involved nodes. This allows us to display aliases later.
                    for (ChannelCloseSummary c : mClosedChannelsList) {
                        fetchNodeInfoFromLND(c.getRemotePubkey());
                    }

                    // ZapLog.debug(LOG_TAG, closedChannelsResponse.toString());
                } catch (InterruptedException e) {
                    ZapLog.debug(LOG_TAG, "List closed channels request interrupted.");
                } catch (ExecutionException e) {
                    ZapLog.debug(LOG_TAG, "Exception in list closed channels request task.");
                }
            }
        }, new ExecuteOnCaller());
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
                    // ZapLog.debug(LOG_TAG, "Exception in get node info request task.");
                    // ZapLog.debug(LOG_TAG, e.getMessage());
                }
            }
        }, new ExecuteOnCaller());
    }


    /**
     * This will fetch an Invoice according to the supplied txHash.
     * The Invoice will then be added to the mPayedInvoicesList (no duplicates) which can then
     * be used for non async tasks, such as getting the memo for lightning payments.
     *
     * @param paymentHash
     */
    public void lookupInvoiceWithLND(ByteString paymentHash) {
        // fetch invoice
        LightningGrpc.LightningFutureStub asyncLookupInvoiceClient = LightningGrpc
                .newFutureStub(LndConnection.getInstance().getSecureChannel())
                .withCallCredentials(LndConnection.getInstance().getMacaroon());

        PaymentHash asyncPaymentHashRequest = PaymentHash.newBuilder()
                .setRHash(paymentHash)
                .build();
        final ListenableFuture<Invoice> invoiceFuture = asyncLookupInvoiceClient.lookupInvoice(asyncPaymentHashRequest);

        invoiceFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    Invoice invoiceResponse = invoiceFuture.get();

                    // Add the invoice to our list, if it is not already a member of the list.
                    boolean invoiceAlreadyExists = false;
                    for (Invoice i : mPayedInvoicesList) {
                        if (i.getRHash().equals(invoiceResponse.getRHash())) {
                            invoiceAlreadyExists = true;
                            break;
                        }
                    }
                    if (!invoiceAlreadyExists) {
                        mPayedInvoicesList.add(invoiceResponse);
                    }

                    ZapLog.debug(LOG_TAG, invoiceResponse.toString());
                } catch (InterruptedException e) {
                    ZapLog.debug(LOG_TAG, "lookup invoice request interrupted.");
                } catch (ExecutionException e) {
                    ZapLog.debug(LOG_TAG, "Exception in lookup invoice request task.");
                    ZapLog.debug(LOG_TAG, e.getMessage());

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

                ZapLog.debug(LOG_TAG, "Received channel event update.");

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

        if (transaction.getAmount() == 0) {
            return true;
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
        String alias = "";
        for (NodeInfo i : Wallet.getInstance().mNodeInfos) {
            if (i.getNode().getPubKey().equals(pubKey)) {
                if (i.getNode().getAlias().startsWith(i.getNode().getPubKey().substring(0, 8))) {
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

    private void setChannelBalance(long balance, long pending) {
        mChannelBalance = balance;
        mChannelBalancePending = pending;
        broadcastBalanceUpdate();
    }


    // Event handling to notify all registered listeners when wallet initialization finished successfully.

    private void broadcastWalletLoadedUpdate(boolean success, String error) {
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

    public interface WalletLoadedListener {
        void onWalletLoadedUpdated(boolean success, String error);
    }


    // Event handling to notify all listeners registered to balance updates.

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

    public interface BalanceListener {
        void onBalanceUpdated();
    }


    // Event handling to notify all listeners registered to info updates.

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

    public interface InfoListener {
        void onInfoUpdated(boolean connected);
    }


    // Event handling to notify all listeners registered to history updates.

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

    public interface HistoryListener {
        void onHistoryUpdated();
    }


    // Event handling to notify all listeners registered to invoice updates.

    private void broadcastInvoiceAdded(Invoice invoice) {
        for (InvoiceSubscriptionListener listener : mInvoiceSubscriptionListeners) {
            listener.onNewInvoiceAdded(invoice);
        }
    }

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

    public interface InvoiceSubscriptionListener {
        void onNewInvoiceAdded(Invoice invoice);

        void onExistingInvoiceUpdated(Invoice invoice);
    }


    // Event handling to notify all listeners registered to transaction update.

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

    public interface TransactionSubscriptionListener {
        void onTransactionEvent(TransactionDetails transactionDetails);
    }


    // Event handling to notify all listeners registered to channel event updates.

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

    public interface ChannelEventSubscriptionListener {
        void onChannelEvent(ChannelEventUpdate channelEventUpdate);
    }


    // Event handling to notify all listeners registered to channel backups.

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

    public interface ChannelBackupSubscriptionListener {
        void onChannelBackupEvent(ChanBackupSnapshot chanBackupSnapshot);
    }

}




