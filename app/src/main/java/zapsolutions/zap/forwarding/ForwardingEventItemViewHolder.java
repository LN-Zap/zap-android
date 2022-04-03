package zapsolutions.zap.forwarding;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.Date;

import zapsolutions.zap.R;
import zapsolutions.zap.contacts.ContactsManager;
import zapsolutions.zap.util.MonetaryUtil;
import zapsolutions.zap.util.OnSingleClickListener;
import zapsolutions.zap.util.Wallet;

public class ForwardingEventItemViewHolder extends RecyclerView.ViewHolder {

    private static final String LOG_TAG = ForwardingEventItemViewHolder.class.getName();

    private TextView mTimeOfDay;
    private TextView mInChannel;
    private TextView mOutChannel;
    private TextView mEarnedFee;
    private TextView mForwardingAmount;
    private View mRootView;
    private ForwardingEventSelectListener mForwardingEventSelectListener;
    private Context mContext;


    public ForwardingEventItemViewHolder(View v) {
        super(v);

        mTimeOfDay = itemView.findViewById(R.id.timeOfDay);
        mInChannel = v.findViewById(R.id.inChannel);
        mOutChannel = v.findViewById(R.id.outChannel);
        mEarnedFee = v.findViewById(R.id.earnedFeeAmount);
        mForwardingAmount = v.findViewById(R.id.forwardingAmount);
        mRootView = v.findViewById(R.id.forwardingEventRootView);
        mContext = v.getContext();
    }

    public void bindForwardingEventListItem(ForwardingEventListItem forwardingEventListItem) {

        // Set time of day
        setTimeOfDay(forwardingEventListItem.getTimestamp());

        // Set in channel name
        long inChanID = forwardingEventListItem.getForwardingEvent().getChanIdIn();
        String inChanPubKey = Wallet.getInstance().getRemotePubKeyFromChannelId(inChanID);
        String inChanName = "";
        if (inChanPubKey == null) {
            inChanName = mContext.getResources().getString(R.string.forwarding_closed_channel);
        } else {
            if (ContactsManager.getInstance().doesContactDataExist(inChanPubKey)) {
                inChanName = ContactsManager.getInstance().getNameByContactData(inChanPubKey);
            } else {
                inChanName = Wallet.getInstance().getNodeAliasFromPubKey(inChanPubKey, mContext);
            }
        }
        mInChannel.setText(inChanName);

        // Set out channel name
        long outChanID = forwardingEventListItem.getForwardingEvent().getChanIdOut();
        String outChanPubKey = Wallet.getInstance().getRemotePubKeyFromChannelId(outChanID);
        String outChanName = "";
        if (outChanPubKey == null) {
            outChanName = mContext.getResources().getString(R.string.forwarding_closed_channel);
        } else {
            if (ContactsManager.getInstance().doesContactDataExist(outChanPubKey)) {
                outChanName = ContactsManager.getInstance().getNameByContactData(outChanPubKey);
            } else {
                outChanName = Wallet.getInstance().getNodeAliasFromPubKey(outChanPubKey, mContext);
            }
        }
        mOutChannel.setText(outChanName);


        // Set earned fee amount
        mEarnedFee.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(forwardingEventListItem.getForwardingEvent().getFee()));

        // Set forwarded amount
        mForwardingAmount.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(forwardingEventListItem.getForwardingEvent().getAmtIn()
        ));

        // Set on click listener
        setOnRootViewClickListener(forwardingEventListItem);
    }

    public void addOnForwardingEventSelectListener(ForwardingEventSelectListener forwardingEventSelectListener) {
        mForwardingEventSelectListener = forwardingEventSelectListener;
    }

    void setTimeOfDay(long creationDate) {
        DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT, mContext.getResources().getConfiguration().locale);
        String formattedTime = df.format(new Date(creationDate * 1000L));
        mTimeOfDay.setText(formattedTime);
    }

    void setOnRootViewClickListener(@NonNull ForwardingEventListItem item) {
        mRootView.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (mForwardingEventSelectListener != null) {
                    mForwardingEventSelectListener.onForwardingEventSelect(item.getForwardingEvent().toByteString());
                }
            }
        });
    }
}
