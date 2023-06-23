package licenta.airQuality.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import java.io.FileInputStream;

@Service // spring boot recognise it as a service
public class FirebaseInitialize {
    @PostConstruct
    public void initialize() {
        try {
            FileInputStream serviceAccount =
                    new FileInputStream("./serviceAccountAirQuality.json");

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://air-quality-bc230-default-rtdb.firebaseio.com")
                    .build();

            FirebaseApp.initializeApp(options);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
