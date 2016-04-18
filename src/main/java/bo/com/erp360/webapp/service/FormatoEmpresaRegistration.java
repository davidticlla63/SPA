package bo.com.erp360.webapp.service;

import javax.ejb.Stateless;

import bo.com.erp360.webapp.model.FormatoEmpresa;

//The @Stateless annotation eliminates the need for manual transaction demarcation

@Stateless
public class FormatoEmpresaRegistration extends DataAccessService<FormatoEmpresa>{
	public FormatoEmpresaRegistration(){
		super(FormatoEmpresa.class);
	}
}
