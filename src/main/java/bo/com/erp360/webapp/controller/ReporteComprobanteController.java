package bo.com.erp360.webapp.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.event.Event;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.primefaces.component.datatable.DataTable;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.AsientoContableRepository;
import bo.com.erp360.webapp.data.ComprobanteRepository;
import bo.com.erp360.webapp.data.SucursalRepository;
import bo.com.erp360.webapp.data.TipoComprobanteRepository;
import bo.com.erp360.webapp.model.AsientoContable;
import bo.com.erp360.webapp.model.CentroCosto;
import bo.com.erp360.webapp.model.Comprobante;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.Sucursal;
import bo.com.erp360.webapp.model.TipoComprobante;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.service.ComprobanteRegistration;
import bo.com.erp360.webapp.util.EDMes;
import bo.com.erp360.webapp.util.SessionMain;

@Named(value = "reporteComprobanteController")
@ConversationScoped
public class ReporteComprobanteController implements Serializable {

	private static final long serialVersionUID = 6009642192297739324L;

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	@Inject
	private ComprobanteRepository comprobanteRepository;

	@Inject
	private ComprobanteRegistration comprobanteRegistration;

	@Inject
	private SucursalRepository sucursalRepository;

	@Inject
	private TipoComprobanteRepository tipoComprobanteRepository;

	@Inject
	private AsientoContableRepository asientoContableRepository;

	private Logger log = Logger.getLogger(this.getClass());

	//login
	private String nombreUsuario;
	private @Inject SessionMain sessionMain; //variable del login
	private Empresa empresaLogin;
	private Gestion gestionLogin;

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventSucursal;

	private String nombreTipoComprobante;
	private String nombreSucursal;
	private String nombreMes;
	private String urlComprobante;
	private String titleDialogAccionComprobante;
	private String textoTipoAccionComprobante;
	private String coulumTable ;

	//list
	private List<AsientoContable> listAsientoContable = new ArrayList<AsientoContable>();
	private List<Comprobante> listComprobante= new ArrayList<Comprobante>();
	private List<Comprobante> listFilterComprobante = new ArrayList<Comprobante>();
	private List<TipoComprobante> listTipoComprobante;
	private List<Sucursal> listSucursal;
	private List<CentroCosto> listCentroCosto = new ArrayList<CentroCosto>();
	private String[] arrayMes = {"ENERO","FEBRERO","MARZO","ABRIL","MAYO","JUNIO","JULIO","AGOSTO","SEPTIEMBRE","OCTUBRE","NOVIEMBRE","DICIEMBRE","TODO"};

	//object
	private Comprobante selectedComprobante;
	private AsientoContable selectedAsientoContable;
	private TipoComprobante selectedTipoComprobante;
	private Sucursal selectedSucursal;
	

	//estados
	private boolean seleccionado = false;
	private boolean crear = true;
	private boolean seleccionarComprobante = false;
	
	@PostConstruct
	public void initNewReporteComprobanteController() {

		log.info(" init new initNewReporteComprobanteController ");
		beginConversation();
		nombreUsuario = sessionMain.getUsuarioLoggin().getLogin();
		empresaLogin = sessionMain.getEmpresaLoggin();
		gestionLogin = sessionMain.getGestionLoggin();

		listTipoComprobante = tipoComprobanteRepository.findAllByEmpresa(empresaLogin);
		listSucursal = sucursalRepository.findAllByEmpresa(empresaLogin);

		loadValuesDefault();
	}

	private void loadValuesDefault(){
		seleccionado = false;
		crear = true;
		nombreMes = "TODO";
		coulumTable = "col-md-12";

		selectedComprobante = new Comprobante();
		listComprobante = comprobanteRepository.findAllByEmpresa(empresaLogin);
		if(listComprobante.size()>0){
			//seleccionarComprobante = true;
			//crear = true;
			//selectedComprobante = listComprobante.get(0);
			//listAsientoContable = asientoContableRepository.findByComprobante(selectedComprobante);
		}

		nombreTipoComprobante =  "TODO" ;// listTipoComprobante.size()> 0 ? listTipoComprobante.get(0).getNombre():"";
		selectedTipoComprobante = listTipoComprobante.size()> 0 ? listTipoComprobante.get(0):new TipoComprobante();

		nombreSucursal = "TODO" ;//listSucursal.size()>0?listSucursal.get(0).getNombre():"";
		selectedSucursal = listSucursal.size()>0?listSucursal.get(0):new Sucursal();
	}

	public void beginConversation() {
		if (conversation.isTransient()) {
			log.info("beginning conversation : " + this.conversation);
			conversation.begin();
			log.info("---> Init Conversation");
		}
	}

	public void endConversation() {
		if (!conversation.isTransient()) {
			conversation.end();
		}
	}

	///---------- metodos comprobante ----------------

	public void cargarAnularComprobante(){
		titleDialogAccionComprobante = "ANULAR COMPROBANTE";
		textoTipoAccionComprobante = "anular";
	}

	public void cargarEliminarComprobante(){
		titleDialogAccionComprobante = "ELIMINAR COMPROBANTE";
		textoTipoAccionComprobante = "eliminar";
	}

	public void eliminarComprobante(){
		try{
			selectedComprobante.setEstado("RM");
			comprobanteRegistration.update(selectedComprobante);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Comprobante Eliminado!", selectedComprobante.getCorrelativo());
			facesContext.addMessage(null, m);
			resetearFitrosTabla("formTableComprobante:dataTableComprobante");
			loadValuesDefault();
		}catch(Exception e){
			log.info("eliminarComprobante() ERROR: "+e.getMessage());
		}
	}

	public void anularComprobante(){
		try{
			selectedComprobante.setEstado("AN");
			comprobanteRegistration.update(selectedComprobante);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Comprobante Anulado!", selectedComprobante.getCorrelativo());
			facesContext.addMessage(null, m);
			resetearFitrosTabla("formTableComprobante:dataTableComprobante");
			loadValuesDefault();
		}catch(Exception e){
			log.info("anularComprobante() ERROR: "+e.getMessage());
		}
	}

	// ----- aciones de la pagina

	public void irAModificarComprobante(){
		//parametro de comprobante 'idC'
		String key = "idC";
		String value = String.valueOf(selectedComprobante.getId());
		sessionMain.removeAttributeSession("key");
		sessionMain.setAttributeSession(key, value);
	}
	
	public void irACopiarComprobante(){
		//parametro de comprobante 'idC'
		String key = "idCCopia";
		String value = String.valueOf(selectedComprobante.getId());
		sessionMain.removeAttributeSession("key");
		sessionMain.setAttributeSession(key, value);
	}
	
	public void irARevertirComprobante(){
		//parametro de comprobante 'idC'
		String key = "idCRev";
		String value = String.valueOf(selectedComprobante.getId());
		sessionMain.removeAttributeSession("key");
		sessionMain.setAttributeSession(key, value);
	}


	public void resetearFitrosTabla(String id) {
		DataTable table = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(id);
		table.setSelection(null);
		table.reset();
	}

	public void onRowSelect(SelectEvent event) {
		seleccionado = true ;		
	}

	public void onRowSelectComprobante(SelectEvent event){
		selectedAsientoContable = new AsientoContable();
		seleccionarComprobante = true;
		crear = true;
		coulumTable = "col-md-6";
		listAsientoContable = asientoContableRepository.findByComprobante(selectedComprobante);
		resetearFitrosTabla("formTableComprobante:dataTableComprobante");
	}
	
	public void onRowSelectedAsientoContable(SelectEvent event){
		
	}

	public void cancelar(){
		seleccionarComprobante = false;
		crear = true;
		coulumTable = "col-md-12";
		selectedComprobante = new Comprobante();
		resetearFitrosTabla("formTableComprobante:dataTableComprobante");
		listAsientoContable = new ArrayList<>();
		resetearFitrosTabla("formTableComprobante:dataTableDetalleComprobante");
	}

	public void actualizarForm(){
		seleccionado = false;
		seleccionarComprobante = false;
		crear = true;
		urlComprobante = loadURL();
		log.info("cargando......");
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("PF('dlgVistaPreviaComprobante').show();");
		resetearFitrosTabla("formTableComprobante:dataTableComprobante");
		loadValuesDefault();
	}

	// --------   reporte ----------

	public String loadURL(){
		try{
			HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();  
			String urlPath = request.getRequestURL().toString();
			urlPath = urlPath.substring(0, urlPath.length() - request.getRequestURI().length()) + request.getContextPath() + "/";
			String urlPDFreporte = urlPath+"ReporteComprobante?pMes="+(selectedComprobante.getFecha().getMonth()+1)+"&pGestion="+gestionLogin.getGestion()+"&pTipoComprobante="+selectedComprobante.getTipoComprobante().getId()+"&pSucursal="+selectedComprobante.getSucursal().getId()+"&pEmpresa="+empresaLogin.getId()+"&pNumeroComprobante="+selectedComprobante.getNumero();
			log.info("getURL() -> "+urlPDFreporte);
			return urlPDFreporte;
		}catch(Exception e){
			log.error("getURL error: "+e.getMessage());
			return "error";
		}
	}

	public void procesar(){
		if(nombreMes.equals("TODO") && nombreSucursal.equals("TODO") && nombreTipoComprobante.equals("TODO")){
			listComprobante = comprobanteRepository.findAllByEmpresaGestion(empresaLogin, gestionLogin);
		}else if(! nombreMes.equals("TODO") && nombreSucursal.equals("TODO") && ! nombreTipoComprobante.equals("TODO")){
			listComprobante = comprobanteRepository.findAllByEmpresaGestionTipoComprobanteMes(empresaLogin, gestionLogin, selectedTipoComprobante, (EDMes.getMesNumeral(nombreMes)+1));
		}else if(! nombreMes.equals("TODO") && nombreSucursal.equals("TODO") && nombreTipoComprobante.equals("TODO")){
			listComprobante = comprobanteRepository.findAllByEmpresaGestionMes(empresaLogin, gestionLogin, (EDMes.getMesNumeral(nombreMes)+1));
		}else if( nombreMes.equals("TODO") && ! nombreSucursal.equals("TODO") && nombreTipoComprobante.equals("TODO")){
			listComprobante = comprobanteRepository.findAllByEmpresaSucursalGestion(empresaLogin, selectedSucursal, gestionLogin);
		}else if( ! nombreMes.equals("TODO") && ! nombreSucursal.equals("TODO") && ! nombreTipoComprobante.equals("TODO")){
			listComprobante = comprobanteRepository.findAllByEmpresaSucursalGestionTipoComprobanteMes(empresaLogin, selectedSucursal, gestionLogin, selectedTipoComprobante, (EDMes.getMesNumeral(nombreMes)+1));
		}
	}

	// ---- get and set ---

	public String getNombreTipoComprobante() {
		return nombreTipoComprobante;
	}

	public void setNombreTipoComprobante(String nombreTipoComprobante) {
		this.nombreTipoComprobante = nombreTipoComprobante;
	}

	public String getNombreSucursal() {
		return nombreSucursal;
	}

	public void setNombreSucursal(String nombreSucursal) {
		this.nombreSucursal = nombreSucursal;
	}

	public List<TipoComprobante> getListTipoComprobante() {
		return listTipoComprobante;
	}

	public void setListTipoComprobante(List<TipoComprobante> listTipoComprobante) {
		this.listTipoComprobante = listTipoComprobante;
	}

	public List<Sucursal> getListSucursal() {
		return listSucursal;
	}

	public void setListSucursal(List<Sucursal> listSucursal) {
		this.listSucursal = listSucursal;
	}

	public List<CentroCosto> getListCentroCosto() {
		return listCentroCosto;
	}

	public void setListCentroCosto(List<CentroCosto> listCentroCosto) {
		this.listCentroCosto = listCentroCosto;
	}

	public List<Comprobante> getListComprobante() {
		return listComprobante;
	}

	public void setListComprobante(List<Comprobante> listComprobante) {
		this.listComprobante = listComprobante;
	}

	public String[] getArrayMes() {
		return arrayMes;
	}

	public void setArrayMes(String[] arrayMes) {
		this.arrayMes = arrayMes;
	}

	public boolean isSeleccionado() {
		return seleccionado;
	}

	public void setSeleccionado(boolean seleccionado) {
		this.seleccionado = seleccionado;
	}

	public String getNombreUsuario() {
		return nombreUsuario;
	}

	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}

	public String getNombreMes() {
		return nombreMes;
	}

	public void setNombreMes(String nombreMes) {
		this.nombreMes = nombreMes;
	}

	public Comprobante getSelectedComprobante() {
		return selectedComprobante;
	}

	public void setSelectedComprobante(Comprobante selectedComprobante) {
		this.selectedComprobante = selectedComprobante;
	}

	public String getUrlComprobante() {
		return urlComprobante;
	}

	public void setUrlComprobante(String urlComprobante) {
		this.urlComprobante = urlComprobante;
	}

	public List<Comprobante> getListFilterComprobante() {
		return listFilterComprobante;
	}

	public void setListFilterComprobante(List<Comprobante> listFilterComprobante) {
		this.listFilterComprobante = listFilterComprobante;
	}

	public boolean isSeleccionarComprobante() {
		return seleccionarComprobante;
	}

	public void setSeleccionarComprobante(boolean seleccionarComprobante) {
		this.seleccionarComprobante = seleccionarComprobante;
	}

	public String getTitleDialogAccionComprobante() {
		return titleDialogAccionComprobante;
	}

	public void setTitleDialogAccionComprobante(
			String titleDialogAccionComprobante) {
		this.titleDialogAccionComprobante = titleDialogAccionComprobante;
	}

	public String getTextoTipoAccionComprobante() {
		return textoTipoAccionComprobante;
	}

	public void setTextoTipoAccionComprobante(String textoTipoAccionComprobante) {
		this.textoTipoAccionComprobante = textoTipoAccionComprobante;
	}

	public boolean isCrear() {
		return crear;
	}

	public void setCrear(boolean crear) {
		this.crear = crear;
	}

	public List<AsientoContable> getListAsientoContable() {
		return listAsientoContable;
	}

	public void setListAsientoContable(List<AsientoContable> listAsientoContable) {
		this.listAsientoContable = listAsientoContable;
	}

	public AsientoContable getSelectedAsientoContable() {
		return selectedAsientoContable;
	}

	public void setSelectedAsientoContable(AsientoContable selectedAsientoContable) {
		this.selectedAsientoContable = selectedAsientoContable;
	}

	public String getCoulumTable() {
		return coulumTable;
	}

	public void setCoulumTable(String coulumTable) {
		this.coulumTable = coulumTable;
	}
}
