
package de.wangchao.musicplayer.http;

import de.wangchao.musicplayer.OnlineMusicApi;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Yingyi Xu
 */
abstract public class AbstractHttpApi implements HttpApi {
    protected static final Logger LOG = Logger.getLogger(AbstractHttpApi.class.getCanonicalName());
    protected static final boolean DEBUG = OnlineMusicApi.DEBUG;

    private static final String DEFAULT_CLIENT_VERSION = "de.wangchao.musicplayer";
    private static final String CLIENT_VERSION_HEADER = "User-Agent";
    private static final int TIMEOUT = 60;

    private final DefaultHttpClient mHttpClient;
    private final String mClientVersion;

    public AbstractHttpApi(DefaultHttpClient httpClient, String clientVersion) {

        mHttpClient = httpClient;
        if (clientVersion != null) {
            mClientVersion = clientVersion;
        } else {
            mClientVersion = DEFAULT_CLIENT_VERSION;
        }
    }

    @Override
    public String doHttpRequest(HttpRequestBase httpRequest) throws IOException{

        if (DEBUG) {
            LOG.log(Level.FINE, "doHttpRequest: " + httpRequest.getURI());
        }

        HttpResponse response = executeHttpRequest(httpRequest);
        if (DEBUG) {
            LOG.log(Level.FINE, "executed HttpRequest for: " + httpRequest.getURI().toString());
        }

        int statusCode = response.getStatusLine().getStatusCode();
        switch (statusCode) {
            case 200:
                String content = EntityUtils.toString(response.getEntity());
                if (DEBUG) {
                    LOG.log(Level.FINE, "respense: " + content);
                }
                return content;

            case 400:
                if (DEBUG) {
                    LOG.log(Level.FINE, "HTTP Code: 400");
                }
                throw new IOException("对不起，域名不能正确解析");

            case 401:
                response.getEntity().consumeContent();
                if (DEBUG) {
                    LOG.log(Level.FINE, "HTTP Code: 401");
                }
                throw new IOException("对不起，您没有权限访问");

            case 404:
                response.getEntity().consumeContent();
                if (DEBUG) {
                    LOG.log(Level.FINE, "HTTP Code: 404");
                }
                throw new IOException("对不起，页面不存在");

            case 500:
                response.getEntity().consumeContent();
                if (DEBUG) {
                    LOG.log(Level.FINE, "HTTP Code: 500");
                }
                throw new IOException("对不起，服务器错误，请稍后");

            default:
                if (DEBUG) {
                    LOG.log(Level.FINE, "Default case for status code reached: "
                            + response.getStatusLine().toString());
                }
                response.getEntity().consumeContent();
                throw new IOException("未知错误" + statusCode + "，请稍后重试.");
        }
    }

    @Override
    public String doHttpPost(String url, NameValuePair... nameValuePairs) throws Exception {

        if (DEBUG) {
            LOG.log(Level.FINE, "doHttpPost: " + url);
        }
        HttpPost httpPost = createHttpPost(url, nameValuePairs);

        HttpResponse response = executeHttpRequest(httpPost);
        if (DEBUG) {
            LOG.log(Level.FINE, "executed HttpRequest for: " + httpPost.getURI().toString());
        }

        switch (response.getStatusLine().getStatusCode()) {
            case 200:
                try {
                    return EntityUtils.toString(response.getEntity());
                } catch (ParseException e) {
                    throw new Exception(e.getMessage());
                }

            case 401:
                response.getEntity().consumeContent();
                throw new Exception(response.getStatusLine().toString());

            case 404:
                response.getEntity().consumeContent();
                throw new Exception(response.getStatusLine().toString());

            default:
                response.getEntity().consumeContent();
                throw new Exception(response.getStatusLine().toString());
        }
    }

    /**
     * execute() an httpRequest catching exceptions and returning null instead.
     * 
     * @param httpRequest
     * @return
     * @throws IOException
     */
    public HttpResponse executeHttpRequest(HttpRequestBase httpRequest) throws IOException {

        if (DEBUG) {
            LOG.log(Level.FINE, "executing HttpRequest for: " + httpRequest.getURI().toString());
        }
        try {
            mHttpClient.getConnectionManager().closeExpiredConnections();
            return mHttpClient.execute(httpRequest);
        } catch (IOException e) {
            httpRequest.abort();
            throw e;
        }
    }

    @Override
    public HttpGet createHttpGet(String url, NameValuePair... nameValuePairs) {

        if (DEBUG) {
            LOG.log(Level.FINE, "creating HttpGet for: " + url);
        }
        String query = URLEncodedUtils.format(stripNulls(nameValuePairs), HTTP.UTF_8);
        HttpGet httpGet = new HttpGet(url + "?" + query);
        httpGet.addHeader(CLIENT_VERSION_HEADER, mClientVersion);
        if (DEBUG) {
            LOG.log(Level.FINE, "Created: " + httpGet.getURI());
        }
        return httpGet;
    }

    @Override
    public HttpPost createHttpPost(String url, NameValuePair... nameValuePairs) {

        if (DEBUG) {
            LOG.log(Level.FINE, "creating HttpPost for: " + url);
        }
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader(CLIENT_VERSION_HEADER, mClientVersion);
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(stripNulls(nameValuePairs), HTTP.UTF_8));
        } catch (UnsupportedEncodingException e1) {
            throw new IllegalArgumentException("Unable to encode http parameters.");
        }
        if (DEBUG) {
            LOG.log(Level.FINE, "Created: " + httpPost);
        }
        return httpPost;
    }

    @Override
    public HttpURLConnection createHttpURLConnectionPost(URL url, String boundary, long dataLength)
            throws IOException {

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(true);
        conn.setDefaultUseCaches(true);
        conn.setConnectTimeout(TIMEOUT * 1000);
        conn.setRequestMethod("POST");

        conn.setRequestProperty(CLIENT_VERSION_HEADER, mClientVersion);
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
        conn.setRequestProperty("Content-Length", String.valueOf(dataLength));

        StringBuilder cookieBuilder = new StringBuilder();
        List<Cookie> cookiesList = mHttpClient.getCookieStore().getCookies();
        for (Cookie cookie : cookiesList) {
            cookieBuilder.append("version = " + cookie.getVersion() + "; ");
            cookieBuilder.append("name = " + cookie.getName() + "; ");
            cookieBuilder.append("value = " + cookie.getValue() + "; ");
            cookieBuilder.append("domain = " + cookie.getDomain() + "; ");
            cookieBuilder.append("path = " + cookie.getPath() + "; ");
            cookieBuilder.append("expiry = " + cookie.getExpiryDate());
        }

        conn.setRequestProperty("Cookie", cookieBuilder.toString());

        return conn;
    }

    private List<NameValuePair> stripNulls(NameValuePair... nameValuePairs) {

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        for (int i = 0; i < nameValuePairs.length; i++) {
            NameValuePair param = nameValuePairs[i];
            if (param.getValue() != null) {
                if (DEBUG) {
                    LOG.log(Level.FINE, "Param: " + param);
                }
                params.add(param);
            }
        }
        return params;
    }

    /**
     * Create a thread-safe client. This client does not do redirecting, to
     * allow us to capture correct "error" codes.
     * 
     * @return HttpClient
     */
    public static final DefaultHttpClient createHttpClient() {

        // Sets up the http part of the service.
        final SchemeRegistry supportedSchemes = new SchemeRegistry();

        // Register the "http" protocol scheme, it is required
        // by the default operator to look up socket factories.
        final SocketFactory sf = PlainSocketFactory.getSocketFactory();
        supportedSchemes.register(new Scheme("http", sf, 80));
        supportedSchemes.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

        // Set some client http client parameter defaults.
        final HttpParams httpParams = createHttpParams();
        HttpClientParams.setRedirecting(httpParams, false);

        final ClientConnectionManager ccm = new ThreadSafeClientConnManager(httpParams,
                supportedSchemes);
        return new DefaultHttpClient(ccm, httpParams);
    }

    /**
     * Create the default HTTP protocol parameters.
     */
    private static final HttpParams createHttpParams() {

        final HttpParams params = new BasicHttpParams();

        // Turn off stale checking. Our connections break all the time anyway,
        // and it's not worth it to pay the penalty of checking every time.
        HttpConnectionParams.setStaleCheckingEnabled(params, false);

        HttpConnectionParams.setConnectionTimeout(params, TIMEOUT * 1000);
        HttpConnectionParams.setSoTimeout(params, TIMEOUT * 1000);
        HttpConnectionParams.setSocketBufferSize(params, 8192);

        return params;
    }

}
