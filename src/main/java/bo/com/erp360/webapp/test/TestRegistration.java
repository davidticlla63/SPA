package bo.com.erp360.webapp.test;

import javax.ejb.Stateless;

import bo.com.erp360.webapp.model.Test;
import bo.com.erp360.webapp.service.DataAccessService;

//The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateless
public class TestRegistration extends DataAccessService<Test> {

    public TestRegistration() {
        super(Test.class);
    }

}