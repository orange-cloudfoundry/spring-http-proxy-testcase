package org.cloudfoundry.client.lib.util;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.cloudfoundry.client.lib.HttpProxyConfiguration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.littleshoot.proxy.HttpProxyServer;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@RunWith(JUnit4.class)
public class ProxyTestWithSpring {

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

	public RestTemplate createRestTemplate(HttpProxyConfiguration httpProxyConfiguration, boolean useCustomRoutePlanner) {
		RestTemplate restTemplate = new RestTemplate();
		HttpClientBuilder httpClientBuilder = HttpClients.custom().useSystemProperties();

		if (httpProxyConfiguration != null) {
			HttpHost proxy = new HttpHost(httpProxyConfiguration.getProxyHost(), httpProxyConfiguration.getProxyPort());
			RequestConfig requestConfig = RequestConfig.custom().setProxy(proxy).build();
			httpClientBuilder.setDefaultRequestConfig(requestConfig);
			if (useCustomRoutePlanner) {
				HttpRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
				httpClientBuilder.setRoutePlanner(routePlanner);
			}
		}

		HttpClient httpClient = httpClientBuilder.build();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
		restTemplate.setRequestFactory(requestFactory);

		return restTemplate;
	}

	public static HttpClient httpClientBuilder(boolean useCustomRoutePlanner, HttpHost httpProxyConfiguration) {
		HttpClientBuilder httpClientBuilder = HttpClients.custom().useSystemProperties();

		if (httpProxyConfiguration != null) {
			RequestConfig requestConfig = RequestConfig.custom().setProxy(httpProxyConfiguration).build();
			httpClientBuilder.setDefaultRequestConfig(requestConfig);
			if (useCustomRoutePlanner) {
				HttpRoutePlanner routePlanner = new DefaultProxyRoutePlanner(httpProxyConfiguration);
				httpClientBuilder.setRoutePlanner(routePlanner);
			}
		}

		HttpClient httpClient = httpClientBuilder.build();

		return httpClient;

	}

	@Test(expected = ResourceAccessException.class)
	public void rest_template_should_fail_to_fetch_url_without_proxy() throws ClientProtocolException, IOException {

		boolean useCustomRoutePlanner = false;
		RestTemplate restTemplate = this.createRestTemplate(null, useCustomRoutePlanner);

		restTemplate.execute(Constant.URL_ONLY_REACHEABLE_THROUGH_PROXY, HttpMethod.GET, null, null);
		fail("Should have thrown UnknownHostException");
	}

	@Test
	public void BUG_HERE_rest_template_should_succeed_to_fetch_url_through_proxy() throws ClientProtocolException, IOException {
		HttpProxyConfiguration localProxy = new HttpProxyConfiguration(Constant.LOCALHOST_PROXY, Constant.LOCALHOST_PROXY_PORT);
		boolean useCustomRoutePlanner = false;

		RestTemplate restTemplate = this.createRestTemplate(localProxy, useCustomRoutePlanner);
		restTemplate.execute(Constant.URL_ONLY_REACHEABLE_THROUGH_PROXY, HttpMethod.GET, null, null);
	}

	@Test
	public void rest_template_should_succeed_to_fetch_url_through_proxy_with_custom_route_planner() throws ClientProtocolException, IOException {
		HttpProxyConfiguration localProxy = new HttpProxyConfiguration(Constant.LOCALHOST_PROXY, Constant.LOCALHOST_PROXY_PORT);
		boolean useCustomRoutePlanner = true;

		RestTemplate restTemplate = this.createRestTemplate(localProxy, useCustomRoutePlanner);
		restTemplate.execute(Constant.URL_ONLY_REACHEABLE_THROUGH_PROXY, HttpMethod.GET, null, null);
	}

	@Test(expected = UnknownHostException.class)
	public void httpComponentsClientHttpRequestFactory_should_fail_to_fetch_url_without_proxy() throws IOException, URISyntaxException {

		HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		ClientHttpRequest request = httpRequestFactory.createRequest(new URI(Constant.URL_ONLY_REACHEABLE_THROUGH_PROXY), HttpMethod.GET);

		ClientHttpResponse httpResponse = request.execute();
		assertEquals(200, httpResponse.getStatusCode());
	}

	@Test
	public void BUG_HERE_HttpComponentsClientHttpRequestFactory_should_succeed_to_fetch_url_through_proxy() throws IOException, URISyntaxException {
		HttpHost localProxy = new HttpHost(Constant.LOCALHOST_PROXY, Constant.LOCALHOST_PROXY_PORT);
		boolean noCustomRoutePlannerUsed = false;
		HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClientBuilder(noCustomRoutePlannerUsed, localProxy));

		ClientHttpRequest request = httpRequestFactory.createRequest(new URI(Constant.URL_ONLY_REACHEABLE_THROUGH_PROXY), HttpMethod.GET);

		ClientHttpResponse httpResponse = request.execute();
		assertEquals(200, httpResponse.getStatusCode());
	}

	@Test
	public void httpComponentsClientHttpRequestFactory_should_succeed_to_fetch_url_through_proxy_with_custom_route_planner() throws IOException,
			URISyntaxException {
		HttpHost localProxy = new HttpHost(Constant.LOCALHOST_PROXY, Constant.LOCALHOST_PROXY_PORT);
		boolean useCustomRoutePlanner = true;
		HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClientBuilder(useCustomRoutePlanner, localProxy));

		ClientHttpRequest request = httpRequestFactory.createRequest(new URI(Constant.URL_ONLY_REACHEABLE_THROUGH_PROXY), HttpMethod.GET);

		ClientHttpResponse httpResponse = request.execute();
		assertTrue("Should have a 200 but it is a " + httpResponse.getStatusCode(), httpResponse.getStatusCode().is2xxSuccessful());
	}
}
