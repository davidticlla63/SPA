package bo.com.erp360.webapp.data;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.PlanCuenta;
import bo.com.erp360.webapp.model.TemplateTipoComprobante;
import bo.com.erp360.webapp.model.TipoComprobante;

@Stateless
public class TemplateTipoComprobanteRepository {

	@Inject
	private EntityManager em;

	public TemplateTipoComprobante findById(int id) {
		return em.find(TemplateTipoComprobante.class, id);
	}

	@SuppressWarnings("unchecked")
	public List<TemplateTipoComprobante> findAllOrderedByID() {
		String query = "select em from TemplateTipoComprobante em ";// where em.estado='AC' or ser.estado='IN' order by em.id desc";
		System.out.println("Query AsientoContable: "+query);
		return em.createQuery(query).getResultList();
	}

	public List<TemplateTipoComprobante> findAll(){
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<TemplateTipoComprobante> criteria = cb.createQuery(TemplateTipoComprobante.class);
		Root<TemplateTipoComprobante> asientoContable = criteria.from(TemplateTipoComprobante.class);
		criteria.select(asientoContable);
		return em.createQuery(criteria).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<PlanCuenta> findPlanCuentaByTipoComprobanteAndEmpresa(TipoComprobante tc, Empresa empresa) {
		try{
			String query = "select em.planCuenta from TemplateTipoComprobante em where em.tipoComprobante.id="+tc.getId()+" and em.tipoComprobante.empresa.id="+empresa.getId();
			System.out.println("Query TemplateTipoComprobante: "+query);
			return em.createQuery(query).getResultList();
		}catch(Exception e){
			return new ArrayList<PlanCuenta>();
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<TemplateTipoComprobante> findTemplateTipoComprobanteByTipoComprobanteAndEmpresa(TipoComprobante tc, Empresa empresa) {
		try{
			String query = "select em from TemplateTipoComprobante em where em.estado='AC' and em.tipoComprobante.id="+tc.getId()+" and em.tipoComprobante.empresa.id="+empresa.getId()+ " order by em.id asc";
			System.out.println("Query TemplateTipoComprobante: "+query);
			return em.createQuery(query).getResultList();
		}catch(Exception e){
			return new ArrayList<TemplateTipoComprobante>();
		}
	}

}
