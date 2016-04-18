package bo.com.erp360.webapp.data;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.TipoProducto;

@Stateless
public class TipoProductoRepository {

	@Inject
	private EntityManager em;

	public TipoProducto findById(int id) {
		return em.find(TipoProducto.class, id);
	}

	public TipoProducto findByNombre(String nombre) {
		String query = "select em from TipoProducto em  where em.nombre='"+nombre+"'";
		System.out.println("Query TipoProducto: "+query);
		return (TipoProducto) em.createQuery(query).getSingleResult();
	}


	@SuppressWarnings("unchecked")
	public List<TipoProducto> findAllOrderedByID() {
		String query = "select em from TipoProducto em ";// where em.state='AC' or em.state='IN' order by em.id desc";
		System.out.println("Query TipoProducto: "+query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<TipoProducto> findAllByEmpresa(Empresa empresa) {
		String query = "select em from TipoProducto em where (em.state='AC' or em.state='IN') and em.empresa.id="+empresa.getId()+" order by em.id desc";
		System.out.println("Query TipoProducto: "+query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<TipoProducto> findAllActivasByEmpresa(Empresa empresa) {
		String query = "select em from TipoProducto em where em.state='AC' and em.empresa.id="+empresa.getId()+"  order by em.id desc";
		System.out.println("Query TipoProducto: "+query);
		return em.createQuery(query).getResultList();
	}
	
	
	@SuppressWarnings("unchecked")
	public List<TipoProducto> findAllActivasByEmpresa(Empresa empresa,String nombre) {
		String query = "select em from TipoProducto em where em.state='AC' and em.empresa.id="+empresa.getId()+" and em.nombre like '"+nombre+"%'  order by em.id desc";
		System.out.println("Query TipoProducto: "+query);
		return em.createQuery(query).getResultList();
	}




}
