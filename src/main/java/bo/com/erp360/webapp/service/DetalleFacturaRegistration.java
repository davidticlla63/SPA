package bo.com.erp360.webapp.service;

import javax.ejb.Stateless;

import bo.com.erp360.webapp.model.DetalleFactura;
import bo.com.erp360.webapp.model.Dosificacion;
import bo.com.erp360.webapp.model.Pais;
import bo.com.erp360.webapp.model.Producto;

//The @Stateless annotation eliminates the need for manual transaction demarcation

@Stateless
public class DetalleFacturaRegistration extends DataAccessService<DetalleFactura>{
	public DetalleFacturaRegistration(){
		super(DetalleFactura.class);
	}
}
