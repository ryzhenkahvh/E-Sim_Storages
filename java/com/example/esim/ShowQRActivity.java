package com.example.esim;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class ShowQRActivity extends AppCompatActivity {

    private ImageView ivQrCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_qr);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ivQrCode = findViewById(R.id.iv_qr_code);

        int profileId = getIntent().getIntExtra("profile_id", -1);
        if (profileId != -1) {
            loadProfileAndGenerateQR(profileId);
        }
    }

    private void loadProfileAndGenerateQR(int profileId) {
        new AsyncTask<Void, Void, EsimProfile>() {
            @Override
            protected EsimProfile doInBackground(Void... voids) {
                AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                return db.esimProfileDao().getProfileById(profileId);
            }

            @Override
            protected void onPostExecute(EsimProfile profile) {
                if (profile != null) {
                    String qrData = "LPA:1$" + profile.activationCode + "$" + profile.matchingId;
                    Bitmap qrBitmap = generateQRCode(qrData);
                    if (qrBitmap != null) {
                        ivQrCode.setImageBitmap(qrBitmap);
                    }
                }
            }
        }.execute();
    }

    private Bitmap generateQRCode(String data) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            return bmp;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
}