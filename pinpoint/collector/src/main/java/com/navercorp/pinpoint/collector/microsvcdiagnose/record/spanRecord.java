package com.navercorp.pinpoint.collector.microsvcdiagnose.record;

public class spanRecord {
    protected String methodRecord;
    protected short sequence;
    protected int depth;
    
    public void setMethodRecord(String methodRecord) {
        this.methodRecord = methodRecord;
    }
    
    public void setSequence(short sequence) {
        this.sequence = sequence;
    }
    
    public void setDepth(int depth) {
        this.depth = depth;
    }
    
    public String getMethodRecord() {
        return methodRecord;
    }
    
    public short getSequence() {
        return sequence;
    }
    
    public int getDepth() {
        return depth;
    }
    
    public String toString(){
        return  "SpanRecord{" +
                "methodRecord='" + methodRecord + '\'' + ", " +
                "sequence='" + sequence + "\'" + ", " +
                "depth='" + depth + "\'" +
                "}";
    }
}
