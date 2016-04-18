package bo.com.erp360.webapp.data;

import org.apache.log4j.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.FormatoEmpresa;

@Stateless
public class FormatoEmpresaRepository {

	@Inject
	private EntityManager em;

	private Logger log = Logger.getLogger(this.getClass());

	public FormatoEmpresa findById(int id) {
		return em.find(FormatoEmpresa.class, id);
	}

	public FormatoEmpresa  findByEmpresa(Empresa empresa) {
		try{
			String query = "SELECT em FROM FormatoEmpresa em  WHERE em.empresa.id = "+empresa.getId();
			log.info("Query FormatoEmpresa "+query);
			return  (FormatoEmpresa) em.createQuery(query).getSingleResult();
		}catch(Exception e){
			return new FormatoEmpresa();
		}
	}
}
