package bo.com.erp360.webapp.service;

import javax.ejb.Stateless;
import bo.com.erp360.webapp.model.AsientoContable;

//The @Stateless annotation eliminates the need for manual transaction demarcation

@Stateless
public class AsientoContableRegistration extends DataAccessService<AsientoContable>{
	public AsientoContableRegistration(){
		super(AsientoContable.class);
	}
}
