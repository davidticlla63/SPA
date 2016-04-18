package bo.com.erp360.webapp.data;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.PlanCuentaBancaria;

@Stateless
public class PlanCuentaBancariaRepository {
	 
	@Inject
    private EntityManager em;
	
	@Inject
    private Logger log;
//	log.info

    public PlanCuentaBancaria findById(int id) {
        return em.find(PlanCuentaBancaria.class, id);
    }
    
    @SuppressWarnings("unchecked")
	public List<PlanCuentaBancaria> findAllOrderedByID() {
    	String query = "select em from PlanCuentaBancaria em  where em.estado='AC' order by em.id desc";
    	log.info("Query PlanCuentaBancaria: "+query);
    	return em.createQuery(query).getResultList();
    }
    
    @SuppressWarnings("unchecked")
	public List<PlanCuentaBancaria> findAllByEmpresa(Empresa empresa) {
    	String query = "select em from PlanCuentaBancaria em  where (em.estado='AC' or em.estado='IN') and em.empresa.id="+empresa.getId()+" order by em.id desc";
    	log.info("Query PlanCuentaBancaria: "+query);
    	return em.createQuery(query).getResultList();
    }
    
    @SuppressWarnings("unchecked")
	public List<PlanCuentaBancaria> findAllActivasByEmpresa(Empresa empresa) {
    	String query = "select em from PlanCuentaBancaria em  where em.estado='AC' and em.empresa.id="+empresa.getId()+" order by em.id desc";
    	log.info("Query PlanCuentaBancaria: "+query);
    	return em.createQuery(query).getResultList();
    }
	
}
