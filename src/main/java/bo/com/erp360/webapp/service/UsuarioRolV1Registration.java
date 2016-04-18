package bo.com.erp360.webapp.service;

import javax.ejb.Stateless;

import bo.com.erp360.webapp.model.security.UsuarioRolV1;

//The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateless
public class UsuarioRolV1Registration extends DataAccessService<UsuarioRolV1>{

	public UsuarioRolV1Registration(){
		super(UsuarioRolV1.class);
	}

	
}

