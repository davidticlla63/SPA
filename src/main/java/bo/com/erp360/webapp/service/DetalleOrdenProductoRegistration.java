package bo.com.erp360.webapp.service;

import javax.ejb.Stateless;

import bo.com.erp360.webapp.model.DetalleOrdenProducto;

//The @Stateless annotation eliminates the need for manual transaction demarcation

@Stateless
public class DetalleOrdenProductoRegistration extends DataAccessService<DetalleOrdenProducto>{
	public DetalleOrdenProductoRegistration(){
		super(DetalleOrdenProducto.class);
	}
}
