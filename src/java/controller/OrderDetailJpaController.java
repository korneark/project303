/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import controller.exceptions.NonexistentEntityException;
import controller.exceptions.PreexistingEntityException;
import controller.exceptions.RollbackFailureException;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.UserTransaction;
import model.Order1;
import model.OrderDetail;

/**
 *
 * @author Windows 10
 */
public class OrderDetailJpaController implements Serializable {

    public OrderDetailJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(OrderDetail orderDetail) throws PreexistingEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Order1 orderOrderId = orderDetail.getOrderOrderId();
            if (orderOrderId != null) {
                orderOrderId = em.getReference(orderOrderId.getClass(), orderOrderId.getOrderId());
                orderDetail.setOrderOrderId(orderOrderId);
            }
            em.persist(orderDetail);
            if (orderOrderId != null) {
                orderOrderId.getOrderDetailList().add(orderDetail);
                orderOrderId = em.merge(orderOrderId);
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findOrderDetail(orderDetail.getOrderId()) != null) {
                throw new PreexistingEntityException("OrderDetail " + orderDetail + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(OrderDetail orderDetail) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            OrderDetail persistentOrderDetail = em.find(OrderDetail.class, orderDetail.getOrderId());
            Order1 orderOrderIdOld = persistentOrderDetail.getOrderOrderId();
            Order1 orderOrderIdNew = orderDetail.getOrderOrderId();
            if (orderOrderIdNew != null) {
                orderOrderIdNew = em.getReference(orderOrderIdNew.getClass(), orderOrderIdNew.getOrderId());
                orderDetail.setOrderOrderId(orderOrderIdNew);
            }
            orderDetail = em.merge(orderDetail);
            if (orderOrderIdOld != null && !orderOrderIdOld.equals(orderOrderIdNew)) {
                orderOrderIdOld.getOrderDetailList().remove(orderDetail);
                orderOrderIdOld = em.merge(orderOrderIdOld);
            }
            if (orderOrderIdNew != null && !orderOrderIdNew.equals(orderOrderIdOld)) {
                orderOrderIdNew.getOrderDetailList().add(orderDetail);
                orderOrderIdNew = em.merge(orderOrderIdNew);
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = orderDetail.getOrderId();
                if (findOrderDetail(id) == null) {
                    throw new NonexistentEntityException("The orderDetail with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            OrderDetail orderDetail;
            try {
                orderDetail = em.getReference(OrderDetail.class, id);
                orderDetail.getOrderId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The orderDetail with id " + id + " no longer exists.", enfe);
            }
            Order1 orderOrderId = orderDetail.getOrderOrderId();
            if (orderOrderId != null) {
                orderOrderId.getOrderDetailList().remove(orderDetail);
                orderOrderId = em.merge(orderOrderId);
            }
            em.remove(orderDetail);
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<OrderDetail> findOrderDetailEntities() {
        return findOrderDetailEntities(true, -1, -1);
    }

    public List<OrderDetail> findOrderDetailEntities(int maxResults, int firstResult) {
        return findOrderDetailEntities(false, maxResults, firstResult);
    }

    private List<OrderDetail> findOrderDetailEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(OrderDetail.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public OrderDetail findOrderDetail(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(OrderDetail.class, id);
        } finally {
            em.close();
        }
    }

    public int getOrderDetailCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<OrderDetail> rt = cq.from(OrderDetail.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
