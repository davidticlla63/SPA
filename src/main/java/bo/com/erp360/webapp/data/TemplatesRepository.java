package bo.com.erp360.webapp.data;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.Sucursal;
import bo.com.erp360.webapp.model.Templates;

@Stateless
public class TemplatesRepository {

	@Inject
	private EntityManager em;

	@Inject
	private Logger log;

	// log.info

	public Templates findById(int id) {
		return em.find(Templates.class, id);
	}

	public Templates findByNombre(String nombre) {
		String query = "select em from Templates em where em.estado='AC' and em.nombre='"
				+ nombre + "' ";
		log.info("Query Templates: " + query);
		return (Templates) em.createQuery(query).getSingleResult();
	}

	@SuppressWarnings("unchecked")
	public List<Templates> findAll() {
		try {
			String query = "select em from Templates em  where (em.estado='AC' or em.estado='IN') order by em.id desc";
			log.info("Query Templates: " + query);
			return em.createQuery(query).getResultList();
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}

	@SuppressWarnings("unchecked")
	public Templates findActivos() {
		String query = "select em from Templates em  where em.estado='AC'  order by em.id desc";
		log.info("Query Templates: " + query);
		return (Templates) em.createQuery(query).getSingleResult();

	}

}
