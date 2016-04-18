package bo.com.erp360.webapp.service;

import javax.ejb.Stateless;

import bo.com.erp360.webapp.model.DetalleOrdenCompra;

//The @Stateless annotation eliminates the need for manual transaction demarcation

@Stateless
public class DetalleOrdenCompraRegistration extends DataAccessService<DetalleOrdenCompra>{
	public DetalleOrdenCompraRegistration(){
		super(DetalleOrdenCompra.class);
	}

}

