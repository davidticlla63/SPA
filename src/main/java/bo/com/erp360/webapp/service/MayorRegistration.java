package bo.com.erp360.webapp.service;

import java.util.Date;

import javax.ejb.Stateless;

import bo.com.erp360.webapp.model.AsientoContable;
import bo.com.erp360.webapp.model.Mayor;

//The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateless
public class MayorRegistration extends DataAccessService<Mayor>{

	public MayorRegistration(){
		super(Mayor.class);
	}

	public void registrarMayor(Mayor mayorAnterior,AsientoContable asientoContable){
		try{
			Date fechaRegistro = new Date();
			//nacional
			double debitoNacional = asientoContable.getDebeNacional();
			double creditoNacional = asientoContable.getHaberNacional();
			//extranjero
			double debitoExtranjero = asientoContable.getDebeExtranjero();
			double creditoExtranjero = asientoContable.getHaberExtranjero();
			//saldo anterior
			double saldoAnteriorNacional = mayorAnterior!=null? mayorAnterior.getSaldoNacional():0;
			double saldoAnteriorExtranjero = mayorAnterior!=null? mayorAnterior.getSaldoExtranjero():0;
			Mayor mayor = new Mayor();
			mayor.setFecha(fechaRegistro);
			mayor.setEstado("AC");
			mayor.setUsuarioRegistro(asientoContable.getUsuarioRegistro());
			mayor.setFechaRegistro(fechaRegistro);
			mayor.setAsientoContable(asientoContable);
			mayor.setDebitoNacional(debitoNacional);
			mayor.setCreditoNacional(creditoNacional);
			mayor.setDebitoExtranjero(debitoExtranjero);
			mayor.setCreditoExtranjero(creditoExtranjero);
			String nombreTipoCuenta = asientoContable.getPlanCuenta().getTipoCuenta().getNombre();
			if(nombreTipoCuenta.equals("ACTIVO") || nombreTipoCuenta.equals("COSTO") || nombreTipoCuenta.equals("EGRESO")){
				//condicion del debe y haber ( DEUDOR )
				if(debitoNacional > creditoNacional){ // ( + )
					mayor.setSaldoNacional(saldoAnteriorNacional + debitoNacional);
					mayor.setSaldoExtranjero(saldoAnteriorExtranjero + debitoExtranjero);
				}else{ // ( - )
					mayor.setSaldoNacional(saldoAnteriorNacional - creditoNacional);
					mayor.setSaldoExtranjero(saldoAnteriorExtranjero - creditoExtranjero);
				}
			}else if(nombreTipoCuenta.equals("PASIVO") || nombreTipoCuenta.equals("PATRIMONIO") || nombreTipoCuenta.equals("INGRESO")){
				//condicion del debe y haber  ( ACREEDOR )
				if(debitoNacional > creditoNacional){ // ( - )
					mayor.setSaldoNacional(saldoAnteriorNacional - debitoNacional);
					mayor.setSaldoExtranjero(saldoAnteriorExtranjero - debitoExtranjero);
				}else{ // ( + )
					mayor.setSaldoNacional(saldoAnteriorNacional + creditoNacional);
					mayor.setSaldoExtranjero(saldoAnteriorExtranjero + creditoExtranjero);
				}
			}
			mayor.setPlanCuenta(asientoContable.getPlanCuenta());
			this.create(mayor);
		}catch(Exception e){
			log.info("registrarMayor ERROR "+e.getMessage());
		}
	}
}

