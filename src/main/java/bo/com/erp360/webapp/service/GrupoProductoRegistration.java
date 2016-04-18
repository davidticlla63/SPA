package bo.com.erp360.webapp.service;

import javax.ejb.Stateless;

import bo.com.erp360.webapp.model.GrupoProducto;
import bo.com.erp360.webapp.model.TipoProducto;

//The @Stateless annotation eliminates the need for manual transaction demarcation

@Stateless
public class GrupoProductoRegistration extends DataAccessService<GrupoProducto>{
	public GrupoProductoRegistration(){
		super(GrupoProducto.class);
	}
}
