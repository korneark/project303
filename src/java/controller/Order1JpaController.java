/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import controller.exceptions.IllegalOrphanException;
import controller.exceptions.NonexistentEntityException;
import controller.exceptions.PreexistingEntityException;
import controller.exceptions.RollbackFailureException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import model.OrderDetail;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;
import model.Order1;

/**
 *
 * @author Windows 10
 */
public class Order1JpaController implements Serializable {

    public Order1JpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Order1 order1) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (order1.getOrderDetailList() == null) {
            order1.setOrderDetailList(new ArrayList<OrderDetail>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            List<OrderDetail> attachedOrderDetailList = new ArrayList<OrderDetail>();
            for (OrderDetail orderDetailListOrderDetailToAttach : order1.getOrderDetailList()) {
                orderDetailListOrderDetailToAttach = em.getReference(orderDetailListOrderDetailToAttach.getClass(), orderDetailListOrderDetailToAttach.getOrderId());
                attachedOrderDetailList.add(orderDetailListOrderDetailToAttach);
            }
            order1.setOrderDetailList(attachedOrderDetailList);
            em.persist(order1);
            for (OrderDetail orderDetailListOrderDetail : order1.getOrderDetailList()) {
                Order1 oldOrderOrderIdOfOrderDetailListOrderDetail = orderDetailListOrderDetail.getOrderOrderId();
                orderDetailListOrderDetail.setOrderOrderId(order1);
                orderDetailListOrderDetail = em.merge(orderDetailListOrderDetail);
                if (oldOrderOrderIdOfOrderDetailListOrderDetail != null) {
                    oldOrderOrderIdOfOrderDetailListOrderDetail.getOrderDetailList().remove(orderDetailListOrderDetail);
                    oldOrderOrderIdOfOrderDetailListOrderDetail = em.merge(oldOrderOrderIdOfOrderDetailListOrderDetail);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findOrder1(order1.getOrderId()) != null) {
                throw new PreexistingEntityException("Order1 " + order1 + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Order1 order1) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Order1 persistentOrder1 = em.find(Order1.class, order1.getOrderId());
            List<OrderDetail> orderDetailListOld = persistentOrder1.getOrderDetailList();
            List<OrderDetail> orderDetailListNew = order1.getOrderDetailList();
            List<String> illegalOrphanMessages = null;
            for (OrderDetail orderDetailListOldOrderDetail : orderDetailListOld) {
                if (!orderDetailListNew.contains(orderDetailListOldOrderDetail)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain OrderDetail " + orderDetailListOldOrderDetail + " since its orderOrderId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            List<OrderDetail> attachedOrderDetailListNew = new ArrayList<OrderDetail>();
            for (OrderDetail orderDetailListNewOrderDetailToAttach : orderDetailListNew) {
                orderDetailListNewOrderDetailToAttach = em.getReference(orderDetailListNewOrderDetailToAttach.getClass(), orderDetailListNewOrderDetailToAttach.getOrderId());
                attachedOrderDetailListNew.add(orderDetailListNewOrderDetailToAttach);
            }
            orderDetailListNew = attachedOrderDetailListNew;
            order1.setOrderDetailList(orderDetailListNew);
            order1 = em.merge(order1);
            for (OrderDetail orderDetailListNewOrderDetail : orderDetailListNew) {
                if (!orderDetailListOld.contains(orderDetailListNewOrderDetail)) {
                    Order1 oldOrderOrderIdOfOrderDetailListNewOrderDetail = orderDetailListNewOrderDetail.getOrderOrderId();
                    orderDetailListNewOrderDetail.setOrderOrderId(order1);
                    orderDetailListNewOrderDetail = em.merge(orderDetailListNewOrderDetail);
                    if (oldOrderOrderIdOfOrderDetailListNewOrderDetail != null && !oldOrderOrderIdOfOrderDetailListNewOrderDetail.equals(order1)) {
                        oldOrderOrderIdOfOrderDetailListNewOrderDetail.getOrderDetailList().remove(orderDetailListNewOrderDetail);
                        oldOrderOrderIdOfOrderDetailListNewOrderDetail = em.merge(oldOrderOrderIdOfOrderDetailListNewOrderDetail);
                    }
                }
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
                Integer id = order1.getOrderId();
                if (findOrder1(id) == null) {
                    throw new NonexistentEntityException("The order1 with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Order1 order1;
            try {
                order1 = em.getReference(Order1.class, id);
                order1.getOrderId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The order1 with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<OrderDetail> orderDetailListOrphanCheck = order1.getOrderDetailList();
            for (OrderDetail orderDetailListOrphanCheckOrderDetail : orderDetailListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Order1 (" + order1 + ") cannot be destroyed since the OrderDetail " + orderDetailListOrphanCheckOrderDetail + " in its orderDetailList field has a non-nullable orderOrderId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(order1);
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

    public List<Order1> findOrder1Entities() {
        return findOrder1Entities(true, -1, -1);
    }

    public List<Order1> findOrder1Entities(int maxResults, int firstResult) {
        return findOrder1Entities(false, maxResults, firstResult);
    }

    private List<Order1> findOrder1Entities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Order1.class));
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

    public Order1 findOrder1(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Order1.class, id);
        } finally {
            em.close();
        }
    }

    public int getOrder1Count() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Order1> rt = cq.from(Order1.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
