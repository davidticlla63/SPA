package bo.com.erp360.webapp.data;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import bo.com.erp360.webapp.model.Privilegio;
import bo.com.erp360.webapp.model.Roles;

@Stateless
public class PrivilegioRepository {

	@Inject
	private EntityManager em;

	public Privilegio findById(int id) {
		return em.find(Privilegio.class, id);
	}

	@SuppressWarnings("unchecked")
	public List<Privilegio> findAllOrderedByID() {
		String query = "select em from Privilegio em ";// where em.estado='AC' or ser.estado='IN' order by em.id desc";
		System.out.println("Query Privilegio: "+query);
		return em.createQuery(query).getResultList();
	}

	public List<Privilegio> findAll(){
		try{
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Privilegio> criteria = cb.createQuery(Privilegio.class);
			Root<Privilegio> company = criteria.from(Privilegio.class);
			criteria.select(company);
			return em.createQuery(criteria).getResultList();
		}catch(Exception e){
			System.out.println("error: "+e.getMessage());
			return new ArrayList<Privilegio>();

		}
	}

	@SuppressWarnings("unchecked")
	public List<Privilegio> findAllByRoles(Roles roles){
		try{
			String query = "select em from Privilegio em where em.estado='AC' and em.roles.id="+roles.getId()+" order by em.id asc";
			System.out.println("Query Privilegio: "+query);
			return em.createQuery(query).getResultList();
		}catch(Exception e){
			System.out.println("error: "+e.getMessage());
			return new ArrayList<Privilegio>();

		}
	}

}
