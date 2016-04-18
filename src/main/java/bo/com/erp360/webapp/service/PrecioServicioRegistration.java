package bo.com.erp360.webapp.service;

import javax.ejb.Stateless;

import bo.com.erp360.webapp.model.PrecioServicio;

//The @Stateless annotation eliminates the need for manual transaction demarcation

@Stateless
public class PrecioServicioRegistration extends DataAccessService<PrecioServicio>{
	public PrecioServicioRegistration(){
		super(PrecioServicio.class);
	}

}

