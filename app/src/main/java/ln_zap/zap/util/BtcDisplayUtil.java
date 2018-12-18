package ln_zap.zap.util;

import android.content.Context;
import android.preference.PreferenceManager;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class BtcDisplayUtil {

    private static final String BTC_UNIT = "BTC";
    private static final String MBTC_UNIT = "mBTC";
    private static final String BIT_UNIT = "bit";
    private static final String SATOSHI_UNIT = "sat";


    public static String getDisplayAmountAndUnit(long value, Context context){

        String selectedBTCUnit = PreferenceManager.getDefaultSharedPreferences(context).getString("btcUnit","BTC");
        switch (selectedBTCUnit) {
            case "BTC":
                return getBtcDisplayAmount(value, context) + " " + BTC_UNIT;
            case "mBTC":
                return getMbtcDisplayAmount(value, context) + " " + MBTC_UNIT;
            case "bit":
                return getBitsDisplayAmount(value, context) + " " + BIT_UNIT;
            case "Satoshi":
                return getSatoshiDisplayAmount(value) + " " + SATOSHI_UNIT;
            default:
                return getBtcDisplayAmount(value, context) + " " + BTC_UNIT;
        }

    }

    public static String getDisplayAmount(long value, Context context){

        String selectedBTCUnit = PreferenceManager.getDefaultSharedPreferences(context).getString("btcUnit","BTC");
        switch (selectedBTCUnit) {
            case "BTC":
                return getBtcDisplayAmount(value, context);
            case "mBTC":
                return getMbtcDisplayAmount(value, context);
            case "bit":
                return getBitsDisplayAmount(value, context);
            case "Satoshi":
                return getSatoshiDisplayAmount(value);
            default:
                return getBtcDisplayAmount(value, context);
        }

    }

    public static String getDisplayUnit(Context context){

        String selectedBTCUnit = PreferenceManager.getDefaultSharedPreferences(context).getString("btcUnit","BTC");
        switch (selectedBTCUnit) {
            case "BTC":
                return BTC_UNIT;
            case "mBTC":
                return MBTC_UNIT;
            case "bit":
                return BIT_UNIT;
            case "Satoshi":
                return SATOSHI_UNIT;
            default:
                return BTC_UNIT;
        }

    }

    private static String getBtcDisplayAmount(long value, Context context) {
        Locale loc = context.getResources().getConfiguration().locale;
        NumberFormat nf = NumberFormat.getNumberInstance(loc);
        DecimalFormat df = (DecimalFormat)nf;
        df.setMaximumFractionDigits(8);
        df.setMinimumIntegerDigits(1);
        df.setMaximumIntegerDigits(16);
        return df.format(value /100000000d);
    }

    private static String getMbtcDisplayAmount(long value, Context context) {
        Locale loc = context.getResources().getConfiguration().locale;
        NumberFormat nf = NumberFormat.getNumberInstance(loc);
        DecimalFormat df = (DecimalFormat)nf;
        df.setMaximumFractionDigits(5);
        df.setMinimumIntegerDigits(1);
        df.setMaximumIntegerDigits(19);
        return df.format(value /100000d);
    }

    private static String getBitsDisplayAmount(long value, Context context) {
        Locale loc = context.getResources().getConfiguration().locale;
        NumberFormat nf = NumberFormat.getNumberInstance(loc);
        DecimalFormat df = (DecimalFormat)nf;
        df.setMaximumFractionDigits(2);
        df.setMinimumIntegerDigits(1);
        df.setMaximumIntegerDigits(22);
        String result= df.format(value /100d);

        // If we have a fraction, then always show 2 fraction digits for bits
        if (result.contains(String.valueOf(df.getDecimalFormatSymbols().getDecimalSeparator()))){
            df.setMinimumFractionDigits(2);
            return df.format(value /100d);
        }
        else {
            return result;
        }
    }

    private static String getSatoshiDisplayAmount(long value) {
        // Satoshis are the smallest unit, there are no fractions.
        // To avoid a value being recognized as a fraction, we use spaces as group separators.
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        DecimalFormat df = new DecimalFormat("#", symbols);
        df.setMinimumIntegerDigits(1);
        df.setMaximumIntegerDigits(16);
        df.setGroupingUsed(true);
        df.setGroupingSize(3);
        return df.format(value);
    }

}
