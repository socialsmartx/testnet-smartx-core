/**
 Copyright (c) 2017-2018 The SmartX Developers
 <p>
 Distributed under the MIT software license, see the accompanying file
 LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.smartx.api.v1;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.SignatureException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.smartx.Kernel;
import com.smartx.api.v1.model.*;
import com.smartx.api.v1.server.SmartXApi;
import com.smartx.block.Block;
import com.smartx.core.Amount;
import com.smartx.core.blockchain.BlockDAG;
import com.smartx.core.blockchain.SATObjFactory;
import com.smartx.core.blockchain.TraverBlock;
import com.smartx.core.consensus.GeneralMine;
import com.smartx.core.consensus.SatException;
import com.smartx.core.state.Account;
import com.smartx.crypto.CryptoException;
import com.smartx.crypto.Hash;
import com.smartx.crypto.Hex;
import com.smartx.crypto.Key;
import com.smartx.message.Message;
import com.smartx.mine.PoolThread;
import com.smartx.util.Tools;
import com.smartx.util.TxBlock;

public final class SmartXApiImpl implements SmartXApi {
    private static final Logger logger = Logger.getLogger("SmartXApiImpl");
    private static final Charset CHARSET = UTF_8;
    private final Kernel kernel;
    public SmartXApiImpl(Kernel kernel) {
        this.kernel = kernel;
    }
    /**
     * Constructs a success response.
     *
     * @param resp
     * @return
     */
    private Response success(ApiHandlerResponse resp) {
        resp.setSuccess(true);
        resp.setMessage("successful operation");
        return Response.ok().entity(resp).build();
    }
    /**
     * Constructs a failure response out of bad request.
     *
     * @param message
     * @return
     */
    private Response badRequest(String message) {
        ApiHandlerResponse resp = new ApiHandlerResponse();
        resp.setSuccess(false);
        resp.setMessage(message);
        logger.error("Bad request: {}" + message);
        return Response.status(BAD_REQUEST).entity(resp).build();
    }
    private static final String IP_ADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
    private String parseIp(String ip, boolean required) {
        if (ip == null) {
            if (required) {
                throw new IllegalArgumentException("Parameter `ip` is required");
            } else {
                return null;
            }
        } else {
            if (ip.matches(IP_ADDRESS_PATTERN)) {
                return ip;
            } else {
                throw new IllegalArgumentException("Parameter `ip` is invalid");
            }
        }
    }
    private byte[] parseAddress(String address, boolean required) {
        return parseAddress(address, required, "address");
    }
    private byte[] parseAddress(String address, boolean required, String name) {
        if (address == null) {
            if (required) {
                throw new IllegalArgumentException("Parameter `" + name + "` is required");
            } else {
                return null;
            }
        }
        try {
            byte[] bytes = Hex.decode0x(address);
            if (bytes.length != Key.ADDRESS_LEN) {
                throw new IllegalArgumentException("Parameter `" + name + "` length is invalid");
            }
            return bytes;
        } catch (CryptoException e) {
            throw new IllegalArgumentException("Parameter `" + name + "` is not a valid hexadecimal string");
        }
    }
    private byte[] parseHash(String hash, boolean required) {
        if (hash == null) {
            if (required) {
                throw new IllegalArgumentException("Parameter `hash` is required");
            } else {
                return null;
            }
        }
        try {
            byte[] bytes = Hex.decode0x(hash);
            if (bytes.length != Hash.HASH_LEN) {
                throw new IllegalArgumentException("Parameter `hash` length is invalid");
            }
            return bytes;
        } catch (CryptoException e) {
            throw new IllegalArgumentException("Parameter `hash` is not a valid hexadecimal string");
        }
    }
    private Integer parseInt(String num, boolean required, String name) {
        if (num == null) {
            if (required) {
                throw new IllegalArgumentException("Parameter `" + name + "` is required");
            } else {
                return null;
            }
        } else {
            try {
                return Integer.parseInt(num);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Parameter `" + name + "` is not a valid hexadecimal string");
            }
        }
    }
    private byte[] parseHex(String data, boolean required, String name) {
        if (data == null) {
            if (required) {
                throw new IllegalArgumentException("Parameter `" + name + "` is required");
            } else {
                return null;
            }
        }
        try {
            return Hex.decode0x(data);
        } catch (CryptoException e) {
            throw new IllegalArgumentException("Parameter `" + name + "` is not a valid hexadecimal string");
        }
    }
    @Override
    public Response getGlobalInfo() {
        try {
            String lpower = GeneralMine.minePowerOur.GetPower();
            String gpower = GeneralMine.minePowerTotal.GetPower();
            GetGlobalInfoResponse response = new GetGlobalInfoResponse();
            response.globalHashRate(gpower).globalHashRatePer("13.8%").globalBlockTimeCost("0.05").globalBlockTimeCostPer("13.8%").currentBlockHeight("23455").currentBlockHeightPer("13.8%").globalDifficulty("2213545435").globalDifficultyPer("13.8%").globalTransactionCount("234234324").globalTransactionCountPer("13.8%");
            return success(response);
        } catch (IllegalArgumentException ex) {
            return badRequest(ex.getMessage());
        }
    }
    @Override
    public Response getGlobalNodeDistInfo() {
        try {
            GetGlobalNodeDistInfoResponse response = new GetGlobalNodeDistInfoResponse();
            List<NodeDistType> nodeDistTypeList = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                NodeDistType nodeDistType = new NodeDistType();
                nodeDistType.setCountry("American");
                nodeDistType.setNodeCount("1000");
                nodeDistTypeList.add(nodeDistType);
            }
            response.setNodeDistInfoCount("3");
            response.setNodeDistInfoList(nodeDistTypeList);
            return success(response);
        } catch (IllegalArgumentException ex) {
            return badRequest(ex.getMessage());
        }
    }
    @Override
    public Response getInfo() {
        try {
            GetInfoResponse resp = new GetInfoResponse();
            resp.setResult(TypeFactory.infoType(kernel));
            return success(resp);
        } catch (Exception ex) {
            ex.printStackTrace();
            return badRequest(ex.getMessage());
        }
    }
    @Override
    public Response getAccount(@NotNull @Pattern(regexp = "^(0x)?[0-9a-fA-F]{40}$") String address) {
        try {
            byte[] addressBytes = parseAddress(address, true);
            BlockDAG blockDAG = SATObjFactory.GetBlockDAG();
            long latestBlockNum = blockDAG.GetLatestHeight();
            int txCount = blockDAG.GetBlocks(latestBlockNum, address).size();
            BigInteger available = blockDAG.GetBalance(address);
            BigInteger locked = BigInteger.valueOf(0);
            int internalTxCount = 0;
            int pendingTxCount = 0;
            Account account = new Account(addressBytes, Amount.of(0), Amount.of(0), 0);
            GetAccountResponse resp = new GetAccountResponse();
            resp.setResult(TypeFactory.accountType(account, available, locked, txCount, internalTxCount, pendingTxCount));
            return success(resp);
        } catch (IllegalArgumentException ex) {
            return badRequest(ex.getMessage());
        }
    }
    @Override
    public Response getAccountTransactions(@NotNull @Pattern(regexp = "^(0x)?[0-9a-fA-F]{40}$") String address, @NotNull @Pattern(regexp = "^\\d+$") String height) {
        try {
            byte[] addressBytes = parseAddress(address, true);
            BlockDAG blockDAG = SATObjFactory.GetBlockDAG();
            List<TxBlock> txBlockList = blockDAG.GetBlocks(address);
            List<TransactionType> txTypeList = new ArrayList<>();
            for (TxBlock block : txBlockList) {
                TransactionType txType = TypeFactory.transactionType(block.hash, block.in, block.out, block.timestamp, BigInteger.ZERO, block.amount);
                txTypeList.add(txType);
            }
            GetAccountTransactionsResponse resp = new GetAccountTransactionsResponse();
            resp.setTransactionCount(txBlockList.size());
            resp.setTransactionList(txTypeList);
            return success(resp);
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        }
    }
    @Override
    public Response getBalance(String address) {
        try {
            address = address.replace("0x", "");
            byte[] addressBytes = parseAddress(address, true);
            BlockDAG blockDAG = SATObjFactory.GetBlockDAG();
            BigInteger balance = blockDAG.GetBalance(address);
            GetBalanceResponse resp = new GetBalanceResponse();
            resp.setResult(TypeFactory.balanceType(address, balance));
            return success(resp);
        } catch (IllegalArgumentException ex) {
            return badRequest(ex.getMessage());
        }
    }
    @Override
    public Response broadcastRawTransaction(String raw) {
        try {
            logger.info("request broadcast a raw transaction " + raw);
            BlockDAG blockDAG = SATObjFactory.GetBlockDAG();
            blockDAG.Transfer(raw);
            DoTransactionResponse resp = new DoTransactionResponse();
            resp.setResult(raw);
            return success(resp);
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest(e.getMessage());
        }
    }
    @Override
    public Response getTransferNonce() {
        try {
            String nonce = Tools.getUUID();
            long timestamp = System.currentTimeMillis();
            GetTransferNonceResponse resp = new GetTransferNonceResponse();
            resp.setResult(TypeFactory.transferNonceType(nonce, timestamp));
            return success(resp);
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        }
    }
    @Override
    public Response getBlockByHash(@NotNull @Pattern(regexp = "^(0x)?[0-9a-fA-F]{64}$") String hashString) {
        try {
            byte[] hash = parseHash(hashString, true);
            BlockDAG blockDAG = SATObjFactory.GetBlockDAG();
            Block blk = blockDAG.ShowBlock(hashString);
            if (blk == null) {
                ApiHandlerResponse resp = new ApiHandlerResponse();
                return badRequest("block not exist");
            }
            GetBlockResponse resp = new GetBlockResponse();
            resp.setResult(TypeFactory.blockType(blk));
            return success(resp);
        } catch (IllegalArgumentException | SatException | SQLException | SignatureException e) {
            return badRequest(e.getMessage());
        }
    }
    @Override
    public Response getLatestBlockHeight() {
        BlockDAG blockDAG = SATObjFactory.GetBlockDAG();
        long latestBlockHeight = blockDAG.GetLatestHeight();
        logger.info("get latest height {} " + latestBlockHeight);
        GetLatestBlockHeightResponse response = new GetLatestBlockHeightResponse();
        response.setHeight(Long.toString(latestBlockHeight));
        return success(response);
    }
    @Override
    public Response getLatestBlockInfo() {
        BlockDAG blockDAG = SATObjFactory.GetBlockDAG();
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        long latestBlockHeight = blockDAG.GetLatestHeight();
        Block latestMcBlock = blockDAG.GetMCBlock(latestBlockHeight);
        if (latestMcBlock == null) {
            logger.error("get latest mc block null at height {}" + latestBlockHeight);
            return badRequest("get latest mc block null");
        }
        try {
            List<Block> blockList = tvblock.GetBlockRef(latestMcBlock);
            List<BlockType> blockTypeList = new ArrayList<>();
            for (Block block : blockList) {
                BlockType blockType = TypeFactory.blockType(block);
                blockTypeList.add(blockType);
            }
            GetLatestMcBlockResponse response = new GetLatestMcBlockResponse();
            response.setMcBlockCount(Integer.toString(blockTypeList.size()));
            response.setMcBlockList(blockTypeList);
            return success(response);
        } catch (IllegalArgumentException | SQLException | SatException | SignatureException e) {
            return badRequest(e.getMessage());
        }
    }
    @Override
    public Response getLatestMcBlockInfo() {
        BlockDAG blockDAG = SATObjFactory.GetBlockDAG();
        long latestBlockHeight = blockDAG.GetLatestHeight();
        long queryStartHeight = latestBlockHeight > 5 ? latestBlockHeight - 5 : 1;
        long queryEndHeight = queryStartHeight > 5 ? queryStartHeight - 5 : 1;
        try {
            List<BlockType> blockTypeList = new ArrayList<>();
            for (queryStartHeight = latestBlockHeight; queryStartHeight > queryEndHeight; queryStartHeight--) {
                Block block = blockDAG.GetMCBlock(queryStartHeight);
                if (block != null) {
                    BlockType blockType = TypeFactory.blockType(block);
                    blockTypeList.add(blockType);
                } else {
                    logger.error("block is null at height {} " + queryStartHeight);
                }
            }
            GetLatestMcBlockResponse response = new GetLatestMcBlockResponse();
            response.setMcBlockCount(Integer.toString(blockTypeList.size()));
            response.setMcBlockList(blockTypeList);
            return success(response);
        } catch (IllegalArgumentException | SQLException | SatException | SignatureException e) {
            return badRequest(e.getMessage());
        }
    }
    @Override
    public Response getNetworkPower() {
        BlockDAG blockDAG = SATObjFactory.GetBlockDAG();
        long latestBlockHeight = blockDAG.GetLatestHeight();
        long queryStartHeight = latestBlockHeight > 5 ? latestBlockHeight - 5 : 1;
        long queryEndHeight = queryStartHeight > 5 ? queryStartHeight - 5 : 1;
        try {
            List<BlockType> blockTypeList = new ArrayList<>();
            for (queryStartHeight = latestBlockHeight; queryStartHeight > queryEndHeight; queryStartHeight--) {
                Block block = blockDAG.GetMCBlock(queryStartHeight);
                if (block != null) {
                    BlockType blockType = TypeFactory.blockType(block);
                    blockTypeList.add(blockType);
                } else {
                    logger.error("block is null at height {} " + queryStartHeight);
                }
            }
            GetLatestMcBlockResponse response = new GetLatestMcBlockResponse();
            response.setMcBlockCount(Integer.toString(blockTypeList.size()));
            response.setMcBlockList(blockTypeList);
            return success(response);
        } catch (IllegalArgumentException | SQLException | SatException | SignatureException e) {
            return badRequest(e.getMessage());
        }
    }
    @Override
    public Response getMineTask(@QueryParam("json") @NotNull String json) {
        PoolThread poolThread = SATObjFactory.GetPoolThread();
        Message message = Message.FromJson(json);
        Message resp = null;
        try {
            resp = poolThread.OnGetMineTask(message);
            resp.args.put("ret", "0");
        } catch (Exception e) {
            e.printStackTrace();
        }
        GetMineTaskResponse response = new GetMineTaskResponse();
        response.setResult(TypeFactory.jsonType(Message.ToJson(resp)));
        return success(response);
    }
}
