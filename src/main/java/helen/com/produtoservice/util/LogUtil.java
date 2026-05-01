package helen.com.produtoservice.util;

import org.slf4j.MDC;
import java.util.UUID;

public class LogUtil {

    private static final String KEY = "correlationId";

    public static String getOrCreate(String id) {
        if(id == null || id.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return id;
    }

    public static void set(String id) {
        MDC.put(KEY, id);
    }

    public static String get() {
        return MDC.get(KEY);
    }

    public static void clear() {
        MDC.remove(KEY);
    }
}


