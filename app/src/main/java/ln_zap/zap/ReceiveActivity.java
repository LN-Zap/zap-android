package ln_zap.zap;

import ln_zap.zap.BaseClasses.BaseAppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;


import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import net.glxn.qrgen.android.QRCode;

public class ReceiveActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);

        Bitmap bmpQRCode = QRCode
                .from("0387e7490905fd06985ceff055ce1b94b82ae8dd16db5d21d4da36f83a6d109bb4")
                .withSize(750,750)
                .withErrorCorrection(ErrorCorrectionLevel.L)
                .bitmap();
        ImageView ivQRCode = findViewById(R.id.imageView);
        ivQRCode.setImageBitmap(bmpQRCode);
    }
}
