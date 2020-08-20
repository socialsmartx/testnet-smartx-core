package com.smartx.api.v1.client;

import com.smartx.api.v1.model.ApiHandlerResponse;
import com.smartx.api.v1.model.GetGlobalInfoResponse;
import com.smartx.api.v1.model.GetGlobalNodeDistInfoResponse;
import com.smartx.api.v1.model.GetInfoResponse;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;
import org.apache.cxf.jaxrs.ext.multipart.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;
import io.swagger.jaxrs.PATCH;

/**
 * SmartX API
 *
 * <p>SmartX is an experimental high-performance blockchain platform that powers decentralized application.
 *
 */
@Path("/")
@Api(value = "/", description = "")
public interface NodeApi  {

    /**
     * Get Global Info
     *
     * Returns Global Info.
     *
     */
    @GET
    @Path("/global-info")
    @Consumes({ "application/x-www-form-urlencoded" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Global Info", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "successful operation", response = GetGlobalInfoResponse.class),
        @ApiResponse(code = 400, message = "bad request", response = ApiHandlerResponse.class) })
    public GetGlobalInfoResponse getGlobalInfo();

    /**
     * Get Global Node Dist Info
     *
     * Returns Global Node Dist Info.
     *
     */
    @GET
    @Path("/global-node-dist-info")
    @Consumes({ "application/x-www-form-urlencoded" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Global Node Dist Info", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "successful operation", response = GetGlobalNodeDistInfoResponse.class),
        @ApiResponse(code = 400, message = "bad request", response = ApiHandlerResponse.class) })
    public GetGlobalNodeDistInfoResponse getGlobalNodeDistInfo();

    /**
     * Get node info
     *
     * Returns kernel info.
     *
     */
    @GET
    @Path("/info")
    @Consumes({ "application/x-www-form-urlencoded" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get node info", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "successful operation", response = GetInfoResponse.class),
        @ApiResponse(code = 400, message = "bad request", response = ApiHandlerResponse.class) })
    public GetInfoResponse getInfo();
}

