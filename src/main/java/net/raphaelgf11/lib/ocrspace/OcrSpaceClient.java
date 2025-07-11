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

import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.OkHttpClient;

@SuppressWarnings("unused")
public class OcrSpaceClient {
    private final OkHttpClient client = new OkHttpClient();
    private final String apiKey;
    private URL endpoint;

    /**
     * Constructor for Client
     * @param apiKey your api key
     */
    public OcrSpaceClient(String apiKey){
        this.apiKey = apiKey;
        try {
            endpoint = new URL("https://api.ocr.space/parse/image");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set HTTP POST endpoint
     * @param endpoint the http(s) POST endpoint
     * @return this
     */
    public OcrSpaceClient setEndpoint(String endpoint) throws MalformedURLException {
        this.endpoint = new URL(endpoint);
        return this;
    }

    /**
     * Set HTTP POST endpoint
     * @param endpoint the http(s) POST endpoint
     * @return this
     */
    public OcrSpaceClient setEndpoint(URL endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public OcrSpaceRequestBuilder getRequestBuilder(){
        return new OcrSpaceRequestBuilder(apiKey,endpoint,client);
    }
}
