package bo.com.erp360.webapp.data;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.GrupoImpuesto;

@Stateless
public class GrupoImpuestoRepository {

	@Inject
	private EntityManager em;
	
	@Inject
    private Logger log;
	//log.info

	public GrupoImpuesto findById(int id) {
		return em.find(GrupoImpuesto.class, id);
	}

	public GrupoImpuesto findByEmpresa(Empresa empresa) {
		try{
			String query = "select em from GrupoImpuesto em  where (em.estado='AC' or em.estado='IN') and em.empresa.id="+empresa.getId()+ " order by em.id desc";
			log.info("Query GrupoImpuesto: "+query);
			return (GrupoImpuesto) em.createQuery(query).getSingleResult();
		}catch(Exception e){
			return new GrupoImpuesto();
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<GrupoImpuesto> findAllByEmpresa(Empresa empresa) {
		try{
			String query = "select em from GrupoImpuesto em  where (em.estado='AC' or em.estado='IN') and em.empresa.id="+empresa.getId()+ " order by em.id desc";
			log.info("Query GrupoImpuesto: "+query);
			return em.createQuery(query).getResultList();
		}catch(Exception e){
			return new ArrayList<GrupoImpuesto>();
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<GrupoImpuesto> findActivosByEmpresa(Empresa empresa) {
		try{
			String query = "select em from GrupoImpuesto em  where em.estado='AC' and em.empresa.id="+empresa.getId()+ " order by em.id desc";
			log.info("Query GrupoImpuesto: "+query);
			return em.createQuery(query).getResultList();
		}catch(Exception e){
			return new ArrayList<GrupoImpuesto>();
		}
	}

}
