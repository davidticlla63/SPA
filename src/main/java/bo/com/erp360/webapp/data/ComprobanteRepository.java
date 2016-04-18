package bo.com.erp360.webapp.data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import bo.com.erp360.webapp.model.Comprobante;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.Sucursal;
import bo.com.erp360.webapp.model.TipoComprobante;

@Stateless
public class ComprobanteRepository {

	@Inject
	private EntityManager em;

	@Inject
	private Logger log;
	//	log.info

	public Comprobante findById(int id) {
		return em.find(Comprobante.class, id);
	}

	@SuppressWarnings("unchecked")
	public List<Comprobante> findAllOrderedByID() {
		String query = "select em from Comprobante em ";// where em.estado='AC' or ser.estado='IN' order by em.id desc";
		log.info("Query Comprobante: "+query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Comprobante> findAllByEmpresa(Empresa empresa) {
		String query = "select em from Comprobante em  where em.estado='AC' and em.empresa.id="+empresa.getId()+" order by em.id desc";
		log.info("Query Comprobante: "+query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Comprobante> findAllByEmpresaGestion(Empresa empresa,Gestion gestion) {
		String query = "select em from Comprobante em  where em.estado='AC' and em.empresa.id="+empresa.getId()+" and em.gestion.id="+gestion.getId()+" order by em.numero desc";
		log.info("Query Comprobante: "+query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Comprobante> findAllByEmpresaSucursalGestionTipoComprobanteMes(Empresa empresa,Sucursal sucursal, Gestion gestion,TipoComprobante tc,int mes) {
		String query = "select em from Comprobante em  where em.estado='AC' and em.empresa.id="+empresa.getId()+" and em.gestion.id="+gestion.getId()+" and em.sucursal.id="+sucursal.getId()+" and em.tipoComprobante.id="+tc.getId()+" and date_part('month', em.fecha) ="+mes+" order by em.numero desc";
		log.info("Query Comprobante: "+query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Comprobante> findAllByEmpresaGestionMes(Empresa empresa, Gestion gestion,int mes) {
		String query = "select em from Comprobante em  where em.estado='AC' and em.empresa.id="+empresa.getId()+" and em.gestion.id="+gestion.getId()+" and date_part('month', em.fecha) ="+mes+" order by em.numero desc";
		log.info("Query Comprobante: "+query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Comprobante> findAllByEmpresaGestionTipoComprobanteMes(Empresa empresa, Gestion gestion,TipoComprobante tc,int mes) {
		String query = "select em from Comprobante em  where em.estado='AC' and em.empresa.id="+empresa.getId()+" and em.gestion.id="+gestion.getId()+" and em.tipoComprobante.id="+tc.getId()+" and date_part('month', em.fecha) ="+mes+" order by em.numero desc";
		log.info("Query Comprobante: "+query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Comprobante> findAllByEmpresaSucursalGestionTipoComprobante(Empresa empresa,Sucursal sucursal, Gestion gestion,TipoComprobante tc) {
		String query = "select em from Comprobante em  where em.estado='AC' and em.empresa.id="+empresa.getId()+" and em.gestion.id="+gestion.getId()+" and em.sucursal.id="+sucursal.getId()+" and em.tipoComprobante.id="+tc.getId()+" order by em.numero desc";
		log.info("Query Comprobante: "+query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Comprobante> findAllByEmpresaSucursalGestion(Empresa empresa,Sucursal sucursal, Gestion gestion) {
		String query = "select em from Comprobante em  where em.estado='AC' and em.empresa.id="+empresa.getId()+" and em.gestion.id="+gestion.getId()+" and em.sucursal.id="+sucursal.getId()+" order by em.numero desc";
		log.info("Query Comprobante: "+query);
		return em.createQuery(query).getResultList();
	}

	public List<Comprobante> findAll(){
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Comprobante> criteria = cb.createQuery(Comprobante.class);
		Root<Comprobante> comprobante = criteria.from(Comprobante.class);
		criteria.select(comprobante);
		return em.createQuery(criteria).getResultList();
	}

	@SuppressWarnings("unchecked")
	public int obtenerNumeroComprobante(Date date,Empresa empresa, Sucursal sucursal,TipoComprobante tc){
		Integer year = Integer.parseInt( new SimpleDateFormat("yyyy").format(date));
		Integer mes = Integer.parseInt(new SimpleDateFormat("MM").format(date));
		//and em.tipoComprobante.id="+tc.getId()+"
		String query = "select em from Comprobante em where (em.estado='AC' or em.estado='AN') and em.sucursal.id="+sucursal.getId()+" and em.empresa.id="+empresa.getId()+" and date_part('month', em.fecha) ="+mes+" and date_part('year', em.fecha) ="+year+" and em.tipoComprobante.id="+tc.getId();
		log.info("Query Comprobante: "+query);
		return (( List<Comprobante>)em.createQuery(query).getResultList()).size() + 1;
	}

	@SuppressWarnings("unchecked")
	public int obtenerCorrelativoTransaccionalComprobante(Empresa empresa, Sucursal sucursal){
		String query = "select em from Comprobante em where (em.estado='AC' or em.estado='AN') and em.sucursal.id="+sucursal.getId()+" and em.empresa.id="+empresa.getId();
		log.info("Query Comprobante: "+query);
		return (( List<Comprobante>)em.createQuery(query).getResultList()).size() + 1;
	}


}
