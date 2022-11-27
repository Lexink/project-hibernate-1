package com.game.repository;

import com.game.entity.Player;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.util.*;

@Repository(value = "db")
public class PlayerRepositoryDB implements IPlayerRepository {
    private final SessionFactory sessionFactory;
    public PlayerRepositoryDB() {
        Properties properties = new Properties();
        properties.put(Environment.DRIVER, "com.p6spy.engine.spy.P6SpyDriver");
        properties.put(Environment.URL, "jdbc:p6spy:mysql://localhost:3306/rpg");
        properties.put(Environment.DIALECT, "org.hibernate.dialect.MySQLDialect");
        properties.put(Environment.HBM2DDL_AUTO, "update");
        properties.put(Environment.USER, "root");
        properties.put(Environment.PASS, "root");
        sessionFactory = new Configuration()
                .setProperties(properties)
                .addAnnotatedClass(Player.class)
                .buildSessionFactory();
    }

    @Override
    public List<Player> getAll(int pageNumber, int pageSize) {
        try (Session session = sessionFactory.openSession()) {
            String sql = "SELECT * FROM player LIMIT :pagesize OFFSET :offset";
            List<Player> playersToReturn = new ArrayList<>();
            List<Player> players = session.createNativeQuery(sql, Player.class)
                    .setParameter("pagesize", pageSize)
                    .setParameter("offset", pageSize*pageNumber)
                    .list();

            return Objects.nonNull(players) ? players : playersToReturn;
        }
    }

    @Override
    public int getAllCount() {
        try (Session session = sessionFactory.openSession()){
            Long countLong = (Long)session.createNamedQuery("getAllCount").uniqueResult();
            return Objects.nonNull(countLong) ? countLong.intValue() : 0;
        }
    }

    @Override
    public Player save(Player player) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            Long id = (Long) session.save(player);
            Player playerToReturn =  session.get(Player.class, id);
            transaction.commit();
            return playerToReturn;
        }
    }

    @Override
    public Player update(Player player) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            Player playerToReturn = (Player)session.merge(player);
            transaction.commit();
            return playerToReturn;
        }
    }

    @Override
    public Optional<Player> findById(long id) {
        try (Session session = sessionFactory.openSession()) {
            Player player = session.get(Player.class, id);
            return Objects.nonNull(player) ? Optional.of(player) : Optional.empty();
        }
    }

    @Override
    public void delete(Player player) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            String hql = "delete from Player where id = :id";
            session.createQuery(hql)
                    .setParameter("id", player.getId())
                    .executeUpdate();
            transaction.commit();
        }
    }

    @PreDestroy
    public void beforeStop() {
        sessionFactory.close();
    }
}