package bo.com.erp360.webapp.data;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import bo.com.erp360.webapp.model.security.Rol;

@Stateless
public class RolRepository {
	 
	@Inject
    private EntityManager em;

    public Rol findById(int id) {
        return em.find(Rol.class, id);
    }
    
    @SuppressWarnings("unchecked")
	public List<Rol> findAllOrderByAsc(){
    	String query = "select em from Rol em where em.estado='AC' or em.estado='IN' or em.estado='SU' order by em.id asc";
    	System.out.println("Query Roles: "+query);
    	return em.createQuery(query).getResultList();
    }
    
    public Rol findRolByNombre(String name) {
    	String query = "select em from Rol em  where em.nombre='"+name+"'";
    	System.out.println("Query Roles: "+query);
    	return (Rol) em.createQuery(query).getSingleResult();
    }
	
}
