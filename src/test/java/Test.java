import net.raphaelgf11.lib.ocrspace.*;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Test implements Runnable {
    public static void main(String[] rawArgs) {
        new Test(rawArgs).run();
    }
    private final Map<String,String> args;
    private final OcrSpaceClient client;
    private Test(String[] rawArgs){
        this.args = parseArgs(rawArgs);
        String aak = args.get("APIKEY");
        String key = aak == null ? System.getenv("OCRSPACE_APIKEY") : aak;
        if (key == null) {
            System.err.println("No api key defined, using default apikey");
            key = "helloworld";
        }
        client = new OcrSpaceClient(key);
    }

    private static Map<String,String> parseArgs(String[] rawArgs){
        Map<String,String> map = new HashMap<>();
        for (String arg:rawArgs){
            if (arg.isEmpty()) continue;
            String[] a = arg.split("=",2);
            map.put(a[0],a.length>1?a[1]:"");
        }
        return map;
    }

    /** Main code */
    @Override
    public void run() {
        String sf = args.get("FILE");
        if (sf==null) {
            System.err.println("Arg FILE is required");
            return;
        }
        OcrSpaceRequestBuilder osrb = client.getRequestBuilder()
                .setOCREngine(2)
                .setLang("auto")
                .setOverlayRequired(false)
                .setTable(true)
                .setScale(true);
        try (
            Response response = osrb
                .target(new File(sf))
                .request()
        ) {
            ResponseBody rb = response.body();
            if (rb == null) System.err.println("Server returned empty body");
            else System.out.println(rb.string());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
