package bo.com.erp360.webapp.service;


import javax.ejb.Stateless;

import bo.com.erp360.webapp.model.Servicio;

//The @Stateless annotation eliminates the need for manual transaction demarcation

@Stateless
public class ServicioRegistration extends DataAccessService<Servicio>{
	public ServicioRegistration(){
		super(Servicio.class);
	}

}
