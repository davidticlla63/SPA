package bo.com.erp360.webapp.service;

import javax.ejb.Stateless;

import bo.com.erp360.webapp.model.Roles;

@Stateless
public class RolService extends DataAccessService<Roles>{
	public RolService(){
		super(Roles.class);
	}
}
