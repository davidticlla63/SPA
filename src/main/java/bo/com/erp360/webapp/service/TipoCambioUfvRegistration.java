package bo.com.erp360.webapp.service;

import javax.ejb.Stateless;

import bo.com.erp360.webapp.model.TipoCambioUfv;

//The @Stateless annotation eliminates the need for manual transaction demarcation

@Stateless
public class TipoCambioUfvRegistration extends DataAccessService<TipoCambioUfv>{
	public TipoCambioUfvRegistration(){
		super(TipoCambioUfv.class);
	}

}

