package zapsolutions.zap.channelManagement;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import zapsolutions.zap.R;
import zapsolutions.zap.lightning.LightningNodeUri;
import zapsolutions.zap.util.DownloadImageTask;
import zapsolutions.zap.util.OnSingleClickListener;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LightningNodeRecyclerAdapter extends RecyclerView.Adapter<LightningNodeRecyclerAdapter.ViewHolder> {

    private List<LightningNodeUri> mLightningNodesList;
    private ExecutorService mExecutors = Executors.newFixedThreadPool(5);
    private LightningNodeSelectedListener mLightningNodeSelectedListener;

    public LightningNodeRecyclerAdapter(List<LightningNodeUri> list, LightningNodeSelectedListener lightningNodeSelectedListener) {
        mLightningNodesList = list;
        mLightningNodeSelectedListener = lightningNodeSelectedListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.view_peers, parent, false));
    }

    public void updateData(List<LightningNodeUri> nodesList) {
        this.mLightningNodesList = nodesList;

        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LightningNodeUri lightningNode = mLightningNodesList.get(position);
        holder.nickname.setText(lightningNode.getNickname());

        if (lightningNode.getImage() != null && !lightningNode.getImage().isEmpty()) {
            new DownloadImageTask(new WeakReference<>(holder.image)).executeOnExecutor(mExecutors, lightningNode.getImage());
        }

        holder.itemView.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (mLightningNodeSelectedListener != null) {
                    mLightningNodeSelectedListener.onNodeSelected(lightningNode);
                }
            }
        });
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        mExecutors.shutdownNow();
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return mLightningNodesList.size();
    }

    interface LightningNodeSelectedListener {
        void onNodeSelected(LightningNodeUri lightningNode);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView nickname;

        ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.peersLogoImageView);
            nickname = itemView.findViewById(R.id.peersNameTextView);
        }
    }
}
