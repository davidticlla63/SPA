package bo.com.erp360.webapp.service;

import javax.ejb.Stateless;

import bo.com.erp360.webapp.model.TipoProducto;

//The @Stateless annotation eliminates the need for manual transaction demarcation

@Stateless
public class TipoProductoRegistration extends DataAccessService<TipoProducto>{
	public TipoProductoRegistration(){
		super(TipoProducto.class);
	}
}
