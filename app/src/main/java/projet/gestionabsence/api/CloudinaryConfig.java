package projet.gestionabsence.api;

import android.content.Context;

import com.cloudinary.Cloudinary;
import java.util.Map;
import java.util.HashMap;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

import projet.gestionabsence.R;

public class CloudinaryConfig {
    public static Cloudinary getCloudinaryInstance(Context context) {
        Map<String, String> config = new HashMap<>();
        try {
            Properties properties = new Properties();
            InputStream inputStream = context.getResources().openRawResource(R.raw.config);
            properties.load(inputStream);

            config.put("cloud_name", properties.getProperty("cloud_name"));
            config.put("api_key", properties.getProperty("api_key"));
            config.put("api_secret", properties.getProperty("api_secret"));

            return new Cloudinary(config);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

