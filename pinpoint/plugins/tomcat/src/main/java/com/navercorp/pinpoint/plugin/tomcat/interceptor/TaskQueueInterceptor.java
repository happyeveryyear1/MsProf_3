package com.navercorp.pinpoint.plugin.tomcat.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.tomcat.TomcatPlugin;

import java.util.Arrays;

/**
 * @author jaehong.kim
 */

public class TaskQueueInterceptor implements AroundInterceptor {
    public static ThreadLocal<String> localSocketProcessTime = new ThreadLocal<>();
    public String socketProcessTime;
    
    private PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private boolean isDebug = logger.isDebugEnabled();
    
    private TraceContext traceContext;
    private MethodDescriptor descriptor;
    
    public TaskQueueInterceptor(TraceContext context, MethodDescriptor descriptor) {
        this.traceContext = context;
        this.descriptor = descriptor;
    }
    
    @Override
    public void before(Object target, Object[] args) {
        this.socketProcessTime = System.currentTimeMillis()+"";
        logger.info("TaskQueueInterceptor before time: " + this.socketProcessTime);
        String socketProcessorID = args[0].toString();
        if(TomcatPlugin.taskQueueRecord.contains(socketProcessorID)){
            logger.info("对象重复，与预期不符");
        }else{
            logger.info("记录对象: {}, 时间: {}" + socketProcessorID, this.socketProcessTime);
            TomcatPlugin.taskQueueRecord.put(socketProcessorID, this.socketProcessTime);
        }
        
    }
    
    
    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    
    }
}