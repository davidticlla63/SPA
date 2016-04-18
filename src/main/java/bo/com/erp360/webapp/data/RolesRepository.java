package bo.com.erp360.webapp.data;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.Roles;

@Stateless
public class RolesRepository {
	 
	@Inject
    private EntityManager em;

    public Roles findById(int id) {
        return em.find(Roles.class, id);
    }
    
    @SuppressWarnings("unchecked")
	public List<Roles> findAllOrderedByID() {
    	String query = "select em from Roles em ";// where em.estado='AC' or ser.estado='IN' order by em.id desc";
    	System.out.println("Query Roles: "+query);
    	return em.createQuery(query).getResultList();
    }
    
    @SuppressWarnings("unchecked")
	public List<Roles> findAllActivos(){
    	String query = "select em from Roles em where em.state='AC' or em.state='SU' order by em.id desc";
    	System.out.println("Query Roles: "+query);
    	return em.createQuery(query).getResultList();
    }
    
    @SuppressWarnings("unchecked")
	public List<Roles> findAll(){
    	String query = "select em from Roles em where em.state='AC' or em.state='IN' or em.state='SU' order by em.id desc";
    	System.out.println("Query Roles: "+query);
    	return em.createQuery(query).getResultList();
    }
    
    public Roles findByName(String name) {
    	String query = "select em from Roles em  where em.name='"+name+"'";
    	System.out.println("Query Roles: "+query);
    	return (Roles) em.createQuery(query).getSingleResult();
    }
    
	
}
