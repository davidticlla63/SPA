package bo.com.erp360.webapp.data;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Sucursal;

@Stateless
public class SucursalRepository {

	@Inject
	private EntityManager em;

	public Sucursal findById(int id) {
		return em.find(Sucursal.class, id);
	}

	public Sucursal findByNombreAndEmpresa(String nombre, Empresa empresa) {
		String query = "select em from Sucursal em  where em.nombre='"+nombre+"' and em.empresa.id="+empresa.getId();
		System.out.println("Query Sucursal: "+query);
		return (Sucursal) em.createQuery(query).getSingleResult();
	}
	
	public Sucursal findByNombre(String nombre) {
		String query = "select em from Sucursal em  where em.nombre='"+nombre+"' ";
		System.out.println("Query Sucursal: "+query);
		return (Sucursal) em.createQuery(query).getSingleResult();
	}
	
	public Sucursal findBySucursalEmpresa(String sucursal, Empresa empresa) {
    	String query = "select em from Sucursal em where em.nombre='"+sucursal+"' and em.empresa.id="+empresa.getId();
    	System.out.println("Query Gestion: "+query);
    	return (Sucursal) em.createQuery(query).getSingleResult();
    }


	@SuppressWarnings("unchecked")
	public List<Sucursal> findAllOrderedByID() {
		String query = "select em from Sucursal em ";// where em.estado='AC' or em.estado='IN' order by em.id desc";
		System.out.println("Query Sucursal: "+query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Sucursal> findAllByEmpresa(Empresa empresa) {
		String query = "select em from Sucursal em where (em.estado='AC' or em.estado='IN') and em.empresa.id="+empresa.getId()+" order by em.id desc";
		System.out.println("Query Sucursal: "+query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Sucursal> findAllActivasByEmpresa(Empresa empresa) {
		String query = "select em from Sucursal em where em.estado='AC' and em.empresa.id="+empresa.getId()+" order by em.id desc";
		System.out.println("Query Sucursal: "+query);
		return em.createQuery(query).getResultList();
	}

	public List<Sucursal> findAll(){
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Sucursal> criteria = cb.createQuery(Sucursal.class);
		Root<Sucursal> company = criteria.from(Sucursal.class);
		criteria.select(company);
		return em.createQuery(criteria).getResultList();
	}

    @SuppressWarnings("unchecked")
	public List<Sucursal> traerSucursalesFacturas() {
        try {
        	String query = "select suc from Sucursal suc where suc.id in (select distinct fac.sucursal.id from Factura fac)";
        	System.out.println("Consulta traerSucursalesFacturas: "+query);
            return  em.createQuery(query).getResultList();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Error en traerSucursalesFacturas: "+e.getMessage());
			return null;
		}
    }


}
