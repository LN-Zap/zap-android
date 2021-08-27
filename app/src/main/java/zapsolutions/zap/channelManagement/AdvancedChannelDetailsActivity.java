package zapsolutions.zap.channelManagement;

import android.os.Bundle;

import com.google.protobuf.ByteString;

import zapsolutions.zap.R;
import zapsolutions.zap.baseClasses.BaseAppCompatActivity;

public class AdvancedChannelDetailsActivity extends BaseAppCompatActivity {

    static final String LOG_TAG = AdvancedChannelDetailsActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_channel_details);


        if (getIntent().getExtras() != null) {
            Bundle extras = getIntent().getExtras();
            ByteString channelString = (ByteString) extras.getSerializable(ChannelDetailBSDFragment.ARGS_CHANNEL);
            int type = extras.getInt(ChannelDetailBSDFragment.ARGS_TYPE);
/*
            try {
                switch (type) {
                    case ChannelListItem.TYPE_OPEN_CHANNEL:
                        //bindOpenChannel(channelString);
                        break;
                    case ChannelListItem.TYPE_PENDING_OPEN_CHANNEL:
                        //bindPendingOpenChannel(channelString);
                        break;
                    case ChannelListItem.TYPE_WAITING_CLOSE_CHANNEL:
                        //bindWaitingCloseChannel(channelString);
                        break;
                    case ChannelListItem.TYPE_PENDING_CLOSING_CHANNEL:
                        //bindPendingCloseChannel(channelString);
                        break;
                    case ChannelListItem.TYPE_PENDING_FORCE_CLOSING_CHANNEL:
                        //bindForceClosingChannel(channelString);
                        break;
                }
            } catch (InvalidProtocolBufferException | NullPointerException exception) {
                Log.e(LOG_TAG, "Failed to parse channel.", exception);
                //finish();
            }

 */
        }
    }
}