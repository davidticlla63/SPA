package bo.com.erp360.webapp.service;


import javax.ejb.Stateless;
import bo.com.erp360.webapp.model.CotizacionServicio;

//The @Stateless annotation eliminates the need for manual transaction demarcation

@Stateless
public class CotizacionServicioRegistration extends DataAccessService<CotizacionServicio>{
	public CotizacionServicioRegistration(){
		super(CotizacionServicio.class);
	}
}
