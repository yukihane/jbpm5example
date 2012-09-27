package com.sample;

import javax.ejb.Local;

import com.sample.entity.MyContent;

@Local
public interface ProcessLocal
{
    public long createContent(String user, MyContent cont);
}
