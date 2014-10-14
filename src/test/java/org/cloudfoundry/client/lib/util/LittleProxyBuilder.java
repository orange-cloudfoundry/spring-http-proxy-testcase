package org.cloudfoundry.client.lib.util;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServerBootstrap;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

public class LittleProxyBuilder {

	public HttpProxyServerBootstrap createProxyAlwaysReturningOk() {
		return DefaultHttpProxyServer.bootstrap().withPort(Constant.LOCALHOST_PROXY_PORT).withFiltersSource(new HttpFiltersSourceAdapter() {
			public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {

				return new HttpFiltersAdapter(originalRequest) {
					
					@Override
					public HttpResponse requestPre(HttpObject httpObject) {
						return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
					}
				};
			}
		});
	}

}
