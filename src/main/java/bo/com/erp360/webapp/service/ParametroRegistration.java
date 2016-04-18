package bo.com.erp360.webapp.service;

import javax.ejb.Stateless;

import bo.com.erp360.webapp.model.Cliente;
import bo.com.erp360.webapp.model.Parametro;

//The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateless
public class ParametroRegistration extends DataAccessService<Parametro>{
	public ParametroRegistration(){
		super(Parametro.class);
	}

}

