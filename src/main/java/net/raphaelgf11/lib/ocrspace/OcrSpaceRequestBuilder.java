/*
    MIT License

    Copyright (c) 2025 Raphaël GAUTHIER

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/

package net.raphaelgf11.lib.ocrspace;

import okhttp3.*;

import java.io.*;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.Base64;

@SuppressWarnings("unused")
public class OcrSpaceRequestBuilder {
    private final String apiKey;
    private final URL endpoint;
    private final OkHttpClient client;

    private String lang;
    private String filetype;
    private String ocrEngine;
    private Boolean overlayRequired;
    private Boolean detectOrientation;
    private Boolean createSearchablePdf;
    private Boolean searchablePdfHideTextLayer;
    private Boolean scale;
    private Boolean table;

    OcrSpaceRequestBuilder(String apiKey, URL endpoint, OkHttpClient client) {
        this.apiKey = apiKey;
        this.endpoint = endpoint;
        this.client=client;
    }

    /**
     * Set lang used by ocr engine
     * @param lang <a href="https://ocr.space/OCRAPI#language">the list</a>
     */
    public OcrSpaceRequestBuilder setLang(String lang) {
        this.lang = lang;
        return this;
    }

    /**
     * If true, returns the coordinates of the bounding boxes for each word.
     * If false, the OCR'ed text is returned only as a text block (this makes the JSON reponse smaller).
     * @param overlayRequired isOverlayRequired
     */
    public OcrSpaceRequestBuilder setOverlayRequired(Boolean overlayRequired) {
        this.overlayRequired = overlayRequired;
        return this;
    }

    /**
     * If set to true, the api autorotates the image correctly and sets the TextOrientation parameter in the JSON response.
     * @param detectOrientation detectOrientation
     */
    public OcrSpaceRequestBuilder setDetectOrientation(Boolean detectOrientation) {
        this.detectOrientation = detectOrientation;
        return this;
    }

    /**
     * If true, API generates a searchable PDF. This parameter automatically sets isOverlayRequired = true.
     * @param createSearchablePdf isCreateSearchablePdf
     */
    public OcrSpaceRequestBuilder setCreateSearchablePdf(Boolean createSearchablePdf) {
        this.createSearchablePdf = createSearchablePdf;
        return this;
    }

    /**
     * Overwrites the automatic file type detection based on content-type.
     * @param filetype PDF/GIF/PNG/JPG/TIF/BMP
     */
    public OcrSpaceRequestBuilder setFiletype(String filetype) {
        this.filetype = filetype;
        return this;
    }

    /**
     * If true, API generates a searchable PDF. This parameter automatically sets isOverlayRequired = true.
     * @param searchablePdfHideTextLayer isCreateSearchablePdf
     */
    public OcrSpaceRequestBuilder setSearchablePdfHideTextLayer(Boolean searchablePdfHideTextLayer) {
        this.searchablePdfHideTextLayer = searchablePdfHideTextLayer;
        return this;
    }

    /**
     * If set to true, the api does some internal upscaling.
     * This can improve the OCR result significantly, especially for low-resolution PDF scans.
     * @param scale scale
     */
    public OcrSpaceRequestBuilder setScale(Boolean scale) {
        this.scale = scale;
        return this;
    }

    /**
     * If set to true, the OCR logic makes sure that the parsed text result is always returned line by line.
     * This switch is recommended for table OCR, receipt OCR, invoice processing and all other type of input documents that have a table like structure.
     * @param table isTable
     */
    public OcrSpaceRequestBuilder setTable(Boolean table) {
        this.table = table;
        return this;
    }

    /**
     * <a href="https://ocr.space/OCRAPI#ocrengine">documentation link</a>
     * @param ocrEngine OCREngine
     */
    public OcrSpaceRequestBuilder setOCREngine(String ocrEngine) {
        this.ocrEngine = ocrEngine;
        return this;
    }

    /**
     * <a href="https://ocr.space/OCRAPI#ocrengine">documentation link</a>
     * @param ocrEngine OCREngine
     */
    public OcrSpaceRequestBuilder setOCREngine(int ocrEngine) {
        this.ocrEngine = String.valueOf(ocrEngine);
        return this;
    }

    /**
     * You can use three methods to upload the input image or PDF.
     * @param target URL of remote image file (Make sure it has the right content type)
     * @return url targeted request
     */
    public UrlTargetedRequest target(URL target) {
        return new UrlTargetedRequest(this,target);
    }

    /**
     * You can use three methods to upload the input image or PDF.
     * @param target Multipart encoded image file with filename
     * @return file targeted request
     */
    public FileTargetedRequest target(File target) {
        return new FileTargetedRequest(this,target);
    }

    /**
     * You can use three methods to upload the input image or PDF.
     * @param content Image or PDF as Base64 encoded string
     * @return base64 image targeted request
     */
    public B64ImageTargetedRequest target(String content) {
        return new B64ImageTargetedRequest(this,content);
    }

    /**
     * You can use three methods to upload the input image or PDF.
     * @param bi BufferedImage
     * @return file targeted request
     */
    public B64ImageTargetedRequest target(BufferedImage bi) {
        if (bi == null) throw new IllegalArgumentException("BufferedImage cannot be null");
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(bi, "png", baos);
            baos.flush();
            String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
            return target(base64);
        } catch (IOException e) {
            throw new RuntimeException("Failed to encode BufferedImage to base64", e);
        }
    }

    public static abstract class TargetedRequest {
        private final OcrSpaceRequestBuilder osrb;
        private TargetedRequest(OcrSpaceRequestBuilder osrb){
            this.osrb=osrb;
        }

        /**
         * Add the target to the body
         * @param requestBody body
         */
        abstract void alterBody(MultipartBody.Builder requestBody);

        /**
         * Run the request
         * @return response
         * @throws IOException on request error
         */
        public final Response request() throws IOException {
            MultipartBody.Builder requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM);
            alterBody(requestBody);
            if (osrb.lang!=null) requestBody.addFormDataPart("language", osrb.lang);
            if (osrb.overlayRequired!=null) requestBody.addFormDataPart("isOverlayRequired", osrb.overlayRequired.toString());
            if (osrb.filetype!=null) requestBody.addFormDataPart("filetype", osrb.filetype);
            if (osrb.detectOrientation!=null) requestBody.addFormDataPart("detectOrientation", osrb.detectOrientation.toString());
            if (osrb.createSearchablePdf!=null) requestBody.addFormDataPart("isCreateSearchablePdf", osrb.createSearchablePdf.toString());
            if (osrb.searchablePdfHideTextLayer!=null) requestBody.addFormDataPart("isSearchablePdfHideTextLayer", osrb.searchablePdfHideTextLayer.toString());
            if (osrb.scale!=null) requestBody.addFormDataPart("scale", osrb.scale.toString());
            if (osrb.table!=null) requestBody.addFormDataPart("isTable", osrb.table.toString());
            if (osrb.ocrEngine!=null) requestBody.addFormDataPart("OCREngine", osrb.ocrEngine);

            Request request = new Request.Builder()
                    .url(osrb.endpoint)
                    .post(requestBody.build())
                    .addHeader("apikey", osrb.apiKey)
                    .build();

            return osrb.client.newCall(request).execute();
        }

        /**
         * run request() asynchronously
         * @return completable future of response
         */
        public final CompletableFuture<Response> asyncRequest(){
            CompletableFuture<Response> completable = new CompletableFuture<>();
            new Thread(()->_complete(completable)).start();
            return completable;
        }

        /**
         * run request() asynchronously
         * @return completable future of response
         */
        public final CompletableFuture<Response> asyncRequest(ThreadPoolExecutor tpe){
            CompletableFuture<Response> completable = new CompletableFuture<>();
            tpe.submit(()->_complete(completable));
            return completable;
        }

        private void _complete(CompletableFuture<Response> completable){
            try {
                completable.complete(request());
            } catch (Throwable t){
                completable.completeExceptionally(t);
            }
        }
    }

    public static final class UrlTargetedRequest extends TargetedRequest {
        private final URL target;
        private UrlTargetedRequest(OcrSpaceRequestBuilder osrb, URL target) {
            super(osrb);
            this.target = target;
        }
        @Override
        void alterBody(MultipartBody.Builder requestBody) {
            requestBody.addFormDataPart("url", target.toString());
        }
    }

    public static final class FileTargetedRequest extends TargetedRequest {
        private final File target;
        private FileTargetedRequest(OcrSpaceRequestBuilder osrb, File target) {
            super(osrb);
            this.target = target;
        }

        @Override
        void alterBody(MultipartBody.Builder requestBody) {
            if (target == null || !target.exists()) {
                throw new IllegalArgumentException("Le fichier fourni est nul ou inexistant : " + target);
            }

            requestBody.addFormDataPart(
                    "file",
                    target.getName(),
                    RequestBody.create(target, guessMediaType(target))
            );
        }

        private static MediaType guessMediaType(File file) {
            String name = file.getName().toLowerCase();
            if (name.endsWith(".png")) return MediaType.get("image/png");
            if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return MediaType.get("image/jpeg");
            if (name.endsWith(".gif")) return MediaType.get("image/gif");
            if (name.endsWith(".bmp")) return MediaType.get("image/bmp");
            if (name.endsWith(".tif") || name.endsWith(".tiff")) return MediaType.get("image/tiff");
            if (name.endsWith(".pdf")) return MediaType.get("application/pdf");
            return MediaType.get("application/octet-stream");
        }
    }

    public static final class B64ImageTargetedRequest extends TargetedRequest {
        private final String content;

        private B64ImageTargetedRequest(OcrSpaceRequestBuilder osrb, String content) {
            super(osrb);
            this.content = content;
        }

        @Override
        void alterBody(MultipartBody.Builder requestBody) {
            if (content == null || content.isEmpty())
                throw new IllegalArgumentException("Le contenu base64 est vide ou nul");
            requestBody.addFormDataPart("base64Image", content);
        }
    }
}
