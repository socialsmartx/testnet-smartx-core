package com.smartx.config;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import com.smartx.util.BuildInfo;

/**
 Created by Anton Nashatyrev on 13.05.2016.
 */
class Initializer implements BeanPostProcessor {
    private static final Logger logger = Logger.getLogger("general");
    // Util to ensure database directory is compatible with code
    private DatabaseVersionHandler databaseVersionHandler = new DatabaseVersionHandler();
    /**
     Method to be called right after the config is instantiated.
     Effectively is called before any other bean is initialized
     */
    private void initConfig(SystemProperties2 config) {
        BuildInfo.printInfo();
        config.nodeId();
    }
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof SystemProperties) {
            initConfig((SystemProperties2) bean);
        }
        return bean;
    }
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
    /**
     We need to persist the DB version, so after core upgrade we can either reset older incompatible db
     or make a warning and let the user reset DB manually.
     Database version is stored in ${database}/version.properties
     Logic will assume that database has version 1 if file with version is absent.
     */
    public static class DatabaseVersionHandler {
    }
}
