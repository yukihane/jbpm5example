package com.sample;

import javax.ejb.Local;

import com.sample.entity.MyContent;

@Local
public interface ProcessLocal
{
    public long editContent(String user, String taskId, String message);
}
