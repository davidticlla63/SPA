package bo.com.erp360.webapp.service;

import javax.ejb.Stateless;
import bo.com.erp360.webapp.model.Gestion;

//The @Stateless annotation eliminates the need for manual transaction demarcation

@Stateless
public class GestionRegistration extends DataAccessService<Gestion>{
	public GestionRegistration(){
		super(Gestion.class);
	}
}
