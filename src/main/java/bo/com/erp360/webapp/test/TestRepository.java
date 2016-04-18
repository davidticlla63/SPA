package bo.com.erp360.webapp.test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.log4j.Logger;

import bo.com.erp360.webapp.model.Test;

@Stateless
public class TestRepository{

	Logger log = Logger.getLogger(TestRepository.class);
	
	@Inject
    private EntityManager em;

	public TestRepository(){
		super();
		log.info("init TestRepositoryRequestScope()");
	}
	
	@SuppressWarnings("unchecked")
	public List<Test> findAllBySize(int size){
		List<Test> list = new ArrayList<>();
		try{
			String query = "select em from Test em ";
			list = em.createQuery(query).getResultList();
			log.info("findAllBySize() OK!"); 
		}catch(Exception e){
			log.error("findAllBySize() ERROR!", e);
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<Test> findAllBySize(int start,int maxRows){
		List<Test> list = new ArrayList<>();
		try{
			Query q = em.createQuery("FROM Test");
			q.setFirstResult(start);
			q.setMaxResults(maxRows);
			list = q.getResultList();
		}catch(Exception e){
			log.error("findAllBySize() ERROR!", e);
		}
		return list;
	}
	
	/**
     * Returns the number of total records
     * @param namedQueryName
     * @return Long
     */
    public BigInteger countTotalRecord() {
        String query = "select count(em) from Test em";
        return (BigInteger) em.createNativeQuery(query).getSingleResult();
    }

}
