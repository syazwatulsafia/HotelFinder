package com.example.hotelfinder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AboutUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        ImageView btnBack = findViewById(R.id.btn_back_arrow);
        btnBack.setOnClickListener(v -> finish());

        View member1 = findViewById(R.id.member1);
        setupMemberData(member1, "Nor Syafieda Adiela binti Mohd Syiful Izham", "2023449218", "CDCS2405B", R.drawable.syafieda);

        View member2 = findViewById(R.id.member2);
        setupMemberData(member2, "Nur Rafhanah binti Mohammad Azhar", "2023435906", "CDCS2405B", R.drawable.raf);

        View member3 = findViewById(R.id.member3);
        setupMemberData(member3, "Syazwatul Safia binti Zainuri", "2023217794", "CDCS2405B", R.drawable.syaz);

        View member4 = findViewById(R.id.member4);
        setupMemberData(member4, "Zureen Salsabila binti Zaiful Amri", "2023400834", "CDCS2405B", R.drawable.zureen);

        LinearLayout btnGithub = findViewById(R.id.btn_github);
        btnGithub.setOnClickListener(v -> openLink("https://github.com/syazwatulsafia/HotelFinder"));

        TextView tvFormLink = findViewById(R.id.tvFormLink);
        tvFormLink.setOnClickListener(v -> openLink("https://forms.gle/cdKNAAb38U8bqHs69"));
    }

    private void setupMemberData(View memberView, String name, String id, String className, int imageRes) {
        TextView tvName = memberView.findViewById(R.id.tvMemberName);
        TextView tvID = memberView.findViewById(R.id.tvMemberID);
        TextView tvClass = memberView.findViewById(R.id.tvMemberClass);
        ImageView ivPhoto = memberView.findViewById(R.id.ivMemberPhoto);

        tvName.setText(name);
        tvID.setText(id);
        tvClass.setText(className);
        ivPhoto.setImageResource(imageRes);
    }

    private void openLink(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}