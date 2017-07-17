package com.msh.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Created by zhangruiqian on 2017/7/9.
 */
@Component
public class SpringContextHolder implements ApplicationContextAware {
    private static ApplicationContext context;

    public static ApplicationContext getContext() {
        return context;
    }

    public static Object getBean(String beanName) {
        return context.getBean(beanName);
    }

    public void setApplicationContext(ApplicationContext paramApplicationContext)
            throws BeansException {
        context = paramApplicationContext;
    }
}

