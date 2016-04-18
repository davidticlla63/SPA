package bo.com.erp360.webapp.service;

import javax.ejb.Stateless;

import bo.com.erp360.webapp.model.Producto;

//The @Stateless annotation eliminates the need for manual transaction demarcation

@Stateless
public class ProductoRegistration extends DataAccessService<Producto>{
	public ProductoRegistration(){
		super(Producto.class);
	}
}
