package com.navercorp.pinpoint.plugin.tomcat.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

/**
 * @author jaehong.kim
 */
public class Nio2SocketProcessorInterceptor implements AroundInterceptor {
    private PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private boolean isDebug = logger.isDebugEnabled();
    
    private TraceContext traceContext;
    private MethodDescriptor descriptor;
    
    public Nio2SocketProcessorInterceptor(TraceContext context, MethodDescriptor descriptor) {
        this.traceContext = context;
        this.descriptor = descriptor;
    }
    
    @Override
    public void before(Object target, Object[] args) {
        // logger.info("Nio2SocketProcessorInterceptor before time: " + System.currentTimeMillis()+"");
    }
    
    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    
    }
}