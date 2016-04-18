package bo.com.erp360.webapp.service;

import javax.ejb.Stateless;

import bo.com.erp360.webapp.model.Cliente;

//The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateless
public class ClienteRegistration extends DataAccessService<Cliente>{
	public ClienteRegistration(){
		super(Cliente.class);
	}

}

