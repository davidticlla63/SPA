package bo.com.erp360.webapp.service;

import javax.ejb.Stateless;

import bo.com.erp360.webapp.model.DetalleOrdenServicio;
import bo.com.erp360.webapp.model.Dosificacion;
import bo.com.erp360.webapp.model.Pais;
import bo.com.erp360.webapp.model.Producto;

//The @Stateless annotation eliminates the need for manual transaction demarcation

@Stateless
public class DetalleOrdenServicioRegistration extends DataAccessService<DetalleOrdenServicio>{
	public DetalleOrdenServicioRegistration(){
		super(DetalleOrdenServicio.class);
	}
}
