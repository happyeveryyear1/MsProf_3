package com.navercorp.pinpoint.plugin.tomcat.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.tomcat.TomcatPlugin;

/**
 * @author jaehong.kim
 */

public class NioSocketProcessorInterceptor implements AroundInterceptor {
    // 为不修改原有trace初始化点，使用ThreadLocal记录
    public static ThreadLocal<String> localSocketProcessTime = new ThreadLocal<>();
    
    private PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private boolean isDebug = logger.isDebugEnabled();
    
    private TraceContext traceContext;
    private MethodDescriptor descriptor;
    
    public NioSocketProcessorInterceptor(TraceContext context, MethodDescriptor descriptor) {
        this.traceContext = context;
        this.descriptor = descriptor;
    }
    
    @Override
    public void before(Object target, Object[] args) {
        
        // logger.info("NioSocketProcessorInterceptor before time: " + this.socketProcessTime);
        String socketProcessorID = target.toString();
        if(TomcatPlugin.taskQueueRecord.containsKey(socketProcessorID)){
            logger.info("记录中查询到对象");
            logger.info("对象: {}, 入队时间: {}", target.toString(), TomcatPlugin.taskQueueRecord.get(socketProcessorID));
            localSocketProcessTime.set(TomcatPlugin.taskQueueRecord.get(socketProcessorID));
            TomcatPlugin.taskQueueRecord.remove(socketProcessorID);
        }else{
            logger.info("记录中找不到对应对象");
        }
    }
    
    
    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    
    }
}