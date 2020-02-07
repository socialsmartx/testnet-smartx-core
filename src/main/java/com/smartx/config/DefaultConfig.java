package com.smartx.config;

import static java.util.Arrays.asList;

import java.util.List;

// import org.ethereum.datasource.Source;
// import org.ethereum.db.BlockStore;
// import org.ethereum.db.IndexedBlockStore;
// import org.ethereum.db.PruneManager;
// import org.ethereum.db.TransactionStore;
import org.apache.log4j.Logger;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 @author Roman Mandeleil
 Created on: 27/01/2015 01:05 */
@Configuration
@Import(CommonConfig.class)
public class DefaultConfig {
    private static Logger logger = Logger.getLogger("general");
    @Autowired
    ApplicationContext appCtx;
    @Autowired
    CommonConfig commonConfig;
    @Autowired
    SystemProperties config;
    private final static List<Class<? extends Exception>> FATAL_EXCEPTIONS = asList(FatalBeanException.class);
    public DefaultConfig() {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            logger.error("Uncaught exception", e);
            FATAL_EXCEPTIONS.stream().filter(errType -> errType.isInstance(e)).findFirst().ifPresent(errType -> System.exit(1));
        });
    }
    //    @Bean
    //    public BlockStore blockStore(){
    //        commonConfig.fastSyncCleanUp();
    //        IndexedBlockStore indexedBlockStore = new IndexedBlockStore();
    //        Source<byte[], byte[]> block = commonConfig.cachedDbSource("block");
    //        Source<byte[], byte[]> index = commonConfig.cachedDbSource("index");
    //        indexedBlockStore.init(index, block);
    //
    //        return indexedBlockStore;
    //    }
    //    @Bean
    //    public TransactionStore transactionStore() {
    //        commonConfig.fastSyncCleanUp();
    //        return new TransactionStore(commonConfig.cachedDbSource("transactions"));
    //    }
    //
    //    @Bean
    //    public PruneManager pruneManager() {
    //        if (config.databasePruneDepth() >= 0) {
    //            return new PruneManager((IndexedBlockStore) blockStore(), commonConfig.stateSource().getJournalSource(),
    //                    commonConfig.stateSource().getNoJournalSource(), config.databasePruneDepth());
    //        } else {
    //            return new PruneManager(null, null, null, -1); // dummy
    //        }
    //    }
}
