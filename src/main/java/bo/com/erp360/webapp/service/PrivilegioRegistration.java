/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bo.com.erp360.webapp.service;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.Permiso;
import bo.com.erp360.webapp.model.Privilegio;
import bo.com.erp360.webapp.model.Roles;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

// The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateless
public class PrivilegioRegistration {

	@Inject
	private Logger log;

	@Inject
	private EntityManager em;

	@Inject
	private Event<Privilegio> privilegioEventSrc;

	public void register(Privilegio privilegio) throws Exception {
		if(!existPrivilegio(privilegio.getPermiso(),privilegio.getRoles())){
			try{
				log.info("Registering Privilegio ");
				em.persist(privilegio);
				privilegioEventSrc.fire(privilegio);
			}catch(Exception e){
				log.info("register(Privilegio privilegio)  ->  error"+e.getMessage());
			}
		}
	}

	public boolean existPrivilegio(Permiso p,Roles roles){
		Privilegio privilegio = null;
		try{
			String query = "select em from Privilegio em where em.permiso.id="+p.getId()+" and em.roles.id="+roles.getId();
			privilegio = (Privilegio) em.createQuery(query).getSingleResult();
		}catch(Exception e){
			privilegio = null;
		}
		return (privilegio!=null)?true:false;
	}

	public void update(Privilegio privilegio) throws Exception {
		log.info("Registering Privilegio: " + privilegio.getId());
		em.merge(privilegio);
		privilegioEventSrc.fire(privilegio);
	}

	public void remove(Privilegio privilegio){
		log.info("Remover Privilegio: " + privilegio.getId());
		em.merge(privilegio);
		privilegioEventSrc.fire(privilegio);
	}

	public void removeByPermisoRoles(Permiso permiso, Roles roles){
		log.info("Remover permiso: " + permiso.getId()+"  Roles :"+roles.getName());
		Privilegio p = obtenerByPermisoRoles(permiso, roles);
		if(p!=null){
			em.remove(p);
			privilegioEventSrc.fire(p);
		}else{log.info("No existe el permiso: ");}
	}
	
	public void removeByRoles(Roles roles){
		log.info("Remover Privilegio: de Roles :"+roles.getName());
		List<Privilegio> list = obtenerAllByRoles( roles);
		for(Privilegio p : list){
			em.remove(p);
			privilegioEventSrc.fire(p);
		}
	}

	private  Privilegio obtenerByPermisoRoles(Permiso permiso, Roles roles){
		try{
			String query = "select em from Privilegio em where em.permiso.id="+permiso.getId()+" and em.roles.id="+roles.getId();
			return (Privilegio) em.createQuery(query).getSingleResult();
		}catch(Exception e){
			log.info("obtenerByPermisoRoles() error: "+e.getMessage());
			return null;
		}
	}
	
	private  List<Privilegio> obtenerAllByRoles(Roles roles){
		try{
			String query = "select em from Privilegio em where em.roles.id="+roles.getId();
			return em.createQuery(query).getResultList();
		}catch(Exception e){
			log.info("obtenerAllByRoles() error: "+e.getMessage());
			return new ArrayList<Privilegio>();
		}
	}
}
