package bo.com.erp360.webapp.service;

import javax.ejb.Stateless;

import bo.com.erp360.webapp.model.TipoCambio;

//The @Stateless annotation eliminates the need for manual transaction demarcation

@Stateless
public class TipoCambioRegistration extends DataAccessService<TipoCambio>{
	public TipoCambioRegistration(){
		super(TipoCambio.class);
	}

}

