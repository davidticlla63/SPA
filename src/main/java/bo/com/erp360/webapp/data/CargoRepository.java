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
import bo.com.erp360.webapp.model.Cargo;

@Stateless
public class CargoRepository {

	@Inject
	private EntityManager em;

	public Cargo findById(Long id) {
		try {

		} catch (Exception e) {
		}
		return em.find(Cargo.class, id);
	}

	public Cargo findById(Integer id){
		return em.find(Cargo.class, id);
	}

	
	
	public Cargo findByName(String name){
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Cargo> criteria = cb.createQuery(Cargo.class);
		Root<Cargo> user = criteria.from(Cargo.class);
		criteria.select(user).where(cb.equal(user.get("nombre"), name));
		return em.createQuery(criteria).getSingleResult();
	}


	@SuppressWarnings("unchecked")
	public List<Cargo> findAllOrderedByID(Empresa empresa) {
		String query = "select em from Cargo em where (em.estado='AC' or em.estado='IN')  and em.empresa.id="+empresa.getId()+" order by em.id desc";
		System.out.println("Query Cargo: "+query);
		return em.createQuery(query).getResultList();
	}
	

	@SuppressWarnings("unchecked")
	public List<Cargo> findAllActive(Empresa empresa) {
		String query = "select em from Cargo em where em.estado='AC' and em.empresa.id="+empresa.getId()+" order by em.id desc";
		System.out.println("Query Cargo: "+query);
		return em.createQuery(query).getResultList();
	}
	
	
	@SuppressWarnings("unchecked")
	public List<Cargo> findAllActiveForNomrbre(Empresa empresa,String nombre) {
		String query = "select em from Cargo em where em.estado='AC' and em.empresa.id="+empresa.getId()+" and em.nombre='"+nombre+"' order by em.id desc";
		System.out.println("Query Cargo: "+query);
		return em.createQuery(query).getResultList();
	}
	


	
}
