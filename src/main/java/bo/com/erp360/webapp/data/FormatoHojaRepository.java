package bo.com.erp360.webapp.data;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.FormatoHoja;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Sucursal;

@Stateless
public class FormatoHojaRepository {

	@Inject
	private EntityManager em;
	
	@Inject
    private Logger log;
	//log.info

	public FormatoHoja findById(int id) {
		return em.find(FormatoHoja.class, id);
	}


	@SuppressWarnings("unchecked")
	public List<FormatoHoja> findByEmpresa(Empresa empresa) {
		try{
			String query = "select em from FormatoHoja em  where (em.estado='AC' or em.estado='IN') and em.empresa.id="+empresa.getId()+ " order by em.id desc";
			log.info("Query FormatoHoja: "+query);
			return em.createQuery(query).getResultList();
		}catch(Exception e){
			return new ArrayList<>();
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<FormatoHoja> findActivosByEmpresa(Empresa empresa,Sucursal sucursal) {
		try{
			String query = "select em from FormatoHoja em  where em.estado='AC' and em.empresa.id="+empresa.getId()+ " and em.sucursal.id="+sucursal.getId()+ " order by em.id desc";
			log.info("Query FormatoHoja: "+query);
			return em.createQuery(query).getResultList();
		}catch(Exception e){
			return new ArrayList<>();
		}
	}
	

}
