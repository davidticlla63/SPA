package bo.com.erp360.webapp.data;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.PlanCuenta;
import bo.com.erp360.webapp.model.Usuario;

@ApplicationScoped
public class PlanCuentaRepository {

	@Inject
	private EntityManager em;

	public PlanCuenta findById(int id) {
		return em.find(PlanCuenta.class, id);
	}

	@SuppressWarnings("unchecked")
	public List<PlanCuenta> findAll() {
		String query = "select em from PlanCuenta em ";
		System.out.println("Query PlanCuenta: "+query);
		return em.createQuery(query).getResultList();
	}

	public PlanCuenta findByDescripcionAndEmpresa(String descripcion, Empresa empresa) {
		try{
			String query = "select pc from PlanCuenta  pc  where pc.descripcion='"+descripcion+"' and pc.empresa.id="+empresa.getId();
			//  select pc from plan_cuenta pc  where pc.descripcion='EXIGIBLE'        and (pc.id_empresa= 2 or pc.id_empresa is null )
			System.out.println("Query PlanCuenta: "+query);
			return (PlanCuenta) em.createQuery(query).getSingleResult();
		}catch(Exception e){
			System.out.println("findByDescripcionAndEmpresa error:"+e.getMessage());
			return null;
		}
	}

	public PlanCuenta findByDescripcionAndEmpresa2(String descripcion, Empresa empresa){
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<PlanCuenta> criteria = cb.createQuery(PlanCuenta.class);
		Root<PlanCuenta> q = criteria.from(PlanCuenta.class);
		criteria.select(q).where(cb.equal(q.get("descripcion"), descripcion),cb.and(cb.equal(q.get("empresa"), empresa)));
		return em.createQuery(criteria).getSingleResult();
	}

	@SuppressWarnings("unchecked")
	public List<PlanCuenta> findAllActivoByEmpresa(Empresa empresa){
		String query = "select em from PlanCuenta em where em.estado='AC' and em.empresa.id="+empresa.getId()+" order by em.id asc";
		//+ " and em.id=ue.empresa.id  order by em.id asc";
		System.out.println("Query PlanCuenta: "+query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<PlanCuenta> findAllByUsuario(Usuario u) {
		String query = "select em from PlanCuenta em ";//,UsuarioEmpresa ue where ue.usuario.id="+u.getId()
		//+ " and em.id=ue.empresa.id  order by em.id asc";
		System.out.println("Query PlanCuenta: "+query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<PlanCuenta> findAllAuxiliarByEmpresa(Empresa empresa,Gestion gestion) {  // order by em.codigo_auxiliar asc
		String query = "select em from PlanCuenta em where em.clase='AUXILIAR' and em.empresa.id="+empresa.getId()+" order by em.codigoAuxiliar asc";
		//+ " and em.id=ue.empresa.id  order by em.id asc";
		System.out.println("Query PlanCuenta: "+query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<PlanCuenta> findQueryAllAuxiliarByEmpresa(Empresa empresa,String query) {
		String queryAux = "select em from PlanCuenta em where em.clase='AUXILIAR' and em.empresa.id="+empresa.getId()+" and upper(em.descripcion) like '%"+query+"%'";
		//+ " and em.id=ue.empresa.id  order by em.id asc";
		System.out.println("Query Empresa: "+queryAux);
		return em.createQuery(queryAux).getResultList();
	}

	public PlanCuenta findByCodigoAndEmpresa(String codigo, Empresa empresa ) {
		String query = "select em from PlanCuenta em  where em.codigo='"+codigo+"' and em.empresa.id="+empresa.getId();
		//+ " and em.id=ue.empresa.id  order by em.id asc";
		System.out.println("Query PlanCuenta: "+query);
		return (PlanCuenta)em.createQuery(query).getSingleResult();
	}

	@SuppressWarnings("unchecked")
	public List<PlanCuenta> findCuentasRoot(Empresa empresa) {
		try{
			String query = "select pc from PlanCuenta pc  where pc.planCuentaPadre is null and pc.empresa.id="+empresa.getId();
			System.out.println("Query PlanCuenta: "+query);
			return em.createQuery(query).getResultList();
		}catch(Exception e){
			return new ArrayList<PlanCuenta>();
		}
	}

	@SuppressWarnings("unchecked")
	public List<PlanCuenta> findCuentasHijas(PlanCuenta root, Empresa empresa){
		try{
			String query = "SELECT  pc FROM PlanCuenta pc where pc.planCuentaPadre="+root.getId()+" and  pc.empresa.id="+empresa.getId()+" order by pc.id asc";
			System.out.println("Query PlanCuenta: "+query);
			return em.createQuery(query).getResultList();
		}catch(Exception e){
			return new ArrayList<PlanCuenta>();
		}
	}

	@SuppressWarnings("unchecked")
	public List<PlanCuenta> getCuentasByNivelAndEmpresa(int nivel, Empresa empresa){
		try{
			String query = "SELECT  pc FROM PlanCuenta pc where pc.nivel.id="+nivel +" and pc.empresa.id="+empresa.getId()+" order by pc.id asc";
			System.out.println("Query PlanCuenta: "+query);
			return em.createQuery(query).getResultList();
		}catch(Exception e){
			System.out.println("getCuentasByNivelAndEmpresa("+nivel+","+empresa.getRazonSocial()+")  ->  error: "+e.getMessage());
			return new ArrayList<PlanCuenta>();
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<PlanCuenta> getCuentasAuxiliaresBalanceGeneral( Empresa empresa,Gestion gestion){
		try{
			String query = "select pc from PlanCuenta pc,Empresa em,TipoCuenta tcu where pc.empresa.id = em.id and em.id = "+empresa.getId()+" and pc.tipoCuenta.id = tcu.id and ( tcu.nombre='ACTIVO' or tcu.nombre='PASIVO' or tcu.nombre='PATRIMONIO' ) order by pc.id asc";
			//String query = "SELECT  pc FROM PlanCuenta pc where pc.nivel.id="+" and pc.empresa.id="+empresa.getId()+" order by pc.id asc";
			System.out.println("Query PlanCuenta: "+query);
			return em.createQuery(query).getResultList();
		}catch(Exception e){
			System.out.println("getCuentasByNivelAndEmpresa("+","+empresa.getRazonSocial()+")  ->  error: "+e.getMessage());
			return new ArrayList<PlanCuenta>();
		}
	}

	@SuppressWarnings("unchecked")
	public int obtenerCodigoParaNuevaCuenta(Empresa empresa){
		List<PlanCuenta> listPlanCuenta = new ArrayList<PlanCuenta>();
		try{
			String query = "SELECT  pc FROM PlanCuenta pc where pc.planCuentaPadre.id is null and pc.empresa.id="+empresa.getId();
			System.out.println("Query PlanCuenta: "+query);
			listPlanCuenta =  em.createQuery(query).getResultList();
		}catch(Exception e){
			System.out.println("");
		}
		return listPlanCuenta.size() + 1;
	}

}
