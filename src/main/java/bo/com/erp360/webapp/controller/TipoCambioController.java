package bo.com.erp360.webapp.controller;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.context.RequestContext;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.ScheduleEntryMoveEvent;
import org.primefaces.event.ScheduleEntryResizeEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.TipoCambioRepository;
import bo.com.erp360.webapp.data.TipoCambioUfvRepository;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.TipoCambio;
import bo.com.erp360.webapp.model.TipoCambioUfv;
import bo.com.erp360.webapp.service.TipoCambioRegistration;
import bo.com.erp360.webapp.service.TipoCambioUfvRegistration;
import bo.com.erp360.webapp.util.EDFechaTipoCambio;
import bo.com.erp360.webapp.util.EDTipoCambio;
import bo.com.erp360.webapp.util.Fechas;
import bo.com.erp360.webapp.util.SessionMain;

@Named(value = "tipoCambioController")
@ConversationScoped
public class TipoCambioController implements Serializable {

	private static final long serialVersionUID = 4518856859581066416L;

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	@Inject
	private TipoCambioUfvRepository tipoCambioUfvRepository;

	@Inject
	private TipoCambioRepository tipoCambioRepository;

	@Inject
	private TipoCambioRegistration tipoCambioRegistration;

	@Inject
	private TipoCambioUfvRegistration tipoCambioUfvRegistration;

	private ScheduleModel eventModel;
	private ScheduleEvent event = new DefaultScheduleEvent();

	private Logger log = Logger.getLogger(this.getClass());
	
	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventSucursal;

	//estados
	private boolean modificar = false;

	private String tituloPanel = "Registrar Tipo de Cambio";

	// login
	private String nombreUsuario;
	private @Inject SessionMain sessionMain; //variable del login
	private Empresa empresaLogin;
	private Gestion gestionLogin;

	private TipoCambio newTipoCambio;
	private EDTipoCambio EDTipoCambio;

	private List<EDTipoCambio> listEDTipoCambio = new ArrayList<EDTipoCambio>();
	List<EDFechaTipoCambio> listEDFechaTipoCambio = new ArrayList<EDFechaTipoCambio>();
	private List<TipoCambio> listTipoCambio = new ArrayList<TipoCambio>();

	@PostConstruct
	public void initNewTipoCambio() {

		beginConversation();
		nombreUsuario = sessionMain.getUsuarioLoggin().getLogin();
		empresaLogin = sessionMain.getEmpresaLoggin();
		gestionLogin = sessionMain.getGestionLoggin();

		// tituloPanel
		tituloPanel = "Centro Costo";

		setModificar(false);
		listTipoCambio = tipoCambioRepository.findAllByEmpresaAndGEstionRegistrados(empresaLogin,gestionLogin);
		newTipoCambio = new TipoCambio();
		TipoCambio tc = tipoCambioRepository.findTipoCambioDiaAnterior(empresaLogin, new Date());
		newTipoCambio.setUnidad(tc!=null?tc.getUnidad():0);
		cargarFechas();
	}

	private void cargarListTCNoRegistrados(){

	}

	private List<TipoCambio> obtenerListaTCRegistrados(){
		List<TipoCambio> listTipoCambio = tipoCambioRepository.findAllByEmpresaAndGEstionRegistrados(empresaLogin, gestionLogin);
		return null;
	}

	private List<TipoCambioUfv> obtenerListaTCUFVRegistrados(){
		List<TipoCambioUfv> listTipoCambioUfv = tipoCambioUfvRepository.findAllByEmpresaAndGEstionRegistrados(empresaLogin, gestionLogin);
		return null;
	}

	public void verificarRegisterTipoCambioDiario(){
		listEDFechaTipoCambio = new ArrayList<EDFechaTipoCambio>();
		log.info("verificarRegisterTipoCambioDiario()");
		//verificar si creo tipo de cambio para fehca actual
		TipoCambio tipoCambio = tipoCambioRepository.findAllByEmpresaAndFecha(empresaLogin, new Date());
		if( tipoCambio == null ){
			log.info("tipoCambio es null");
			//obtener el ultimo registro de tipo de cambio
			/*TipoCambio tipoCambio2 = tipoCambioRepository.findUltimoRegistroTipoCambio(empresaLogin, gestionLogin);*/
	
			TipoCambio tipoCambio2= tipoCambioRepository.findUltimoRegistroTipoCambio(empresaLogin, gestionLogin);
			/*List<TipoCambio> listTipoCambio2= tipoCambioRepository.findUltimoRegistroTipoCambio2(empresaLogin, gestionLogin);*/
			
			if(tipoCambio2!=null){
				/*tipoCambio2= listTipoCambio2.get(0);*/
				Date fechaUltima = tipoCambio2.getFecha();
				log.info("tipoCambio fechaUltima: "+fechaUltima);
				//comparar fechas(ultima fecha de registro con la fecha actual)
				int difDias = Fechas.diferenciasDeFechas(fechaUltima, new Date()); 
				log.info("tipoCambio difDias: "+difDias);
				if (difDias>0) {					
					EDFechaTipoCambio edTC = new EDFechaTipoCambio(new Date(), 6.92, 2.3);
					listEDFechaTipoCambio.add(edTC);
				}
				/*for(int i=1; i <= difDias;i++){
					Date fechaAux = Fechas.sumarFechaDia(fechaUltima, i);
					EDFechaTipoCambio edTC = new EDFechaTipoCambio(fechaAux, 6.92, 2.3);
					listEDFechaTipoCambio.add(edTC);						
				}*/
				log.info("listEDFechaTipoCambio1: "+listEDFechaTipoCambio.size());
				RequestContext context = RequestContext.getCurrentInstance();
				context.update("formDlgTipoCambio");
				context.execute("PF('dlgTipoCambio').show();");
			}else{
				//no tiene ni un registro de la gestion actual
				//(posiblemente es 1 de enero)
				int year=gestionLogin.getGestion();
				Calendar fecha1 = new GregorianCalendar(year, 1, 1);
				int difDias = Fechas.diferenciasDeFechas(fecha1.getTime(), new Date());
				log.info("Diferencia de Dias : "+difDias);
				Date fechaUltima2 = fecha1.getTime();
				if (difDias>0) {
					EDFechaTipoCambio edTC = new EDFechaTipoCambio(new Date(), 6.92, 2.3);
					listEDFechaTipoCambio.add(edTC);
					/*for(int i=1; i <= difDias;i++){
						Date fechaAux = Fechas.sumarFechaDia(fechaUltima2, i);
						EDFechaTipoCambio edTC = new EDFechaTipoCambio(fechaAux, 6.92, 2.3);
						listEDFechaTipoCambio.add(edTC);						
					}*/
				}else{
					Date fechaAux = new Date();
					EDFechaTipoCambio edTC = new EDFechaTipoCambio(fechaAux, 6.92, 2.3);
					listEDFechaTipoCambio.add(edTC);
				}
				
				log.info("listEDFechaTipoCambio2: "+listEDFechaTipoCambio.size());
				RequestContext context = RequestContext.getCurrentInstance();
				context.update("formDlgTipoCambio");
				context.execute("PF('dlgTipoCambio').show();");
			}
		}	
		//		RequestContext context = RequestContext.getCurrentInstance();
		//		context.execute("PF('dlgTipoCambio').show();");
	}


	public TipoCambio obtenerTipoCambioActual(){
		log.info("obtenerTipoCambioActual()");
		TipoCambio tipoCambio = tipoCambioRepository.findAllByEmpresaAndFecha(empresaLogin, new Date());
		if( tipoCambio != null ){
			log.info("tipoCambio: "+tipoCambio.getUnidad());
			return tipoCambio;
		}
		tipoCambio = new TipoCambio();
		tipoCambio.setUnidad(0);
		return tipoCambio;
	}

	private void cargarFechas(){
		eventModel = new DefaultScheduleModel();
		for(TipoCambio tc: listTipoCambio){
			eventModel.addEvent(new DefaultScheduleEvent(""+tc.getUnidad(), tc.getFecha(), tc.getFecha(), tc));
		}
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
	
	private String obtenerLiteralFecha(Date fechaActual){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(fechaActual);     
		String year = new SimpleDateFormat("yyyy").format(new Date());
		Integer month = Integer.parseInt(new SimpleDateFormat("MM").format(new Date()).toString());
		String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
		return year+"-"+month+"-"+day;
	}

	public void registrar(){
		try{

			for(EDFechaTipoCambio eDTC: listEDFechaTipoCambio){

				TipoCambio newTipoCambio = new TipoCambio();
				TipoCambioUfv newTipoCambioUfv = new TipoCambioUfv();
				Date fecha = eDTC.getFecha();

				newTipoCambio.setUnidad(eDTC.getTipoCambio());
				newTipoCambio.setFecha(fecha);
				newTipoCambio.setEmpresa(empresaLogin);
				newTipoCambio.setEstado("AC");
				newTipoCambio.setFechaLiteral(obtenerLiteralFecha(fecha));
				newTipoCambio = tipoCambioRegistration.create(newTipoCambio);

				newTipoCambioUfv.setUnidad(eDTC.getTipoCambioUFV());
				newTipoCambioUfv.setFecha(fecha);
				newTipoCambioUfv.setEmpresa(empresaLogin);
				newTipoCambioUfv.setEstado("AC");
				newTipoCambioUfv.setFechaLiteral(obtenerLiteralFecha(fecha));
				tipoCambioUfvRegistration.create(newTipoCambioUfv);
			}
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Tipo de cambio Registrado!", "");
			facesContext.addMessage(null, m);
			//initNewTipoCambio();
		}catch(Exception e){
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Registro Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	public void modificar(){
		try{
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Tipo de cambio Modificado!", "");
			facesContext.addMessage(null, m);
			initNewTipoCambio();
		}catch(Exception e){
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Registro Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	public void eliminar(){
		try{
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Tipo de cambio Eliminado!", "");
			facesContext.addMessage(null, m);
			initNewTipoCambio();
		}catch(Exception e){
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Registro Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	private String getRootErrorMessage(Exception e) {
		String errorMessage = "Registration failed. See server log for more information";
		if (e == null) {
			return errorMessage;
		}
		Throwable t = e;
		while (t != null) {
			errorMessage = t.getLocalizedMessage();
			t = t.getCause();
		}
		return errorMessage;
	}

	public void actualizarFormReg(){
		this.setModificar(false);
	}

	public ScheduleModel getEventModel() {
		return eventModel;
	}

	public ScheduleEvent getEvent() {
		return event;
	}

	public void setEvent(ScheduleEvent event) {
		this.event = event;
	}

	public void addEvent(ActionEvent actionEvent) {
		Date date = event.getStartDate();
		double unidad = Double.parseDouble(event.getTitle());
		TipoCambio tipoCambio = new TipoCambio();
		tipoCambio.setFecha(date);
		tipoCambio.setUnidad(unidad);
		if(event.getId() == null){
			try{
				tipoCambio.setEmpresa(empresaLogin);
				tipoCambio.setEstado("AC");
				tipoCambio = tipoCambioRegistration.create(tipoCambio);
				listTipoCambio.add(tipoCambio);
				eventModel.addEvent(event);
			}catch(Exception e){
				System.out.println("addEvent - error : "+e.getMessage());
			}
		}else{
			try{
				tipoCambio = listTipoCambio.get(listTipoCambio.indexOf((TipoCambio)event.getData()));
				tipoCambio.setUnidad(unidad);
				tipoCambioRegistration.update(tipoCambio);
				eventModel.updateEvent(event);
			}catch(Exception e){
				System.out.println("addEvent - else - error : "+e.getMessage());
			}
		}
		event = new DefaultScheduleEvent();
	}

	public void onEventSelect(SelectEvent selectEvent) {
		event = (ScheduleEvent) selectEvent.getObject();
	}

	public void onDateSelect(SelectEvent selectEvent) {
		event = new DefaultScheduleEvent("", (Date) selectEvent.getObject(), (Date) selectEvent.getObject());
	}

	public void onEventMove(ScheduleEntryMoveEvent event) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Event moved", "Day delta:" + event.getDayDelta() + ", Minute delta:" + event.getMinuteDelta());

		addMessage(message);
	}

	public void onEventResize(ScheduleEntryResizeEvent event) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Event resized", "Day delta:" + event.getDayDelta() + ", Minute delta:" + event.getMinuteDelta());

		addMessage(message);
	}

	private void addMessage(FacesMessage message) {
		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	public void onCellEdit(CellEditEvent event) {
		Object oldValue = event.getOldValue();
		Object newValue = event.getNewValue();

		if(newValue != null && !newValue.equals(oldValue)) {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Cell Changed", "Old: " + oldValue + ", New:" + newValue);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}

	// ---------    get and set  -------
	@Produces
	@Named
	public List<TipoCambio> getListTipoCambio() {
		return listTipoCambio;
	}

	public String getTituloPanel() {
		return tituloPanel;
	}

	public void setTituloPanel(String tituloPanel) {
		this.tituloPanel = tituloPanel;
	}

	public boolean isModificar() {
		return modificar;
	}

	public void setModificar(boolean modificar) {
		this.modificar = modificar;
	}

	public TipoCambio getNewTipoCambio() {
		return newTipoCambio;
	}

	public void setNewTipoCambio(TipoCambio newTipoCambio) {
		this.newTipoCambio = newTipoCambio;
	}

	public EDTipoCambio getEDTipoCambio() {
		return EDTipoCambio;
	}

	public void setEDTipoCambio(EDTipoCambio eDTipoCambio) {
		EDTipoCambio = eDTipoCambio;
	}

	public List<EDTipoCambio> getListEDTipoCambio() {
		return listEDTipoCambio;
	}

	public void setListEDTipoCambio(List<EDTipoCambio> listEDTipoCambio) {
		this.listEDTipoCambio = listEDTipoCambio;
	}

	public List<EDFechaTipoCambio> getListEDFechaTipoCambio() {
		return listEDFechaTipoCambio;
	}

	public void setListEDFechaTipoCambio(List<EDFechaTipoCambio> listEDFechaTipoCambio) {
		this.listEDFechaTipoCambio = listEDFechaTipoCambio;
	}

}
