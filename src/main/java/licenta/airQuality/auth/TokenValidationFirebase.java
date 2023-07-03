package licenta.airQuality.auth;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

public class TokenValidationFirebase {
    public static FirebaseToken tokenValidation(String idToken) throws FirebaseAuthException {

        return FirebaseAuth.getInstance().verifyIdToken(idToken);
    }
}
