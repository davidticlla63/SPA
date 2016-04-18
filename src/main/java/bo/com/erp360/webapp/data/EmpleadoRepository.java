package bo.com.erp360.webapp.data;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Roles;
import bo.com.erp360.webapp.model.Empleado;

@Stateless
public class EmpleadoRepository {

	@Inject
	private EntityManager em;

	public Empleado findById(Long id) {
		try {

		} catch (Exception e) {
		}
		return em.find(Empleado.class, id);
	}

	public Empleado findById(Integer id){
		return em.find(Empleado.class, id);
	}

	
	
	public Empleado findByName(String name){
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Empleado> criteria = cb.createQuery(Empleado.class);
		Root<Empleado> user = criteria.from(Empleado.class);
		criteria.select(user).where(cb.equal(user.get("nombre"), name));
		return em.createQuery(criteria).getSingleResult();
	}


	@SuppressWarnings("unchecked")
	public List<Empleado> findAllOrderedByID(Empresa empresa) {
		String query = "select em from Empleado em where (em.state='AC' or em.state='IN')  and em.empresa.id="+empresa.getId()+" order by em.id desc";
		System.out.println("Query Empleado: "+query);
		return em.createQuery(query).getResultList();
	}
	
	
	@SuppressWarnings("unchecked")
	public List<Empleado> findAllActive(Empresa empresa) {
		String query = "select em from Empleado em where em.state='AC' and em.empresa.id="+empresa.getId()+" order by em.id desc";
		System.out.println("Query Empleado: "+query);
		return em.createQuery(query).getResultList();
	}
	
/*	@SuppressWarnings("unchecked")
	public List<Empleado> findAllForEmpresaOrderedByID(Empresa empresa) {
		String query = "select em from Empleado em where (em.state='AC' or em.state='IN' or em.state='SU') and em.empresa.id="+empresa.getId()+" order by em.id desc";
		System.out.println("Query Empleado: "+query);
		return em.createQuery(query).getResultList();
	}*/

	

	public Empleado findByEmail(String email) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Empleado> criteria = cb.createQuery(Empleado.class);
		Root<Empleado> user = criteria.from(Empleado.class);
		criteria.select(user).where(cb.equal(user.get("email"), email));
		return em.createQuery(criteria).getSingleResult();
	}

	
}
