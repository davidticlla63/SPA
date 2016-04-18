package bo.com.erp360.webapp.data;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.Linea;
import bo.com.erp360.webapp.model.LineaProveedor;
import bo.com.erp360.webapp.model.Proveedor;

@Stateless
public class LineaProveedorRepository {

	@Inject
	private EntityManager em;

	public LineaProveedor findById(int id) {
		return em.find(LineaProveedor.class, id);
	}

	@SuppressWarnings("unchecked")
	public List<LineaProveedor> findAllByProveedor(Proveedor proveedor) {
		String query = "select em from LineaProveedor em where (em.estado='AC' or em.estado='IN') and em.proveedor.id="
				+ proveedor.getId() + " order by em.id desc";
		System.out.println("Query LineaProveedor: " + query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<LineaProveedor> findAllActivasByProveedor(Proveedor proveedor) {
		String query = "select em from LineaProveedor em where em.estado='AC' and em.proveedor.id="
				+ proveedor.getId() + "  order by em.id desc";
		System.out.println("Query LineaProveedor: " + query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<LineaProveedor> findAllByLinea(Linea linea) {
		String query = "select em from LineaProveedor em where (em.estado='AC' or em.estado='IN') and em.proveedor.id="
				+ linea.getId() + " order by em.id desc";
		System.out.println("Query LineaProveedor: " + query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<LineaProveedor> findAllActivasByLinea(Linea linea) {
		String query = "select em from LineaProveedor em where em.estado='AC' and em.proveedor.id="
				+ linea.getId() + "  order by em.id desc";
		System.out.println("Query LineaProveedor: " + query);
		return em.createQuery(query).getResultList();
	}

}
