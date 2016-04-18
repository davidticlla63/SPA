package bo.com.erp360.webapp.service;

import javax.ejb.Stateless;

import bo.com.erp360.webapp.model.Dosificacion;
import bo.com.erp360.webapp.model.Linea;
import bo.com.erp360.webapp.model.Pais;

//The @Stateless annotation eliminates the need for manual transaction demarcation

@Stateless
public class LineaRegistration extends DataAccessService<Linea>{
	public LineaRegistration(){
		super(Linea.class);
	}
}
