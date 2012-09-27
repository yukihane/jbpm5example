package com.sample;

import java.util.List;

import javax.ejb.Local;

import com.sample.TaskBean.MyTask;


@Local
public interface TaskLocal
{
    public List<MyTask> retrieveTaskList(String actorId);
    public void approveTask(String actorId, long taskId) throws Exception;
}
