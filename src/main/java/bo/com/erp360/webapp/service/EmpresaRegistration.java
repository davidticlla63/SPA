package bo.com.erp360.webapp.service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import javax.ejb.Stateless;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.MonedaEmpresa;
import bo.com.erp360.webapp.model.Nivel;
import bo.com.erp360.webapp.model.PlanCuenta;
import bo.com.erp360.webapp.model.TipoCuenta;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.util.PlanCuentaUtil;

//The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateless
public class EmpresaRegistration extends DataAccessService<Empresa>{

	Logger log = Logger.getLogger(EmpresaRegistration.class);

	private Empresa empresa;
	private Usuario usuario;

	private List<Nivel> listNivel = new ArrayList<Nivel>();

	private List<PlanCuenta> listPlanCuenta = new ArrayList<PlanCuenta>();

	private MonedaEmpresa monedaNacional ;
	private MonedaEmpresa monedaExtranjera;
	@Inject
	private PlanCuentaRegistration planCuentaRegistration;

	private List<TipoCuenta> listTipoCuenta = new ArrayList<TipoCuenta>();

	private static final String  RELATIVE_WEB_PATH= "/resources/file/PC_01.txt";

	public EmpresaRegistration(){
		super(Empresa.class);
	}

	public void cargarPlanCuentaDesdeArchivo(List<TipoCuenta> listTipoCuenta,List<Nivel> listNivel ,Empresa empresa,Usuario usuario,MonedaEmpresa monedaNacional,MonedaEmpresa monedaExtranjera){
		this.listTipoCuenta = listTipoCuenta ;
		this.listNivel = listNivel; 
		this.monedaNacional = monedaNacional;
		this.monedaExtranjera = monedaExtranjera;
		this.empresa = empresa;
		this.usuario = usuario;
		ServletContext servletContext = (ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext();
		String absoluteDiskPath = servletContext.getRealPath(RELATIVE_WEB_PATH);
		try{
			cargarPlanCuenta(absoluteDiskPath);
		}catch(Exception e){
			log.error("cargarPlanCuentaDesdeArchivo() -> error: "+e.getMessage());
		}
	}

	void cargarPlanCuenta(String path ) throws FileNotFoundException, IOException {

		listPlanCuenta = new ArrayList<PlanCuenta>();
		cargarTamanioDigitos("9.9.9.99.999");
		String cadena = "";
		Date fecha = new Date();
		FileReader f = new FileReader(path);
		BufferedReader b = new BufferedReader(f);
		try{
			while((cadena = b.readLine())!=null) {
				PlanCuenta pc = new PlanCuenta();
				// id codigo  descripcion clase ufv tipoCuenta planCuentaPadre moneda tipoRegistro correlativo1 correlativo2 empresa nivel fecha estado usuarioRegistro
				// CODIGO|DESCRIPCION|CLASE|TIPO|MONEDA|PADRE|NIVEL|

				String codigo = obtenerColumna(1,cadena);
				String descripcion = obtenerColumna(2,cadena);
				String clase = obtenerColumna(3,cadena);
				String tipo = obtenerColumna(4,cadena);
				String moneda = obtenerColumna(5,cadena);
				String padre = obtenerColumna(6,cadena);
				String nivel = obtenerColumna(7,cadena);
				//log.info("codigo="+codigo+" | descripcion="+descripcion+" | clase="+clase+" | tipo="+tipo+" | moneda="+moneda+" | padre="+padre+" | nivel="+nivel);
				log.info("-----------------------------");
				Nivel nivelAux = obtenerNivel(Integer.valueOf(nivel));
				log.info("0");
				PlanCuenta pcPadre = obtenerPlaCuentaPadreByCodigoLocal(padre);
				log.info("1");
				MonedaEmpresa me = obtenerMoneda(moneda);
				log.info("2");
				pc.setDescripcion(descripcion);
				log.info("getDescripcion="+pc.getDescripcion());
				pc.setClase(clase);
				log.info("getClase="+pc.getClase());
				pc.setUfv("NO");
				pc.setTipoCuenta(obtenerTipoCuenta(tipo));
				pc.setPlanCuentaPadre(pcPadre);
				log.info("getPlanCuentaPadre="+pcPadre);
				pc.setMonedaEmpresa(me);
				log.info("monedaEmpresa="+ me);
				pc.setEmpresa(empresa);
				pc.setNivel(nivelAux);
				log.info("nivel="+nivelAux);
				pc.setFecha(fecha);
				pc.setEstado("AC");
				pc.setUsuarioRegistro(usuario.getLogin());
				pc.setCodigo(PlanCuentaUtil.llenarDelanteConCeroCodificacion(codigo,listTamanio.get(nivelAux.getNivel()-1)));
				log.info("getCodigo="+pc.getCodigo());
				pc.setCodigoAuxiliar(PlanCuentaUtil.llenarDespuesConCeroCodificacion(codigo,listTamanio.get(nivelAux.getNivel()-1),listTamanio));

				log.info("getCodigoAuxiliar="+pc.getCodigoAuxiliar());

				log.info("create");
				pc = planCuentaRegistration.create(pc);
				log.info("-----------------------------");
				listPlanCuenta.add(pc);
			}
			log.info("REGISTRO CORRECTO PLAN DE CUENTA TOTAL ITEMS: "+listPlanCuenta.size());

		}catch(Exception e){
			log.error("cargarPlanCuenta -> error: "+e.getMessage());
		}finally{
			b.close();
		}
	}

	private List<Integer> listTamanio = new ArrayList<>();

	private void cargarTamanioDigitos(String codigo){
		int anterior = 0;
		for(int i=0;i<codigo.length();i++){
			String letra = String.valueOf(codigo.charAt(i));
			if(letra.equals(".")){
				String numeroString = codigo.substring(anterior, i);
				int numero = 1;
				for(int j=1 ; j < numeroString.length(); j++){
					numero = numero + 1;
				}
				listTamanio.add(numero);
				anterior = i + 1;
			}
		}

		String numeroString = codigo.substring(anterior, codigo.length());
		int numero = 1;
		for(int j=1 ; j < numeroString.length(); j++){
			numero = numero + 1;
		}
		listTamanio.add(numero);
	}

	public List<PlanCuenta> obtenerPlanCuentaDefault(){
		List<PlanCuenta> listPlanCuenta = new ArrayList<PlanCuenta>();
		try{
			ServletContext servletContext = (ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext();
			String absoluteDiskPath = servletContext.getRealPath(RELATIVE_WEB_PATH);
			String cadena;
			FileReader f = new FileReader(absoluteDiskPath);
			BufferedReader b = new BufferedReader(f);
			while((cadena = b.readLine())!=null) {
				PlanCuenta pc = new PlanCuenta();
				// id codigo  descripcion clase ufv tipoCuenta planCuentaPadre moneda tipoRegistro correlativo1 correlativo2 empresa nivel fecha estado usuarioRegistro
				// CODIGO|DESCRIPCION|CLASE|TIPO|MONEDA|PADRE|NIVEL|
				//				log.info(obtenerColumna(1,cadena)+" "+obtenerColumna(2,cadena)+" "+obtenerColumna(3,cadena)+" "+obtenerColumna(4,cadena)+" "+obtenerColumna(5,cadena)
				//						+" "+obtenerColumna(6,cadena)+" "+obtenerColumna(7,cadena));
				pc.setCodigo(obtenerColumna(1,cadena));
				pc.setDescripcion(obtenerColumna(2,cadena));
				pc.setClase(obtenerColumna(3,cadena));
				pc.setUfv("NO");
				//pc.setTipoCuenta(obtenerTipoCuenta(obtenerColumna(4,cadena)));
				PlanCuenta padre = new PlanCuenta();
				padre.setCodigo(obtenerColumna(6,cadena));

				pc.setPlanCuentaPadre( padre.getCodigo().equals("null")?null:padre);
				//pc.setMonedaEmpresa(obtenerMoneda(obtenerColumna(5,cadena)));
				pc.setEmpresa(empresa);
				//pc.setNivel(obtenerNivel(Integer.valueOf(obtenerColumna(7, cadena))));
				//pc.setFecha(fecha);
				pc.setEstado("AC");
				//pc.setUsuarioRegistro(usuario.getLogin());
				listPlanCuenta.add(pc);
			}
			b.close();
		}catch(Exception e){
			log.error("cargarPlanCuenta -> error: "+e.getMessage());
		}
		return listPlanCuenta;
	}

	private String obtenerColumna(int nroColumna, String cadena){
		String outPut = "";
		int anterior= 0; int actual = 0; int contColumn = 0;
		for(int index= 0;index < cadena.length();index++){
			String letra = String.valueOf(cadena.charAt(index));
			if(letra.equals("|")){
				anterior = actual; 	actual = index; contColumn ++;
				if(nroColumna==1){return  cadena.substring(anterior, actual);}
				if(contColumn==nroColumna){	return  cadena.substring(anterior+1, actual);}
			}			
		}
		return outPut;
	}

	private Nivel obtenerNivel(int parmNivel){
		for(Nivel nivel: listNivel){
			if(nivel.getNivel()==parmNivel){
				return nivel;
			}
		}
		return null;
	}

	private TipoCuenta obtenerTipoCuenta(String tipoCuenta){
		for(TipoCuenta tc : listTipoCuenta){
			if(tc.getNombre().equals(tipoCuenta)){
				return tc;
			}
		}
		return null;
	}

	private MonedaEmpresa obtenerMoneda(String moneda){
		MonedaEmpresa output = null;
		switch (moneda) {
		case "NACIONAL":
			output = monedaNacional;
			break;
		case "EXTRANJERA":
			output = monedaExtranjera;
			break;
		default:
			break;
		}
		return output;
	}

	private PlanCuenta obtenerPlaCuentaPadreByCodigoLocal(String codigo){
		if( ! codigo.equals("null")){
			for(PlanCuenta pc :listPlanCuenta){
				if(pc.getCodigo().equals(codigo)){
					return pc;
				}
			}
		}
		return null;
	}

}

