package bo.com.erp360.webapp.data;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.TipoCuenta;

@Stateless
public class TipoCuentaRepository {

	@Inject
	private EntityManager em;
	
	@Inject
	private Logger log;

	public TipoCuenta findById(int id) {
		return em.find(TipoCuenta.class, id);
	}

	@SuppressWarnings("unchecked")
	public List<TipoCuenta> findAllByEmpresaGestion(Empresa empresa, Gestion gestion){
		String query = "select em from TipoCuenta em  where em.empresa.id="+empresa.getId()+" and em.gestion.id="+gestion.getId()+" order by em.id asc";
		log.info("Query BienServicio: "+query);
		return em.createQuery(query).getResultList();
	}
	
	
}
