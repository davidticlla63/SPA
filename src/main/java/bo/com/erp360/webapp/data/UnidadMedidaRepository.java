package bo.com.erp360.webapp.data;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.TipoProducto;
import bo.com.erp360.webapp.model.UnidadMedida;

@Stateless
public class UnidadMedidaRepository {

	@Inject
	private EntityManager em;

	public UnidadMedida findById(int id) {
		return em.find(UnidadMedida.class, id);
	}

	public UnidadMedida findByNombre(String nombre) {
		String query = "select em from UnidadMedida em  where em.nombre='"+nombre+"'";
		System.out.println("Query UnidadMedida: "+query);
		return (UnidadMedida) em.createQuery(query).getSingleResult();
	}


	@SuppressWarnings("unchecked")
	public List<UnidadMedida> findAllOrderedByID() {
		String query = "select em from UnidadMedida em ";// where em.state='AC' or em.state='IN' order by em.id desc";
		System.out.println("Query UnidadMedida: "+query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<UnidadMedida> findAllByEmpresa(Empresa empresa) {
		String query = "select em from UnidadMedida em where (em.state='AC' or em.state='IN') and em.empresa.id="+empresa.getId()+" order by em.id desc";
		System.out.println("Query UnidadMedida: "+query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<UnidadMedida> findAllActivasByEmpresa(Empresa empresa) {
		String query = "select em from UnidadMedida em where em.state='AC' and em.empresa.id="+empresa.getId()+"  order by em.id desc";
		System.out.println("Query UnidadMedida: "+query);
		return em.createQuery(query).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<UnidadMedida> findAllActivasByEmpresa(Empresa empresa,String nombre) {
		String query = "select em from UnidadMedida em where em.state='AC' and em.empresa.id="+empresa.getId()+" and (em.nombre like '"+nombre+"%' or em.sigla like '"+nombre+"%' ) order by em.id desc";
		System.out.println("Query UnidadMedida: "+query);
		return em.createQuery(query).getResultList();
	}




}
