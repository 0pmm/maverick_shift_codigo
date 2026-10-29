package br.com.oficina.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Ponto único de acesso ao EntityManagerFactory.
 * Usado por todos os DAOs (atual e futuros) da aplicação.
 */
public final class JPAUtil {

    private static final String PERSISTENCE_UNIT = "sistemaOficinaPU";
    private static EntityManagerFactory factory;

    private JPAUtil() {
    }

    private static EntityManagerFactory getFactory() {
        if (factory == null) {
            factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
        }
        return factory;
    }

    public static EntityManager getEntityManager() {
        return getFactory().createEntityManager();
    }
}
