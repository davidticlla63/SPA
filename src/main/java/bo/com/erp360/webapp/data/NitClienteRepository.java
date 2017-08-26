package bo.com.erp360.webapp.data;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.Cliente;
import bo.com.erp360.webapp.model.NitCliente;

@Stateless
public class NitClienteRepository {

	@Inject
	private EntityManager em;

	public NitCliente findById(int id) {
		return em.find(NitCliente.class, id);
	}

	@SuppressWarnings("unchecked")
	public List<NitCliente> findClienteAllByNit(String nit) {
		String query = "select em from NitCliente em  where em.cliente.estado='AC' and em.estado='AC' and em.nit like '%"
				+ nit + "%' order by em.id asc";
		System.out.println("Query NitCliente: " + query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public boolean ExistNit(Cliente cliente,String nit) {
		String query = "select em from NitCliente em  where em.cliente.estado='AC' and em.estado='AC' and em.cliente.id="+cliente.getId()+" and em.nit like '"
				+ nit + "'";
		System.out.println("Query NitCliente: " + query);
		return em.createQuery(query).getResultList().size() > 0;
	}
	
	@SuppressWarnings("unchecked")
	public NitCliente findNitClienteNit(Cliente cliente,String nit) {
		String query = "select em from NitCliente em  where em.cliente.estado='AC' and em.estado='AC' and em.cliente.id="+cliente.getId()+"  and em.nit like '"
				+ nit + "'";
		System.out.println("Query NitCliente: " + query);
		return (NitCliente) em.createQuery(query).getSingleResult();
	}


	@SuppressWarnings("unchecked")
	public List<NitCliente> findNitClienteAllByEmpresa(Cliente cliente) {
		String query = "select em from NitCliente em  where em.cliente.estado='AC' and ( em.estado='AC' or em.estado='IN') and em.cliente.id="
				+ cliente.getId() + " order by em.id asc";
		System.out.println("Query NitCliente: " + query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<NitCliente> findNitClienteAllActivasByCliente(Cliente cliente) {
		String query = "select em from NitCliente em  where em.cliente.estado='AC' and em.estado='AC' and em.cliente.id="
				+ cliente.getId() + " order by em.id asc";
		System.out.println("Query NitCliente: " + query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<NitCliente> findNitClienteAllActivasByNombreCliente(
			String nombrecliente) {
		String query = "select em from NitCliente em  where em.cliente.estado='AC' and em.estado='AC'  and em.cliente.nombre like '%"
				+ nombrecliente + "%' order by em.id asc";
		System.out.println("Query NitCliente: " + query);
		return em.createQuery(query).getResultList();
	}

}
