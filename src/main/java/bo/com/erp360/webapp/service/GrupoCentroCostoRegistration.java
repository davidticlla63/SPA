package bo.com.erp360.webapp.service;

import javax.ejb.Stateless;

import bo.com.erp360.webapp.model.GrupoCentroCosto;

//The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateless
public class GrupoCentroCostoRegistration extends DataAccessService<GrupoCentroCosto>{
	public GrupoCentroCostoRegistration(){
		super(GrupoCentroCosto.class);
	}

}