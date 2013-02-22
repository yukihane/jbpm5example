package com.sample;

import javax.ejb.Local;

@Local
public interface ProcessLocal
{
    public long editContent(String user, String taskId, String message);
}
