package bo.com.erp360.webapp.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
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

import org.primefaces.event.ScheduleEntryMoveEvent;
import org.primefaces.event.ScheduleEntryResizeEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.TipoCambioUfvRepository;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.TipoCambioUfv;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.service.TipoCambioUfvRegistration;
import bo.com.erp360.webapp.util.SessionMain;

@Named(value = "tipoCambioUfvController")
@ConversationScoped
public class TipoCambioUfvController implements Serializable {

	private static final long serialVersionUID = -7819149623543804669L;

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	@Inject
	private TipoCambioUfvRepository tipoCambioUfvRepository;

	@Inject
	private TipoCambioUfvRegistration tipoCambioUfvRegistration;
	
	
	private Logger log = Logger.getLogger(this.getClass());

	//login
	private @Inject SessionMain sessionMain; //variable del login
	private Empresa empresaLogin;
	private Gestion gestionLogin;
	private Usuario usuarioSession;
	private String nombreUsuario;

	private ScheduleModel eventModel;
	private ScheduleEvent event = new DefaultScheduleEvent();

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventSucursal;

	//estados
	private boolean modificar = false;

	private String tituloPanel = "Registrar Tipo de Cambio Ufv";
	
	private List<TipoCambioUfv> listTipoCambioUfv = new ArrayList<TipoCambioUfv>();

	@PostConstruct
	public void initNewTipoCambioUfv() {

		beginConversation();
		nombreUsuario = sessionMain.getUsuarioLoggin().getLogin();
		empresaLogin = sessionMain.getEmpresaLoggin();
		gestionLogin = sessionMain.getGestionLoggin();

		// tituloPanel
		tituloPanel = "Centro Costo";
		setModificar(false);
		listTipoCambioUfv = tipoCambioUfvRepository.findAllByEmpresa(empresaLogin);	
		cargarFechas();
	}

	private void cargarFechas(){
		eventModel = new DefaultScheduleModel();
		for(TipoCambioUfv tc: listTipoCambioUfv){
			eventModel.addEvent(new DefaultScheduleEvent(""+tc.getUnidad(), tc.getFecha(), tc.getFecha(), tc));
		}
	}

	public void obtenerUsuarios(){
		try {
			log.info("Ingreso a obtenerUsuarios");
			//listUsuario = usuarioRepository.traerUsuariosPorSucursal(almacenSucursal.getSucursal());
		} catch (Exception e) {
			System.err.println("Error en obtenerUsuarios : "+e.getMessage());
			e.getStackTrace();
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

	public void registrar(){
		try{
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					" Registrado!", ""+"!");
			facesContext.addMessage(null, m);
			initNewTipoCambioUfv();
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
					" Registrado!", ""+"!");
			facesContext.addMessage(null, m);
			initNewTipoCambioUfv();
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
					"Registrado!", ""+"!");
			facesContext.addMessage(null, m);
			initNewTipoCambioUfv();
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
		TipoCambioUfv tipoCambioUfv = new TipoCambioUfv();
		tipoCambioUfv.setFecha(date);
		tipoCambioUfv.setUnidad(unidad);
		if(event.getId() == null){
			try{
				tipoCambioUfv.setEmpresa(empresaLogin);
				tipoCambioUfv.setEstado("AC");
				tipoCambioUfv = tipoCambioUfvRegistration.create(tipoCambioUfv);
				listTipoCambioUfv.add(tipoCambioUfv);
				eventModel.addEvent(event);
			}catch(Exception e){
				log.error("addEvent() -> "+e.getMessage());
			}
		}else{
			try{
				tipoCambioUfv = listTipoCambioUfv.get(listTipoCambioUfv.indexOf((TipoCambioUfv)event.getData()));
				tipoCambioUfv.setUnidad(unidad);
				tipoCambioUfvRegistration.update(tipoCambioUfv);
				eventModel.updateEvent(event);
			}catch(Exception e){
				log.error("addEvent - else - error : "+e.getMessage());
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

	// ---------    get and set  -------
	@Produces
	@Named
	public List<TipoCambioUfv> getListTipoCambioUfv() {
		return listTipoCambioUfv;
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

}
