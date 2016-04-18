package bo.com.erp360.webapp.service;

import javax.ejb.Stateless;

import bo.com.erp360.webapp.model.TipoProducto;
import bo.com.erp360.webapp.model.TipoServicio;

//The @Stateless annotation eliminates the need for manual transaction demarcation

@Stateless
public class TipoServicioRegistration extends DataAccessService<TipoServicio>{
	public TipoServicioRegistration(){
		super(TipoServicio.class);
	}
}
