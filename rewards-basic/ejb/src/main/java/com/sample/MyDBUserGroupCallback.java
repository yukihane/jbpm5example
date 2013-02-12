package com.sample;

import org.jbpm.task.identity.DBUserGroupCallbackImpl;

/**
 * UserGroupCallbackを実装してみるためのカスタムダミークラス。 (jbpm.usergroup.callback.properties
 * ファイルで定義)
 */
public class MyDBUserGroupCallback extends DBUserGroupCallbackImpl {

    public MyDBUserGroupCallback() {
        super();
    }
}
