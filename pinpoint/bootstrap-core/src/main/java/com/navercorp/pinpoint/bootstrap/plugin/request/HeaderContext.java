package com.navercorp.pinpoint.bootstrap.plugin.request;

public class HeaderContext {
    static class HttpInfo{
        String taskId;
        String testId;
        String myToken;
        String toolType;
        HttpInfo(String taskId, String testId, String myToken, String toolType) {
            this.taskId = taskId;
            this.testId = testId;
            this.myToken = myToken;
            this.toolType = toolType;
        }
    }
    
    private static ThreadLocal<HttpInfo> threadLocal = new ThreadLocal<>();
    
    public static void set(String taskId, String testId, String myToken, String toolType){
        HttpInfo httpInfo = new HttpInfo(taskId, testId, myToken, toolType);
        threadLocal.set(httpInfo);
    }
    
    public static String getTaskId(){
        HttpInfo httpInfo = threadLocal.get();
        if(httpInfo != null){
            return httpInfo.taskId;
        }
        return null;
    }
    
    public static String getTestId(){
        HttpInfo httpInfo = threadLocal.get();
        if(httpInfo != null){
            return httpInfo.testId;
        }
        return null;
    }
    
    public static String getMyToken(){
        HttpInfo httpInfo = threadLocal.get();
        if(httpInfo != null){
            return httpInfo.myToken;
        }
        return null;
    }
    
    public static String getToolType(){
        HttpInfo httpInfo = threadLocal.get();
        if(httpInfo != null){
            return httpInfo.toolType;
        }
        return null;
    }
    
    public static void clear(){
        threadLocal.remove();
    }
    
}
