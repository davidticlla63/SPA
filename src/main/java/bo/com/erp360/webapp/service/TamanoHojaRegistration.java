package bo.com.erp360.webapp.service;

import javax.ejb.Stateless;

import bo.com.erp360.webapp.model.Dosificacion;
import bo.com.erp360.webapp.model.Linea;
import bo.com.erp360.webapp.model.Pais;
import bo.com.erp360.webapp.model.TamanoHoja;

//The @Stateless annotation eliminates the need for manual transaction demarcation

@Stateless
public class TamanoHojaRegistration extends DataAccessService<TamanoHoja>{
	public TamanoHojaRegistration(){
		super(TamanoHoja.class);
	}
}
