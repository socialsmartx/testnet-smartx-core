package com.smartx.api.v1.server;

import com.smartx.api.v1.model.ApiHandlerResponse;
import com.smartx.api.v1.model.DoTransactionResponse;
import com.smartx.api.v1.model.GetTransferNonceResponse;

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
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
 * SmartX API
 *
 * <p>SmartX is an experimental high-performance blockchain platform that powers decentralized application.
 *
 */
@Path("/v1.0.0")
@Api(value = "/", description = "")
public interface WalletApi  {

    /**
     * Broadcast a raw transaction
     *
     * Broadcasts a raw transaction to the network.
     *
     */
    @POST
    @Path("/transaction/raw")
    @Consumes({ "application/x-www-form-urlencoded" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Broadcast a raw transaction", tags={ "Wallet",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "successful operation", response = DoTransactionResponse.class),
        @ApiResponse(code = 400, message = "bad request", response = ApiHandlerResponse.class) })
    public Response broadcastRawTransaction(@QueryParam("raw") @NotNull String raw);

    /**
     * get latest block height
     *
     * get latest block height
     *
     */
    @GET
    @Path("/transaction/transfernonce")
    @Consumes({ "application/x-www-form-urlencoded" })
    @Produces({ "application/json" })
    @ApiOperation(value = "get latest block height", tags={ "Wallet" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "successful operation", response = GetTransferNonceResponse.class),
        @ApiResponse(code = 400, message = "bad request", response = ApiHandlerResponse.class) })
    public Response getTransferNonce();
}

