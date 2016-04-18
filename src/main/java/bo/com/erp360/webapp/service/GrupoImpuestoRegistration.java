package bo.com.erp360.webapp.service;

import javax.ejb.Stateless;

import bo.com.erp360.webapp.model.GrupoImpuesto;

//The @Stateless annotation eliminates the need for manual transaction demarcation

@Stateless
public class GrupoImpuestoRegistration extends DataAccessService<GrupoImpuesto>{
	public GrupoImpuestoRegistration(){
		super(GrupoImpuesto.class);
	}

}

