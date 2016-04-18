package bo.com.erp360.webapp.service;

import javax.ejb.Stateless;

import bo.com.erp360.webapp.model.security.Rol;

@Stateless
public class RolRegistration extends DataAccessService<Rol>{
	public RolRegistration(){
		super(Rol.class);
	}
}
