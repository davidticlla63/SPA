package bo.com.erp360.webapp.data;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.OrdenVenta;
import bo.com.erp360.webapp.model.Producto;
import bo.com.erp360.webapp.model.Servicio;

@Stateless
public class ServicioRepository {

	@Inject
	private EntityManager em;

	public Servicio findById(int id) {
		return em.find(Servicio.class, id);
	}

	@SuppressWarnings("unchecked")
	public List<Servicio> findAllOrderedByID() {
		String query = "select em from Servicio em where em.estado='AC' or em.estado='IN' order by em.id desc";
		System.out.println("Query Servicio: " + query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Servicio> findAllByEmpresa(Empresa empresa) {
		String query = "select em from Servicio em where (em.estado='AC' or em.estado='IN') and em.empresa.id="
				+ empresa.getId() + " order by em.id desc";
		System.out.println("Query Servicio: " + query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Servicio> findAllActivosByEmpresa(Empresa empresa) {
		String query = "select em from Servicio em where em.estado='AC' and em.empresa.id="
				+ empresa.getId() + " order by em.id desc";
		System.out.println("Query Servicio: " + query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Servicio> findAllActivosByEmpresa(Empresa empresa, String filter) {
		String query = "select em from Servicio em where em.estado='AC' and em.empresa.id="
				+ empresa.getId() + " order by em.id desc";
		System.out.println("Query Servicio: " + query);
		return em.createQuery(query).getResultList();
	}
	
	 @SuppressWarnings("unchecked")
	   	public List<Servicio> findAllActivasNotList(Empresa empresa,OrdenVenta ordenVenta) {
	       	String query = "select em from Servicio em  where em.estado='AC' and em.empresa.id="+empresa.getId()+"  and em.id not in (select ord.servicios.id from DetalleOrdenServicio ord where ord.ordenVenta.id="+ordenVenta.getId()+"   ) order by em.id desc";
	       	System.out.println("Query Producto: "+query);
	       	return em.createQuery(query).getResultList();
	       }

}
