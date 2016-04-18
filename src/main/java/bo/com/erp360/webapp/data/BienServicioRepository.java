package bo.com.erp360.webapp.data;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.BienServicio;
import bo.com.erp360.webapp.model.Proveedor;

@Stateless
public class BienServicioRepository {

	@Inject
	private EntityManager em;
	
	@Inject
	private Logger log;

	public BienServicio findById(int id) {
		return em.find(BienServicio.class, id);
	}

	@SuppressWarnings("unchecked")
	public List<BienServicio> findAllOrderedByID() {
		String query = "select em from BienServicio em ";// where em.estado='AC' or em.estado='IN' order by em.id desc";
		log.info("Query BienServicio: "+query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<BienServicio> findAllByProveedor(Proveedor proveedor){
		String query = "select em from BienServicio em  where em.proveedor.id="+proveedor.getId()+" order by em.id asc";
		log.info("Query BienServicio: "+query);
		return em.createQuery(query).getResultList();
	}
	
	
}
