package com.sample;

import javax.ejb.Local;

@Local
public interface ProcessLocal
{
    public long startProcess() throws Exception;
}
