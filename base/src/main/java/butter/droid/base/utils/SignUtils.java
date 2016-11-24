package butter.droid.base.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;

import java.security.MessageDigest;

import timber.log.Timber;

public class SignUtils {

    private static final String SIGNATURE = "00phZ568ikxfwglO+VVC1qLQCq3DjA7/K970qP00i0Q=";
    private static final String SIGNATURE_DEV = "RjdMyRUrPzkCPaFHCZ9c4Ip40JHCkw3LpMG3p97+zcA=";

    private static final int VALID = 0;

    private static final int INVALID = 1;

    public static int checkAppSignature(Context context) {
        try {

            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);

            for (Signature signature : packageInfo.signatures) {
                byte[] signatureBytes = signature.toByteArray();

                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(signatureBytes);
                String currentSignature = Base64.encodeToString(md.digest(), Base64.NO_WRAP);

                Timber.d("Detected signature: %s", currentSignature);

                //compare signatures
                if (SIGNATURE.equals(currentSignature) || SIGNATURE_DEV.equals(currentSignature)){
                    return VALID;
                }
            }
        } catch (Exception e) {
            //assumes an issue in checking signature., but we let the caller decide on what to do.
        }

        return INVALID;

    }

}
