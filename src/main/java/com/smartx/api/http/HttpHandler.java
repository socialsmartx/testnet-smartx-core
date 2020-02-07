/**
 Copyright (c) 2017-2018 The SmartX Developers
 <p>
 Distributed under the MIT software license, see the accompanying file
 LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.smartx.api.http;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartx.Kernel;
import com.smartx.api.ApiHandler;
import com.smartx.config.SystemProperties;
import com.smartx.util.BasicAuth;
import com.smartx.util.Bytes;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedStream;
import io.netty.util.CharsetUtil;

/**
 * HTTP handler for SmartX API.
 */
public class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger logger = Logger.getLogger("HttpHandler");
    public static final String INTERNAL_SERVER_ERROR_RESPONSE = "{\"success\":false,\"message\":\"500 Internal Server Error\"}";
    public static final String NOT_FOUND_RESPONSE = "{\"success\":false,\"message\":\"404 Not Found\"}";
    public static final String BAD_REQUEST_RESPONSE = "{\"success\":false,\"message\":\"400 Bad Request\"}";
    public static final String FORBIDDEN_RESPONSE = "{\"success\":false,\"message\":\"403 Forbidden\"}";
    private static final Charset CHARSET = CharsetUtil.UTF_8;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap(HttpHandler.class.getResourceAsStream("/com/smartx/api/mime.types"));
    private static final String JSON_CONTENT_TYPE = "application/json; charset=UTF-8";
    private static final Pattern STATIC_FILE_PATTERN = Pattern.compile("^.+\\.(html|json|js|css|png)$");
    private final SystemProperties config;
    private final ApiHandler apiHandler;
    private Boolean isKeepAlive = false;
    /**
     * Construct a HTTP handler.
     *
     * @param kernel
     * @param apiHandler
     */
    public HttpHandler(Kernel kernel, ApiHandler apiHandler) {
        this.config = kernel.getConfig();
        this.apiHandler = apiHandler;
    }
    /**
     * For testing only.
     *
     * @param config
     *            semux config instance.
     * @param apiHandler
     *            a customized ApiHandler for testing purpose.
     */
    protected HttpHandler(SystemProperties config, ApiHandler apiHandler) {
        this.config = config;
        this.apiHandler = apiHandler;
    }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) {
        URI uri = URI.create(msg.uri());
        // copy collection to ensure it is writable
        Map<String, List<String>> params = new HashMap<>(new QueryStringDecoder(msg.uri(), CHARSET).parameters());
        HttpHeaders headers = msg.headers();
        ByteBuf body = Unpooled.buffer(HttpConstants.MAX_BODY_SIZE);
        // check decoding result
        if (!msg.decoderResult().isSuccess()) {
            writeJsonResponse(ctx, BAD_REQUEST, BAD_REQUEST_RESPONSE);
            return;
        }
        // check if keep-alive is supported
        isKeepAlive = HttpUtil.isKeepAlive(msg);
        // read request body
        ByteBuf content = msg.content();
        int length = content.readableBytes();
        if (length > 0) {
            body.writeBytes(content, length);
        }
        // parse parameter from request body
        if (body.readableBytes() > 0) {
            // FIXME: assuming "application/x-www-form-urlencoded"
            QueryStringDecoder decoder = new QueryStringDecoder("?" + body.toString(CHARSET));
            Map<String, List<String>> map = decoder.parameters();
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                if (params.containsKey(entry.getKey())) {
                    params.get(entry.getKey()).addAll(entry.getValue());
                } else {
                    params.put(entry.getKey(), entry.getValue());
                }
            }
        }
        // filter parameters
        Map<String, String> map = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
            List<String> v = entry.getValue();
            // duplicate names are not allowed.
            if (!v.isEmpty()) {
                map.put(entry.getKey(), v.get(0));
            }
        }
        // delegate the request
        ChannelFuture lastContentFuture;
        String path = uri.getPath();
        if ("/".equals(path)) {
            lastContentFuture = writeStaticFile(ctx, "/com/smartx/api/index.html");
        } else if (STATIC_FILE_PATTERN.matcher(path).matches()) {
            if (path.startsWith("/swagger-ui/")) {
                lastContentFuture = writeStaticFile(ctx, "/META-INF/resources/webjars/swagger-ui/3.22.2" + path.substring(11));
            } else {
                lastContentFuture = writeStaticFile(ctx, "/com/smartx/api" + path);
            }
        } else {
            // check basic access authentication
            if (apiHandler.isAuthRequired(msg.method(), path) && !checkBasicAuth(headers)) {
                FullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.UNAUTHORIZED);
                resp.headers().set(HttpHeaderNames.WWW_AUTHENTICATE, "Basic realm=\"SmartX RESTful API\"");
                resp.headers().set(HttpHeaderNames.CONTENT_LENGTH, resp.content().readableBytes());
                ctx.writeAndFlush(resp);
                return;
            }
            // do the service
            boolean prettyPrint = Boolean.parseBoolean(map.get("pretty"));
            Response response = apiHandler.service(msg.method(), path, map, headers);
            lastContentFuture = writeApiResponse(ctx, prettyPrint, response);
        }
        if (!isKeepAlive) {
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Exception in API http handler", cause);
        writeJsonResponse(ctx, INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_RESPONSE);
    }
    private boolean checkBasicAuth(HttpHeaders headers) {
        Pair<String, String> auth = BasicAuth.parseAuth(headers.get(HttpHeaderNames.AUTHORIZATION));
        return auth != null && MessageDigest.isEqual(Bytes.of(auth.getLeft()), Bytes.of(config.apiUsername())) && MessageDigest.isEqual(Bytes.of(auth.getRight()), Bytes.of(config.apiPassword()));
    }
    private ChannelFuture writeStaticFile(ChannelHandlerContext ctx, String resourceFullPath) {
        InputStream inputStream = getClass().getResourceAsStream(resourceFullPath);
        if (inputStream == null) {
            return writeJsonResponse(ctx, NOT_FOUND, NOT_FOUND_RESPONSE);
        }
        DefaultHttpResponse resp = new DefaultHttpResponse(HTTP_1_1, OK);
        resp.headers().set(CONNECTION, isKeepAlive ? KEEP_ALIVE : CLOSE);
        resp.headers().set(CONTENT_TYPE, mimeTypesMap.getContentType(resourceFullPath));
        HttpUtil.setTransferEncodingChunked(resp, true);
        ctx.write(resp);
        return ctx.writeAndFlush(new HttpChunkedInput(new ChunkedStream(inputStream)));
    }
    private ChannelFuture writeApiResponse(ChannelHandlerContext ctx, Boolean prettyPrint, Response response) {
        HttpResponseStatus status = HttpResponseStatus.valueOf(response.getStatus());
        String responseBody;
        Object entity = response.getEntity();
        if (entity instanceof String) {
            responseBody = (String) entity;
        } else {
            try {
                if (prettyPrint) {
                    responseBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(entity);
                } else {
                    responseBody = objectMapper.writeValueAsString(entity);
                }
            } catch (JsonProcessingException e) {
                status = INTERNAL_SERVER_ERROR;
                responseBody = INTERNAL_SERVER_ERROR_RESPONSE;
            }
        }
        return writeJsonResponse(ctx, status, responseBody);
    }
    private ChannelFuture writeJsonResponse(ChannelHandlerContext ctx, HttpResponseStatus status, String responseBody) {
        return writeResponse(ctx, JSON_CONTENT_TYPE, status, responseBody);
    }
    private ChannelFuture writeResponse(ChannelHandlerContext ctx, String contentType, HttpResponseStatus status, String responseBody) {
        // construct a HTTP response
        FullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1, status, Unpooled.copiedBuffer(responseBody == null ? "" : responseBody, CHARSET));
        // set response headers
        resp.headers().set(CONNECTION, isKeepAlive ? KEEP_ALIVE : CLOSE);
        resp.headers().set(CONTENT_TYPE, contentType);
        resp.headers().set(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        resp.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        HttpUtil.setTransferEncodingChunked(resp, true);
        // write response
        return ctx.writeAndFlush(resp);
    }
}
