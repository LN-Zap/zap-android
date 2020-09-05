package zapsolutions.zap.licenses;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import zapsolutions.zap.R;
import zapsolutions.zap.baseClasses.BaseAppCompatActivity;
import zapsolutions.zap.util.AppUtil;

public class LicensesActivity extends BaseAppCompatActivity {

    private static final String LOG_TAG = LicensesActivity.class.getName();

    private RecyclerView mRecyclerView;
    private LicenseItemAdapter mAdapter;
    private Set<LicenseListItem> mLicenseItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licenses);

        mRecyclerView = findViewById(R.id.licensesList);

        mLicenseItems = new HashSet<>();

        mAdapter = new LicenseItemAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        showLicensesList();
    }

    private void showLicensesList() {
        mLicenseItems.clear();

        // Add the generated licenses report
        String licenseReport = AppUtil.getInstance(this).loadJSONFromResource(R.raw.license_report);
        try {
            JSONArray licensesArray = new JSONArray(licenseReport);
            for (int i = 0; i < licensesArray.length(); i++) {
                JSONObject license = licensesArray.getJSONObject(i);
                String licenseString = license.toString();
                Dependency licenseJson = new Gson().fromJson(licenseString, Dependency.class);
                mLicenseItems.add(new LicenseListItem(licenseJson));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Fix errors in the report (by replacing those licenses completely)
        String licenseReportFixes = AppUtil.getInstance(this).loadJSONFromResource(R.raw.license_report_fixes);
        try {
            JSONArray licensesArray = new JSONArray(licenseReportFixes);
            for (int i = 0; i < licensesArray.length(); i++) {
                JSONObject license = licensesArray.getJSONObject(i);
                String licenseString = license.toString();
                Dependency licenseJson = new Gson().fromJson(licenseString, Dependency.class);
                mLicenseItems.remove(new LicenseListItem(licenseJson));
                mLicenseItems.add(new LicenseListItem(licenseJson));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Add additional licenses (licenses that are not included in the automatically generated record)
        String additionalLicenses = AppUtil.getInstance(this).loadJSONFromResource(R.raw.additional_licenses);
        try {
            JSONArray licensesArray = new JSONArray(additionalLicenses);
            for (int i = 0; i < licensesArray.length(); i++) {
                JSONObject license = licensesArray.getJSONObject(i);
                String licenseString = license.toString();
                Dependency licenseJson = new Gson().fromJson(licenseString, Dependency.class);
                mLicenseItems.add(new LicenseListItem(licenseJson));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Update the view
        mAdapter.replaceAll(new ArrayList<>(mLicenseItems));
    }

}
