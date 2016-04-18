package bo.com.erp360.webapp.service;

import javax.ejb.Stateless;

import bo.com.erp360.webapp.model.Ciudad;
import bo.com.erp360.webapp.model.Dosificacion;

//The @Stateless annotation eliminates the need for manual transaction demarcation

@Stateless
public class CiudadRegistration extends DataAccessService<Ciudad>{
	public CiudadRegistration(){
		super(Ciudad.class);
	}
}
