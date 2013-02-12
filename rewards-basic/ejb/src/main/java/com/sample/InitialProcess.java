package com.sample;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.sample.entity.MyServiceGroup;
import com.sample.entity.MyServiceUser;

@Startup
@Singleton
public class InitialProcess {

    @PersistenceContext(unitName = "org.jbpm.persistence.jpa")
    private EntityManager em;

    @PostConstruct
    public void init() {
        /*
         * インメモリDBを用いたサンプルなので、起動時にDBへユーザを登録しておきます。1
         */
        /*
         * jbpm-human-task-coreパッケージ内のTaskorm.xmlで定義されているNamedQuery
         * "TasksAssignedAsPotentialOwnerWithGroups" をみると
         * ユーザは必ずグループに所属している必要があるようなのでグループも設定します。
         */
        final MyServiceGroup group = new MyServiceGroup("onegroup");
        em.persist(group);

        MyServiceUser user = new MyServiceUser("Administrator");
        user.setGroup(group);
        em.persist(user);

        user = new MyServiceUser("john");
        user.setGroup(group);
        em.persist(user);

        user = new MyServiceUser("mary");
        user.setGroup(group);
        em.persist(user);

    }
}
