package bo.com.erp360.webapp.data;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.Ciudad;
import bo.com.erp360.webapp.model.Pais;

@Stateless
public class CiudadRepository {

	@Inject
	private EntityManager em;

	public Ciudad findById(int id) {
		return em.find(Ciudad.class, id);
	}

	public Ciudad findByNombre(String nombre) {
		String query = "select em from Ciudad em  where em.nombre='"+nombre+"'";
		System.out.println("Query Ciudad: "+query);
		return (Ciudad) em.createQuery(query).getSingleResult();
	}


	@SuppressWarnings("unchecked")
	public List<Ciudad> findAllOrderedByID() {
		String query = "select em from Ciudad em ";// where em.state='AC' or em.state='IN' order by em.id desc";
		System.out.println("Query Ciudad: "+query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Ciudad> findAll() {
		String query = "select em from Ciudad em where (em.state='AC' or em.state='IN') order by em.id desc";
		System.out.println("Query Ciudad: "+query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Ciudad> findAllActivasByPais(Pais pais) {
		String query = "select em from Ciudad em where em.state='AC' and em.pais.id="+pais.getId()+" order by em.nombre asc";
		System.out.println("Query Ciudad: "+query);
		return em.createQuery(query).getResultList();
	}
	@SuppressWarnings("unchecked")
	public List<Ciudad> findAllActivas() {
		String query = "select em from Ciudad em where em.state='AC'  order by em.nombre asc";
		System.out.println("Query Ciudad: "+query);
		return em.createQuery(query).getResultList();
	}




}
