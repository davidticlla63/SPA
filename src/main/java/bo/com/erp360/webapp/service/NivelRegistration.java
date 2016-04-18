package bo.com.erp360.webapp.service;

import javax.ejb.Stateless;

import bo.com.erp360.webapp.model.Nivel;

//The @Stateless annotation eliminates the need for manual transaction demarcation

@Stateless
public class NivelRegistration extends DataAccessService<Nivel>{
	public NivelRegistration(){
		super(Nivel.class);
	}

}

