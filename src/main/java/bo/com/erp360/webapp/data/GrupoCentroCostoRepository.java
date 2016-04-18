package bo.com.erp360.webapp.data;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.GrupoCentroCosto;

@Stateless
public class GrupoCentroCostoRepository {
	 
	@Inject
    private EntityManager em;

    public GrupoCentroCosto findById(int id) {
        return em.find(GrupoCentroCosto.class, id);
    }
    
    @SuppressWarnings("unchecked")
	public List<GrupoCentroCosto> findAllOrderedByID() {
    	String query = "select em from GrupoCentroCosto em where em.estado='AC' or em.estado='IN' order by em.id desc";
    	System.out.println("Query GrupoCentroCosto: "+query);
    	return em.createQuery(query).getResultList();
    }

    public List<GrupoCentroCosto> findAll(){
    	CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<GrupoCentroCosto> criteria = cb.createQuery(GrupoCentroCosto.class);
		Root<GrupoCentroCosto> comprobante = criteria.from(GrupoCentroCosto.class);
		criteria.select(comprobante);
		return em.createQuery(criteria).getResultList();
    }
    
    @SuppressWarnings("unchecked")
	public List<GrupoCentroCosto> findAllByEmpresa(Empresa empresa,Gestion gestion) {
    	String query = "select em from GrupoCentroCosto em where (em.estado='AC' or em.estado='IN') and em.empresa.id="+empresa.getId()+" and em.gestion.id="+gestion.getId();
    	System.out.println("Query GrupoCentroCosto: "+query);
    	return em.createQuery(query).getResultList();
    }
	
}
