package bo.com.erp360.webapp.data;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.Pais;

@Stateless
public class PaisRepository {

	@Inject
	private EntityManager em;

	public Pais findById(int id) {
		return em.find(Pais.class, id);
	}

	public Pais findByNombre(String nombre) {
		String query = "select em from Pais em  where em.nombre='"+nombre+"'";
		System.out.println("Query Pais: "+query);
		return (Pais) em.createQuery(query).getSingleResult();
	}


	@SuppressWarnings("unchecked")
	public List<Pais> findAllOrderedByID() {
		String query = "select em from Pais em ";// where em.state='AC' or em.state='IN' order by em.id desc";
		System.out.println("Query Pais: "+query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Pais> findAll() {
		String query = "select em from Pais em where (em.state='AC' or em.state='IN') order by em.id desc";
		System.out.println("Query Pais: "+query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Pais> findAllActivas() {
		String query = "select em from Pais em where em.state='AC'  order by em.nombre asc";
		System.out.println("Query Pais: "+query);
		return em.createQuery(query).getResultList();
	}




}
