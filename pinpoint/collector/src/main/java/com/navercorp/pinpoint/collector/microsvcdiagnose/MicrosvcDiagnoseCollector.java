package com.navercorp.pinpoint.collector.microsvcdiagnose;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.collector.microsvcdiagnose.http.HttpTrace;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MicrosvcDiagnoseCollector {
    
    public static final short METHOD_ANNOTATION_CODE = 12;
    public static final Logger logger = LoggerFactory.getLogger("MsD");
    private static ObjectMapper objectMapper;
    private static RestTemplate restTemplate;
    private static ExecutorService executorService;
    
    private static String spanUrl;
    private static String spanChunkUrl;
    private static int retryCount;
    private static boolean annotationDisable;
    private static int threadCount;
    
    static {
        objectMapper = new ObjectMapper();
        restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(Charset.forName("UTF8")));
        
    //    read properity
        Properties properties = new Properties();
        InputStream resourceAsStream = MicrosvcDiagnoseCollector.class.getClassLoader().getResourceAsStream("microsvcdiagnose.properties");
        try {
            properties.load(resourceAsStream);
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                if (resourceAsStream != null)
                    resourceAsStream.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        spanUrl = readProperity(properties, "MsD.span.url");
        spanChunkUrl = readProperity(properties, "MsD.spanChunk.url");
        threadCount = 8;
        try {
            retryCount = Integer.valueOf(readProperity(properties, "MsD.retry.count"));
            threadCount = Integer.valueOf(readProperity(properties,"MsD.thread.count"));
            annotationDisable = Boolean.valueOf(readProperity(properties, "MsD.annotation.disable"));
        } catch (NumberFormatException e){
            retryCount = 2;
            threadCount = 8;
        }
        executorService = Executors.newFixedThreadPool(threadCount);
    }
    
    private static String readProperity(Properties properties, String key){
        String value = System.getProperty(key);
        if(value == null){
            value = properties.getProperty(key);
        }
        return value;
    }
    
    public static void handleSpan(SpanBo spanBo){
        MicrosvcDiagnoseCollector.processTrace(spanBo);
    }
    
    public static void handleSpan(SpanChunkBo spanChunkBo){
        MicrosvcDiagnoseCollector.processTrace(spanChunkBo);
    }
    
    public static void processTrace(SpanBo spanBo){
        executorService.execute(()->{
            for (int i = 0; i < retryCount; i++){
                try{
                    Ret ret = restTemplate.postForObject(spanUrl, objectMapper.writeValueAsString(spanBo), Ret.class);
                    if(ret.isSuccess())
                        break;
                }catch (RestClientException e){
                    System.err.printf("Fail Sending Trace(%d): %s\n", i, e.getMessage());
                }catch (JsonProcessingException e){
                    e.printStackTrace();
                    break;
                }
            }
        });
    }
    
    public static void processTrace(SpanChunkBo spanChunkBo){
        /*executorService.execute(()->{
            for (int i = 0; i < retryCount; i++){
                try{
                    Ret ret = restTemplate.postForObject(spanChunkUrl, objectMapper.writeValueAsString(spanChunkBo), Ret.class);
                    if(ret.isSuccess())
                        break;
                }catch (RestClientException e){
                    System.err.printf("Fail Sending Trace(%d): %s\n", i, e.getMessage());
                }catch (JsonProcessingException e){
                    e.printStackTrace();
                    break;
                }
            }
        });*/
    }
    
    public static void processMethodSpanEvent(HttpTrace trace, List<SpanEventBo> spanEventBoList){
    
    }
}
