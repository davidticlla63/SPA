package bo.com.erp360.webapp.data;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.UsuarioEmpresa;
import bo.com.erp360.webapp.model.Usuario;
 
@Stateless
public class UsuarioEmpresaRepository {
	
	@Inject
    private EntityManager em;
	
	@Inject
	private Logger log;

    public UsuarioEmpresa findById(int id) {
        return em.find(UsuarioEmpresa.class, id);
    }
    
  
    
    public List<Usuario> findByUsuarioForEmpresa(Empresa empresa) {
    	String query = "select em.usuario from UsuarioEmpresa em where em.empresa.id="+empresa.getId();
    	log.info("Query UsuarioEmpresa: "+query);
    	return  em.createQuery(query).getResultList();
    }
    
   
    
    @SuppressWarnings("unchecked")
	public List<UsuarioEmpresa> findAllByUsuario(Usuario u) {
    	String query = "select em from UsuarioEmpresa em ,UsuarioUsuarioEmpresa ue where (em.estado='AC' or em.estado='IN') and ue.usuario.id="+u.getId()
    			+ " and em.id=ue.empresa.id  order by em.id desc";
    	log.info("Query UsuarioEmpresa: "+query);
    	return em.createQuery(query).getResultList();
    }
    
    @SuppressWarnings("unchecked")
	public List<UsuarioEmpresa> findAllActivasByUsuario(Usuario u) {
    	String query = "select em from UsuarioEmpresa em ,UsuarioUsuarioEmpresa ue where em.estado='AC' and ue.usuario.id="+u.getId()
    			+ " and em.id=ue.empresa.id  order by em.id desc";
    	log.info("Query UsuarioEmpresa: "+query);
    	return em.createQuery(query).getResultList();
    }
    
    public UsuarioEmpresa findByUsuarioUsuarioEmpresa(Usuario u, String nombreUsuarioEmpresa) {
    	String query = "select em from UsuarioEmpresa em ,UsuarioUsuarioEmpresa ue where ue.usuario.id="+u.getId()
    			+ " and em.id=ue.empresa.id and em.razonSocial='"+nombreUsuarioEmpresa+"' order by em.id desc";
    	log.info("Query UsuarioEmpresa: "+query);
    	return (UsuarioEmpresa) em.createQuery(query).getSingleResult();
    }
    
	
}
