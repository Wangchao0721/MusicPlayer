
package de.wangchao.musicplayer.http;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public interface HttpApi {

    abstract public String doHttpRequest(HttpRequestBase httpRequest) throws Exception;

    abstract public String doHttpPost(String url, NameValuePair... nameValuePairs) throws Exception;

    abstract public HttpGet createHttpGet(String url, NameValuePair... nameValuePairs);

    abstract public HttpPost createHttpPost(String url, NameValuePair... nameValuePairs);

    abstract public HttpURLConnection createHttpURLConnectionPost(URL url, String boundary,
            long dataLength) throws Exception;
}
