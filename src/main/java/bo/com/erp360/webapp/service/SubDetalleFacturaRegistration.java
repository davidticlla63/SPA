package bo.com.erp360.webapp.service;

import javax.ejb.Stateless;

import bo.com.erp360.webapp.model.DetalleFactura;
import bo.com.erp360.webapp.model.Dosificacion;
import bo.com.erp360.webapp.model.Pais;
import bo.com.erp360.webapp.model.Producto;
import bo.com.erp360.webapp.model.SubDetalleFactura;

//The @Stateless annotation eliminates the need for manual transaction demarcation

@Stateless
public class SubDetalleFacturaRegistration extends DataAccessService<SubDetalleFactura>{
	public SubDetalleFacturaRegistration(){
		super(SubDetalleFactura.class);
	}
}
