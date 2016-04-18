package bo.com.erp360.webapp.data;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.TamanoHoja;

@Stateless
public class TamanoHojaRepository {

	@Inject
	private EntityManager em;

	public TamanoHoja findById(int id) {
		return em.find(TamanoHoja.class, id);
	}

	public TamanoHoja findByNombre(String nombre) {
		String query = "select em from TamanoHoja em  where em.tamano='"+nombre+"'";
		System.out.println("Query TamanoHoja: "+query);
		return (TamanoHoja) em.createQuery(query).getSingleResult();
	}


	@SuppressWarnings("unchecked")
	public List<TamanoHoja> findAllOrderedByID() {
		String query = "select em from TamanoHoja em ";// where em.estado='AC' or em.estado='IN' order by em.id desc";
		System.out.println("Query TamanoHoja: "+query);
		return em.createQuery(query).getResultList();
	}


	@SuppressWarnings("unchecked")
	public List<TamanoHoja> findAllActivas() {
		String query = "select em from TamanoHoja em  order by em.tamano asc";
		System.out.println("Query TamanoHoja: "+query);
		return em.createQuery(query).getResultList();
	}

	
	public TamanoHoja traerHojaActiva() {
		String query = "select em from TamanoHoja em  where em.estado='AC'";
		System.out.println("Query TamanoHoja: "+query);
		return (TamanoHoja) em.createQuery(query).getSingleResult();
	}



}
