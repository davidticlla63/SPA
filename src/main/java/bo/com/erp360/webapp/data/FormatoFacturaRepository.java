package bo.com.erp360.webapp.data;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.FormatoFactura;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Sucursal;

@Stateless
public class FormatoFacturaRepository {

	@Inject
	private EntityManager em;
	
	@Inject
    private Logger log;
	//log.info

	public FormatoFactura findById(int id) {
		return em.find(FormatoFactura.class, id);
	}


	@SuppressWarnings("unchecked")
	public List<FormatoFactura> findByEmpresa(Empresa empresa) {
		try{
			String query = "select em from FormatoFactura em  where (em.estado='AC' or em.estado='IN') and em.empresa.id="+empresa.getId()+ " order by em.id desc";
			log.info("Query FormatoFactura: "+query);
			return em.createQuery(query).getResultList();
		}catch(Exception e){
			return new ArrayList<>();
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<FormatoFactura> findActivosByEmpresa(Empresa empresa,Sucursal sucursal) {
		try{
			String query = "select em from FormatoFactura em  where em.estado='AC' and em.empresa.id="+empresa.getId()+ " and em.sucursal.id="+sucursal.getId()+ " order by em.id desc";
			log.info("Query FormatoFactura: "+query);
			return em.createQuery(query).getResultList();
		}catch(Exception e){
			return new ArrayList<>();
		}
	}
	

}
