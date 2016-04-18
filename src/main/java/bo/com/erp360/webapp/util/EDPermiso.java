package bo.com.erp360.webapp.util;

import java.util.ArrayList;
import java.util.List;

import bo.com.erp360.webapp.model.Permiso;
import bo.com.erp360.webapp.model.Privilegio;

public class EDPermiso {

	private List<String[]> mListArrayPrivilegio = new ArrayList<String[]>();
	
	private boolean groupSeguridad;
	private boolean groupParametrizacion;
	private boolean groupProceso;
	private boolean groupFormulario;
	private boolean groupReporte;
	
	private boolean subGroupLibros;
	private boolean subGroupEstadoFinanciero;
	private boolean subGroupCuadroActivoFijo;

	// Constructor
	public EDPermiso() {
		super();		
	}

	//cargar la lista con todos los permisos por defectos
	public void loadPrivilegeDefaul(List<Permiso> listPermiso){
		for(Permiso p: listPermiso){
			String[] a = {p.getName().toString(),"IN","IN","IN"};
			mListArrayPrivilegio.add(a);
		}
		loadGroupAndSubGroup();
	}

	//activar todos los privilegios que tiene el usuario
	public void activePrivilegeOfUser(List<Privilegio> listPrivilege){
		for(Privilegio p : listPrivilege){
			modifyPrivilege(p.getPermiso().getName().toString());
		}		
	}
	
	//pone toda las lista de privilegios por defecto
	public void resetValuesArrayPrivilege(){
		for(int i=0; i < mListArrayPrivilegio.size() ; i++ ){
			String[] s = mListArrayPrivilegio.get(i);
			String[] newS = {s[0],"IN","IN","IN"};
			mListArrayPrivilegio.set(i, newS);
		}
	}
	
	//modifica un privilegio, si esta activo lo inactiva, y al contrario
	public void modifyPrivilege(String privilege){
		System.out.println("modifyPrivilege("+privilege+")");
		for(int index=0 ; index < mListArrayPrivilegio.size();index++){
			String[] s = mListArrayPrivilegio.get(index);
			if(s[0].toString().equals(privilege)){
				if(s[1].toString().equals("AC")){
					String[] newS = {s[0],"IN",s[2],s[3]}; 
					mListArrayPrivilegio.set(index, newS);
				}else{
					String[] newS = {s[0],"AC",s[2],s[3]}; 
					mListArrayPrivilegio.set(index, newS);
				}				
			}
		}
	}

	//verifica si el privilegio tiene permiso
	public boolean  existPrivilege(String privilege){
		for(String[] s: mListArrayPrivilegio ){
			if(s[0].equals(privilege)){
				if(s[1].toString().equals("AC")){ return true; } else{ return false; }				
			}
		}
		return false;
	}
	
	//verifica si el privilegio tiene permiso de lectura
	public boolean  existPrivilegeRead(String privilege){
		for(String[] s: mListArrayPrivilegio ){
			if(s[0].equals(privilege)){
				if(s[2].toString().equals("AC")){ return true; } else{ return false; }
			}
		}
		return false;
	}
	
	//verifica si el privilegio tiene permiso de escritura
	public boolean  existPrivilegeWrite(String privilege){
		for(String[] s: mListArrayPrivilegio ){
			if(s[0].equals(privilege)){
				if(s[3].toString().equals("AC")){ return true; } else{ return false; }
			}
		}
		return false;
	}
	
	private void loadGroupAndSubGroup(){
		// aqui se cargar los menu
	}
	
	public List<String[]> getmListArrayPrivilegio() {
		return mListArrayPrivilegio;
	}

	public void setmListArrayPrivilegio(List<String[]> mListArrayPrivilegio) {
		this.mListArrayPrivilegio = mListArrayPrivilegio;
	}

	public boolean isGroupSeguridad() {
		return groupSeguridad;
	}

	public void setGroupSeguridad(boolean groupSeguridad) {
		this.groupSeguridad = groupSeguridad;
	}

	public boolean isGroupParametrizacion() {
		return groupParametrizacion;
	}

	public void setGroupParametrizacion(boolean groupParametrizacion) {
		this.groupParametrizacion = groupParametrizacion;
	}

	public boolean isGroupProceso() {
		return groupProceso;
	}

	public void setGroupProceso(boolean groupProceso) {
		this.groupProceso = groupProceso;
	}

	public boolean isGroupFormulario() {
		return groupFormulario;
	}

	public void setGroupFormulario(boolean groupFormulario) {
		this.groupFormulario = groupFormulario;
	}

	public boolean isGroupReporte() {
		return groupReporte;
	}

	public void setGroupReporte(boolean groupReporte) {
		this.groupReporte = groupReporte;
	}

	public boolean isSubGroupLibros() {
		return subGroupLibros;
	}

	public void setSubGroupLibros(boolean subGroupLibros) {
		this.subGroupLibros = subGroupLibros;
	}

	public boolean isSubGroupEstadoFinanciero() {
		return subGroupEstadoFinanciero;
	}

	public void setSubGroupEstadoFinanciero(boolean subGroupEstadoFinanciero) {
		this.subGroupEstadoFinanciero = subGroupEstadoFinanciero;
	}

	public boolean isSubGroupCuadroActivoFijo() {
		return subGroupCuadroActivoFijo;
	}

	public void setSubGroupCuadroActivoFijo(boolean subGroupCuadroActivoFijo) {
		this.subGroupCuadroActivoFijo = subGroupCuadroActivoFijo;
	}

}
