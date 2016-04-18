package bo.com.erp360.webapp.service;

import javax.ejb.Stateless;

import bo.com.erp360.webapp.model.UsuarioEmpresa;

//The @Stateless annotation eliminates the need for manual transaction demarcation

@Stateless
public class UsuarioEmpresaRegistration extends DataAccessService<UsuarioEmpresa>{
	public UsuarioEmpresaRegistration(){
		super(UsuarioEmpresa.class);
	}

}

