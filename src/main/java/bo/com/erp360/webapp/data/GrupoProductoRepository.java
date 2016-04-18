package bo.com.erp360.webapp.data;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.GrupoProducto;
import bo.com.erp360.webapp.model.Linea;

@Stateless
public class GrupoProductoRepository {

	@Inject
	private EntityManager em;

	public GrupoProducto findById(int id) {
		return em.find(GrupoProducto.class, id);
	}

	public GrupoProducto findByNombre(String nombre) {
		String query = "select em from GrupoProducto em  where em.nombre='"+nombre+"'";
		System.out.println("Query GrupoProducto: "+query);
		return (GrupoProducto) em.createQuery(query).getSingleResult();
	}


	@SuppressWarnings("unchecked")
	public List<GrupoProducto> findAllOrderedByID() {
		String query = "select em from GrupoProducto em ";// where em.state='AC' or em.state='IN' order by em.id desc";
		System.out.println("Query GrupoProducto: "+query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<GrupoProducto> findAllByEmpresa(Empresa empresa) {
		String query = "select em from GrupoProducto em where (em.state='AC' or em.state='IN') and em.empresa.id="+empresa.getId()+" order by em.id desc";
		System.out.println("Query GrupoProducto: "+query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<GrupoProducto> findAllActivasByEmpresa(Empresa empresa) {
		String query = "select em from GrupoProducto em where em.state='AC' and em.empresa.id="+empresa.getId()+"  order by em.id desc";
		System.out.println("Query GrupoProducto: "+query);
		return em.createQuery(query).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<GrupoProducto> findAllActivasByEmpresaForLinea(Empresa empresa,Linea  linea) {
		String query = "select em from GrupoProducto em where em.state='AC' and em.empresa.id="+empresa.getId()+" and em.linea.id="+linea.getId()+"  order by em.id desc";
		System.out.println("Query GrupoProducto: "+query);
		return em.createQuery(query).getResultList();
	}
	
	
	@SuppressWarnings("unchecked")
	public List<GrupoProducto> findAllActivasByEmpresaForLinea(Empresa empresa,Linea  linea,String nombre) {
		String query = "select em from GrupoProducto em where em.state='AC' and em.empresa.id="+empresa.getId()+" and em.linea.id="+linea.getId()+"  and em.nombre like '"+nombre+"%' order by em.id desc";
		System.out.println("Query GrupoProducto: "+query);
		return em.createQuery(query).getResultList();
	}




}
