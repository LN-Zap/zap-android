package zapsolutions.zap.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * Adds a CompositeDisposable to the lifecycle of the fragment.
 * Will be created in onCreate() and disposed in onDestroy().
 */
public class RxBSDFragment extends BottomSheetDialogFragment {

    private CompositeDisposable compositeDisposable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        compositeDisposable = new CompositeDisposable();
    }

    @NonNull
    public CompositeDisposable getCompositeDisposable() {
        return compositeDisposable;
    }

    @Override
    public void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }
}
