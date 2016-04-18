package bo.com.erp360.webapp.service;


import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.CentroCosto;
import bo.com.erp360.webapp.model.ConfiguracionDB;
import bo.com.erp360.webapp.model.GrupoCentroCosto;

//The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateless
public class ConfiguracionDBRegistration extends DataAccessService<ConfiguracionDB>{
	@Inject
	private EntityManager em;

	public ConfiguracionDBRegistration(){
		super(ConfiguracionDB.class);
	}



	public List<ConfiguracionDB> obtenerConfiguracionDB(){
		String query = "select em from ConfiguracionDB em where em.estado='AC' ";
		System.out.println("Query ConfiguracionDB: "+query);
		return em.createQuery(query).getResultList();
	}
}
//
//
//
//
//@Stateless
//public class CentroCostoRegistration extends DataAccessService<CentroCosto>{
//	@Inject
//    private EntityManager em;
//	
//	public CentroCostoRegistration(){
//		super(CentroCosto.class);
//	}
//	
//	public List<CentroCosto> deleteCentGrupoCentroCosto(GrupoCentroCosto gcc){
//		List<CentroCosto> listCentroCosto = obtenerCentroCostoByGrupoCentroCosto(gcc);
//		for(CentroCosto cc : listCentroCosto ){
//			cc.setEstado("RM");
//			super.update(cc);
//		}
//		return listCentroCosto;
//	}
//	
//	private List<CentroCosto> obtenerCentroCostoByGrupoCentroCosto(GrupoCentroCosto gcc){
//		String query = "select em from CentroCosto em where em.grupoCentroCosto.id="+gcc.getId();
//    	System.out.println("Query CentroCosto: "+query);
//    	return em.createQuery(query).getResultList();
//	}
//
//}

