package com.smartx.api.v1.client;

import com.smartx.api.v1.model.ApiHandlerResponse;
import com.smartx.api.v1.model.GetBlockResponse;
import com.smartx.api.v1.model.GetLatestBlockHeightResponse;
import com.smartx.api.v1.model.GetLatestMcBlockResponse;
import com.smartx.api.v1.model.GetMineTaskResponse;
import com.smartx.api.v1.model.RegisterERCResponse;
import com.smartx.api.v1.model.SaveAddressResponse;

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
public interface BlockchainApi  {

    /**
     * Get block by hash
     *
     * Returns a block by block hash.
     *
     */
    @GET
    @Path("/block-by-hash")
    @Consumes({ "application/x-www-form-urlencoded" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get block by hash", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "successful operation", response = GetBlockResponse.class),
        @ApiResponse(code = 400, message = "bad request", response = ApiHandlerResponse.class) })
    public GetBlockResponse getBlockByHash(@QueryParam("hash")String hash);

    /**
     * get latest block height
     *
     * get latest block height
     *
     */
    @GET
    @Path("/latest-block-height")
    @Consumes({ "application/x-www-form-urlencoded" })
    @Produces({ "application/json" })
    @ApiOperation(value = "get latest block height", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "successful operation", response = GetLatestBlockHeightResponse.class),
        @ApiResponse(code = 400, message = "bad request", response = ApiHandlerResponse.class) })
    public GetLatestBlockHeightResponse getLatestBlockHeight();

    /**
     * get latest block info list
     *
     * get latest block info list
     *
     */
    @GET
    @Path("/latest-block")
    @Consumes({ "application/x-www-form-urlencoded" })
    @Produces({ "application/json" })
    @ApiOperation(value = "get latest block info list", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "successful operation", response = GetLatestMcBlockResponse.class),
        @ApiResponse(code = 400, message = "bad request", response = ApiHandlerResponse.class) })
    public GetLatestMcBlockResponse getLatestBlockInfo();

    /**
     * get latest mc block info list
     *
     * get latest mc block info list
     *
     */
    @GET
    @Path("/latest-mc-block")
    @Consumes({ "application/x-www-form-urlencoded" })
    @Produces({ "application/json" })
    @ApiOperation(value = "get latest mc block info list", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "successful operation", response = GetLatestMcBlockResponse.class),
        @ApiResponse(code = 400, message = "bad request", response = ApiHandlerResponse.class) })
    public GetLatestMcBlockResponse getLatestMcBlockInfo();

    /**
     * get latest block info list
     *
     * get latest block info list
     *
     */
    @GET
    @Path("/getmine-task")
    @Consumes({ "application/x-www-form-urlencoded" })
    @Produces({ "application/json" })
    @ApiOperation(value = "get latest block info list", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "successful operation", response = GetMineTaskResponse.class),
        @ApiResponse(code = 400, message = "bad request", response = ApiHandlerResponse.class) })
    public GetMineTaskResponse getMineTask(@QueryParam("json")String json);

    /**
     * get latest block info list
     *
     * get latest block info list
     *
     */
    @GET
    @Path("/getnetwork-power")
    @Consumes({ "application/x-www-form-urlencoded" })
    @Produces({ "application/json" })
    @ApiOperation(value = "get latest block info list", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "successful operation", response = GetLatestMcBlockResponse.class),
        @ApiResponse(code = 400, message = "bad request", response = ApiHandlerResponse.class) })
    public GetLatestMcBlockResponse getNetworkPower();

    /**
     * get latest block info list
     *
     * get latest block info list
     *
     */
    @GET
    @Path("/registerERC")
    @Consumes({ "application/x-www-form-urlencoded" })
    @Produces({ "application/json" })
    @ApiOperation(value = "get latest block info list", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "successful operation", response = RegisterERCResponse.class),
        @ApiResponse(code = 400, message = "bad request", response = ApiHandlerResponse.class) })
    public RegisterERCResponse registerERC(@QueryParam("json")String json);

    /**
     * get latest block info list
     *
     * get latest block info list
     *
     */
    @GET
    @Path("/saveaddress")
    @Consumes({ "application/x-www-form-urlencoded" })
    @Produces({ "application/json" })
    @ApiOperation(value = "get latest block info list", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "successful operation", response = SaveAddressResponse.class),
        @ApiResponse(code = 400, message = "bad request", response = ApiHandlerResponse.class) })
    public SaveAddressResponse saveAddress(@QueryParam("json")String json);
}

