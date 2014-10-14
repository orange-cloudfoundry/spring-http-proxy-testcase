package org.cloudfoundry.client.lib.util;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.littleshoot.proxy.HttpProxyServer;

@RunWith(JUnit4.class)
public class ProxyTestWithHttpClient {

	private HttpHost httpProxyConfiguration;

	private static HttpProxyServer server;

	@BeforeClass
	public static void launchLocalProxy() {
		server = new LittleProxyBuilder().createProxyAlwaysReturningOk().start();
	}

	@AfterClass
	public static void shutdownLocalProxy() {
		if (server != null) {
			server.stop();
		}
	}


	public ProxyTestWithHttpClient() {
	}

	public HttpClient builder() {
		HttpClientBuilder httpClientBuilder = HttpClients.custom().useSystemProperties();

		if (httpProxyConfiguration != null) {
			RequestConfig requestConfig = RequestConfig.custom().setProxy(httpProxyConfiguration).build();
			httpClientBuilder.setDefaultRequestConfig(requestConfig);
		}

		HttpClient httpClient = httpClientBuilder.build();

		return httpClient;

	}

	@Test(expected = UnknownHostException.class)
	public void should_fail_to_fetch_url_without_proxy() throws ClientProtocolException, IOException {
		ProxyTestWithHttpClient proxyTestWithHttpClient = new ProxyTestWithHttpClient();

		HttpClient httpClient = proxyTestWithHttpClient.builder();

		HttpGet httpget = new HttpGet(Constant.URL_ONLY_REACHEABLE_THROUGH_PROXY);
		httpClient.execute(httpget);
		fail("Should have thrown UnknownHostException");
	}

	@Test
	public void should_succeed_to_fetch_url_through_proxy_without_context() throws ClientProtocolException, IOException {
		ProxyTestWithHttpClient proxyTestWithHttpClient = new ProxyTestWithHttpClient();
		proxyTestWithHttpClient.httpProxyConfiguration = new HttpHost(Constant.LOCALHOST_PROXY, Constant.LOCALHOST_PROXY_PORT);

		HttpClient httpClient = proxyTestWithHttpClient.builder();

		HttpGet httpget = new HttpGet(Constant.URL_ONLY_REACHEABLE_THROUGH_PROXY);
		HttpResponse response = httpClient.execute(httpget);
		assertNotNull(response);
		assertNotNull(response.getStatusLine());
		assertEquals("Failed to contact " + Constant.URL_ONLY_REACHEABLE_THROUGH_PROXY, 200, response.getStatusLine().getStatusCode());

	}

	@Test
	public void should_succeed_to_fetch_url_through_proxy_with_context() throws ClientProtocolException, IOException {
		ProxyTestWithHttpClient proxyTestWithHttpClient = new ProxyTestWithHttpClient();
		proxyTestWithHttpClient.httpProxyConfiguration = new HttpHost(Constant.LOCALHOST_PROXY, Constant.LOCALHOST_PROXY_PORT);

		HttpClient httpClient = proxyTestWithHttpClient.builder();

		HttpGet httpget = new HttpGet(Constant.URL_ONLY_REACHEABLE_THROUGH_PROXY);
		HttpClientContext httpClientContext = HttpClientContext.create();
		HttpResponse response = httpClient.execute(httpget, httpClientContext);
		assertNotNull(response);
		assertNotNull(response.getStatusLine());
		assertEquals("Failed to contact " + Constant.URL_ONLY_REACHEABLE_THROUGH_PROXY, 200, response.getStatusLine().getStatusCode());

	}
	

}
