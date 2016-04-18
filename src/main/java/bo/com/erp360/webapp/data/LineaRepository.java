package bo.com.erp360.webapp.data;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Linea;

@Stateless
public class LineaRepository {

	@Inject
	private EntityManager em;

	public Linea findById(int id) {
		return em.find(Linea.class, id);
	}

	public Linea findByNombre(String nombre) {
		String query = "select em from Linea em  where em.nombre='"+nombre+"'";
		System.out.println("Query Linea: "+query);
		return (Linea) em.createQuery(query).getSingleResult();
	}


	@SuppressWarnings("unchecked")
	public List<Linea> findAllOrderedByID() {
		String query = "select em from Linea em ";// where em.state='AC' or em.state='IN' order by em.id desc";
		System.out.println("Query Linea: "+query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Linea> findAllByEmpresa(Empresa empresa) {
		String query = "select em from Linea em where (em.state='AC' or em.state='IN') and em.empresa.id="+empresa.getId()+" order by em.nombre asc";
		System.out.println("Query Linea: "+query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Linea> findAllActivasByEmpresa(Empresa empresa) {
		String query = "select em from Linea em where em.state='AC'  and em.empresa.id="+empresa.getId()+" order by em.nombre asc";
		System.out.println("Query Linea: "+query);
		return em.createQuery(query).getResultList();
	}
	
	
	@SuppressWarnings("unchecked")
	public List<Linea> findAllActivasByEmpresaName(Empresa empresa, String nombre) {
		String query = "select em from Linea em where em.state='AC'  and em.empresa.id="+empresa.getId()+" and em.nombre='"+nombre.toUpperCase()+"' order by em.nombre asc";
		System.out.println("Query Linea: "+query);
		return em.createQuery(query).getResultList();
	}




}
