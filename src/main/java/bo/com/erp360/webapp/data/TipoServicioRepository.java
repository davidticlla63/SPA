package bo.com.erp360.webapp.data;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.TipoServicio;

@Stateless
public class TipoServicioRepository {

	@Inject
	private EntityManager em;

	public TipoServicio findById(int id) {
		return em.find(TipoServicio.class, id);
	}

	public List<TipoServicio> findAllByNombre(String nombre) {
		String query = "select em from TipoServicio em  where em.nombre='"+nombre+"'";
		System.out.println("Query TipoServicio: "+query);
		return  em.createQuery(query).getResultList();
	}
	
	public TipoServicio findByNombre(String nombre) {
		String query = "select em from TipoServicio em  where em.nombre='"+nombre+"'";
		System.out.println("Query TipoServicio: "+query);
		return (TipoServicio) em.createQuery(query).getSingleResult();
	}


	@SuppressWarnings("unchecked")
	public List<TipoServicio> findAllOrderedByID() {
		String query = "select em from TipoServicio em ";// where em.estado='AC' or em.estado='IN' order by em.id desc";
		System.out.println("Query TipoServicio: "+query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<TipoServicio> findAllByEmpresa(Empresa empresa) {
		String query = "select em from TipoServicio em where (em.estado='AC' or em.estado='IN') and em.empresa.id="+empresa.getId()+" order by em.id desc";
		System.out.println("Query TipoServicio: "+query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<TipoServicio> findAllActivasByEmpresa(Empresa empresa) {
		String query = "select em from TipoServicio em where em.estado='AC' and em.empresa.id="+empresa.getId()+"  order by em.id desc";
		System.out.println("Query TipoServicio: "+query);
		return em.createQuery(query).getResultList();
	}




}
