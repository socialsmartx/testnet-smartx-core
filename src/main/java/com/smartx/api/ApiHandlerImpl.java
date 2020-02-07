/**
 Copyright (c) 2017-2018 The SmartX Developers
 <p>
 Distributed under the MIT software license, see the accompanying file
 LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.smartx.api;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.smartx.Kernel;
import com.smartx.api.http.HttpHandler;
import com.smartx.api.v1.SmartXApiImpl;
import com.smartx.api.v1.server.*;
import com.smartx.util.exception.UnreachableException;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;

/**
 * The handler that processes all api requests. It delegates the request to
 * SmartX API implementations based on version.
 */
public class ApiHandlerImpl implements ApiHandler {
    private static final Logger logger = Logger.getLogger(ApiHandlerImpl.class);
    private static final Pattern VERSIONED_PATH = Pattern.compile("/(v[.0-9]+)(/.*)");
    private final Map<ApiVersion, Map<ImmutablePair<HttpMethod, String>, Route>> routes = new HashMap<>();
    public ApiHandlerImpl(Kernel kernel) {
        Map<ImmutablePair<HttpMethod, String>, Route> routesV1 = new HashMap<>();
        SmartXApi implV1 = new SmartXApiImpl(kernel);
        load(routesV1, implV1, kernel.getConfig().apiPublicServices(), true);
        load(routesV1, implV1, kernel.getConfig().apiPrivateServices(), false);
        this.routes.put(ApiVersion.v1_0_0, routesV1);
    }
    private void load(Map<ImmutablePair<HttpMethod, String>, Route> routes, SmartXApi impl, String[] services, boolean isPublic) {
        for (String service : services) {
            switch (service) {
                case "wallet":
                    routes.putAll(loadRoutes(impl, WalletApi.class, isPublic));
                    break;
                case "node":
                    routes.putAll(loadRoutes(impl, NodeApi.class, isPublic));
                    break;
                case "account":
                    routes.putAll(loadRoutes(impl, AccountApi.class, isPublic));
                    break;
                case "blockchain":
                    routes.putAll(loadRoutes(impl, BlockchainApi.class, isPublic));
                    break;
            }
        }
    }
    @Override
    public Response service(HttpMethod method, String path, Map<String, String> params, HttpHeaders headers) {
        Route route = matchRoute(method, path);
        if (route == null) {
            return Response.status(NOT_FOUND).entity(HttpHandler.NOT_FOUND_RESPONSE).build();
        }
        // invoke the params
        try {
            return (Response) route.invoke(params);
        } catch (Exception e) {
            logger.warn("Internal error", e);
            return Response.status(INTERNAL_SERVER_ERROR).entity(HttpHandler.INTERNAL_SERVER_ERROR_RESPONSE).build();
        }
    }
    @Override
    public boolean isAuthRequired(HttpMethod method, String path) {
        Route route = matchRoute(method, path);
        return route != null && !route.isPublic;
    }
    /**
     * Matches route by [version, method, path]
     */
    private Route matchRoute(HttpMethod method, String path) {
        Route route = null;
        Matcher m = VERSIONED_PATH.matcher(path);
        if (m.matches() && routes.containsKey(ApiVersion.of(m.group(1)))) {
            route = routes.get(ApiVersion.of(m.group(1))).get(ImmutablePair.of(method, m.group(2)));
        }
        return route;
    }
    private HttpMethod readHttpMethod(Method method) {
        for (Annotation anno : method.getAnnotations()) {
            if (anno.annotationType().equals(GET.class)) {
                return HttpMethod.GET;
            } else if (anno.annotationType().equals(POST.class)) {
                return HttpMethod.POST;
            } else if (anno.annotationType().equals(PUT.class)) {
                return HttpMethod.PUT;
            } else if (anno.annotationType().equals(DELETE.class)) {
                return HttpMethod.DELETE;
            }
        }
        logger.error(String.format("Failed to read HTTP method from {%s}", method));
        return null;
    }
    private String readPath(Method method) {
        try {
            Path path = method.getAnnotation(Path.class);
            return path.value();
        } catch (NullPointerException e) {
            logger.error(String.format("Failed to read HTTP path from {%s %s}", method, e));
            return null;
        }
    }
    private Map<ImmutablePair<HttpMethod, String>, Route> loadRoutes(Object impl, Class<?> api, boolean isPublic) {
        Map<ImmutablePair<HttpMethod, String>, Route> result = new HashMap<>();
        try {
            for (Method methodInterface : api.getMethods()) {
                Method methodImpl = impl.getClass().getMethod(methodInterface.getName(), methodInterface.getParameterTypes());
                HttpMethod httpMethod = readHttpMethod(methodInterface);
                String path = readPath(methodInterface);
                if (httpMethod != null && path != null) {
                    result.put(ImmutablePair.of(httpMethod, path), new Route(impl, httpMethod, path, methodInterface, methodImpl, isPublic));
                    logger.debug(String.format("Loaded route: {%s} {%s}", httpMethod, path));
                }
            }
        } catch (SecurityException | NoSuchMethodException e) {
            throw new UnreachableException(e);
        }
        return result;
    }
    private class Route {
        final Object smartxApi;
        @SuppressWarnings("unused")
        final HttpMethod httpMethod;
        @SuppressWarnings("unused")
        final String path;
        @SuppressWarnings("unused")
        final Method methodInterface;
        final Method methodImpl;
        final List<Pair<QueryParam, Class<?>>> queryParams;
        final boolean isPublic;
        Route(Object smartxApi, HttpMethod httpMethod, String path, Method methodInterface, Method methodImpl, boolean isPublic) {
            this.smartxApi = smartxApi;
            this.httpMethod = httpMethod;
            this.path = path;
            this.methodInterface = methodInterface;
            this.methodImpl = methodImpl;
            this.queryParams = Arrays.stream(methodInterface.getParameters()).map(p -> new ImmutablePair<QueryParam, Class<?>>(p.getAnnotation(QueryParam.class), p.getType())).collect(Collectors.toList());
            this.isPublic = isPublic;
        }
        Object invoke(Map<String, String> params) throws Exception {
            Object[] args = queryParams.stream().map(p -> {
                String param = params.getOrDefault(p.getLeft().value(), null);
                if (param == null) {
                    return null;
                }
                // convert params
                if (p.getRight().equals(Boolean.class)) {
                    return Boolean.parseBoolean(param);
                }
                return param;
            }).toArray();
            return this.methodImpl.invoke(smartxApi, args);
        }
    }
}
