package com.smartx.config;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class CommonConfig {
    private static final Logger logger = Logger.getLogger("general");
    private static CommonConfig defaultInstance;
    //    public static CommonConfig getDefault()
    //    {
    //        if (defaultInstance == null && !SystemProperties.isUseOnlySpringConfig()) {
    //            defaultInstance = new CommonConfig();
    //        }
    //        return defaultInstance;
    //    }
    //    public SystemProperties systemProperties()
    //    {
    //        return SystemProperties.getSpringDefault();
    //    }
    BeanPostProcessor initializer() {
        return new Initializer();
    }
}
