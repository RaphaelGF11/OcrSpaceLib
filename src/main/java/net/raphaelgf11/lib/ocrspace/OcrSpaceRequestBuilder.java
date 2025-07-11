package net.raphaelgf11.lib.ocrspace;

import java.io.IOException;
import java.net.HttpURLConnection;

public class OcrSpaceRequestBuilder {
    private String lang;
    private boolean overlayRequired = false;
    private boolean detectOrientation = false;

    /**
     * Set lang used by ocr engine
     * @param lang <a href="https://ocr.space/OCRAPI#language">the list</a>
     * @return this
     */
    public OcrSpaceRequestBuilder setLang(String lang) {
        this.lang = lang;
        return this;
    }

    public OcrSpaceClient setOverlayRequired(boolean overlayRequired) {
        this.overlayRequired = overlayRequired;
        return this;
    }

    public void setDetectOrientation(boolean detectOrientation) {
        this.detectOrientation = detectOrientation;
    }

    private HttpURLConnection connect() throws IOException {
        HttpURLConnection conn = (HttpURLConnection) endpoint.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("apikey", apiKey);
        if (lang!=null) conn.setRequestProperty("language", lang);
        if (overlayRequired) conn.setRequestProperty("isOverlayRequired", "true");
        return conn;
    }
}
