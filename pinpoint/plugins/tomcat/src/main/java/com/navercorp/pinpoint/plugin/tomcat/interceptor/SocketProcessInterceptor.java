package com.navercorp.pinpoint.plugin.tomcat.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.tomcat.TomcatAsyncListener;
import com.navercorp.pinpoint.plugin.tomcat.TomcatConstants;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;

/**
 * @author jaehong.kim
 */
public class SocketProcessInterceptor implements AroundInterceptor {
    private PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private boolean isDebug = logger.isDebugEnabled();
    
    private TraceContext traceContext;
    private MethodDescriptor descriptor;
    
    public SocketProcessInterceptor(TraceContext context, MethodDescriptor descriptor) {
        this.traceContext = context;
        this.descriptor = descriptor;
    }
    
    @Override
    public void before(Object target, Object[] args) {
        // logger.info("SocketProcessInterceptor before time: " + System.currentTimeMillis()+"");
    }
    
    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    
    }
}