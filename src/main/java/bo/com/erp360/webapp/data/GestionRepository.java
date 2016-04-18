package bo.com.erp360.webapp.data;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;

@Stateless
public class GestionRepository {
	 
	@Inject
    private EntityManager em;
	
	@Inject
	private Logger log;

    public Gestion findById(int id) {
        return em.find(Gestion.class, id);
    }
    
    @SuppressWarnings("unchecked")
	public List<Gestion> findAllOrderedByID() {
    	String query = "select em from Gestion em ";// where em.estado='AC' or ser.estado='IN' order by em.id desc";
    	log.info("Query Gestion: "+query);
    	return em.createQuery(query).getResultList();
    }
    
    public Gestion findByGestionEmpresa(Integer gestion, Empresa empresa) {
    	String query = "select em from Gestion em where em.gestion="+gestion+" and em.empresa.id="+empresa.getId();
    	log.info("Query Gestion: "+query);
    	return (Gestion) em.createQuery(query).getSingleResult();
    }

    public List<Gestion> findAll(){
    	CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Gestion> criteria = cb.createQuery(Gestion.class);
		Root<Gestion> company = criteria.from(Gestion.class);
		criteria.select(company);
		return em.createQuery(criteria).getResultList();
    }
    
    
    public List<Gestion> findByGestionForEmpresa(Integer gestion, Empresa empresa) {
    	String query = "select em from Gestion em where em.gestion="+gestion+" and em.empresa.id="+empresa.getId();
    	log.info("Query Gestion: "+query);
    	return em.createQuery(query).getResultList();
    }
    
    @SuppressWarnings("unchecked")
	public List<Gestion> findAllByEmpresa(Empresa empresa){
    	String query = "select em from Gestion em  where em.empresa.id="+empresa.getId()+" order by em.gestion desc";
    	log.info("Query Gestion: "+query);
    	return em.createQuery(query).getResultList();
    }    
	
}
