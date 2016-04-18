package bo.com.erp360.webapp.data;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.Comprobante;
import bo.com.erp360.webapp.model.Egreso;
import bo.com.erp360.webapp.model.Empresa;

@Stateless
public class EgresoRepository {

	@Inject
	private EntityManager em;
	
	@Inject
    private Logger log;
	//log.info

	public Egreso findById(int id) {
		return em.find(Egreso.class, id);
	}

	@SuppressWarnings("unchecked")
	public List<Egreso> findByEmpresa(Empresa empresa) {
		try{
			String query = "select em from Egreso em  where (em.estado='AC' or em.estado='IN') and em.empresa.id="+empresa.getId()+ " order by em.id desc";
			log.info("Query Egreso: "+query);
			return em.createQuery(query).getResultList();
		}catch(Exception e){
			return new ArrayList<>();
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Egreso> findActivosByEmpresa(Empresa empresa) {
		try{
			String query = "select em from Egreso em  where em.estado='AC' and em.empresa.id="+empresa.getId()+ " order by em.id desc";
			log.info("Query Egreso: "+query);
			return em.createQuery(query).getResultList();
		}catch(Exception e){
			return new ArrayList<>();
		}
	}
	
	public Egreso findByComprobante(Comprobante comprobante){
		String query = "select em from Egreso em  where em.comprobante.id="+comprobante.getId();
		log.info("Query Egreso: "+query);
		return (Egreso) em.createQuery(query).getSingleResult();
	}

}
