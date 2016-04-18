package bo.com.erp360.webapp.service;

import javax.ejb.Stateless;

import bo.com.erp360.webapp.model.TipoCuenta;

//The @Stateless annotation eliminates the need for manual transaction demarcation

@Stateless
public class TipoCuentaRegistration extends DataAccessService<TipoCuenta>{
	public TipoCuentaRegistration(){
		super(TipoCuenta.class);
	}

}

