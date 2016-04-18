package bo.com.erp360.webapp.service;

import javax.ejb.Stateless;

import bo.com.erp360.webapp.model.DetalleNotaVenta;
import bo.com.erp360.webapp.model.NotaVenta;
import bo.com.erp360.webapp.model.OrdenVenta;

//The @Stateless annotation eliminates the need for manual transaction demarcation

@Stateless
public class NotaVentaRegistration extends DataAccessService<NotaVenta>{
	public NotaVentaRegistration(){
		super(NotaVenta.class);
	}
}
