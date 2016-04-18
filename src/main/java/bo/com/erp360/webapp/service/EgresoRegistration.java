package bo.com.erp360.webapp.service;

import javax.ejb.Stateless;

import bo.com.erp360.webapp.model.Egreso;

//The @Stateless annotation eliminates the need for manual transaction demarcation

@Stateless
public class EgresoRegistration extends DataAccessService<Egreso>{
	public EgresoRegistration(){
		super(Egreso.class);
	}

}

