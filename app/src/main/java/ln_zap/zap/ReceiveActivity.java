package ln_zap.zap;

import ln_zap.zap.BaseClasses.BaseAppCompatActivity;
import ln_zap.zap.Interfaces.UserGuardianInterface;
import ln_zap.zap.util.UserGuardian;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.content.ClipboardManager;


import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import net.glxn.qrgen.android.QRCode;

public class ReceiveActivity extends BaseAppCompatActivity implements UserGuardianInterface {

    private UserGuardian UG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);

        UG = new UserGuardian(this,this);

        Bitmap bmpQRCode = QRCode
                .from("0387e7490905fd06985ceff055ce1b94b82ae8dd16db5d21d4da36f83a6d109bb4")
                .withSize(750,750)
                .withErrorCorrection(ErrorCorrectionLevel.L)
                .bitmap();
        ImageView ivQRCode = findViewById(R.id.imageView);
        ivQRCode.setImageBitmap(bmpQRCode);

        ivQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UG.securityCopyToClipboard();
            }
        });

    }

    @Override
    public void guardianDialogConfirmed(String DialogName) {
        switch (DialogName) {
            case UserGuardian.COPY_TO_CLIPBOARD:
                copyAddressToClipboard();
                break;
        }
    }

    private void copyAddressToClipboard(){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Address", "Clipboard works!");
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this,"Copied to clipboard",Toast.LENGTH_SHORT).show();
    }
}
