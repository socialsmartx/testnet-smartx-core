package com.smartx.api.v1.client;

import com.smartx.api.v1.model.ApiHandlerResponse;
import com.smartx.api.v1.model.GetAccountResponse;
import com.smartx.api.v1.model.GetAccountTransactionsResponse;
import com.smartx.api.v1.model.GetBalanceResponse;

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
public interface AccountApi  {

    /**
     * Get account info
     *
     * Returns the basic information about an account.
     *
     */
    @GET
    @Path("/account")
    @Consumes({ "application/x-www-form-urlencoded" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get account info", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "successful operation", response = GetAccountResponse.class),
        @ApiResponse(code = 400, message = "bad request", response = ApiHandlerResponse.class) })
    public GetAccountResponse getAccount(@QueryParam("address")String address);

    /**
     * Get account transactions by height.
     *
     * Returns transactions by height.
     *
     */
    @GET
    @Path("/account/transactions")
    @Consumes({ "application/x-www-form-urlencoded" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get account transactions by height.", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "successful operation", response = GetAccountTransactionsResponse.class),
        @ApiResponse(code = 400, message = "bad request", response = ApiHandlerResponse.class) })
    public GetAccountTransactionsResponse getAccountTransactions(@QueryParam("address")String address, @QueryParam("height")String height);

    /**
     * get address balance
     *
     * return balance of address
     *
     */
    @GET
    @Path("/balance")
    @Consumes({ "application/x-www-form-urlencoded" })
    @Produces({ "application/json" })
    @ApiOperation(value = "get address balance", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "successful operation", response = GetBalanceResponse.class),
        @ApiResponse(code = 400, message = "bad request", response = ApiHandlerResponse.class) })
    public GetBalanceResponse getBalance(@QueryParam("address")String address);
}

