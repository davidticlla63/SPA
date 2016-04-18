package bo.com.erp360.webapp.data;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.CentroCosto;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.GrupoCentroCosto;

@Stateless
public class CentroCostoRepository {
	 
	@Inject
    private EntityManager em;
	
	@Inject
    private Logger log;
	//log.info

    public CentroCosto findById(int id) {
        return em.find(CentroCosto.class, id);
    }
    
    @SuppressWarnings("unchecked")
	public List<CentroCosto> findAllOrderedByID() {
    	String query = "select em from CentroCosto em ";// where em.estado='AC' or ser.estado='IN' order by em.id desc";
    	log.info("Query CentroCosto: "+query);
    	return em.createQuery(query).getResultList();
    }
    
    public CentroCosto findByNombre(String nombre) {
		String query = "select pc from CentroCosto pc  where pc.nombre='"+nombre+"'";
		log.info("Query CentroCosto: "+query);
		return (CentroCosto) em.createQuery(query).getSingleResult();
	}

    @SuppressWarnings("unchecked")
	public List<CentroCosto> findAllCentroCostoByEmpresa(Empresa empresa) {
		String query = "select em from CentroCosto em,GrupoCentroCosto gcc where (em.estado='AC' or em.estado='IN') and em.grupoCentroCosto.id=gcc.id and gcc.empresa.id="+empresa.getId();
		System.out.println("Query CentroCosto: "+query);
		return  em.createQuery(query).getResultList();
	}
    
    @SuppressWarnings("unchecked")
	public List<CentroCosto> findQueryAllCentroCostoByEmpresa(Empresa empresa,String query) {
		String queryAux = "select em from CentroCosto em,GrupoCentroCosto gcc where em.grupoCentroCosto.id=gcc.id and gcc.empresa.id="+empresa.getId()+" and upper(em.nombre) like '%"+query+"%'";
		log.info("Query CentroCosto: "+queryAux);
		return  em.createQuery(queryAux).getResultList();
	}
    
    @SuppressWarnings("unchecked")
	public List<CentroCosto> findAllByGrupoCentroCosto(GrupoCentroCosto gcc) {
		String query = "select em from CentroCosto em,GrupoCentroCosto gcc where (em.estado='AC' or em.estado='IN') and em.grupoCentroCosto.id=gcc.id and em.grupoCentroCosto.id="+gcc.getId();
		log.info("Query CentroCosto: "+query);
		return  em.createQuery(query).getResultList();
	}
	
}
