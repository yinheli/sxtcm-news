package com.yinheli.sxtcm.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import com.yinheli.sxtcm.Constants;


/**
 * simple httpclient util
 * 
 * @author yinheli <yinheli.wrok@gmail.com>
 *
 */
public class HttpClientUtil {
	
	/*
	 * Chrome agent
	 */
	private static String AGENT = "SXTCM NEWS ANDROID CLIENT " + Constants.VER_ID;
	
	/**
	 * 同一域名下使用相同的 cookie
	 */
	private Map<String, CookieStore> cookies = new HashMap<String, CookieStore>();
	
	private DefaultHttpClient getClient(String host) throws Exception {
		HttpParams params = new BasicHttpParams();
		// 连接超时
		params.setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 1000 * 5);
		// 加载超时
		// params.setIntParameter(HttpConnectionParams.SO_TIMEOUT, Integer.MAX_VALUE);
		// 缓冲区大小
		params.setIntParameter(HttpConnectionParams.SOCKET_BUFFER_SIZE, 1024 * 8);
		
		//params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		params.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");
		params.setParameter(CoreProtocolPNames.HTTP_ELEMENT_CHARSET, "UTF-8");
		params.setParameter(CoreProtocolPNames.STRICT_TRANSFER_ENCODING, "UTF-8");
		
		DefaultHttpClient httpClient = new DefaultHttpClient(params);
		setClientCookie(httpClient, host);
		
		httpClient.addResponseInterceptor(new HttpResponseInterceptor() {

            public void process(
                    final HttpResponse response,
                    final HttpContext context) throws HttpException, IOException {
                HttpEntity entity = response.getEntity();
                Header ceheader = entity.getContentEncoding();
                if (ceheader != null) {
                    HeaderElement[] codecs = ceheader.getElements();
                    for (int i = 0; i < codecs.length; i++) {
                        if (codecs[i].getName().equalsIgnoreCase("gzip")) {
                            response.setEntity(
                                    new GzipDecompressingEntity(response.getEntity()));
                            return;
                        }
                    }
                }
            }

        });
		
		return httpClient;
	}
	
	private void close(DefaultHttpClient httpClient, String host) throws Exception {
		if (httpClient == null) {
			return;
		}
		saveClientCookie(httpClient, host);
		ClientConnectionManager connectionManager = httpClient.getConnectionManager();
		connectionManager.closeExpiredConnections();
		connectionManager.shutdown();
	}
	
	private void saveClientCookie(DefaultHttpClient httpClient, String host) {
		if (httpClient == null) {
			return;
		}
		CookieStore c = httpClient.getCookieStore();
		if (c != null) {
			cookies.put(host, c);
		}
	}
	
	private void setClientCookie(DefaultHttpClient httpClient, String host) {
		CookieStore cookie = cookies.get(host);
		if (cookie != null) {
			httpClient.setCookieStore(cookie);
		}
	}
	
	/**
	 * POST
	 * 
	 * @param url
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Result post(String url, Map<String, String> param) throws Exception {
		URL u = new URL(url);
		HttpPost post = null;
		DefaultHttpClient client = null;
		try {
			client = getClient(u.getHost());
			post = new HttpPost(url);
			post.setHeader("User-Agent", AGENT);
			post.setHeader("Accept-Encoding", "gzip");
			if (param != null && param.size() > 0) {
				List<NameValuePair> formparams = new ArrayList<NameValuePair>();
				Set<String> keys = param.keySet();
				for (String key : keys) {
					formparams.add(new BasicNameValuePair(key, param.get(key)));
				}
				
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
				post.setEntity(entity);
			}
			return getResult(client.execute(post));
		} finally {
			close(client, u.getHost());
		}
	}
	
	/**
	 * post
	 * 
	 * @param url
	 * @param content
	 * @return
	 * @throws Exception
	 */
	public Result post(String url, String content) throws Exception {
		URL u = new URL(url);
		HttpPost post = null;
		DefaultHttpClient client = null;
		try {
			client = getClient(u.getHost());
			post = new HttpPost(url);
			post.setHeader("User-Agent", AGENT);
			post.setHeader("Accept-Encoding", "gzip");
			post.setHeader("Accept-Language", "zh-cn,zh;q=0.5");
			
			if (content != null) {
				StringEntity entity = new StringEntity(content, "UTF-8");
				post.setEntity(entity);
			}
			return getResult(client.execute(post));
		} finally {
			close(client, u.getHost());
		}
	}
	
	/**
	 * GET
	 * 
	 * @param url
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Result get(String url, Map<String, String> param) throws Exception {
		URL u = new URL(url);
		HttpGet get = null;
		DefaultHttpClient client = null;
		try {
			client = getClient(u.getHost());
			if (param != null && param.size() > 0) {
				StringBuilder p = new StringBuilder();
				Set<String> keys = param.keySet();
				for (String key : keys) {
					if (url.indexOf("?") != -1) {
						p.append("&").append(URLEncoder.encode(key, "UTF-8"))
						.append("=")
						.append(URLEncoder.encode(param.get(key) == null ? "" : param.get(key), "UTF-8"));
					}
				}
				url += p.toString();
			}
			get = new HttpGet(url);
			get.setHeader("User-Agent", AGENT);
			get.setHeader("Accept-Encoding", "gzip");
			return getResult(client.execute(get));
		} finally {
			close(client, u.getHost());
		}
	}
	
	/**
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public Result get(String url) throws Exception {
		return get(url, null);
	}
	
	private Result getResult(HttpResponse resp) throws Exception {
		Result r = new Result();
		r.statusLine = resp.getStatusLine();
		r.head = resp.getAllHeaders();
		HttpEntity entity = resp.getEntity();
		r.contentType = entity.getContentType();
		r.encoding = entity.getContentEncoding();
		r.contentLength = entity.getContentLength();
		BufferedInputStream bis = new BufferedInputStream(entity.getContent());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024 * 8];
		int len = -1;
		while ((len = bis.read(buf)) != -1) {
			baos.write(buf, 0, len);
		}
		baos.flush();
		r.content = baos.toByteArray();
		baos.close();
		return r;
	}
	
	private static class GzipDecompressingEntity extends HttpEntityWrapper {

        public GzipDecompressingEntity(final HttpEntity entity) {
            super(entity);
        }

        @Override
        public InputStream getContent()
            throws IOException, IllegalStateException {

            // the wrapped entity's getContent() decides about repeatability
            InputStream wrappedin = wrappedEntity.getContent();

            return new GZIPInputStream(wrappedin);
        }

        @Override
        public long getContentLength() {
            // length of ungzipped content is not known
            return -1;
        }

    }
	
	/**
	 * HttpClient 响应
	 * 
	 * @author yinheli <yinheli@gmail.com>
	 *
	 */
	public final class Result {
		public StatusLine statusLine;
		public Header[] head;
		public byte[] content;
		public Header contentType;
		public Header encoding;
		public long contentLength;
	}

}
