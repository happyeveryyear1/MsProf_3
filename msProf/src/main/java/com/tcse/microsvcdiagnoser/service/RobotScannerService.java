package com.tcse.microsvcdiagnoser.service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Deprecated
@Service("robot-scanner")
public class RobotScannerService {
    
    
    public List<String> scan(String testCaseDir) {
        File testCaseDirFile = new File(testCaseDir);
        if(!testCaseDirFile.exists()){
            return null;
        }
        List<String> list = FileUtils.listFiles(testCaseDirFile,
            new IOFileFilter() {  // 文件过滤
                public boolean accept(File file) {
                    String name = file.getName();
                    return name.endsWith(".robot") || name.endsWith(".txt");
                }
                public boolean accept(File dir, String name) {
                    return true;
                }
            },
            new IOFileFilter() {  // 目录过滤
                public boolean accept(File file) {
                    String name = file.getName();
                    return !".git".equals(name);
                }
                
                public boolean accept(File dir, String name) {
                    return true;
                }
            }
        ).stream().map(f -> FilenameUtils.separatorsToUnix(getRelativePath(testCaseDirFile, f)).replace(".java", "")).collect(Collectors.toList());
        return list;
    }
    
    public static String getRelativePath(File parent, File child){
        try {
            String parentPath = parent.getCanonicalPath();
            String childPath = child.getCanonicalPath();
            return childPath.substring(parentPath.length() + 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
    
}
